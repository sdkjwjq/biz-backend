// Read-only browser regression: first-list delay + saved virtual scroll/expansion.
const {chromium,request}=require('playwright');
const {expect}=require('playwright/test');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
(async()=>{
 const state=JSON.parse(await fs.readFile(process.env.SHUANGGAO_UI_STATE_FILE,'utf8'));
 const out=path.join(path.dirname(process.env.SHUANGGAO_UI_STATE_FILE),'virtual-restore-evidence');await fs.mkdir(out,{recursive:true});
 const browser=await chromium.launch({channel:'msedge',headless:true}),api=await request.newContext({baseURL:state.frontend});
 const checks=[],errors=[];let token;
 const call=async(url,data,t)=>{const r=await api.post('/api'+url,{headers:t?{Authorization:t}:{},data});assert.equal(r.status(),200,await r.text());return r.json()};
 const rows=p=>p.locator('.virtual-tree-table .el-table-v2__main .el-table-v2__row');
 const visibleCount=p=>p.locator('.virtual-tree-table').evaluate(el=>{const b=el.getBoundingClientRect();return [...el.querySelectorAll('.el-table-v2__main .el-table-v2__row')].filter(r=>{const a=r.getBoundingClientRect();return a.bottom>b.top+40&&a.top<b.bottom}).length});
 // Public table API, discovered via Vue's mounted vnode; no app test hooks.
 const table=p=>p.evaluateHandle(()=>{const seen=new Set();const find=n=>{if(!n||typeof n!=='object'||seen.has(n))return;seen.add(n);if(n.component?.type.name==='VirtualTreeTable')return n.component.exposed;return find(n.component?.subTree)||find(n.suspense?.activeBranch)||(Array.isArray(n.children)?n.children.map(find).find(Boolean):null)};return find(document.querySelector('#app')._vnode)});
 try{
  token=(await call('/system/login',{user_id:110228,password:process.env.SHUANGGAO_UI_PASSWORD||'review-fixture-password'})).token;
  token=(await call('/system/review-context',{experience:'BUSINESS'},token)).token;
  for(const [domain,route,key] of [['tasks','/home/works','tasks'],['performance','/home/works/performance','performance']]){
   for(let n=0;n<3;n++){
    const context=await browser.newContext({viewport:{width:1440,height:900}});
    await context.addInitScript(({t,key})=>{localStorage.setItem('token',t);sessionStorage.setItem('works_browse_v1_'+key+':'+t.split('.')[2],JSON.stringify({fields:{year:'2026',quick:'all'},expanded:['930000','930001','1','10','40'],rowKey:'obsolete-row',anchor:'detail-row-obsolete-row',offset:40,scrollTop:100000,scrollLeft:0}))},{t:token,key});
    const p=await context.newPage();p.on('pageerror',e=>errors.push(e.message));
    await p.route(new RegExp('/api/list-filters/'+domain+'\\?'),async r=>{await new Promise(resolve=>setTimeout(resolve,1800));try{await r.continue()}catch{/* navigation cancellation */}});
    await p.goto(state.frontend+route);await expect.poll(()=>rows(p).count(),{timeout:30000}).toBeGreaterThan(0);
    await expect.poll(()=>visibleCount(p)).toBeGreaterThan(0);await p.waitForTimeout(250);
    const apiHandle=await table(p);assert.ok(apiHandle);const original=await apiHandle.evaluate(t=>t.getScrollState());
    assert.ok(original.rowKey,'a visible anchor is recorded');
    await apiHandle.evaluate(async(t,s)=>{await t.restoreScrollState({...s,scrollTop:1e6})},original);
    await expect.poll(()=>visibleCount(p)).toBeGreaterThan(0);
    const restored=await apiHandle.evaluate(t=>t.getScrollState());assert.equal(restored.rowKey,original.rowKey);
    await p.reload();await expect.poll(()=>visibleCount(p),{timeout:30000}).toBeGreaterThan(0);
    await p.setViewportSize({width:960,height:540});await expect.poll(()=>visibleCount(p)).toBeGreaterThan(0);
    await p.setViewportSize({width:1440,height:900});await expect.poll(()=>visibleCount(p)).toBeGreaterThan(0);
    if(n===0)await p.screenshot({path:path.join(out,domain+'-restored.png'),fullPage:true});
    checks.push(domain+' delayed first query '+(n+1)+': invalid saved anchor recovers, valid anchor and reload/resize remain visible');
    await context.close();
   }
  }
  assert.deepEqual(errors,[]);await fs.writeFile(path.join(out,'results.json'),JSON.stringify({passed:true,checks,errors},null,2));console.log(JSON.stringify({passed:true,checks:checks.length}));
 }finally{if(token)await call('/system/logout',{},token);await api.dispose();await browser.close()}
})().catch(e=>{console.error(e.stack||e);process.exitCode=1});

