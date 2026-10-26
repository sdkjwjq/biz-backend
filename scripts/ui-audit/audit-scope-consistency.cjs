// Run against the paired, disposable --fixture core server. Never against production.
const {chromium,request}=require('../../target/ui-audit-tools/node_modules/playwright');
const {expect}=require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
const {execFileSync}=require('node:child_process');
(async()=>{
 const state=JSON.parse(await fs.readFile(process.env.UI_AUDIT_STATE_FILE,'utf8'));assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/);assert.ok(state.comparison);
 for(const url of [state.frontend,state.comparison])assert.equal(new URL(url).hostname,'127.0.0.1');
 const out=path.resolve(__dirname,'../../target/audit-scope-fix-20261026/browser');await fs.mkdir(out,{recursive:true});
 const sql=["UPDATE biz_level4_task SET target_value=10,current_value=0,progress=0,data_type='1'","DELETE FROM biz_performance_audit_snapshot","DELETE FROM biz_performance_audit_log","DELETE FROM biz_performance_submission","DELETE FROM biz_core_audit_log","DELETE FROM biz_core_report","DELETE FROM biz_achievement_audit_log","DELETE FROM biz_achievement_submission","DELETE FROM biz_achievement","DELETE FROM biz_material_submission WHERE task_id BETWEEN 930002 AND 930035","DELETE FROM sys_file WHERE file_id=940001","INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by) VALUES(940001,'audit.pdf','audit.pdf','/uploads/audit.pdf','pdf',910001)"];
 for(let i=0;i<34;i++){
  const id=930002+i;
  if(i)sql.push(`INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete) VALUES(${id},1,930001,2026,'1.1.${i+1}','审计待办任务${i+1}',3,910001,910003,910002,920001,'1',10,0,0,'2',0) ON DUPLICATE KEY UPDATE task_name=VALUES(task_name)`);
  sql.push(`INSERT INTO biz_material_submission(task_id,file_id,reported_value,data_type,submit_by,submit_dept_id,manage_dept_id,file_suffix,submit_time,flow_status,current_handler_id,is_delete) VALUES(${id},940001,3,'1',910001,920001,920001,'pdf',NOW(),10,910003,0)`);
 }
 execFileSync('C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe',['--host=127.0.0.1','--user=root','--default-character-set=utf8mb4',state.schema],{input:sql.join(';\n')+';',env:{...process.env,MYSQL_PWD:process.env.SHUANGGAO_TEST_DB_PASSWORD},stdio:['pipe','pipe','pipe']});
 const api=await request.newContext({baseURL:state.frontend});
 const login=async id=>(await(await api.post('/api/system/login',{data:{user_id:id,password:'review-fixture-password'}})).json()).token;
 const admin=await login(110228),outside=await login(910004);
 const call=async(method,url,token,data)=>{const r=await api[method](url,{headers:{Authorization:token},data});assert.equal(r.status(),200,await r.text());const body=await r.json().catch(()=>null);assert.ok(!body?.code||body.code<400,JSON.stringify(body));return body};
 const query='/api/list-filters/audits?tab=todo&responseMode=page&includeFields=false&pageSize=10';
 const beforeApi=await request.newContext({baseURL:state.comparison});
 const old=(await(await beforeApi.get(query,{headers:{Authorization:admin}})).json());assert.equal(old.total,0);assert.equal(old.summary.todoCount,34);
 const fixed=await call('get',query,admin);assert.equal(fixed.total,34);assert.equal(fixed.summary.todoCount,34);
 const errors=[],checks=[];const browser=await chromium.launch({channel:'msedge',headless:true});
 const show=async(base,token)=>{const context=await browser.newContext({viewport:{width:1600,height:1000}});await context.addInitScript(t=>localStorage.setItem('token',t),token);const page=await context.newPage();page.on('pageerror',e=>errors.push(e.message));await page.goto(base+'/home/audit');return{context,page}};
 try{
  const before=await show(state.comparison,admin);
  await expect(before.page.locator('.audit-container .header-left')).toContainText('34 待处理');await expect(before.page.locator('.audit-container .el-table__empty-block')).toContainText('暂无匹配审核记录');
  await before.page.screenshot({path:path.join(out,'before-34-empty.png'),fullPage:true});await before.context.close();checks.push('production-binary-reproduces-34-count-zero-list');
  const {context,page}=await show(state.frontend,admin);const rows=page.locator('.audit-container .el-table__body tbody tr.el-table__row');
  await expect(rows).toHaveCount(10);await expect(page.locator('.audit-container .header-left')).toContainText('34 待处理');await expect(page.locator('.el-pagination')).toContainText('34');
  const seen=new Set();for(let n=1;n<=4;n++){if(n>1){const previous=await rows.first().locator('.task-name').textContent();await page.locator('.el-pager li').filter({hasText:new RegExp('^'+n+'$')}).click();await expect(rows.first().locator('.task-name')).not.toHaveText(previous)}await expect(rows).toHaveCount(n===4?4:10);for(const text of await rows.locator('.task-name').allTextContents())seen.add(text)}assert.equal(seen.size,34);checks.push('all-34-pending-records-across-four-pages');
  await page.locator('.el-pager li').filter({hasText:/^\s*1\s*$/}).click();await expect(rows).toHaveCount(10);await rows.first().getByRole('button',{name:'审批',exact:true}).click();await expect(page.getByRole('dialog',{name:'业务审批',exact:true})).toBeVisible();
  await page.getByRole('dialog',{name:'业务审批',exact:true}).locator('.el-dialog__headerbtn').click();checks.push('administrator-can-open-current-handler-business-dialog');
  const bar=page.locator('[aria-label="审核筛选"]');await bar.getByLabel('审核编号／名称',{exact:true}).fill('not-found-in-fixtures');await bar.getByRole('button',{name:'查询',exact:true}).click();await expect(rows).toHaveCount(0);await expect(page.locator('.audit-container .header-left')).toContainText('34 待处理');await bar.getByRole('button',{name:'重置全部',exact:true}).click();await expect(rows).toHaveCount(10);checks.push('filters-restrict-list-without-changing-global-badge');
  await page.reload();await expect(rows).toHaveCount(10);await expect(page.locator('.audit-container .header-left')).toContainText('34 待处理');checks.push('refresh-restores-correct-list');
  await page.setViewportSize({width:960,height:640});await expect(rows).toHaveCount(10);await page.screenshot({path:path.join(out,'after-34-list-narrow.png'),fullPage:true});await page.setViewportSize({width:1600,height:1000});await page.screenshot({path:path.join(out,'after-34-list.png'),fullPage:true});await context.close();
  const outsider=await call('get',query,outside);assert.equal(outsider.total,0);assert.equal(outsider.summary.todoCount,0);checks.push('ordinary-user-remains-zero-with-no-elevated-scope');
  const response=await api.get('/api/list-filters/audits/locate?taskId=930003',{headers:{Authorization:outside}});assert.ok([403,404].includes(response.status()));checks.push('ordinary-user-direct-link-denied');
  assert.deepEqual(errors,[]);
  await fs.writeFile(path.join(out,'results.json'),JSON.stringify({checks,errors,before:{total:old.total,todoCount:old.summary.todoCount},after:{total:fixed.total,todoCount:fixed.summary.todoCount}},null,2));
  console.log(JSON.stringify({passed:true,checks:checks.length,output:out}));
 }finally{await browser.close();await api.dispose();await beforeApi.dispose()}
})().catch(e=>{console.error(e);process.exitCode=1});
