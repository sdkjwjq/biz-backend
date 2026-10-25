// Synthetic serve.py fixture only; compares current combined API with bounded module API loading.
const {request}=require('../../target/ui-audit-tools/node_modules/playwright');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
const {performance}=require('node:perf_hooks'),{pathToFileURL}=require('node:url');
(async()=>{
 const state=JSON.parse(await fs.readFile(process.env.UI_AUDIT_STATE_FILE||path.resolve(__dirname,'../../target/ui-audit/state.json')));assert.match(state.schema,/^biz_review_test_[0-9a-f]{32}$/);assert.equal(new URL(state.frontend).hostname,'127.0.0.1');
 const {createRequestPool}=await import(pathToFileURL(path.resolve(__dirname,'../../../biz/src/utils/requestPool.js')));
 const api=await request.newContext({baseURL:state.frontend});
 try{
  const login=await api.post('/api/system/login',{data:{user_id:110228,password:'review-fixture-password'}});const auth=(await login.json()).token;assert.ok(auth);
  const modules=['tasks','performance','core','achievements','budget','records'];
  const get=async module=>{const response=await api.get('/api/admin/overview/summary?year=2026&month=4'+(module?'&module='+module:''),{headers:{Authorization:auth}});assert.equal(response.status(),200);const body=await response.text();return {data:JSON.parse(body),bytes:Buffer.byteLength(body)}};
  const combined=[],split=[],first=[],combinedBytes=[],splitBytes=[];
  for(let run=0;run<6;run++){
   const start=performance.now(),old=await get(),middle=performance.now();const pool=createRequestPool(2);let firstAt=null,bytes=0;
   const results=await Promise.all(modules.map(module=>pool.run(module,async()=>{const result=await get(module);if(firstAt==null)firstAt=performance.now();bytes+=result.bytes;return result.data.modules})));const end=performance.now();pool.dispose();
   const next=Object.assign({},...results);assert.deepEqual(old.data.modules,next);
   if(run>0){combined.push(middle-start);split.push(end-middle);first.push(firstAt-middle);combinedBytes.push(old.bytes);splitBytes.push(bytes);}
  }
  const median=values=>Number([...values].sort((a,b)=>a-b)[2].toFixed(2));
  const result={method:'One synthetic administrator, current optimized backend, sequential paired runs, one warm-up discarded and median of five samples. API response timings only, not browser rendering, cold start, multi-account concurrency or full-system P95.',combined:{requests:1,complete_median_ms:median(combined),bytes:median(combinedBytes)},modules:{requests:6,max_parallel:2,first_response_median_ms:median(first),complete_median_ms:median(split),bytes:median(splitBytes)},equalModules:true};
  const output=process.env.UI_AUDIT_STATE_FILE?path.join(path.dirname(process.env.UI_AUDIT_STATE_FILE),'overview-load-benchmark.json'):path.resolve(__dirname,'../../target/ui-audit/overview-load-benchmark.json');
  await fs.writeFile(output,JSON.stringify(result,null,2));console.log(JSON.stringify(result));
 }finally{await api.dispose()}
})().catch(error=>{console.error(error);process.exitCode=1});
