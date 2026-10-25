const {chromium,request}=require('../../target/ui-audit-tools/node_modules/playwright');
const {expect}=require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
(async()=>{
 const file=process.env.UI_AUDIT_STATE_FILE,state=JSON.parse(await fs.readFile(file));assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/);const base=state.comparison||state.frontend;
 const api=await request.newContext({baseURL:base}),token=(await(await api.post('/api/system/login',{data:{user_id:110228,password:'review-fixture-password'}})).json()).token;
 const browser=await chromium.launch({channel:'chrome',headless:true}),context=await browser.newContext({viewport:{width:1536,height:864}});await context.addInitScript(t=>localStorage.setItem('token',t),token);const page=await context.newPage(),errors=[],checks=[];page.on('pageerror',e=>errors.push(e.message));
 const rows=()=>page.locator('.el-table-v2__main .el-table-v2__row'),main=()=>page.locator('.el-table-v2__main'),scroll=async fraction=>main().evaluate((root,p)=>{const el=[...root.querySelectorAll('*')].find(e=>e.scrollHeight>e.clientHeight+50&&/(auto|scroll|hidden)/.test(getComputedStyle(e).overflowY));if(!el)throw new Error('No virtual scroll host');el.scrollTop=(el.scrollHeight-el.clientHeight)*p},fraction);
 try{for(const route of ['works','works/performance']){
  const domain=route==='works'?'tasks':'performance';const source=await(await api.get('/api/list-filters/'+domain+'?f.year=2026&responseMode=tree&separateOptions=true',{headers:{Authorization:token}})).json();
  await page.goto(base+'/home/'+route);await expect(rows().first()).toBeVisible();await expect(page.locator('.query-filter-bar [role=status]')).toHaveCount(0);
  const model=Number(await page.locator('.virtual-tree-table').getAttribute('data-model-row-count'));assert.ok(model>100);
  // Expand visible directories; performance synthetic indicators can already be roots.
  for(let pass=0;pass<5;pass++){
   const closed=main().locator('.el-table-v2__expand-icon:not(.is-expanded)');let clicked=false;
   for(let i=0;i<await closed.count();i++){const icon=closed.nth(i);if(await icon.isVisible()&&!(await icon.getAttribute('class')).includes('is-placeholder')){await icon.click();clicked=true;break;}}
   if(!clicked)break;await page.waitForTimeout(80);
  }
  const mounted=await rows().count();assert.ok(mounted<80,'Rendered rows must be bounded by viewport');await scroll(1);await page.waitForTimeout(250);assert.ok(await rows().count()<80);checks.push(route+':viewport-only-rendering');
  const expectedSelectable=domain==='performance'?source.items.filter(item=>String(item.ancestors||'').split(',').filter(Boolean).length>=3).length:source.total;await page.getByRole('button',{name:'批量移交',exact:true}).click();const head=main().locator('.el-table-v2__header input[type=checkbox]');await head.locator('..').click();await expect(head).toBeChecked();await expect(page.getByRole('button',{name:`确认移交（${expectedSelectable}）`,exact:true})).toBeVisible();checks.push(route+':all-filtered-rows-selected');await scroll(0);await page.waitForTimeout(200);await expect(main().locator('input[type=checkbox]').last()).toBeChecked();checks.push(route+':selection-survives-scroll');
  await scroll(.7);await page.waitForTimeout(200);const anchor=await rows().evaluateAll(els=>els.filter(e=>e.getBoundingClientRect().top>450&&e.getBoundingClientRect().top<800).map(e=>e.dataset.rowKey)[0]);
  await page.getByRole('menuitem',{name:/消息中心/}).click();await page.waitForURL('**/home/notice');await page.getByRole('menuitem',{name:route==='works'?'任务':'绩效',exact:true}).click();await expect(rows().first()).toBeVisible();await page.waitForTimeout(300);if(anchor)await expect(main().locator(`[data-row-key="${anchor}"]`)).toBeAttached();checks.push(route+':browse-anchor-restored');
  for(const width of [960,1920,1280]){await page.setViewportSize({width,height:720});await page.waitForTimeout(200);assert.ok(await rows().count()<90);assert.ok(await page.locator('.virtual-tree-table').evaluate(e=>e.clientWidth<=e.parentElement.clientWidth));}checks.push(route+':resize-and-narrow-width');
  const candidates=domain==='tasks'?source.items.filter(item=>Number(item.level)===3):source.items.filter(item=>String(item.ancestors||'').split(',').filter(Boolean).length>=3);const target=candidates.at(-1),id=domain==='tasks'?target.taskId:target.perfId;
  await page.goto(base+'/home/'+route+'?'+(domain==='tasks'?'taskId':'perfId')+'='+id);const locate=page.getByRole('button',{name:'返回列表并定位',exact:true});await expect(locate).toBeVisible();if(domain==='tasks'){const editor=page.getByRole('textbox',{name:'专家评语正文',exact:true});await expect(editor).toBeEnabled();const original=await editor.inputValue();await editor.fill('虚拟滚动未提交内容验证');await scroll(0);await page.waitForTimeout(100);await expect(editor).toHaveValue('虚拟滚动未提交内容验证');await editor.fill(original);checks.push(route+':unsaved-drawer-content-survives-row-unmount');}await locate.click();await expect(locate).not.toBeVisible();const located=main().locator('[data-row-key="'+(domain==='tasks'?'':'perf-')+id+'"]');await expect(located).toBeVisible();checks.push(route+':deep-link-expands-ancestors-and-locates-virtual-row');
  await page.screenshot({path:path.join(path.dirname(file),'virtual-'+route.replaceAll('/','-')+'.png'),fullPage:true});
 }
 assert.deepEqual(errors,[]);checks.push('no-browser-errors');
 }finally{await context.close();await browser.close();await api.dispose();await fs.writeFile(path.join(path.dirname(file),'virtual-tree-results.json'),JSON.stringify({checks,errors},null,2));}console.log(JSON.stringify({checks:checks.length,errors}));
})().catch(e=>{console.error(e);process.exitCode=1});
