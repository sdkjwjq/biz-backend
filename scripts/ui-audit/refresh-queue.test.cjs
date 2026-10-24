const test=require('node:test');
const assert=require('node:assert/strict');
const {pathToFileURL}=require('node:url');
const path=require('node:path');
const factory=import(pathToFileURL(path.resolve(__dirname,'../../../biz/src/utils/refreshQueue.js'))).then(m=>m.createRefreshQueue);
const deferred=()=>{let resolve;const promise=new Promise(r=>{resolve=r});return {promise,resolve}};

test('a burst before the request starts performs one refresh',async()=>{
 const create=await factory;let calls=0;const queue=create(async()=>{calls++},{delay:0});
 const first=queue.refresh({force:true});
 for(let n=0;n<30;n++)assert.equal(queue.refresh({force:true}),first);
 await first;assert.equal(calls,1);queue.dispose();
});

test('changes during a slow read trigger one follow-up without concurrent reads',async()=>{
 const create=await factory;const gate=deferred();let calls=0,active=0,maxActive=0;
 const queue=create(async()=>{calls++;active++;maxActive=Math.max(maxActive,active);if(calls===1)await gate.promise;active--});
 const first=queue.refresh();
 for(let n=0;n<30;n++)assert.equal(queue.refresh({force:true}),first);
 for(let n=0;n<30;n++)assert.equal(queue.refresh(),first);
 assert.equal(calls,1);gate.resolve();await first;
 assert.equal(calls,2);assert.equal(maxActive,1);queue.dispose();
});

test('periodic refreshes join the active read without scheduling another',async()=>{
 const create=await factory;const gate=deferred();let calls=0;
 const queue=create(async()=>{calls++;await gate.promise});const first=queue.refresh();
 for(let n=0;n<30;n++)assert.equal(queue.refresh(),first);
 gate.resolve();await first;assert.equal(calls,1);queue.dispose();
});

test('disposing cancels delayed and follow-up refreshes',async()=>{
 const create=await factory;let calls=0;
 const delayed=create(async()=>{calls++},{delay:0});const scheduled=delayed.refresh({force:true});delayed.dispose();await scheduled;
 const gate=deferred();const running=create(async()=>{calls++;await gate.promise});const first=running.refresh();
 running.refresh({force:true});running.dispose();gate.resolve();await first;
 await running.refresh();assert.equal(calls,1);
});

test('failed refresh can be retried',async()=>{
 const create=await factory;let calls=0;const queue=create(async()=>{if(++calls===1)throw new Error('synthetic failure')});
 await assert.rejects(queue.refresh(),/synthetic failure/);await queue.refresh();assert.equal(calls,2);queue.dispose();
});
