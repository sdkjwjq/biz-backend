const { chromium, request } = require('../../target/ui-audit-tools/node_modules/playwright');
const { expect } = require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
(async()=>{
 const state=JSON.parse(await fs.readFile(path.resolve(__dirname,'../../target/ui-audit/state.json')));
 assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/);
 const api=await request.newContext({baseURL:state.frontend});
 const token=(await(await api.post('/api/system/login',{data:{user_id:110228,password:'review-fixture-password'}})).json()).token;
 const before=await(await api.get('/api/biz/tasks/930002',{headers:{Authorization:token}})).json();
 const browser=await chromium.launch({channel:'msedge',headless:true});const errors=[];
 const context=await browser.newContext({viewport:{width:1600,height:1000}});await context.addInitScript(value=>localStorage.setItem('token',value),token);
 const page=await context.newPage();page.on('pageerror',e=>errors.push(e.message));
 const output=path.resolve(__dirname,'../../target/ui-audit/task-attributes');await fs.mkdir(output,{recursive:true});
 try{
  await page.goto(state.frontend+'/home/works?taskId=930002&year=2026');
  const detail=page.getByRole('dialog',{name:'任务详情与反馈'});
  await expect(detail.getByRole('button',{name:'修改管理属性'})).toBeVisible();
  await detail.getByRole('button',{name:'修改管理属性'}).click();
  const dialog=page.getByRole('dialog',{name:'任务管理属性'});
  await dialog.getByRole('button',{name:'预览并保存'}).click();await expect(page.getByText('请选择计划日期并填写变更原因')).toBeVisible();
  if(!await dialog.getByRole('checkbox',{name:'重点',exact:true}).isChecked())await dialog.getByText('重点',{exact:true}).click();
  if(!await dialog.getByRole('checkbox',{name:'困难',exact:true}).isChecked())await dialog.getByText('困难',{exact:true}).click();
  await dialog.locator('.el-date-editor input').fill('2027-03-01');await dialog.locator('.el-date-editor input').press('Enter');
  await dialog.locator('textarea').fill('Playwright 管理属性验证');
  await dialog.getByRole('button',{name:'预览并保存'}).dblclick();
  await page.getByRole('dialog',{name:'确认变更'}).getByRole('button',{name:'确认保存'}).click();await expect(dialog).toBeHidden();
  await expect(detail.locator('.info-row').filter({hasText:'计划完成日期：'})).toContainText('2027-03-01');
  const saved=await(await api.get('/api/biz/tasks/930002',{headers:{Authorization:token}})).json();
  assert.equal(saved.isPriority,true);assert.equal(saved.isDifficult,true);assert.equal(saved.managementVersion,before.managementVersion+1);assert.equal(saved.actualCompletionDate,null);
  await page.screenshot({path:path.join(output,'task-attributes.png'),fullPage:true});
  await detail.locator('.el-drawer__close-btn').click();
  await expect(detail).toBeHidden();
  await page.locator('.filter-item').filter({hasText:'任务类型'}).locator('.el-select__wrapper').click();await page.getByRole('option',{name:'重点且困难',exact:true}).click();
  for(let i=0;i<3;i++){
   const icon=page.locator('.el-table__body-wrapper .el-table__expand-icon:not(.el-table__expand-icon--expanded):visible').first();
   if(!await icon.count())break;await icon.click();
  }
  await expect(page.locator('.el-table__body-wrapper .el-tag:visible').filter({hasText:'重点'}).first()).toBeVisible();
  await page.screenshot({path:path.join(output,'task-type-filter.png'),fullPage:true});
  assert.deepEqual(errors,[]);await fs.writeFile(path.join(output,'result.json'),JSON.stringify({passed:true,groups:4,pageErrors:errors},null,2));
  console.log('Task attributes Playwright passed: validation, preview/save, duplicate click, type filter');
 }finally{await browser.close();await api.dispose();}
})().catch(error=>{console.error(error);process.exitCode=1;});
