// Run only with serve.py --fixture browser-repairs; all writes target synthetic records.
const fs=require('node:fs/promises'),syncFs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
let playwright;
try { playwright=require('../../target/ui-audit-tools/node_modules/playwright'); }
catch { playwright=require('playwright'); }
const {chromium,request}=playwright;
let expect;
try { ({expect}=require('../../target/ui-audit-tools/node_modules/playwright/test')); }
catch { ({expect}=require('playwright/test')); }
const ROOT=path.resolve(__dirname,'../../..');
const statePath=process.env.SHUANGGAO_UI_STATE_FILE;
assert(statePath,'Set SHUANGGAO_UI_STATE_FILE to the disposable serve.py state.json');
const state=JSON.parse(syncFs.readFileSync(statePath,'utf8'));
assert(/^biz_review_test_[0-9a-f]{32}$/.test(state.schema),'Refusing a non-disposable database');
const origin=new URL(state.frontend);
assert(origin.protocol==='http:'&&origin.hostname==='127.0.0.1'&&origin.port,'Refusing non-local UI');
const OUT=path.join(path.dirname(path.resolve(statePath)),'browser-repair-evidence'),BASE=origin.origin;
const mode=process.argv[2],errors=[],checks=[];
assert(['budget','performance','achievement','audit','finance','admin-tabs'].includes(mode),'Choose budget, performance, achievement, audit, finance or admin-tabs');
let browser,context,page,api;
const settle=()=>page.waitForFunction(()=>[...document.querySelectorAll('.el-loading-mask')].filter(e=>e.getBoundingClientRect().width&&e.getBoundingClientRect().height).length===0);
async function show(user=110228,url='/home/works/budget'){
 if(context)await context.close();
 context=await browser.newContext({viewport:{width:1680,height:1050},reducedMotion:'reduce'});
 await context.route('**/*',r=>new URL(r.request().url()).hostname==='127.0.0.1'?r.continue():r.abort());
 page=await context.newPage();page.setDefaultTimeout(15000);page.on('pageerror',e=>errors.push(e.message));if(mode==='performance')await page.clock.install();
 await page.goto(BASE+'/login');await page.getByPlaceholder('账号',{exact:true}).fill(String(user));await page.getByPlaceholder('密码',{exact:true}).fill('review-fixture-password');
 await page.getByRole('button',{name:/登\s*录/}).click();await page.waitForURL('**/home/**');
 await page.goto(BASE+url);await page.locator('.query-filter-bar').first().waitFor({state:'visible'});await expect(page.locator('.query-filter-bar').first().locator('p[role=status]')).toBeHidden();await settle();
 return page;
}
async function call(method,url,data){
 const token=await page.evaluate(()=>localStorage.getItem('token'));
 const response=await api[method](url,{headers:{Authorization:token},...(data===undefined?{}:{data})});
 const value=await response.json().catch(()=>null);return{status:response.status(),value};
}
async function select(label,option,scope=page){
 await scope.getByRole('combobox',{name:label,exact:true}).click();
 await page.getByRole('option',{name:option,exact:true}).filter({visible:true}).click();
}
const queryBar=()=>page.locator('[aria-label="预算筛选"]');
async function budgetMonth(month){await select('预算月份',`${month}月`);await queryBar().getByRole('button',{name:'查询',exact:true}).click();await settle();}
const amount=()=>page.locator('.budget-sheet').first().locator('.input-cell input').first();
async function edit(value){await amount().fill(String(value),{force:true});await amount().press('Tab');await expect(page.getByText('有未保存的修改，请保存后再离开或切换预算。',{exact:true})).toBeVisible();}
async function save(){const response=page.waitForResponse(r=>r.url().endsWith('/api/budget/save')&&r.request().method()==='POST');await page.getByRole('button',{name:'保存修改',exact:true}).click();const r=await response;assert.equal(r.status(),200,await r.text());await settle();}
async function shot(name){await page.screenshot({path:path.join(OUT,'evidence',name+'.png'),fullPage:true});}
async function performance(year){
 const bar=page.locator('[aria-label="绩效筛选"]');
 await select('绩效年度',`${year}年`,bar);await bar.getByLabel('绩效编号／名称',{exact:true}).fill('审计手动绩效');
 await bar.getByRole('button',{name:'查询',exact:true}).click();await settle();
 const target=page.getByText('审计手动绩效',{exact:true});
 if(!await target.isVisible())await page.getByText('社会效益指标',{exact:true}).click();
 await expect(target).toBeVisible();return bar;
}
const perfDrawer=()=>page.getByRole('dialog',{name:'绩效指标详情',exact:true});
const perfHeader=()=>perfDrawer().locator('.info-row').filter({hasText:'当前完成值'}).locator('.value');
async function openPerf(){await page.getByText('审计手动绩效',{exact:true}).click();await expect(perfDrawer()).toBeVisible();await settle();}
async function closePerf(){await perfDrawer().locator('.el-drawer__close-btn').click();await expect(perfDrawer()).toBeHidden();}

(async()=>{
 await fs.mkdir(path.join(OUT,'evidence'),{recursive:true});browser=await chromium.launch({channel:'msedge',headless:true});api=await request.newContext({baseURL:BASE});
 try{
  await show();
  if(mode==='budget'){
   await expect(page.getByRole('button',{name:'解锁',exact:true})).toBeVisible();
   await page.getByRole('button',{name:'版本记录',exact:true}).click();let history=page.getByRole('dialog',{name:'预算版本记录',exact:true});
   await expect(history.locator('.applied-tags')).toContainText('5月');await history.locator('.el-dialog__headerbtn').click();
   await budgetMonth(6);await page.getByRole('button',{name:'导入',exact:true}).click();let upload=page.getByRole('dialog',{name:'导入预算数据',exact:true});
   await upload.locator('input[type=file]').setInputFiles(path.join(ROOT,'biz/public/templates/budget-template.xlsx'));
   await upload.getByRole('button',{name:'确认导入并锁定',exact:true}).click();await expect(upload).toBeHidden();await settle();
   await page.getByRole('button',{name:'版本记录',exact:true}).click();history=page.getByRole('dialog',{name:'预算版本记录',exact:true});
   await expect(history.locator('.applied-tags')).toContainText('6月');await expect(history.locator('.el-table__body')).toContainText('2026年6月');
   await shot('budget-history-current-month');await history.locator('.el-dialog__headerbtn').click();checks.push('May history then June import opens June history immediately');
   const baseline=(await call('get','/api/budget?year=2026&month=6')).value;
   await page.getByRole('button',{name:'解锁',exact:true}).click();await expect(amount()).not.toHaveAttribute('disabled','');await edit('222.22');await save();await expect(amount()).toHaveValue('222.22');
   await page.getByRole('button',{name:'锁定',exact:true}).click();await expect(amount()).toBeDisabled();await page.reload();await settle();await expect(amount()).toHaveValue('222.22');
   const persisted=(await call('get','/api/budget?year=2026&month=6')).value;
   assert.equal(persisted.sources[0].annualPlan,222.22);
   for(const field of ['fiveYearTotal','available','carryover','arrived'])assert.deepEqual(persisted.sources[0][field],baseline.sources[0][field]);
   assert.deepEqual(persisted.tasks,baseline.tasks);await shot('budget-edits-persist-after-lock-reload');checks.push('222.22 survives save, lock and reload; imported columns unchanged');
   const download=page.waitForEvent('download');await page.getByRole('button',{name:'整表导出',exact:true}).click();await(await download).saveAs(path.join(OUT,'budget-after-edit.xlsx'));checks.push('Saved budget exported as Excel');
   await page.getByRole('button',{name:'解锁',exact:true}).click();await expect(amount()).not.toHaveAttribute('disabled','');await edit('333.33');
   await select('预算月份','7月');await queryBar().getByRole('button',{name:'查询',exact:true}).click();let decision=page.getByRole('dialog',{name:'未保存修改',exact:true});
   await expect(decision.getByRole('button',{name:'保存后继续',exact:true})).toBeVisible();await decision.getByRole('button',{name:'取消',exact:true}).click();await expect(decision).toBeHidden();
   await expect(amount()).toHaveValue('333.33');await expect(queryBar().locator('.applied-tags')).toContainText('6月');
   await select('预算月份','7月');await queryBar().getByRole('button',{name:'查询',exact:true}).click();await decision.getByRole('button',{name:'保存后继续',exact:true}).click();await expect(decision).toBeHidden();await settle();
   assert.equal((await call('get','/api/budget?year=2026&month=6')).value.sources[0].annualPlan,333.33);await expect(queryBar().locator('.applied-tags')).toContainText('7月');checks.push('Cancel retains draft and applied month; Save then continue persists before querying');
   await budgetMonth(6);await edit('444.44');await select('预算月份','5月');await queryBar().getByRole('button',{name:'查询',exact:true}).click();await decision.getByRole('button',{name:'放弃修改',exact:true}).click();await expect(decision).toBeHidden();await settle();assert.equal((await call('get','/api/budget?year=2026&month=6')).value.sources[0].annualPlan,333.33);checks.push('Discard changes does not write values');
   await budgetMonth(6);await edit('555.55');await page.getByRole('button',{name:'锁定',exact:true}).click();decision=page.getByRole('dialog',{name:'锁定预算',exact:true});await decision.getByRole('button',{name:'保存并锁定',exact:true}).click();await expect(decision).toBeHidden();await expect(amount()).toBeDisabled();assert.equal((await call('get','/api/budget?year=2026&month=6')).value.sources[0].annualPlan,555.55);checks.push('Save and lock is atomic');
   await page.getByRole('button',{name:'解锁',exact:true}).click();await expect(amount()).not.toHaveAttribute('disabled','');await edit('666.66');
   await page.route('**/api/budget/save',r=>r.abort('failed'),{times:1});await page.getByRole('button',{name:'保存修改',exact:true}).click();await expect(page.getByRole('button',{name:'保存修改',exact:true})).toBeEnabled();await expect(amount()).toHaveValue('666.66');assert.equal((await call('get','/api/budget?year=2026&month=6')).value.sources[0].annualPlan,555.55);await shot('budget-save-failure-keeps-draft');await save();checks.push('Failed save keeps draft; retry succeeds');
   await edit('777.77');await page.getByRole('menuitem',{name:'核心指标',exact:true}).click();decision=page.getByRole('dialog',{name:'未保存修改',exact:true});await decision.getByRole('button',{name:'取消',exact:true}).click();assert.ok(page.url().endsWith('/home/works/budget'));await page.getByRole('menuitem',{name:'核心指标',exact:true}).click();await decision.getByRole('button',{name:'放弃修改',exact:true}).click();await page.waitForURL('**/home/core-indicators');checks.push('Route navigation offers save/discard/cancel without losing canceled draft');
  }else if(mode==='performance'){
   await page.goto(BASE+'/home/works/performance');await settle();await performance(2027);await openPerf();await expect(perfHeader()).toHaveText('4.00');await closePerf();
   let release,entered;const gate=new Promise(r=>release=r),reached=new Promise(r=>entered=r);let held=false;
   await page.route('**/api/list-filters/performance?*',async route=>{if(held||new URL(route.request().url()).searchParams.get('f.year')!=='2026')return route.continue();held=true;const real=await route.fetch();entered();await gate;await route.fulfill({response:real});});
   const bar=page.locator('[aria-label="绩效筛选"]');await select('绩效年度','2026年',bar);await bar.getByRole('button',{name:'查询',exact:true}).click();await reached;
   await page.getByText('审计手动绩效',{exact:true}).click({force:true});await expect(perfDrawer()).toBeHidden();release();await settle();await page.unroute('**/api/list-filters/performance?*');
   await openPerf();await expect(perfHeader()).toHaveText('3.00');await expect(perfDrawer()).toContainText('2026');await shot('performance-2026-header-and-history');await closePerf();checks.push('Old row cannot open during delayed year query; 2026 header and history agree');
   await performance(2027);await openPerf();await expect(perfHeader()).toHaveText('4.00');await shot('performance-2027-header-and-form');checks.push('Reverse year change returns current 2027 values');
   const form=perfDrawer().getByPlaceholder('请输入完成值',{exact:true});
   if(await form.count()){await form.fill('9',{force:true});const response=page.waitForResponse(r=>r.url().includes('/api/list-filters/performance?'));await page.clock.fastForward(31000);await response;await expect(form).toHaveValue('9');checks.push('Background refresh retains unsaved manual value');}
   await page.goto(BASE+'/home/works/performance?perfId=950011&year=2027');await settle();await expect(perfDrawer()).toBeVisible();await expect(perfHeader()).toHaveText('4.00');await expect(page.locator('[aria-label="绩效筛选"] .applied-tags')).toContainText('2027年');await expect(perfDrawer()).toContainText('2027');await shot('performance-deep-link-keeps-year');checks.push('Direct performance link uses requested year in header, form and applied filters');
   let releaseLink,enteredLink;const linkGate=new Promise(r=>releaseLink=r),linkReached=new Promise(r=>enteredLink=r);let linkHeld=false;
   await page.route('**/api/list-filters/performance?*',async route=>{if(linkHeld||new URL(route.request().url()).searchParams.get('f.year')!=='2026')return route.continue();linkHeld=true;const real=await route.fetch();enteredLink();await linkGate;await route.fulfill({response:real}).catch(()=>{});});
   await page.evaluate(()=>document.querySelector('#app').__vue_app__.config.globalProperties.$router.push('/home/works/performance?perfId=950011&year=2026'));await linkReached;
   const newest=page.waitForResponse(r=>r.url().includes('/api/list-filters/performance?')&&new URL(r.url()).searchParams.get('f.year')==='2027');
   await page.evaluate(()=>document.querySelector('#app').__vue_app__.config.globalProperties.$router.push('/home/works/performance?perfId=950011&year=2027'));await newest;releaseLink();await settle();await expect(perfDrawer()).toBeVisible();await expect(perfHeader()).toHaveText('4.00');await expect(page.locator('[aria-label="绩效筛选"] .applied-tags')).toContainText('2027年');await page.unroute('**/api/list-filters/performance?*');checks.push('Rapid same-page year links cancel old query and open only the latest annual detail');
  }else if(mode==='achievement'){
   await show(1910001,'/home/works/achievement');
   const prior=(await call('get','/api/achievement/audit/achievement/990101')).value;
   const active=Array.isArray(prior)?prior.find(row=>Number(row.flowStatus)===10):null;
   if(active){const admin=(await(await api.post('/api/system/login',{data:{user_id:110228,password:'review-fixture-password'}})).json()).token;const reset=await api.post('/api/achievement/audit',{headers:{Authorization:admin},data:{sub_id:active.subId,is_pass:false,content:'合成重测退回，保留已有审核记录'}});assert.equal(reset.status(),200);await page.reload();await settle();}
   const target=page.getByText(/合成长名称成果.*全名结尾/).first();
   if(!await target.isVisible())await page.getByText('1.落实立德树人根本任务',{exact:true}).click();
   const row=page.locator('.el-table__row').filter({hasText:'全名结尾'}).first();await expect(row.getByRole('button',{name:'重新提交',exact:true})).toBeVisible();
   await row.getByRole('button',{name:'修改',exact:true}).click();const form=page.getByRole('dialog',{name:'修改成果',exact:true});await form.getByPlaceholder('请输入具体内容').fill('已补充上级发文依据，保留原记录。');await form.getByRole('button',{name:'保存修改',exact:true}).click();await expect(form).toBeHidden();await settle();
   await row.getByRole('button',{name:'重新提交',exact:true}).click();const confirmation=page.getByRole('dialog',{name:'重新提交成果',exact:true});
   const resubmitted=page.waitForResponse(r=>r.url().endsWith('/api/achievement/990101/resubmit')&&r.request().method()==='POST');
   await confirmation.getByRole('button',{name:'确认提交',exact:true}).click();const sent=await resubmitted;assert.equal(sent.status(),200,await sent.text());await expect(confirmation).toBeHidden();await settle();
   const audits=(await call('get','/api/achievement/audit/achievement/990101')).value;
   assert.ok(Array.isArray(audits)&&audits.some(x=>Number(x.flowStatus)===10)&&audits.some(x=>Number(x.flowStatus)<0),JSON.stringify(audits));await expect(row.getByRole('button',{name:'重新提交',exact:true})).toHaveCount(0);await shot('achievement-resubmitted-keeps-record');checks.push('Owner saves separately then resubmits same achievement; old rejection preserved');
  }else if(mode==='audit'){
   await show(910001,'/home/works/performance');await performance(2027);await openPerf();
   const number=perfDrawer().getByPlaceholder('请输入完成值',{exact:true});
   await expect(number).not.toHaveAttribute('disabled','');await number.fill('6',{force:true});
   const submitted=page.waitForResponse(r=>r.url().includes('/api/performance/submit')&&r.request().method()==='POST');
   await perfDrawer().getByRole('button',{name:'提交填报',exact:true}).click();await page.getByRole('dialog',{name:'提交确认',exact:true}).getByRole('button',{name:'确认提交',exact:true}).click();const submittedResponse=await submitted;assert.equal(submittedResponse.status(),200,await submittedResponse.text());await settle();
   await show(910003,'/home/audit');const bar=page.locator('[aria-label="审核筛选"]');await bar.getByLabel('审核编号／名称',{exact:true}).fill('审计手动绩效');await bar.getByRole('button',{name:'查询',exact:true}).click();await settle();
   const row=page.locator('.audit-container .el-table__body tbody tr.el-table__row').first();await row.getByRole('button',{name:'审批',exact:true}).click();const dialog=page.getByRole('dialog',{name:'业务审批',exact:true});await dialog.getByText('驳回',{exact:true}).click();await dialog.locator('textarea').fill('请补充年度完成情况');await dialog.getByRole('button',{name:'确认提交',exact:true}).click();await expect(dialog).toBeHidden();
   await page.getByText('审批记录',{exact:true}).click();await settle();await expect(page.locator('.audit-container .el-table__body')).toContainText('审计手动绩效');await shot('reviewer-can-find-rejected-performance-history');checks.push('Actual reviewer rejection remains visible in completed records');
  }else if(mode==='admin-tabs'){
   await show(110228,'/home/admin/data?tab=budget&year=2026&month=6');await settle();
   await expect(amount()).not.toHaveAttribute('disabled','');const previous=await amount().inputValue();await edit('999.99');
   const banner=page.locator('.admin-banner');await banner.getByText('工作纪实',{exact:true}).click();let decision=page.getByRole('dialog',{name:'未保存修改',exact:true});await decision.getByRole('button',{name:'取消',exact:true}).click();await expect(decision).toBeHidden();await expect(amount()).toHaveValue('999.99');await expect(banner.getByRole('radio',{name:'预算版本',exact:true})).toBeChecked();
   await banner.getByText('工作纪实',{exact:true}).click();await decision.getByRole('button',{name:'放弃修改',exact:true}).click();await expect(page.locator('.budget-sheet')).toHaveCount(0);assert.equal((await call('get','/api/budget?year=2026&month=6')).value.sources[0].annualPlan,Number(previous));checks.push('Management tab cancel retains draft; discard changes tab without writing');
  }else if(mode==='finance'){
   await show(910007,'/home/works/budget');await budgetMonth(6);await expect(page.getByRole('button',{name:'保存修改',exact:true})).toBeVisible();
   assert.equal(await page.getByRole('button',{name:/^解锁$|^锁定$/}).count(),0);
   await edit('888.88');await save();assert.equal((await call('get','/api/budget?year=2026&month=6')).value.sources[0].annualPlan,888.88);checks.push('Finance can save an unlocked budget without lock permission');
   await show(111651,'/home/works/budget');await budgetMonth(6);assert.equal(await page.getByRole('button',{name:'保存修改',exact:true}).count(),0);assert.equal(await page.getByRole('button',{name:'导入',exact:true}).count(),0);await expect(amount()).toBeDisabled();checks.push('Leader budget remains read-only');await shot('leader-budget-read-only');
  }
  assert.deepEqual(errors,[]);await fs.writeFile(path.join(OUT,mode+'-results.json'),JSON.stringify({mode,checks,errors},null,2));console.log(JSON.stringify({passed:true,mode,checks}));
 }catch(error){if(page){await fs.writeFile(path.join(OUT,mode+'-failure.txt'),await page.locator('body').innerText().catch(()=>''));await shot(mode+'-failure').catch(()=>{});}throw error}
 finally{if(context)await context.close();if(browser)await browser.close();if(api)await api.dispose()}
})().catch(error=>{console.error(error.stack||error);process.exitCode=1});
