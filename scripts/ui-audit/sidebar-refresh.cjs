const {chromium,request}=require('../../target/ui-audit-tools/node_modules/playwright');
const {expect}=require('../../target/ui-audit-tools/node_modules/playwright/test');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
(async()=>{
 const state=JSON.parse(await fs.readFile(process.env.UI_AUDIT_STATE_FILE||path.resolve(__dirname,'../../target/ui-audit/state.json')));
 assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/);assert.equal(new URL(state.frontend).hostname,'127.0.0.1');
 const api=await request.newContext({baseURL:state.frontend});
 const login=await api.post('/api/system/login',{data:{user_id:110228,password:'review-fixture-password'}}),token=(await login.json()).token;assert.ok(token);
 const auditPaths=['/api/biz/audit/todo','/api/performance/audit/todo','/api/achievement/audit/todo','/api/core-indicators/audit/todo'];
 const noticePath='/api/system/notice',counts=Object.fromEntries([...auditPaths,noticePath].map(p=>[p,0]));
 const checks=[],errors=[],countModes=[];let active=0,gate=null;
 const browser=await chromium.launch({channel:process.env.UI_AUDIT_BROWSER_CHANNEL||'chrome',headless:true});
 const context=await browser.newContext({viewport:{width:1536,height:864}});
 await context.addInitScript(t=>{
  localStorage.setItem('token',t);window.__testHidden=false;window.__intervalCallbacks=[];
  Object.defineProperty(document,'hidden',{configurable:true,get:()=>window.__testHidden});
  const original=window.setInterval.bind(window);
  window.setInterval=(callback,delay,...args)=>{
   if(delay===30000)window.__intervalCallbacks.push(()=>callback(...args));
   return original(callback,delay,...args);
  };
 },token);
 const page=await context.newPage();page.setDefaultTimeout(20000);page.on('pageerror',e=>errors.push(e.message));
 const sum=()=>auditPaths.reduce((n,p)=>n+counts[p],0);
 await page.route('**/api/**',async route=>{
  const pathname=new URL(route.request().url()).pathname;
  if(!Object.hasOwn(counts,pathname))return route.continue();
  counts[pathname]++;active++;
  try{if(gate&&auditPaths.includes(pathname))await gate;const response=await route.fetch();countModes.push(new URL(route.request().url()).searchParams.get('countOnly')==='true'&&typeof(await response.json())==='number');await route.fulfill({response});}
  finally{active--;}
 });
 try{
  await page.goto(state.frontend+'/home/admin/operations');
  await expect.poll(()=>Object.values(counts).every(n=>n>=1)&&active===0).toBeTruthy();
  let expectedCount=0;
  for(let i=0;i<auditPaths.length;i++){
   const response=await api.get(auditPaths[i],{headers:{Authorization:token}});assert.equal(response.status(),200);
   const body=await response.json(),rows=Array.isArray(body)?body:body.data;
   const statuses=i===0?[10,20,30]:i===2?[10]:[10,20];
   expectedCount+=rows.filter(r=>statuses.includes(Number(r.flowStatus??r.flow_status))).length;
  }
  const badge=page.locator('.el-menu-item').filter({hasText:'审核中心'}).locator('.el-badge__content');
  if(expectedCount)await expect(badge).toHaveText(String(expectedCount));else await expect(badge).toHaveCount(0);
  checks.push('badge-matches-current-business-todos');

  const burstStart=sum();
  await page.evaluate(()=>{for(let n=0;n<30;n++)window.dispatchEvent(new Event('audit-count-updated'));});
  await expect.poll(()=>sum()-burstStart===4&&active===0).toBeTruthy();
  checks.push('30-events-coalesce-to-one-four-module-read');

  let release;gate=new Promise(resolve=>{release=resolve});const raceStart=sum();
  await page.evaluate(()=>window.dispatchEvent(new Event('audit-count-updated')));
  await expect.poll(()=>sum()-raceStart).toBe(4);
  await page.evaluate(()=>{for(let n=0;n<30;n++)window.dispatchEvent(new Event('audit-count-updated'));});
  assert.equal(sum()-raceStart,4);gate=null;release();
  await expect.poll(()=>sum()-raceStart===8&&active===0).toBeTruthy();
  checks.push('changes-during-slow-read-produce-one-follow-up');

  const hiddenStart={...counts};
  await page.evaluate(()=>{
   window.__testHidden=true;document.dispatchEvent(new Event('visibilitychange'));
   for(let n=0;n<3;n++)for(const tick of window.__intervalCallbacks)tick();
   for(let n=0;n<30;n++)window.dispatchEvent(new Event('audit-count-updated'));
  });
  assert.deepEqual(counts,hiddenStart);checks.push('hidden-periodic-and-business-triggers-skip-network');
  assert.ok(await page.evaluate(()=>window.__intervalCallbacks.length)>=1);
  await page.evaluate(()=>{window.__testHidden=false;document.dispatchEvent(new Event('visibilitychange'));});
  await expect.poll(()=>auditPaths.every(p=>counts[p]===hiddenStart[p]+1)&&counts[noticePath]===hiddenStart[noticePath]+1&&active===0).toBeTruthy();
  checks.push('visible-page-immediately-refreshes-both-counts');
  if(expectedCount)await expect(badge).toHaveText(String(expectedCount));
  assert.deepEqual(errors,[]);assert.ok(countModes.length>0&&countModes.every(Boolean));checks.push('sidebar-uses-count-only-responses');
  const out=process.env.UI_AUDIT_STATE_FILE?path.join(path.dirname(process.env.UI_AUDIT_STATE_FILE),'sidebar-refresh'):path.resolve(__dirname,'../../target/ui-audit/sidebar-refresh');await fs.mkdir(out,{recursive:true});
  await page.screenshot({path:path.join(out,'counts.png'),fullPage:true});
  await fs.writeFile(path.join(out,'report.json'),JSON.stringify({checks,counts,expectedAuditCount:expectedCount,errors},null,2));
  console.log(checks.join('\n'));
 }finally{await browser.close();await api.dispose();}
})().catch(e=>{console.error(e);process.exitCode=1;});
