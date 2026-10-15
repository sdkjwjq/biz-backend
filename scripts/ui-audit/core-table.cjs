const {chromium,request}=require('../../target/ui-audit-tools/node_modules/playwright');
const {expect}=require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
(async()=>{
 const state=JSON.parse(await fs.readFile(path.resolve(__dirname,'../../target/ui-audit/state.json')));assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/);
 const api=await request.newContext({baseURL:state.frontend}),browser=await chromium.launch({channel:'msedge',headless:true}),contexts=[],errors=[],checks=[];
 const output=path.resolve(__dirname,'../../target/ui-audit/core-table');await fs.mkdir(output,{recursive:true});
 const token=async id=>(await(await api.post('/api/system/login',{data:{user_id:id,password:'review-fixture-password'}})).json()).token;
 const pageFor=async id=>{const context=await browser.newContext({viewport:{width:1920,height:1080}});contexts.push(context);await context.addInitScript(t=>localStorage.setItem('token',t),await token(id));const page=await context.newPage();page.on('pageerror',e=>errors.push(e.message));await page.goto(state.frontend+'/home/core-indicators');return page;};
 try{
  const admin=await pageFor(110228);await expect(admin.locator('.indicator-table .indicator-row')).toHaveCount(10);
  await expect(admin.getByRole('button',{name:'填报',exact:true})).toHaveCount(10);await expect(admin.locator('.recent-reports')).toHaveCount(0);
  await expect(admin.locator('.indicator-name').last()).toHaveText('发布《工程机械产业发展报告》数量');
  await admin.screenshot({path:path.join(output,'table-desktop.png'),fullPage:true});
  for(const viewport of [{width:1366,height:768},{width:960,height:540}]){
   await admin.setViewportSize(viewport);await expect(admin.locator('.indicator-table')).toBeVisible();
   const widths=await admin.locator('.core-page').evaluate(el=>({client:el.clientWidth,scroll:el.scrollWidth}));assert.ok(widths.scroll<=widths.client+1,JSON.stringify(widths));
   const long=admin.locator('.indicator-name').nth(8);await long.scrollIntoViewIfNeeded();assert.equal(await long.evaluate(el=>getComputedStyle(el).textOverflow),'clip');
   await admin.screenshot({path:path.join(output,`table-${viewport.width}.png`),fullPage:true});
  }
  checks.push('ten-indicators-one-table-long-names-and-internal-scroll');
  await admin.setViewportSize({width:1500,height:950});
  await admin.getByRole('button',{name:'填报',exact:true}).nth(1).click();let dialog=admin.getByRole('dialog',{name:'核心指标填报'});
  await dialog.locator('.el-input input').nth(0).fill('0');await dialog.locator('.el-input input').nth(1).fill('2');await dialog.getByRole('button',{name:'保存草稿'}).click();await expect(admin.getByText('草稿已保存')).toBeVisible();
  await dialog.getByRole('button',{name:'返回',exact:true}).click();await admin.getByRole('button',{name:'填报',exact:true}).nth(1).click();await expect(dialog.locator('.el-input input').nth(1)).toHaveValue('2');
  await dialog.getByRole('button',{name:'提交审核',exact:true}).click();await admin.getByRole('dialog',{name:'提交确认',exact:true}).getByRole('button',{name:'确认提交',exact:true}).click();await expect(admin.getByRole('dialog',{name:'核心指标详情'})).toContainText('待归口审核');
  await admin.getByRole('dialog',{name:'核心指标详情'}).getByRole('button',{name:'返回',exact:true}).click();
  let h=await(await api.get('/api/core-indicators/2/history',{headers:{Authorization:await token(110228)}})).json();assert.equal(h[0].submitBy,110228);assert.equal(h[0].currentHandlerId,910002);
  await admin.locator('.indicator-table .indicator-row').nth(1).locator('.el-table__expand-icon').click();await expect(admin.locator('.recent-reports').getByText('截至本次累计值')).toBeVisible();await expect(admin.locator('.recent-reports')).toContainText('审计管理员');
  checks.push('admin-real-save-reopen-submit-and-expand-recent-record');
  const office=await pageFor(910006);await expect(office.getByRole('button',{name:'填报',exact:true})).toHaveCount(10);
  await expect(office.getByRole('menuitem',{name:'核心指标',exact:true})).toBeVisible();
  await office.getByRole('button',{name:'填报',exact:true}).nth(2).click();dialog=office.getByRole('dialog',{name:'核心指标填报'});await dialog.locator('.el-input input').nth(0).fill('0');await dialog.locator('.el-input input').nth(1).fill('3');
  await dialog.getByRole('button',{name:'提交审核',exact:true}).click();await office.getByRole('dialog',{name:'提交确认',exact:true}).getByRole('button',{name:'确认提交',exact:true}).click();await expect(office.getByRole('dialog',{name:'核心指标详情'})).toContainText('待归口审核');
  h=await(await api.get('/api/core-indicators/3/history',{headers:{Authorization:await token(910006)}})).json();assert.equal(h[0].submitBy,910006);assert.equal(h[0].currentHandlerId,910002);
  checks.push('office-menu-and-real-submission-without-task-responsibilities');
  const reviewer=await pageFor(910002);await expect(reviewer.locator('.indicator-table .indicator-row')).toHaveCount(10);await expect(reviewer.getByRole('button',{name:'填报',exact:true})).toHaveCount(0);checks.push('reviewer-without-fill-duty-remains-readonly');
  assert.deepEqual(errors,[]);await fs.writeFile(path.join(output,'result.json'),JSON.stringify({checks,pageErrors:errors},null,2));console.log(JSON.stringify({checks,pageErrors:errors}));
 }finally{for(const c of contexts)await c.close();await api.dispose();await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
