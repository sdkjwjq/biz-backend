// Real requests against serve.py's disposable permissions fixture.
const {chromium,request}=require('../../target/ui-audit-tools/node_modules/playwright');
const {expect}=require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
(async()=>{
 const state=JSON.parse(await fs.readFile(path.resolve(__dirname,'../../target/ui-audit/state.json')));assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/);
 const out=path.resolve(__dirname,'../../target/ui-audit/list-filters');await fs.mkdir(out,{recursive:true});
 const api=await request.newContext({baseURL:state.frontend});
 const login=await api.post('/api/system/login',{data:{user_id:110228,password:'review-fixture-password'}}),token=(await login.json()).token;assert.ok(token);
 const checks=[],errors=[];const browser=await chromium.launch({channel:process.env.UI_AUDIT_BROWSER_CHANNEL||'msedge',headless:true}),context=await browser.newContext({viewport:{width:1536,height:864}});
 await context.addInitScript(t=>localStorage.setItem('token',t),token);const page=await context.newPage();page.setDefaultTimeout(20000);page.on('pageerror',e=>errors.push(e.message));
 try{
  for(const domain of ['tasks','performance','achievements','core','records','notices','supervision','budget-versions','admin-operations']){
   const r=await api.get('/api/list-filters/'+domain+'?f.year=2026'+(domain==='core'?'&f.month=10':''),{headers:{Authorization:token}});
   // Notices have no business year; do not invent a date mapping.
   if(domain==='notices'||domain==='admin-operations'){assert.equal(r.status(),400);continue;}
   assert.equal(r.status(),200,domain+': '+await r.text());const body=await r.json();assert.ok(body.fields.length>=4);assert.equal(new Set(body.ids).size,body.total);checks.push(domain+'-real-api');
  }
  let releaseOptions;const optionsGate=new Promise(resolve=>{releaseOptions=resolve});
  await page.route('**/api/list-filters/admin-users/filter-options',async route=>{await optionsGate;await route.continue()});
  let accountRequests=0,releaseAccount;const gate=new Promise(resolve=>{releaseAccount=resolve});
  await page.route('**/api/list-filters/admin-users?**',async route=>{accountRequests++;await gate;await route.continue()});
  await page.goto(state.frontend+'/home/admin/accounts');
  await expect(page.getByText('正在加载账号…',{exact:true})).toBeVisible();
  await expect(page.locator('.pager')).toHaveCount(0);
  await expect(page.getByText('No Data',{exact:true})).toHaveCount(0);
  releaseAccount();await expect(page.locator('.pager')).toBeVisible();await expect(page.locator('[aria-label="账号筛选"] .basic-fields > .query-field')).toHaveCount(4);releaseOptions();await page.unroute('**/api/list-filters/admin-users/filter-options');checks.push('account-data-does-not-wait-for-options');
  assert.equal(accountRequests,1);await page.unroute('**/api/list-filters/admin-users?**');checks.push('account-first-load-never-shows-false-empty-result');
  const accountBar=page.locator('[aria-label="账号筛选"]');await expect(accountBar.getByRole('button',{name:'查询',exact:true})).not.toHaveClass(/is-loading/);await accountBar.getByRole('textbox',{name:'账号姓名／工号'}).fill('不存在的账号');
  const accountResponse=page.waitForResponse(r=>r.url().includes('/list-filters/admin-users?')&&r.url().includes('responseMode=page')&&new URL(r.url()).searchParams.get('f.keyword')==='不存在的账号');
  await accountBar.getByRole('button',{name:'查询',exact:true}).click();const accountBody=await (await accountResponse).json();assert.equal(accountBody.total,0);assert.equal(accountBody.ids,undefined);
  await expect(page.getByText('无匹配账号',{exact:true})).toBeVisible();await accountBar.getByRole('button',{name:'重置全部',exact:true}).click();await expect(page.locator('.el-table__row').first()).toBeVisible();checks.push('account-server-pagination-empty-and-reset');

  await page.goto(state.frontend+'/home/works');const bar=page.locator('.query-filter-bar').first();await expect(bar.locator('.basic-fields > .query-field')).toHaveCount(4);
  const keyword=bar.getByRole('textbox',{name:'任务编号／名称'});await keyword.fill('不存在的筛选名称');await expect(bar.getByText('条件已修改，待查询',{exact:true})).toBeVisible();
  await bar.getByRole('button',{name:'查询',exact:true}).click();await expect(bar.locator('.pending-change')).toHaveCount(0);await expect(page.locator('.el-table__row')).toHaveCount(0);checks.push('draft-query-and-empty-results');
  await bar.getByRole('button',{name:'高级筛选',exact:true}).click();const from=bar.getByRole('spinbutton',{name:'任务完成进度（%）起始'});await from.fill('50');await bar.getByRole('button',{name:'查询',exact:true}).click();await expect(bar.locator('.applied-tags')).toContainText('完成进度（%）：50 至 不限');
  await bar.getByRole('button',{name:/收起高级筛选/}).click();await expect(bar.locator('.advanced-groups')).toHaveCount(0);await expect(bar.getByRole('button',{name:/高级筛选\s*（1）/})).toBeVisible();checks.push('advanced-collapse-keeps-applied-condition');
  await keyword.fill('尚未查询的其他文字');await bar.locator('.el-tag').filter({hasText:'完成进度'}).locator('.el-tag__close').click();await expect(bar.locator('.applied-tags')).toContainText('不存在的筛选名称');await expect(keyword).toHaveValue('尚未查询的其他文字');await expect(bar.locator('.pending-change')).toBeVisible();checks.push('tag-removal-does-not-apply-other-draft-edits');
  await bar.getByRole('button',{name:'重置全部',exact:true}).click();await expect(keyword).toHaveValue('');await expect(bar.locator('.pending-change')).toHaveCount(0);checks.push('reset-defaults');
  await page.route('**/api/list-filters/tasks?*',async route=>{if(new URL(route.request().url()).searchParams.get('f.keyword')==='旧请求'){const response=await route.fetch();await new Promise(r=>setTimeout(r,900));try{await route.fulfill({response})}catch{/* cancelled old response */}}else await route.continue();});
  await keyword.fill('旧请求');await keyword.press('Enter');await keyword.fill('新请求');await keyword.press('Enter');await expect(bar.locator('.applied-tags')).toContainText('编号／名称：新请求');await page.waitForTimeout(1000);await expect(bar.locator('.applied-tags')).not.toContainText('编号／名称：旧请求');await page.unroute('**/api/list-filters/tasks?*');checks.push('delayed-old-response-cannot-overwrite-new-query');
  await page.route('**/api/list-filters/tasks?*',r=>r.fulfill({status:500,contentType:'application/json',body:JSON.stringify({code:500,message:'模拟筛选请求失败'})}));await keyword.fill('失败条件');await keyword.press('Enter');await expect(bar.locator('.el-alert')).toBeVisible();await expect(keyword).toHaveValue('失败条件');await expect(bar.locator('.applied-tags')).toContainText('编号／名称：新请求');await page.unroute('**/api/list-filters/tasks?*');checks.push('failed-query-preserves-draft-and-applied-results');await bar.getByRole('button',{name:'重置全部',exact:true}).click();await expect(bar.locator('.pending-change')).toHaveCount(0);
  await page.setViewportSize({width:960,height:540});await bar.getByRole('button',{name:'高级筛选',exact:true}).click();await expect(bar.getByRole('button',{name:'查询',exact:true})).toBeVisible();await bar.getByRole('button',{name:'清空高级条件',exact:true}).scrollIntoViewIfNeeded();await expect(bar.getByRole('button',{name:'清空高级条件',exact:true})).toBeInViewport();await page.locator('.table-wrapper').scrollIntoViewIfNeeded();await expect(page.locator('.table-wrapper')).toBeInViewport();await page.screenshot({path:path.join(out,'tasks-960.png'),fullPage:true});checks.push('advanced-panel-and-table-remain-scroll-accessible');
  for(const route of ['works/performance','works/achievement','core-indicators','work-records','notice','expert-reviews?view=tasks','works/budget','audit','admin/accounts','admin/permissions']){
   await page.goto(state.frontend+'/home/'+route);await expect(page.locator('.query-filter-bar').first().locator('.basic-fields > .query-field')).toHaveCount(4);checks.push(route+'-basic-controls');
  }
  for(let month=1;month<=12;month++){const created=await api.post('/api/work-records',{headers:{Authorization:token},data:{year:2025,month}});assert.equal(created.status(),200,await created.text());}
  let recordRequests=0,authorRequests=0,releaseRecords;const recordGate=new Promise(resolve=>{releaseRecords=resolve});
  const watchAuthors=req=>{if(req.url().includes('/work-records/authors'))authorRequests++};page.on('request',watchAuthors);
  await page.route('**/api/list-filters/records?**',async route=>{recordRequests++;await recordGate;await route.continue()});
  await page.goto(state.frontend+'/home/work-records');
  await expect(page.getByText('正在加载工作纪实…',{exact:true})).toBeVisible();await expect(page.getByText('暂无工作纪实',{exact:true})).toHaveCount(0);await expect(page.locator('.el-pagination')).toHaveCount(0);
  releaseRecords();await expect(page.locator('.el-pagination')).toBeVisible();assert.equal(recordRequests,1);assert.equal(authorRequests,0);page.off('request',watchAuthors);await page.unroute('**/api/list-filters/records?**');checks.push('record-first-load-no-false-empty-or-unused-author-request');
  const recordBar=page.locator('[aria-label="纪实筛选"]');await recordBar.getByRole('combobox',{name:'纪实年度',exact:true}).click();await page.getByRole('option',{name:'2025年',exact:true}).click();await recordBar.getByRole('button',{name:'查询',exact:true}).click();
  await expect(page.locator('.el-pagination .btn-next')).toBeEnabled();
  const recordPageResponse=page.waitForResponse(r=>r.url().includes('/list-filters/records?')&&new URL(r.url()).searchParams.get('responseMode')==='page');
  await page.locator('.el-pagination .btn-next').click();
  const recordPage=await recordPageResponse;assert.equal(recordPage.status(),200);const recordBody=await recordPage.json();assert.ok(Array.isArray(recordBody.items));assert.ok(!Object.hasOwn(recordBody,'ids'));
  await expect(page.locator('[aria-label="纪实筛选"] .basic-fields > .query-field')).toHaveCount(4);checks.push('record-list-uses-paged-response-without-losing-filter-controls');
  const retainedRows=await page.locator('.el-table__row').count();assert.ok(retainedRows>0);
  await page.route('**/api/list-filters/records?**',route=>route.fulfill({status:500,contentType:'application/json',body:JSON.stringify({code:500,message:'模拟纪实查询失败'})}));await recordBar.getByRole('button',{name:'查询',exact:true}).click();await expect(recordBar.locator('.el-alert')).toContainText('更新失败');await expect(page.locator('.el-table__row')).toHaveCount(retainedRows);await page.unroute('**/api/list-filters/records?**');checks.push('record-failed-refresh-retains-existing-rows');

  await page.goto(state.frontend+'/home/admin/accounts');await page.getByRole('tab',{name:'部门管理',exact:true}).click();await expect(page.locator('[aria-label="部门筛选"] .basic-fields > .query-field')).toHaveCount(3);checks.push('department-basic-and-advanced-controls');
  await page.goto(state.frontend+'/home/works/budget');await page.getByRole('button',{name:/版本记录|历史版本/}).click();await expect(page.locator('[aria-label="预算版本筛选"] .basic-fields > .query-field')).toHaveCount(4);checks.push('budget-version-basic-controls');
  await page.goto(state.frontend+'/home/admin/operations');await expect(page.locator('[aria-label="操作记录筛选"] .basic-fields > .query-field')).toHaveCount(4);checks.push('operation-log-basic-controls');
  await page.goto(state.frontend+'/home/audit');
  const doneResponse=page.waitForResponse(r=>r.url().includes('/list-filters/audits?')&&new URL(r.url()).searchParams.get('tab')==='done');
  await page.getByText('审批记录',{exact:true}).click();const done=await doneResponse;assert.equal(done.status(),200);assert.ok((await done.json()).fields.some(f=>f.key==='handledTime'));
  await expect(page.locator('.query-filter-bar').first().locator('.basic-fields > .query-field')).toHaveCount(4);await page.screenshot({path:path.join(out,'audit-history.png'),fullPage:true});checks.push('audit-history-tab-keeps-handled-time-filter');
  await page.goto(state.frontend+'/home/admin/overview');await page.locator('[data-metric="tasks.total"]').click();
  await expect(page.locator('[aria-label="统计分析筛选"]')).toBeVisible();checks.push('overview-analysis-filters');
  await page.goto(state.frontend+'/home/works');await page.setViewportSize({width:1536,height:864});await expect(page.locator('[aria-label="任务筛选"] .basic-fields > .query-field')).toHaveCount(4);
  const taskBar=page.locator('[aria-label="任务筛选"]');if(await taskBar.locator('.advanced-groups').count()===0)await taskBar.getByRole('button',{name:/高级筛选/}).click();await taskBar.locator('.basic-fields').scrollIntoViewIfNeeded();await page.screenshot({path:path.join(out,'task-filters-1536.png'),fullPage:true});
  assert.deepEqual(errors,[]);await fs.writeFile(path.join(out,'report.json'),JSON.stringify({checks,errors},null,2));console.log(checks.join('\n'));
 }catch(e){await page.screenshot({path:path.join(out,'failure.png'),fullPage:true});await fs.writeFile(path.join(out,'failure.txt'),await page.locator('body').innerText());throw e;}finally{await browser.close();await api.dispose();}
})().catch(e=>{console.error(e);process.exitCode=1;});
