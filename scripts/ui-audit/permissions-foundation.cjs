// Real HTTP + Chromium verification. Requires serve.py --fixture permissions.
const {chromium,request}=require('../../target/ui-audit-tools/node_modules/playwright');
const {expect}=require('../../target/ui-audit-tools/node_modules/playwright/test');
const assert=require('node:assert/strict'),fs=require('node:fs/promises'),path=require('node:path');
(async()=>{
 const state=JSON.parse(await fs.readFile(path.resolve(__dirname,'../../target/ui-audit/state.json')));assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/);
 const out=path.resolve(__dirname,'../../target/ui-audit/permissions-foundation');await fs.mkdir(out,{recursive:true});
 const api=await request.newContext({baseURL:state.frontend}),browser=await chromium.launch({channel:'msedge',headless:true}),checks=[],errors=[];
 const login=async id=>{const r=await api.post('/api/system/login',{data:{user_id:id,password:'review-fixture-password'}});const data=await r.json();assert.ok(data.token,JSON.stringify(data));return data.token;};
 const admin=await login(110228),expert=await login(910009);
 const get=async(url,token=expert)=>api.get('/api'+url,{headers:{Authorization:token}});
 const done=name=>{checks.push(name);console.log('PASS '+name);};
 const save=async(scopes,overrides={})=>{const current=await(await get('/admin/permissions/users/910009',admin)).json();return api.put('/api/admin/permissions/users/910009',{headers:{Authorization:admin},data:{mode:'CUSTOM',roles:['EXPERT'],scopes,overrides,version:current.policy.version,revision:current.revision,reason:'隔离浏览器测试授权调整'}});};
 try{
  assert.equal((await save({tasks:{mode:'CUSTOM',items:[930002]}})).status(),200);
  const context=await browser.newContext({viewport:{width:1600,height:1000}});await context.addInitScript(value=>localStorage.setItem('token',value),expert);const page=await context.newPage();page.on('pageerror',e=>errors.push(e.message));
  const settled=async()=>{await expect(page.locator('.el-loading-mask:visible')).toHaveCount(0,{timeout:30000});await expect.poll(async()=>{const box=await page.getByRole('dialog').first().boundingBox();return Math.round((box?.x||0)+(box?.width||0));}).toBe(1600);};
  await page.goto(state.frontend+'/home/works?taskId=930002');await expect(page.getByRole('dialog').first()).toBeVisible({timeout:30000});await expect(page.getByRole('dialog').first()).toContainText('仅用户A可见的三级任务');
  await settled();await page.screenshot({path:path.join(out,'scoped-task-detail.png'),fullPage:true});done('scoped-expert-opens-existing-task-detail-with-real-material-and-history');
  assert.equal((await get('/biz/tasks/930102')).status(),403);assert.equal((await get('/biz/audit/task/930102')).status(),403);assert.equal((await get('/system/download/940002')).status(),403);assert.equal((await get('/uploads/permission-file-b.pdf')).status(),403);assert.equal((await get('/system/download/940001')).status(),200);done('direct-details-history-and-static-material-cannot-bypass-scope');
  assert.equal((await save({tasks:{mode:'ALL'}})).status(),200);const expanded=await(await get('/biz/tasks')).json();assert.equal(expanded.filter(t=>t.level===3).length,2);done('range-expansion-is-effective-with-the-original-token');
  assert.equal((await save({tasks:{mode:'CUSTOM',items:[930002]}})).status(),200);assert.equal((await get('/biz/tasks/930102')).status(),403);await page.reload();await expect(page.getByRole('dialog').first()).toBeVisible();await settled();await page.screenshot({path:path.join(out,'range-restored.png'),fullPage:true});done('range-tightening-and-refresh-do-not-restore-old-authorization');
  assert.equal((await save({tasks:{mode:'ALL'}},{'tasks.materials':false})).status(),200);assert.equal((await get('/system/download/940001')).status(),403);assert.equal((await get('/biz/audit/file/930002')).status(),403);done('individual-deny-overrides-the-expert-template');
  const current=await(await get('/admin/permissions/users/910003',admin)).json();const r=await api.post('/api/admin/permissions/users/910003/preview',{headers:{Authorization:admin},data:{mode:'LEGACY',roles:[],overrides:{'tasks.audit':false},scopes:{},version:current.policy.version,revision:current.revision,reason:'隔离待办保护测试'}});assert.equal(r.status(),200);assert.equal((await r.json()).blocked.length,2);done('pending-review-impact-preview-blocks-stranded-work');
  assert.deepEqual(errors,[]);await fs.writeFile(path.join(out,'report.json'),JSON.stringify({checks,errors,data:'random isolated database; real APIs'},null,2));
 }finally{await browser.close();await api.dispose();}
})().catch(e=>{console.error(e);process.exitCode=1;});
