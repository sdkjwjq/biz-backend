const {chromium,request}=require('../../target/ui-audit-tools/node_modules/playwright');
const {expect}=require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
(async()=>{
 const file=process.env.UI_AUDIT_STATE_FILE,state=JSON.parse(await fs.readFile(file));assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/);
 const api=await request.newContext({baseURL:state.frontend});const token=(await(await api.post('/api/system/login',{data:{user_id:110228,password:'review-fixture-password'}})).json()).token;
 const browser=await chromium.launch({channel:'chrome',headless:true}),context=await browser.newContext();await context.addInitScript(t=>localStorage.setItem('token',t),token);
 const page=await context.newPage(),checks=[],errors=[];page.on('pageerror',e=>errors.push(e.message));let release;const gate=new Promise(r=>release=r);
 const query=u=>u.pathname==='/api/expert-reviews';await page.route(query,async route=>{await gate;await route.continue().catch(()=>{})});
 try{
  await page.goto(state.frontend+'/home/expert-reviews?view=tasks');await expect(page.locator('.query-filter-bar [role=status]')).toBeVisible();await expect(page.locator('.expert-page .pager')).toHaveCount(0);await expect(page.getByText('No Data',{exact:true})).toHaveCount(0);checks.push('initial-slow-request-does-not-show-empty-list-or-zero-total');
  release();const rows=page.locator('.expert-page .panel .el-table__row');await expect(rows.first()).toBeVisible();await page.unroute(query);const count=await rows.count();checks.push('authorized-real-results-load-after-gate');
  await page.route(query,r=>r.fulfill({status:500,contentType:'application/json',body:JSON.stringify({code:500,message:'模拟列表暂时失败'})}));await page.getByRole('button',{name:'刷新',exact:true}).click();await expect(page.locator('.query-filter-bar .el-alert')).toContainText('保留上次');await expect(rows).toHaveCount(count);checks.push('temporary-refresh-failure-retains-last-successful-results');
  await page.unroute(query);await page.getByRole('button',{name:'刷新',exact:true}).click();await expect(page.locator('.query-filter-bar .el-alert')).toHaveCount(0);await expect(page.locator('.query-filter-bar [role=status]')).toHaveCount(0);await expect(rows.first()).toBeVisible();checks.push('retry-recovers-with-the-same-applied-query');
  await page.route(query,r=>r.fulfill({status:403,contentType:'application/json',body:JSON.stringify({code:403,message:'模拟权限收紧'})}));await page.getByRole('button',{name:'刷新',exact:true}).click();await expect(rows).toHaveCount(0);await expect(page.locator('.query-filter-bar .el-alert')).not.toContainText('保留上次');checks.push('permission-denial-clears-stale-results');await page.unroute(query);
  let releaseFormal;const formalGate=new Promise(r=>releaseFormal=r);const formal=u=>u.pathname==='/api/expert-reviews/admin';await page.route(formal,async r=>{await formalGate;await r.continue().catch(()=>{})});await page.goto(state.frontend+'/home/expert-reviews');await expect(page.locator('.query-filter-bar [role=status]')).toBeVisible();await expect(page.locator('.expert-page .pager')).toHaveCount(0);releaseFormal();await expect(page.locator('.expert-page .pager')).toBeVisible();checks.push('formal-opinion-query-hides-zero-total-until-success');
  assert.deepEqual(errors,[]);checks.push('no-browser-runtime-errors');await page.screenshot({path:path.join(path.dirname(file),'expert-list-loading.png'),fullPage:true});
 }finally{release();await fs.writeFile(path.join(path.dirname(file),'expert-list-loading.json'),JSON.stringify({checks,errors},null,2));await context.close();await browser.close();await api.dispose()}
 console.log(JSON.stringify({checks:checks.length,errors}));
})().catch(e=>{console.error(e);process.exitCode=1});
