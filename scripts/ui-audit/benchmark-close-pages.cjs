const {chromium,request}=require('../../target/ui-audit-tools/node_modules/playwright');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
const p95=values=>[...values].sort((a,b)=>a-b)[Math.ceil(values.length*.95)-1];
(async()=>{
 const file=process.env.UI_AUDIT_STATE_FILE,state=JSON.parse(await fs.readFile(file));assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/);const label=process.env.PERF_BUILD||'optimized',base=label==='optimized'?state.comparison||state.frontend:state.frontend;
 const api=await request.newContext({baseURL:base});const token=(await(await api.post('/api/system/login',{data:{user_id:110228,password:'review-fixture-password'}})).json()).token;
 const browser=await chromium.launch({channel:'chrome',headless:true});const results=[],errors=[];
 const ready=async page=>{await page.waitForFunction(()=>{const root=document.querySelector('.virtual-tree-table,.table-wrapper .el-table'),bar=document.querySelector('.query-filter-bar');return root&&bar&&!bar.querySelector('[role=status]')&&root.querySelector('.el-table__row')&&!Array.from(document.querySelectorAll('.el-loading-mask')).some(e=>e.getBoundingClientRect().height&&getComputedStyle(e).display!=='none')});};
 try{for(const [route,menu] of [['works','任务'],['works/performance','绩效']]){
   const cold=[],warm=[],counts=[],pageRequests=[];let page,context,worstCold={},worstWarm={};
   const trace=async mark=>page.evaluate(since=>{const all=performance.getEntriesByType('resource').filter(e=>e.startTime>=since);return {requests:all.length,bytes:all.reduce((sum,e)=>sum+e.encodedBodySize,0),slowest:all.sort((a,b)=>b.duration-a.duration).slice(0,6).map(e=>({path:new URL(e.name).pathname,duration:Math.round(e.duration)}))}},mark);
   for(let i=0;i<20;i++){
     context=await browser.newContext({viewport:{width:1536,height:864}});await context.addInitScript(t=>localStorage.setItem('token',t),token);page=await context.newPage();page.on('pageerror',e=>errors.push(e.message));
     const start=Date.now();await page.goto(base+'/home/'+route);await ready(page);const elapsed=Date.now()-start;cold.push(elapsed);const evidence=await trace(0);pageRequests.push({phase:'cold',...evidence});if(elapsed>=Math.max(...cold))worstCold={elapsed,...evidence};
     if(i<19)await context.close();
   }
   for(let i=0;i<20;i++){
     await page.getByRole('menuitem',{name:/消息中心/}).click();await page.waitForURL('**/home/notice');await page.locator('.query-filter-bar').first().waitFor();
     const mark=await page.evaluate(()=>performance.now());const start=Date.now();await page.getByRole('menuitem',{name:menu,exact:true}).click();await page.waitForURL('**/home/'+route);await ready(page);const elapsed=Date.now()-start;warm.push(elapsed);const evidence=await trace(mark);pageRequests.push({phase:'warm',...evidence});if(elapsed>=Math.max(...warm))worstWarm={elapsed,...evidence};
     counts.push(await page.locator((await page.locator('.el-table-v2__main').count())?'.el-table-v2__main .el-table-v2__row':'.table-wrapper .el-table__row').count());
   }
   await page.screenshot({path:path.join(path.dirname(file),'close-'+label+'-'+menu+'.png'),fullPage:true});
   const result={route,coldSamples:cold.length,coldP50:cold.sort((a,b)=>a-b)[10],coldP95:p95(cold),warmSamples:warm.length,warmP50:warm.sort((a,b)=>a-b)[10],warmP95:p95(warm),maxMountedMainRows:Math.max(...counts),worstCold,worstWarm,pageRequests,errors:[...errors]};results.push(result);console.log(JSON.stringify(result));await context.close();
 }}finally{await browser.close();await api.dispose();await fs.writeFile(path.join(path.dirname(file),'close-page-performance-'+label+'.json'),JSON.stringify(results,null,2));}assert.deepEqual(errors,[]);
})().catch(e=>{console.error(e);process.exitCode=1});
