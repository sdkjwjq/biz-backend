"""Capacity of the new current-page management candidate endpoints."""
import concurrent.futures,json,os,math,time,urllib.request
from pathlib import Path
state_file=Path(os.environ['UI_AUDIT_STATE_FILE']);state=json.loads(state_file.read_text());assert state['schema'].startswith('biz_review_test_');base=state.get('comparison') or state['frontend']
def call(path,token=None,data=None):
 headers={'Content-Type':'application/json'}
 if token:headers['Authorization']=token
 start=time.perf_counter()
 with urllib.request.urlopen(urllib.request.Request(base+'/api'+path,headers=headers,data=json.dumps(data).encode() if data else None),timeout=120) as response:body=json.load(response)
 assert int(body.get('code',200))<400
 return (time.perf_counter()-start)*1000,body
tokens=[call('/system/login',data={'user_id':5100000+i,'password':'review-fixture-password'})[1]['token'] for i in range(30)]
rows=[]
for name,path in {'permissionUsers':'/admin/permissions/candidates?domain=users','taskUsers':'/biz/tasks/manage/candidates?domain=users','departmentLeaders':'/admin/departments/920001/leader-candidates'}.items():
 _,body=call(path,tokens[0]);assert len(body['items'])==50 and body['total']>100
 for concurrency in [1,10,30]:
  with concurrent.futures.ThreadPoolExecutor(max_workers=concurrency) as pool:values=list(pool.map(lambda i:call(path,tokens[i%concurrency])[0],range(60)))
  values.sort();row={'workload':name,'concurrency':concurrency,'samples':60,'p50Ms':round(values[29],2),'p95Ms':round(values[56],2)};rows.append(row);print(json.dumps(row),flush=True)
state_file.with_name('close-candidate-capacity.json').write_text(json.dumps(rows,indent=2),encoding='utf8')
assert all(row['p95Ms']<=1000 for row in rows)
