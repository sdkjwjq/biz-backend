const test=require('node:test'),assert=require('node:assert/strict');
const {pathToFileURL}=require('node:url'),path=require('node:path');
const factory=import(pathToFileURL(path.resolve(__dirname,'../../../biz/src/utils/requestPool.js'))).then(m=>m.createRequestPool);
const deferred=()=>{let resolve;const promise=new Promise(r=>{resolve=r});return {promise,resolve}};
const tick=()=>new Promise(resolve=>setImmediate(resolve));
test('runs at most two reads and starts the next as each finishes',async()=>{
 const create=await factory,pool=create(2),gates=Array.from({length:6},deferred);let active=0,peak=0,started=[];
 const all=gates.map((gate,id)=>pool.run(id,async()=>{started.push(id);active++;peak=Math.max(peak,active);await gate.promise;active--;return id}));
 await tick();assert.deepEqual(started,[0,1]);gates[0].resolve();await tick();assert.deepEqual(started,[0,1,2]);
 for(const gate of gates)gate.resolve();assert.deepEqual(await Promise.all(all),[0,1,2,3,4,5]);assert.equal(peak,2);pool.dispose();
});
test('duplicate key shares its in-flight read without caching the response',async()=>{
 const create=await factory,pool=create(),gate=deferred();let calls=0;
 const first=pool.run('a',async()=>{calls++;await gate.promise;return 1});assert.equal(pool.run('a',()=>{calls++}),first);
 gate.resolve();assert.equal(await first,1);await tick();await pool.run('a',()=>{calls++});assert.equal(calls,2);pool.dispose();
});
test('clearing queued work prevents it from starting and permits a new generation',async()=>{
 const create=await factory,pool=create(1),gate=deferred(),started=[];
 const first=pool.run('old:a',async()=>{started.push('old:a');await gate.promise});const old=pool.run('old:b',()=>started.push('old:b'));
 pool.clearQueued();assert.equal(await old,undefined);const next=pool.run('new:a',()=>started.push('new:a'));gate.resolve();await Promise.all([first,next]);assert.deepEqual(started,['old:a','new:a']);pool.dispose();
});
test('a rejected read frees its slot for retry and other reads',async()=>{
 const create=await factory,pool=create(1);const bad=pool.run('a',()=>{throw new Error('synthetic')});const other=pool.run('b',()=>2);
 await assert.rejects(bad,/synthetic/);assert.equal(await other,2);await tick();assert.equal(await pool.run('a',()=>3),3);pool.dispose();
});
test('dispose skips queued and future reads',async()=>{
 const create=await factory,pool=create(1),gate=deferred();let calls=0;
 const first=pool.run('a',async()=>{calls++;await gate.promise});const queued=pool.run('b',()=>calls++);pool.dispose();gate.resolve();await Promise.all([first,queued,pool.run('c',()=>calls++)]);assert.equal(calls,1);
});
