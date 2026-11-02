const {chromium,request}=require('playwright'),{expect}=require('playwright/test');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
(async()=>{
 const state=JSON.parse(await fs.readFile(process.env.SHUANGGAO_UI_STATE_FILE,'utf8')),out=path.join(path.dirname(process.env.SHUANGGAO_UI_STATE_FILE),'dashboard-evidence');await fs.mkdir(out,{recursive:true});
 const browser=await chromium.launch({channel:'msedge',headless:true}),api=await request.newContext({baseURL:state.frontend}),checks=[],errors=[];let p;
 const call=async(method,url,t,data)=>{const r=await api[method]('/api'+url,{headers:t?{Authorization:t}:{},...(data===undefined?{}:{data})});assert.equal(r.status(),200,await r.text());const value=await r.json();assert.ok(!value.code||value.code<400,JSON.stringify(value));return value};
 const login=async(id,password='review-fixture-password')=>call('post','/system/login',null,{user_id:id,password});
 const open=async token=>{const c=await browser.newContext({viewport:{width:1440,height:900}});await c.addInitScript(t=>{if(!sessionStorage.getItem('dashboard-seed')){localStorage.setItem('token',t);sessionStorage.setItem('dashboard-seed','1')}},token);p=await c.newPage();p.on('pageerror',e=>errors.push(e.message));await p.goto(state.frontend+'/home/works');return c};
 try{
  const initial=await login(121362);assert.equal(initial.requiresPasswordChange,true);let context=await open(initial.token);
  const dialog=p.getByRole('dialog',{name:'请先修改密码',exact:true});await expect(dialog).toBeVisible();await expect(dialog).not.toContainText('系统需升级');
  await dialog.getByPlaceholder('请输入新密码',{exact:true}).fill('ChangedUI123');await dialog.getByPlaceholder('请再次输入新密码',{exact:true}).fill('ChangedUI123');await dialog.getByRole('button',{name:'保存',exact:true}).click();await expect(dialog).toBeHidden();await context.close();
  const changed=await login(121362,'ChangedUI123');assert.equal(changed.requiresPasswordChange,false);assert.equal((await call('get','/system/password/status',changed.token)).requiresPasswordChange,false);
  context=await open(changed.token);await expect(p.getByRole('dialog',{name:'请先修改密码',exact:true})).toHaveCount(0);await expect(p.getByRole('menuitem',{name:'数据大屏',exact:true})).toHaveCount(0);await p.screenshot({path:path.join(out,'ordinary-menu.png'),fullPage:true});
  await p.goto(state.frontend+'/dashboard');await expect(p).toHaveURL(/\/home\/works/);assert.equal((await api.get('/api/dashboard/summary',{headers:{Authorization:changed.token}})).status(),403);await context.close();checks.push('Flagged synthetic account changes password once; new login does not prompt again and dashboard route/API are denied');
  for(const [id,allowed,label] of [[910001,false,'ordinary'],[980102,false,'department-head'],[980104,false,'finance'],[980100,true,'office'],[980200,true,'school-leader'],[110228,true,'administrator']]){
   let token=(await login(id)).token;if(id===110228)token=(await call('post','/system/review-context',token,{experience:'BUSINESS'})).token;
   const access=await call('get','/system/access',token);assert.equal(access.canViewDashboard,allowed,label);context=await open(token);
   if(allowed){await expect(p.getByRole('menuitem',{name:'数据大屏',exact:true})).toBeVisible();await call('get','/dashboard/summary',token);}else{await expect(p.getByRole('menuitem',{name:'数据大屏',exact:true})).toHaveCount(0);assert.equal((await api.get('/api/dashboard/summary',{headers:{Authorization:token}})).status(),403);}
   if(id===980100)await p.screenshot({path:path.join(out,'office-menu.png'),fullPage:true});await context.close();checks.push(label+': menu and current backend access agree');
  }
  assert.deepEqual(errors,[]);await fs.writeFile(path.join(out,'results.json'),JSON.stringify({passed:true,checks,errors},null,2));console.log(JSON.stringify({passed:true,checks:checks.length}));
 }catch(e){if(p&&!p.isClosed()){await p.screenshot({path:path.join(out,'failure.png'),fullPage:true});await fs.writeFile(path.join(out,'failure.txt'),await p.locator('body').innerText())}console.error(JSON.stringify({checks,errors}));throw e}finally{await api.dispose();await browser.close()}
})().catch(e=>{console.error(e.stack||e);process.exitCode=1});
