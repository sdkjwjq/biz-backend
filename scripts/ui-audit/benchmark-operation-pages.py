"""Compare compatible and SQL-paged operation APIs on a guarded disposable UI schema."""
import concurrent.futures
import json
import math
import os
from pathlib import Path
import re
import shutil
import subprocess
import time
import urllib.request

root=Path(__file__).resolve().parents[2]
state=json.loads((root/'target/ui-audit/state.json').read_text(encoding='utf-8'))
if not re.fullmatch(r'biz_review_test_[0-9a-f]{32}',state['schema']) or state['frontend']!='http://127.0.0.1:15173':
    raise RuntimeError('Disposable UI schema and port required')
env=dict(os.environ,MYSQL_PWD=os.environ['SHUANGGAO_TEST_DB_PASSWORD'])
mysql=shutil.which('mysql') or 'C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe'
command=[mysql,'--host=127.0.0.1','--port=3306','--user=root','--default-character-set=utf8mb4',state['schema']]
module='SYNTHETIC_PAGE_BENCHMARK'

def sql(statement):
    subprocess.run(command,input=statement.encode('utf-8'),env=env,check=True,capture_output=True)

def request(path,token=None,data=None):
    headers={'Content-Type':'application/json'}
    if token:headers['Authorization']=token
    req=urllib.request.Request(state['frontend']+'/api'+path,headers=headers,
                               data=None if data is None else json.dumps(data).encode())
    start=time.perf_counter()
    with urllib.request.urlopen(req,timeout=60) as response:payload=response.read()
    return json.loads(payload),round((time.perf_counter()-start)*1000,2),len(payload)

def result_view(body):
    return {k:body[k] for k in ('items','total','page','pageSize')}

def cleanup():
    sql("DELETE FROM sys_admin_operation_log WHERE log_id BETWEEN 880000000 AND 880000599 AND module='"+module+"'")

token=request('/system/login',data={'user_id':110228,'password':'review-fixture-password'})[0]['token']
path='/list-filters/admin-operations?f.module='+module+'&page=2&pageSize=10'
reports=[]
try:
    for count in (120,600):
        cleanup()
        rows=','.join("(%d,'%s','UPDATE','USER','%d','Synthetic benchmark',110228,'Synthetic','{\"value\":1}',DATE_ADD('2026-01-01',INTERVAL %d SECOND))"%(880000000+i,module,i,i) for i in range(count))
        sql('INSERT INTO sys_admin_operation_log(log_id,module,action,target_type,target_id,target_name,operator_id,reason,before_json,create_time) VALUES '+rows)
        baseline=request(path,token)[0]
        optimized=request(path+'&responseMode=page&includeFields=false',token)[0]
        assert result_view(baseline)==result_view(optimized) and baseline['total']==count
        assert 'fields' not in optimized
        expected=result_view(baseline)
        for clients in (1,10,30):
            sample_count=20 if clients<30 else 30
            row={'synthetic_rows':count,'concurrency':clients,'samples_per_mode':sample_count}
            for label,suffix in [('legacy',''),('sql_page','&responseMode=page&includeFields=false')]:
                for _ in range(2):request(path+suffix,token)
                def run(_):
                    body,elapsed,size=request(path+suffix,token)
                    assert result_view(body)==expected
                    return elapsed,size
                with concurrent.futures.ThreadPoolExecutor(max_workers=clients) as pool:results=list(pool.map(run,range(sample_count)))
                timings=sorted(r[0] for r in results)
                row[label]={'p50_ms':timings[len(timings)//2],'p95_ms':timings[math.ceil(len(timings)*.95)-1],
                            'response_bytes':results[0][1],'all_results_equal':True}
            reports.append(row)
    report={'scope':'Disposable schema; synthetic operation logs; same administrator session; warmed short batches. Not a multi-account, cold-start, long-duration or full-system acceptance benchmark. No SQL-count instrumentation.',
            'production_changes':False,'results':reports}
    (root/'target/performance-operation-sql-pages.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(report,indent=2))
finally:cleanup()
