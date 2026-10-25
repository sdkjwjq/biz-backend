const {chromium,request}=require('../../target/ui-audit-tools/node_modules/playwright');
const {expect}=require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
(async()=>{
 const file=process.env.UI_AUDIT_STATE_FILE,state=JSON.parse(await fs.readFile(file));assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/);
 const out=path.join(path.dirname(file),'audit-consolidated');await fs.mkdir(out,{recursive:true});const api=await request.newContext({baseURL:state.frontend});
 const token=(await(await api.post('/api/system/login',{data:{user_id:910003,password:'review-fixture-password'}})).json()).token;assert.ok(token);
 const browser=await chromium.launch({channel:'chrome',headless:true}),context=await browser.newContext({viewport:{width:1536,height:864}});await context.addInitScript(t=>localStorage.setItem('token',t),token);
 const page=await context.newPage(),checks=[],errors=[],requests=[];page.setDefaultTimeout(25000);page.on('pageerror',e=>errors.push(e.message));page.on('request',r=>requests.push(new URL(r.url())));
 const table=page.locator('.audit-container .el-table__body tbody'),rows=table.locator('tr.el-table__row');
 const ready=()=>expect(page.locator('.audit-container > .el-loading-mask:visible, .audit-container .audit-table > .el-loading-mask:visible')).toHaveCount(0);
 const bar=()=>page.locator('[aria-label="审核筛选"]');
 try{
  await page.goto(state.frontend+'/home/audit');await expect(rows).toHaveCount(10);await ready();
  assert.equal(requests.filter(u=>u.pathname==='/api/list-filters/audits').length,1);
  assert.equal(requests.filter(u=>/\/(biz|performance|achievement|core-indicators)\/audit\/(todo|records)$/.test(u.pathname)&&!u.searchParams.has('countOnly')).length,0);checks.push('initial-list-is-one-paged-summary-without-four-source-reads');
  await page.locator('.el-pager li').filter({hasText:/^2$/}).click();await expect(rows).toHaveCount(5);checks.push('pending-list-uses-server-pagination');
  const secondPage=await(await api.get('/api/list-filters/audits?tab=todo&responseMode=page&includeFields=false&page=2&pageSize=10',{headers:{Authorization:token}})).json();const target=secondPage.items[0];
  await page.goto(state.frontend+'/home/audit?type=performance&subId='+target.subId);const dialog=page.getByRole('dialog',{name:'业务审批',exact:true});await expect(dialog).toBeVisible();await expect(dialog).toContainText(String(target.subId));assert.ok(requests.some(u=>u.pathname==='/api/list-filters/audits/locate'));checks.push('deep-link-locates-off-page-pending-record');
  await dialog.locator('.el-dialog__headerbtn').click();
  const ownerToken=(await(await api.post('/api/system/login',{data:{user_id:110228,password:'review-fixture-password'}})).json()).token;const ownerContext=await browser.newContext();await ownerContext.addInitScript(t=>localStorage.setItem('token',t),ownerToken);const ownerPage=await ownerContext.newPage();await ownerPage.goto(state.frontend+'/home/audit?type=performance&subId=972099');const history=ownerPage.getByRole('dialog');await expect(history).toBeVisible();await expect(history).toContainText('已通过');checks.push('processed-message-opens-authorized-readonly-history');await ownerContext.close();
  await page.goto(state.frontend+'/home/audit');await bar().getByRole('button',{name:'重置全部',exact:true}).click();await expect(rows).toHaveCount(10);await ready();
  let release;const gate=new Promise(r=>{release=r});await page.route(u=>u.pathname==='/api/list-filters/audits'&&u.searchParams.get('tab')==='done',async route=>{await gate;await route.continue().catch(()=>{})});
  await page.getByText('审批记录',{exact:true}).click();await page.getByText('待我审批',{exact:true}).click();await expect(rows).toHaveCount(10);release();await page.waitForTimeout(200);await expect(rows).toHaveCount(10);checks.push('slow-history-cannot-overwrite-current-todo-tab');await page.unroute(u=>u.pathname==='/api/list-filters/audits'&&u.searchParams.get('tab')==='done');
  await page.screenshot({path:path.join(out,'paged-todo.png'),fullPage:true});
  await page.goto(state.frontend+'/home/audit');await bar().getByRole('button',{name:'重置全部',exact:true}).click();await expect(rows).toHaveCount(10);
  const allPending=await(await api.get('/api/list-filters/audits?tab=todo&responseMode=page&includeFields=false&pageSize=100',{headers:{Authorization:token}})).json();assert.equal(allPending.total,15);
  const chosen=allPending.items[9],next=allPending.items[10];let approvalPosts=0;page.on('request',r=>{if(r.method()==='POST'&&new URL(r.url()).pathname==='/api/performance/audit')approvalPosts++});
  await rows.nth(9).getByRole('button',{name:'审批',exact:true}).click();const approving=page.getByRole('dialog',{name:'业务审批',exact:true});await expect(approving).toContainText(String(chosen.subId));
  await approving.getByPlaceholder('请输入审批意见（必填）...').fill('隔离测试：连续处理跨页记录');await approving.getByRole('button',{name:'提交并审核下一条',exact:true}).dblclick({delay:1});
  await expect(approving).toContainText(String(next.subId));assert.equal(approvalPosts,1);checks.push('double-click-produces-one-approval-and-advances-across-page-boundary');
  await approving.getByRole('button',{name:'关闭',exact:true}).click();await page.locator('.el-pager li').filter({hasText:/^2$/}).click();await expect(rows).toHaveCount(4);
  await rows.last().getByRole('button',{name:'审批',exact:true}).click();await approving.getByPlaceholder('请输入审批意见（必填）...').fill('隔离测试：最后一条不循环');await approving.getByRole('button',{name:'提交并审核下一条',exact:true}).click();
  await expect(approving).not.toBeVisible();assert.equal(approvalPosts,2);checks.push('last-filtered-record-closes-without-reopening-earlier-records');

  assert.deepEqual(errors,[]);
 }finally{await fs.writeFile(path.join(out,'report.json'),JSON.stringify({checks,errors},null,2));await page.close();await context.close();await browser.close();await api.dispose()}
 console.log(JSON.stringify({checks:checks.length,output:out}));
})().catch(e=>{console.error(e);process.exitCode=1});
