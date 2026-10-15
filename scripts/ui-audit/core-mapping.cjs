const {chromium,request}=require('../../target/ui-audit-tools/node_modules/playwright');
const {expect}=require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
(async()=>{
 const state=JSON.parse(await fs.readFile(path.resolve(__dirname,'../../target/ui-audit/state.json')));assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/);
 const api=await request.newContext({baseURL:state.frontend});
 const token=(await(await api.post('/api/system/login',{data:{user_id:910001,password:'review-fixture-password'}})).json()).token;
 const browser=await chromium.launch({channel:'msedge',headless:true});
 const context=await browser.newContext({viewport:{width:1500,height:950}});await context.addInitScript(t=>localStorage.setItem('token',t),token);
 const page=await context.newPage(),errors=[];page.on('pageerror',e=>errors.push(e.message));
 const output=path.resolve(__dirname,'../../target/ui-audit/core-mapping');await fs.mkdir(output,{recursive:true});
 try{
  await page.goto(state.frontend+'/home/core-indicators');await expect(page.locator('.indicator-table .indicator-row')).toHaveCount(10);
  const last=page.locator('.indicator-table .indicator-row').last();
  await expect(last).toContainText('1.2.45');await expect(last.locator('.indicator-name')).toHaveText('发布《工程机械产业发展报告》数量');
  await expect(last.locator('.el-table__cell').nth(4)).toHaveText('5');await expect(last).toContainText('尚未归档');
  await expect(last).not.toContainText('毕业生就业相关度');
  await last.getByRole('button',{name:'历史记录'}).click();
  const history=page.getByRole('dialog',{name:'核心指标历史记录'});await expect(history).toContainText('毕业生就业相关度');await expect(history.getByText('旧关联记录')).toHaveCount(2);
  await history.getByRole('button',{name:'Close this dialog'}).click();
  await last.getByRole('button',{name:'填报',exact:true}).click();const draft=page.getByRole('dialog',{name:'核心指标填报'});
  await expect(draft).toContainText('发布《工程机械产业发展报告》数量');await expect(draft.locator('.el-input input').nth(1)).toHaveValue('');
  await draft.locator('.el-input input').nth(0).fill('0');await draft.locator('.el-input input').nth(1).fill('2');
  await draft.getByRole('button',{name:'提交审核',exact:true}).click();await page.getByRole('dialog',{name:'提交确认',exact:true}).getByRole('button',{name:'确认提交',exact:true}).click();
  await expect(page.getByRole('dialog',{name:'核心指标详情'})).toContainText('待归口审核');
  const headers={Authorization:token};const reports=await(await api.get('/api/core-indicators/10/history',{headers})).json();const submitted=reports.find(r=>r.performanceCode==='1.2.45');
  assert.equal(submitted.currentHandlerId,910002);assert.equal(submitted.targetValue,5);assert.equal(submitted.actualValue,2);assert.equal(reports.filter(r=>r.performanceCode==='1.2.15').length,2);
  await page.screenshot({path:path.join(output,'correct-report-indicator.png'),fullPage:true});
  await page.getByRole('dialog',{name:'核心指标详情'}).getByRole('button',{name:'返回',exact:true}).click();
  await page.goto(state.frontend+'/home/core-indicators?reportId=980010');
  const old=page.getByRole('dialog',{name:'核心指标详情'});await expect(old).toContainText('旧关联记录');await expect(old).toContainText('毕业生就业相关度');await expect(old.getByRole('button',{name:'提交审核',exact:true})).toHaveCount(0);
  assert.deepEqual(errors,[]);const result={checks:['correct-code-name-target','obsolete-history-retained-readonly','old-archived-result-excluded','new-report-correct-principal-and-values'],browserErrors:errors};
  await fs.writeFile(path.join(output,'result.json'),JSON.stringify(result,null,2));console.log(JSON.stringify(result));
 }finally{await context.close();await browser.close();await api.dispose();}
})().catch(e=>{console.error(e);process.exitCode=1;});
