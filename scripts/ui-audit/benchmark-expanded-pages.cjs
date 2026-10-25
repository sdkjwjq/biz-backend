const {chromium,request}=require('../../target/ui-audit-tools/node_modules/playwright');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
(async()=>{
 const file=process.env.UI_AUDIT_STATE_FILE,state=JSON.parse(await fs.readFile(file));assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/);
 if(process.env.PERF_BUILD==='optimized'&&state.comparison)state.frontend=state.comparison;
 const api=await request.newContext({baseURL:state.frontend});const token=(await(await api.post('/api/system/login',{data:{user_id:110228,password:'review-fixture-password'}})).json()).token;
 const browser=await chromium.launch({channel:'chrome',headless:true}),context=await browser.newContext({viewport:{width:1536,height:864}});await context.addInitScript(t=>localStorage.setItem('token',t),token);
 const results=[];
 const routes=['admin/accounts','audit','works','works/performance','works/achievement','work-records','notice','expert-reviews?view=tasks','report','admin/overview'];
 try{for(const route of process.env.PERF_ROUTES?routes.filter(r=>process.env.PERF_ROUTES.split(',').includes(r)):routes){
  const page=await context.newPage();const errors=[],responses=[];page.on('pageerror',e=>errors.push(e.message));page.on('response',r=>{if(new URL(r.url()).pathname.startsWith('/api/'))responses.push(r)});
  for(const phase of ['cold','warm']){
   responses.length=0;const start=Date.now();await page.goto(state.frontend+'/home/'+route);
   if(route==='admin/overview')await page.locator('[data-metric="tasks.total"]').waitFor();
   else{await page.locator('.query-filter-bar').first().waitFor();await page.waitForFunction(()=>{const bar=document.querySelector('.query-filter-bar');return bar&&bar.querySelectorAll('.basic-fields .query-field').length>=3&&!bar.querySelector('[role=status]')&&!bar.querySelector('.el-alert--error')});}
   const firstSectionMs=Date.now()-start;
   await page.waitForFunction(()=>!Array.from(document.querySelectorAll('.el-loading-mask')).some(e=>e.getBoundingClientRect().height>0&&getComputedStyle(e).display!=='none'));
   const firstContentMs=Date.now()-start;await page.waitForTimeout(150);let bytes=0;for(const r of responses){try{bytes+=(await r.body()).length}catch{}}
   const slowestApi=await page.evaluate(()=>performance.getEntriesByType('resource').filter(r=>new URL(r.name).pathname.startsWith('/api/')).sort((a,b)=>b.duration-a.duration).slice(0,6).map(r=>({path:new URL(r.name).pathname,durationMs:Math.round(r.duration),transferSize:r.transferSize})));
   if(route==='admin/overview')assert.equal(await page.locator('.module-error').count(),0);
   results.push({route,phase,firstContentMs,firstSectionMs,requests:responses.length,responseBytes:bytes,slowestApi,errors:[...errors]});
   if(phase==='warm')await page.screenshot({path:path.join(path.dirname(file),'perf-'+(process.env.PERF_BUILD||'optimized')+'-'+route.replace(/[^a-z]/g,'-')+'.png'),fullPage:true});
  }await page.close();
 }}finally{await context.close();await browser.close();await api.dispose();await fs.writeFile(path.join(path.dirname(file),'page-performance-'+(process.env.PERF_BUILD||'optimized')+(process.env.PERF_REPORT_SUFFIX||'')+'.json'),JSON.stringify(results,null,2))}
 console.log(JSON.stringify(results));
})().catch(e=>{console.error(e);process.exitCode=1});
