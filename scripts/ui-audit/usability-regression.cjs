// Production-build UX regression; writes are confined to the serve.py random schema.
const fs=require('node:fs/promises'),sync=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const {chromium}=require('../../target/ui-audit-tools/node_modules/playwright');
const {expect:baseExpect}=require('../../target/ui-audit-tools/node_modules/playwright/test');
const expect=baseExpect.configure({timeout:20000});
const statePath=process.env.SHUANGGAO_UI_STATE_FILE,state=JSON.parse(sync.readFileSync(statePath,'utf8'));
assert(/^biz_review_test_[0-9a-f]{32}$/.test(state.schema));const origin=new URL(state.frontend);assert(origin.hostname==='127.0.0.1'&&origin.protocol==='http:');
const BASE=origin.origin,OUT=path.join(path.dirname(statePath),'usability-evidence'),checks=[],errors=[],measurements=[];
let browser,context,page,releases=[];
const frame=()=>page.evaluate(()=>new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r))));
const settle=async()=>{await expect(page.locator('.query-filter-bar p[role=status]').filter({visible:true})).toHaveCount(0);await expect(page.locator('.el-loading-mask').filter({visible:true})).toHaveCount(0);await frame()};
const shot=name=>page.screenshot({path:path.join(OUT,name+'.png'),fullPage:true});
function gate(){let release,enter;const promise=new Promise(r=>release=r),entered=new Promise(r=>enter=r);releases.push(release);return{promise,entered,release,enter}}
async function stats(){return page.evaluate(()=>{const wrapper=document.querySelector('.table-wrapper'),table=document.querySelector('.virtual-tree-table'),api=table?.__vueParentComponent?.exposed;return {wrapper:wrapper?.getBoundingClientRect().toJSON(),button:document.querySelector('.query-submit')?.getBoundingClientRect().toJSON(),scroll:api?.getScrollState(),transfer:document.querySelector('.transfer-btn')?.textContent,expanded:[...document.querySelectorAll('button[aria-expanded=true]')].map(e=>e.getAttribute('aria-label')),selected:document.querySelector('.query-filter-bar+*')?.textContent?.length}})}
async function masks(){return page.evaluate(()=>[...document.querySelectorAll('.el-loading-mask')].filter(e=>getComputedStyle(e).display!=='none'&&Number(getComputedStyle(e).opacity)>0).map(e=>({mask:e.getBoundingClientRect().toJSON(),parent:e.parentElement.getBoundingClientRect().toJSON(),position:getComputedStyle(e.parentElement).position})))}
async function unchanged(before,label){const after=await stats();for(const key of ['y','width'])assert.ok(Math.abs(before.wrapper[key]-after.wrapper[key])<1,label+' '+key);assert.ok(Math.abs(before.button.width-after.button.width)<1);if(before.scroll?.rowKey)assert.equal(after.scroll.rowKey,before.scroll.rowKey);if(before.scroll)assert.ok(Math.abs(after.scroll.scrollTop-before.scroll.scrollTop)<2);assert.deepEqual(after.expanded,before.expanded);measurements.push({label,before,after});}
async function navigate(route){await page.evaluate(url=>document.querySelector('#app').__vue_app__.config.globalProperties.$router.push(url),route);await settle()}
async function loadingOrder(route,domain,permissionFirst){
 const data=gate(),permissions=gate();let held=false;
 await page.route('**/api/**',async r=>{const u=new URL(r.request().url());
  if(u.pathname==='/api/list-filters/'+domain&&!held){held=true;const response=await r.fetch();data.enter();await data.promise;return r.fulfill({response}).catch(()=>{})}
  if(!permissionFirst&&u.pathname.endsWith('/capabilities')){const response=await r.fetch();permissions.enter();await permissions.promise;return r.fulfill({response}).catch(()=>{})}
  return r.continue();
 });
 // The menu click exercises the actual shared layout, including warm re-entry.
 await page.getByRole('menuitem',{name:domain==='tasks'?'任务':'绩效',exact:true}).click();await data.entered;
 await expect(page.getByText('条件已修改，待查询',{exact:true})).toHaveCount(0);
 if(permissionFirst)await expect(page.locator('.table-wrapper.transfer-capable')).toBeVisible();
 for(const item of await masks()){assert.equal(item.position,'relative');assert.ok(item.mask.x>=item.parent.x-1&&item.mask.y>=item.parent.y-1);assert.ok(item.mask.width<=item.parent.width+1&&item.mask.height<=item.parent.height+1)}
 assert.equal(await page.locator('.works-container').evaluate(e=>getComputedStyle(e).opacity),'1');await shot('loading-'+domain+'-'+permissionFirst);
 data.release();if(!permissionFirst){await permissions.entered;permissions.release()}await settle();await page.unroute('**/api/**');
 checks.push(domain+' loading stays within the table with '+(permissionFirst?'permissions first':'data first')+'; no initial false dirty hint');
}

(async()=>{await fs.mkdir(OUT,{recursive:true});browser=await chromium.launch({channel:'msedge',headless:true});context=await browser.newContext({viewport:{width:1280,height:720},reducedMotion:'reduce'});await context.route('**/*',r=>new URL(r.request().url()).hostname==='127.0.0.1'?r.continue():r.abort());page=await context.newPage();page.setDefaultTimeout(30000);page.on('pageerror',e=>errors.push(e.message));await page.clock.install();
 await page.addInitScript(()=>{window.__uxFrames=[];const sample=()=>{const main=document.querySelector('.content-wrapper');if(!main)return;const root=main.firstElementChild;if(root&&Number(getComputedStyle(root).opacity)<0.98)window.__uxFrames.push('transparent-route');for(const e of document.querySelectorAll('.el-loading-mask')){const r=e.getBoundingClientRect(),s=getComputedStyle(e);if(s.display!=='none'&&Number(s.opacity)>0.1&&r.x<2&&r.y<2&&r.width>=innerWidth-2&&r.height>=innerHeight-2)window.__uxFrames.push('fullscreen-mask')}};new MutationObserver(sample).observe(document,{subtree:true,attributes:true,childList:true});});
 try{
  await page.goto(BASE+'/login');await page.getByPlaceholder('账号',{exact:true}).fill('110228');await page.getByPlaceholder('密码',{exact:true}).fill('review-fixture-password');await page.getByRole('button',{name:/登\s*录/}).click();await page.waitForURL('**/home/**');await settle();
  await loadingOrder('/home/works/performance','performance',true);
  await loadingOrder('/home/works','tasks',false);
  await loadingOrder('/home/works/performance','performance',false);
  await loadingOrder('/home/works','tasks',true);
  await page.getByRole('button',{name:'批量移交',exact:true}).click();await page.getByRole('checkbox',{name:'全选当前筛选结果',exact:true}).locator('..').click();
  const collapsed=page.getByRole('button',{name:'展开审计专用A二级任务',exact:true});if(await collapsed.count())await collapsed.click();
  const table=page.locator('.virtual-tree-table');await table.hover();await page.mouse.wheel(0,500);await frame();
  const keyword=page.getByLabel('任务编号／名称',{exact:true});await keyword.fill('尚未应用的筛选');await expect(page.getByText('条件已修改，待查询',{exact:true})).toBeVisible();
  const before=await stats(),selection=await page.getByRole('button',{name:/^确认移交/}).innerText();
  for(let cycle=1;cycle<=3;cycle++){
   const data=gate();let held=false;await page.route('**/api/list-filters/tasks?*',async r=>{if(held)return r.continue();held=true;const response=await r.fetch();data.enter();await data.promise;return r.fulfill({response}).catch(()=>{})});
   await page.clock.fastForward(16000);await data.entered;await expect(page.getByText('正在后台更新…',{exact:true})).toBeVisible();assert.equal((await masks()).length,0);await unchanged(before,'task-background-'+cycle+'-pending');await expect(keyword).toHaveValue('尚未应用的筛选');await expect(page.getByText('条件已修改，待查询',{exact:true})).toBeVisible();
   data.release();await settle();await page.unroute('**/api/list-filters/tasks?*');await unchanged(before,'task-background-'+cycle+'-done');await expect(page.getByRole('button',{name:selection,exact:true})).toBeVisible();await expect(page.getByRole('checkbox',{name:'全选当前筛选结果',exact:true})).toBeChecked();
   checks.push('Automatic task refresh cycle '+cycle+' preserves layout, reading anchor, expansion, selection and draft filters');
  }
  await shot('background-refresh-stable');
  await page.route('**/api/list-filters/tasks?*',r=>r.fulfill({status:503,contentType:'application/json',body:JSON.stringify({message:'合成后台更新失败'})}),{times:1});await page.clock.fastForward(16000);await expect(page.locator('.query-status')).toContainText('后台更新失败，当前显示上次结果');await unchanged(before,'background-error');assert.equal((await masks()).length,0);await page.getByRole('button',{name:'重试',exact:true}).click();await settle();await unchanged(before,'background-retry');checks.push('Background failure and retry preserve existing data and layout');
  const bar=page.locator('[aria-label="任务筛选"]'),explicit=gate();await page.route('**/api/list-filters/tasks?*',async r=>{const response=await r.fetch();explicit.enter();await explicit.promise;return r.fulfill({response}).catch(()=>{})},{times:1});await bar.getByRole('button',{name:'查询',exact:true}).click();await explicit.entered;await expect(page.getByText('条件已修改，待查询',{exact:true})).toHaveCount(0);explicit.release();await settle();await expect(page.getByText('暂无匹配任务',{exact:true})).toBeVisible();await bar.getByRole('button',{name:'重置全部',exact:true}).click();await settle();await expect(page.getByText('条件已修改，待查询',{exact:true})).toHaveCount(0);checks.push('Explicit query hides the pending hint; empty results and default reset are accurate');
  for(const route of ['/home/works/achievement','/home/audit','/home/work-records','/home/core-indicators','/home/notice']){await navigate(route);assert.deepEqual(await page.evaluate(()=>window.__uxFrames),[]);checks.push('Shared navigation remains visible and non-flashing at '+route)}
  // Inspect a real, deliberately different budget while its successful response is held.
  const budget=gate();await page.route('**/api/list-filters/budget?*',async r=>{const response=await r.fetch();budget.enter();await budget.promise;return r.fulfill({response}).catch(()=>{})},{times:1});await page.evaluate(()=>document.querySelector('#app').__vue_app__.config.globalProperties.$router.push('/home/works/budget?year=2026&month=7'));await budget.entered;await expect(page.locator('.budget-sheet')).toHaveCount(0);await expect(page.locator('.budget-placeholder')).toContainText('2026年7月');await expect(page.locator('.works-container')).not.toContainText('36100.00');await expect(page.locator('.works-container')).not.toContainText('7580.24');await shot('budget-before-real-data');budget.release();await settle();await expect(page.locator('.budget-sheet').first()).toContainText('120.00');await expect(page.locator('.budget-sheet').first()).toContainText('合成资金来源');checks.push('Initial budget loading displays no sample or unconfirmed amounts and success shows actual source and 120.00');
  for(const status of [503,403]){
   await page.route('**/api/list-filters/budget?*',r=>r.fulfill({status,contentType:'application/json',body:JSON.stringify({message:'合成预算读取失败'})}),{times:1});const b=page.locator('[aria-label="预算筛选"]');await b.getByRole('button',{name:'查询',exact:true}).click();await expect(page.locator('.budget-placeholder')).toContainText('预算加载失败');await expect(page.locator('.budget-sheet')).toHaveCount(0);checks.push('Budget '+status+' response does not expose old or sample amounts');await b.getByRole('button',{name:'查询',exact:true}).click();await settle();
  }
  const period=gate();await page.route('**/api/list-filters/budget?*',async r=>{const response=await r.fetch();period.enter();await period.promise;return r.fulfill({response}).catch(()=>{})},{times:1});await page.getByRole('combobox',{name:'预算月份',exact:true}).click();await page.getByRole('option',{name:'5月',exact:true}).filter({visible:true}).click();await page.locator('[aria-label="预算筛选"]').getByRole('button',{name:'查询',exact:true}).click();await period.entered;await expect(page.locator('.budget-sheet')).toHaveCount(0);await expect(page.locator('.budget-placeholder')).toContainText('2026年5月');period.release();await settle();checks.push('Changing budget month hides stale values until the matching response succeeds');
  assert.deepEqual(await page.evaluate(()=>window.__uxFrames),[]);assert.deepEqual(errors,[]);await fs.writeFile(path.join(OUT,'results.json'),JSON.stringify({checks,errors,measurements},null,2));console.log(JSON.stringify({passed:true,checks:checks.length}));
 }catch(error){await fs.writeFile(path.join(OUT,'partial.json'),JSON.stringify({checks,errors,measurements},null,2));await fs.writeFile(path.join(OUT,'failure.txt'),await page.locator('body').innerText().catch(()=>''));await shot('failure').catch(()=>{});throw error}
 finally{releases.forEach(r=>r());if(context)await context.close();if(browser)await browser.close()}
})().catch(error=>{console.error(error.stack||error);process.exitCode=1});
