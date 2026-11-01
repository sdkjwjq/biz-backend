// Original Home/Works UI, real HTTP, disposable database only.
const {chromium,request}=require('playwright');
const {expect}=require('playwright/test');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
(async()=>{
 const state=JSON.parse(await fs.readFile(process.env.SHUANGGAO_UI_STATE_FILE,'utf8'));
 const out=path.join(path.dirname(process.env.SHUANGGAO_UI_STATE_FILE),'task-page-evidence');await fs.mkdir(out,{recursive:true});
 const browser=await chromium.launch({channel:'msedge',headless:true}),api=await request.newContext({baseURL:state.frontend});
 const checks=[],errors=[],network=[];let currentPage;
 const call=async(method,url,token,data)=>{const r=await api[method]('/api'+url,{headers:token?{Authorization:token}:{},...(data===undefined?{}:{data})});assert.equal(r.status(),200,await r.text());return r.json()};
 const login=async id=>(await call('post','/system/login',null,{user_id:id,password:'review-fixture-password'})).token;
 const switchMode=async(token,experience)=>(await call('post','/system/review-context',token,{experience})).token;
 const local=d=>`${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}T${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}:00`;
 const newPage=async(token,size={width:1440,height:900})=>{const context=await browser.newContext({viewport:size});await context.addInitScript(t=>{if(!sessionStorage.getItem('seeded')){sessionStorage.setItem('seeded','1');localStorage.setItem('token',t)}},token);const page=await context.newPage();page.setDefaultTimeout(20000);page.on('pageerror',e=>errors.push(e.message));page.on('request',r=>{if(r.url().includes('/api/'))network.push(r.url())});currentPage=page;return page};
 const drawer=p=>p.locator('.el-drawer:visible');
 const button=(p,name)=>p.locator('button:visible').filter({hasText:new RegExp('^'+name+'$')}).last();
 const row=(p,id)=>p.locator(`.el-table-v2__row[data-row-key="${id}"]`);
 const action=(p,id,name)=>row(p,id).locator('button:visible').filter({hasText:new RegExp(name)}).last();
 async function expand(p){await expect(row(p,930000).first()).toBeVisible();for(const [name,id] of [['展开审计专用A一级任务',930001],['展开审计专用A二级任务',930002]]){const b=p.getByRole('button',{name,exact:true});if(await b.count())await b.click();await expect(row(p,id).first()).toBeVisible()}}
 async function createRound(admin,name,year,users,ids,start=-3600000,end=3600000){
  let r=await call('post','/admin/review-tasks',admin,{name,year,startsAt:local(new Date(Date.now()+start)),endsAt:local(new Date(Date.now()+end))});
  r=await call('post',`/admin/review-tasks/${r.round_id}/members`,admin,{version:r.version,userIds:users,reason:'isolated UI test'});
  r=await call('put',`/admin/review-tasks/${r.round_id}/scope`,admin,{version:r.version,userIds:users,taskIds:ids,reason:'isolated UI scope'});
  return call('post',`/admin/review-tasks/${r.round_id}/publish`,admin,{version:r.version,reason:'isolated UI publish'});
 }
 try{
  const admin=await switchMode(await login(110228),'BUSINESS');
  let round=await createRound(admin,'原任务页面评审验证',2026,[910001,910009],[930002]);
  round=await call('put',`/admin/review-tasks/${round.round_id}/scope`,admin,{version:round.version,userIds:[910001],taskIds:[930002,930102],reason:'teacher two tasks; external one'});
  const future=await createRound(admin,'管理员跨时段评审验证',null,[910003],[930002],7200000,10800000);
  const teacher=await newPage(await login(910001));await teacher.goto(state.frontend+'/review');
  await expect(teacher.getByRole('heading',{name:'选择使用角色'})).toBeVisible();await teacher.getByRole('button',{name:/^专家评审/}).click();
  await expect(teacher.locator('.round-strip')).toContainText('原任务页面评审验证');await expect(teacher.locator('.virtual-tree-table')).toBeVisible();await expand(teacher);
  await expect(action(teacher,930002,'查看')).toBeVisible();await expect(teacher.locator('.top-header')).toBeVisible();
  for(const name of ['消息中心','审核中心','管理中心'])await expect(teacher.getByRole('menuitem',{name,exact:true})).toHaveCount(0);
  await expect(teacher.getByRole('button',{name:'新增任务',exact:true})).toHaveCount(0);checks.push('Expert role enters original Home shell and virtual Works tree without business menus/actions');
  const firstDetail=teacher.waitForResponse(r=>r.url().includes('/tasks/930002/page-detail'));
  await action(teacher,930002,'查看').click();await firstDetail;await expect(drawer(teacher).getByText('基本信息与完成情况',{exact:true})).toBeVisible();
  await expect(drawer(teacher).getByText('实际完成率 150%',{exact:true})).toBeVisible();await expect(drawer(teacher).getByText('计入汇总 100%',{exact:true})).toBeVisible();
  assert.ok(await drawer(teacher).locator('.task-info-box').evaluate(el=>el.getBoundingClientRect().top>0));
  await drawer(teacher).locator('.el-drawer__close-btn').click();
  await action(teacher,930002,'专家评语').click();const text=drawer(teacher).getByRole('textbox',{name:'专家评语正文',exact:true});await expect(text).toBeFocused();
  await text.fill('原任务抽屉填写，完成情况与材料同时可查。');await drawer(teacher).locator('label.el-radio').filter({hasText:/^通过$/}).click();
  const originalToken=await teacher.evaluate(()=>localStorage.getItem('token'));
  // A modal drawer covers the top-bar role menu. Exercise the same router
  // navigation directly, rather than force-clicking through its overlay.
  await teacher.evaluate(()=>{void document.querySelector('#app').__vue_app__.config.globalProperties.$router.push('/choose-role')});await teacher.getByRole('button',{name:'继续填写',exact:true}).click();await expect(text).toHaveValue('原任务抽屉填写，完成情况与材料同时可查。');assert.equal(await teacher.evaluate(()=>localStorage.getItem('token')),originalToken);
  await drawer(teacher).getByRole('button',{name:'保存草稿',exact:true}).click();await expect(teacher.getByText('评语草稿已保存',{exact:true})).toBeVisible();
  await drawer(teacher).getByRole('button',{name:'提交评语',exact:true}).click();await teacher.getByRole('button',{name:'确认提交',exact:true}).click();await expect(teacher.getByText('评语已正式提交',{exact:true})).toBeVisible();checks.push('View starts at original completion section; review jumps to focused editor; canceled role switch keeps token and input; draft/formal save work');
  await drawer(teacher).getByRole('button',{name:'下一个',exact:true}).click();await expect(drawer(teacher).locator('.task-info-box')).toContainText('未获准浏览的合成三级任务');await drawer(teacher).getByRole('button',{name:'上一个',exact:true}).click();await expect(drawer(teacher).locator('.task-info-box')).toContainText('仅用户A可见的三级任务');
  await drawer(teacher).getByRole('button',{name:'专家评语 ↓',exact:true}).click();await expect(text).toHaveValue('原任务抽屉填写，完成情况与材料同时可查。');
  await drawer(teacher).getByRole('button',{name:'查看任务完成情况 ↑',exact:true}).click();
  const fileCount=network.filter(u=>u.includes('/files/940001')).length;
  const material=drawer(teacher).getByText('点击预览',{exact:true}).first();await material.scrollIntoViewIfNeeded();await material.click();
  await expect(teacher.getByRole('dialog',{name:'文件预览',exact:true})).toBeVisible();assert.equal(network.filter(u=>u.includes('/files/940001')).length,fileCount+1);
  await teacher.getByRole('dialog',{name:'文件预览',exact:true}).locator('.el-dialog__headerbtn').click();
  await teacher.screenshot({path:path.join(out,'original-drawer-1440.png'),fullPage:true});await drawer(teacher).locator('.el-drawer__close-btn').click();checks.push('Previous/next stays within scoped hits; original material preview uses one controlled round file request');
  await teacher.locator('.query-filter-bar').getByRole('textbox',{name:'任务编号／名称',exact:true}).fill('1.1.1');await teacher.locator('.query-filter-bar').getByRole('button',{name:'查询',exact:true}).click();
  await button(teacher,'总体评语').click();await drawer(teacher).getByText('本次评审范围（2 条三级任务）',{exact:true}).click();await expect(drawer(teacher).getByText('未获准浏览的合成三级任务',{exact:true}).last()).toBeVisible();await drawer(teacher).locator('.el-drawer__close-btn').click();
  await teacher.locator('.query-filter-bar').getByRole('button',{name:'重置全部',exact:true}).click();checks.push('Filtering one leaf does not shrink the formal overall-comment scope');
  await expand(teacher);await action(teacher,930002,'专家评语').click();await text.fill('版本冲突时保留这段未保存的文字');
  const version=await call('get',`/admin/review-tasks/${round.round_id}`,admin);
  await call('put',`/admin/review-tasks/${round.round_id}`,admin,{name:version.name,year:version.review_year,startsAt:version.startsAt,endsAt:version.endsAt,description:version.description,version:version.version,reason:'conflict fixture'});
  await drawer(teacher).getByRole('button',{name:'保存草稿',exact:true}).click();await expect(drawer(teacher).locator('.expert-review-section .el-alert')).toContainText('当前文字仍保留');await expect(text).toHaveValue('版本冲突时保留这段未保存的文字');
  await drawer(teacher).getByRole('button',{name:'重新加载',exact:true}).click();await teacher.getByRole('button',{name:'继续填写',exact:true}).click();await expect(text).toHaveValue('版本冲突时保留这段未保存的文字');
  await drawer(teacher).getByRole('button',{name:'重新加载',exact:true}).click();await teacher.getByRole('button',{name:'放弃修改',exact:true}).click();await expect(text).toHaveValue('原任务抽屉填写，完成情况与材料同时可查。');await drawer(teacher).locator('.el-drawer__close-btn').click();checks.push('Real round-version 409 keeps input; reload asks once and cancellation retains it');
  const external=await newPage(await login(910009),{width:1280,height:720});await external.goto(state.frontend+'/review');await expect(external.locator('.round-strip')).toContainText('原任务页面评审验证');await expand(external);
  await expect(external.getByText('未获准浏览的合成三级任务',{exact:true})).toHaveCount(0);await action(external,930002,'专家评语').click();await expect(drawer(external).getByRole('textbox',{name:'专家评语正文'})).toHaveValue('');
  await expect(drawer(external).getByText('原任务抽屉填写，完成情况与材料同时可查。',{exact:true})).toHaveCount(0);
  await external.screenshot({path:path.join(out,'partial-expert-1280.png'),fullPage:true});await drawer(external).locator('.el-drawer__close-btn').click();
  await button(external,'总体评语').click();await expect(drawer(external).getByText('本次总体评语仅针对下方获准任务，不代表整个一级任务。',{exact:true})).toBeVisible();
  await external.setViewportSize({width:960,height:540});await external.screenshot({path:path.join(out,'partial-overall-960.png'),fullPage:true});await drawer(external).getByRole('textbox',{name:'专家评语正文'}).scrollIntoViewIfNeeded();await expect(drawer(external).getByRole('textbox',{name:'专家评语正文'})).toBeInViewport();await drawer(external).getByRole('button',{name:'保存草稿',exact:true}).scrollIntoViewIfNeeded();await expect(drawer(external).getByRole('button',{name:'保存草稿',exact:true})).toBeInViewport();await drawer(external).locator('.el-drawer__close-btn').click();checks.push('External expert sees only its assignment, not another expert opinion; partial overall scope and narrow original drawer are usable');
  await external.setViewportSize({width:1280,height:720});let fail=true;const listRoute=/\/api\/review-work\/\d+\/tasks\?/;
  await external.route(listRoute,async route=>{if(fail&&new URL(route.request().url()).searchParams.get('f.keyword')==='1.1.1'){fail=false;await route.fulfill({status:500,contentType:'application/json',body:JSON.stringify({message:'隔离网络失败测试'})})}else await route.continue()});
  await external.locator('.query-filter-bar').getByRole('textbox',{name:'任务编号／名称',exact:true}).fill('1.1.1');await external.locator('.query-filter-bar').getByRole('button',{name:'查询',exact:true}).click();await expect(external.locator('.query-filter-bar .el-alert')).toContainText('隔离网络失败测试');assert.equal(await external.locator('.virtual-tree-table').getAttribute('data-model-row-count'),'3');
  await external.locator('.query-filter-bar').getByRole('button',{name:'重新查询',exact:true}).click();await expect(external.locator('.query-filter-bar .el-alert')).toHaveCount(0);await external.unroute(listRoute);checks.push('Failed list refresh retains the previous scoped tree and conditions, and real retry recovers');
  const allYearAdmin=await newPage(await switchMode(await login(110228),'EXPERT'));
  await allYearAdmin.goto(state.frontend+`/home/works?reviewRoundId=${future.round_id}`);await expect(allYearAdmin.locator('.round-strip')).toContainText('2025—2029全年度');await expand(allYearAdmin);await action(allYearAdmin,930002,'专家评语').click();await expect(drawer(allYearAdmin).getByRole('textbox',{name:'专家评语正文'})).toBeEditable();
  await expect(drawer(allYearAdmin).getByRole('button',{name:'修改管理属性',exact:true})).toHaveCount(0);await expect(allYearAdmin.getByRole('button',{name:'新增任务',exact:true})).toHaveCount(0);
  await drawer(allYearAdmin).getByRole('textbox',{name:'专家评语正文'}).fill('跨轮次未保存内容');await drawer(allYearAdmin).locator('.el-drawer__close-btn').click();await allYearAdmin.getByRole('button',{name:'继续填写',exact:true}).click();await expect(drawer(allYearAdmin).getByRole('textbox',{name:'专家评语正文'})).toHaveValue('跨轮次未保存内容');
  await drawer(allYearAdmin).locator('.el-drawer__close-btn').click();await allYearAdmin.getByRole('button',{name:'放弃修改',exact:true}).click();await allYearAdmin.locator('.round-strip .el-select').click();await allYearAdmin.locator('.el-select-dropdown__item:visible').filter({hasText:'原任务页面评审验证'}).click();await expect(allYearAdmin.locator('.round-strip')).toContainText('2026年度');checks.push('Admin can choose a future/all-year published round but has no business actions in expert mode; round change and close guards preserve drafts');
  let release,started;const seen=new Promise(resolve=>started=resolve),hold=new Promise(resolve=>release=resolve);
  await allYearAdmin.route(listRoute,async route=>{if(route.request().url().includes(`/review-work/${future.round_id}/`)){started();await hold;try{await route.continue()}catch{/* old request was canceled */}}else await route.continue()});
  await allYearAdmin.locator('.round-strip .el-select').click();await allYearAdmin.locator('.el-select-dropdown__item:visible').filter({hasText:'管理员跨时段评审验证'}).click();await seen;
  await allYearAdmin.locator('.round-strip .el-select').click();await allYearAdmin.locator('.el-select-dropdown__item:visible').filter({hasText:'原任务页面评审验证'}).click();release();await expect(allYearAdmin.locator('.round-strip')).toContainText('2026年度');await expect(allYearAdmin.locator('[data-stat="total"] .stat-value')).toHaveText('2');await allYearAdmin.unroute(listRoute);checks.push('Rapid round switching cancels the held old tree request and keeps the latest round result');
  const fresh=await call('get',`/admin/review-tasks/${round.round_id}`,admin);
  await call('put',`/admin/review-tasks/${round.round_id}`,admin,{name:fresh.name,year:fresh.review_year,startsAt:local(new Date(Date.now()-172800000)),endsAt:local(new Date(Date.now()-86400000)),description:fresh.description,version:fresh.version,reason:'test expiry'});
  await teacher.goto(state.frontend+'/review-history');await expect(teacher.locator('.round-strip')).toContainText('历史评审');await expand(teacher);await action(teacher,930002,'专家评语').click();await expect(drawer(teacher).getByRole('textbox',{name:'专家评语正文'})).toBeDisabled();await expect(drawer(teacher).getByRole('button',{name:'提交评语',exact:true})).toHaveCount(0);await teacher.screenshot({path:path.join(out,'history-original-drawer.png'),fullPage:true});checks.push('Expired rounds reopen original drawer in formal-history read-only mode');
  await external.goto(state.frontend+'/review');await expect(external.getByText('暂无可参加的有效评审任务',{exact:true})).toBeVisible();await expect(external.locator('.virtual-tree-table')).toHaveCount(0);checks.push('Expert without an active round gets an empty state and never loads ordinary business tasks');
  const businessStart=network.length;await teacher.goto(state.frontend+'/choose-role');await teacher.getByRole('button',{name:/^信息填报/}).click();await expect(teacher.getByRole('menuitem',{name:'工作中心 (我的任务)',exact:true})).toBeVisible();await expect(teacher.locator('.round-strip')).toHaveCount(0);await expect(teacher.getByRole('button',{name:'专家评语',exact:true})).toHaveCount(0);
  const expertRequests=network.slice(0,businessStart);assert.deepEqual(expertRequests.filter(u=>/\/api\/(biz\/|system\/allUsers|system\/users|list-filters\/tasks|work-records\/capabilities|admin\/)/.test(u)),[]);assert.deepEqual(errors,[]);
  checks.push('Expert navigation makes no ordinary task, whole-school account, business todo or administration requests; business switch restores original behavior');
  await fs.writeFile(path.join(out,'results.json'),JSON.stringify({passed:true,checks,errors,requestCount:expertRequests.length},null,2));console.log(JSON.stringify({passed:true,checks:checks.length}));
 }catch(e){if(currentPage&&!currentPage.isClosed()){await currentPage.screenshot({path:path.join(out,'failure.png'),fullPage:true});await fs.writeFile(path.join(out,'failure.txt'),await currentPage.locator('body').innerText())}console.error(JSON.stringify({checks,errors}));throw e}finally{await api.dispose();await browser.close()}
})().catch(e=>{console.error(e.stack||e);process.exitCode=1});
