const {chromium,request}=require('../../target/ui-audit-tools/node_modules/playwright');
const {expect}=require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
(async()=>{
 const state=JSON.parse(await fs.readFile(path.resolve(__dirname,'../../target/ui-audit/state.json')));assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/);
 const api=await request.newContext({baseURL:state.frontend});const tokens={};
 for(const user of [910001,910002,110228])tokens[user]=(await(await api.post('/api/system/login',{data:{user_id:user,password:'review-fixture-password'}})).json()).token;
 const call=async(method,url,user,data)=>{const response=await api[method](url,{headers:{Authorization:tokens[user]},data});assert.equal(response.status(),200,await response.text());return response.json();};
 for(const [month,value] of [[1,12],[3,33],[2,22]]){
  let r=await call('post','/api/core-indicators/1/draft',910001,{year:2026,month});
  r=await call('put','/api/core-indicators/reports/'+r.reportId,910001,{version:r.version,previousYearValue:0,actualValue:value,comment:'预算联动真实审核测试'});
  r=await call('post','/api/core-indicators/reports/'+r.reportId+'/submit',910001,{version:r.version});
  if(month!==2){r=await call('post','/api/core-indicators/reports/'+r.reportId+'/audit',910002,{version:r.version,pass:true,comment:'归口通过'});await call('post','/api/core-indicators/reports/'+r.reportId+'/audit',110228,{version:r.version,pass:true,comment:'归档通过'});}
 }
 const browser=await chromium.launch({channel:'msedge',headless:true}),errors=[];
 const context=await browser.newContext({viewport:{width:1600,height:1050}});await context.addInitScript(t=>localStorage.setItem('token',t),tokens[110228]);
 const page=await context.newPage();page.on('pageerror',e=>errors.push(e.message));const output=path.resolve(__dirname,'../../target/ui-audit/budget-trends');await fs.mkdir(output,{recursive:true});
 try{
  await page.goto(state.frontend+'/home/works/budget?year=2026&month=2');
  const panel=page.locator('.budget-trend');await expect(panel.locator('canvas')).toBeVisible();await expect(panel.locator('.el-loading-mask')).toBeHidden();
  await expect(panel).toContainText('可能存在财务调账');
  const coreRow=page.locator('.lower-sheet tr').last();await expect(coreRow).toContainText('2026年1月归档');await expect(coreRow).not.toContainText('33');await expect(coreRow).not.toContainText('原始导入文字');
  const trend=await call('get','/api/budget/trends?year=2026',110228);assert.equal(trend.points[0].cumulative,10);assert.equal(trend.points[1].incremental,20);assert.equal(trend.points[3].incremental,null);assert.equal(trend.points[4].incremental,-10);
  await panel.locator('.el-select__wrapper').nth(1).click();await page.getByRole('option',{name:'当月新增支出',exact:true}).click();await page.keyboard.press('Escape');await page.waitForTimeout(150);await page.screenshot({path:path.join(output,'incremental.png'),fullPage:true});
  await panel.locator('.el-select__wrapper').first().click();await page.getByRole('option',{name:'1. 改革任务1',exact:true}).click();await expect(panel.locator('.el-loading-mask')).toBeHidden();
  await panel.locator('.el-select__wrapper').nth(1).click();await page.getByRole('option',{name:'年度预算执行率',exact:true}).click();await page.keyboard.press('Escape');await page.waitForTimeout(150);await page.screenshot({path:path.join(output,'task-execution-rate.png'),fullPage:true});
  const downloadEvent=page.waitForEvent('download');await page.getByRole('button',{name:'数据导出',exact:true}).click();const download=await downloadEvent;assert.match(download.suggestedFilename(),/2026.*2/);await download.saveAs(path.join(output,'budget-export.xlsx'));
  let fail=true;await page.route('**/api/budget/trends?**',async route=>{if(fail){fail=false;return route.fulfill({status:503,contentType:'application/json',body:JSON.stringify({message:'注入趋势失败',code:503})});}return route.continue();});
  await panel.getByRole('button',{name:'刷新趋势'}).click();await expect(panel).toContainText('注入趋势失败');await panel.getByRole('button',{name:'刷新趋势'}).click();await expect(panel.locator('.el-loading-mask')).toBeHidden();await expect(panel).not.toContainText('注入趋势失败');await page.unroute('**/api/budget/trends?**');
  await page.setViewportSize({width:960,height:640});await expect(panel.locator('canvas')).toBeVisible();await coreRow.scrollIntoViewIfNeeded();await expect(coreRow).toBeVisible();await page.screenshot({path:path.join(output,'narrow-budget.png'),fullPage:true});
  await page.route('**/api/budget/trends?**',async route=>{await new Promise(resolve=>setTimeout(resolve,700));try{await route.continue();}catch{}});
  await panel.getByRole('button',{name:'刷新趋势'}).click();await page.goto(state.frontend+'/home/work-records');await page.waitForTimeout(900);
  assert.deepEqual(errors,[]);await fs.writeFile(path.join(output,'result.json'),JSON.stringify({passed:true,groups:7,pageErrors:errors,simulated:['503 then retry','leave page during delayed trend request']},null,2));console.log('Budget trends Playwright passed: charts, task/metric, archived cutoff, export, retry, narrow scroll, unmount');
 }finally{await browser.close();await api.dispose();}
})().catch(e=>{console.error(e);process.exitCode=1;});
