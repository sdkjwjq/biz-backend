"""Compare the original unread-list calculation and SQL count in disposable UI data."""
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

def sql(statement):
    subprocess.run(command,input=statement.encode('utf-8'),env=env,check=True,capture_output=True)

def request(path,token=None,data=None):
    headers={'Content-Type':'application/json'}
    if token:headers['Authorization']=token
    req=urllib.request.Request(state['frontend']+'/api'+path,headers=headers,data=None if data is None else json.dumps(data).encode())
    start=time.perf_counter()
    with urllib.request.urlopen(req,timeout=60) as response:payload=response.read()
    return json.loads(payload),round((time.perf_counter()-start)*1000,2),len(payload)

def cleanup():
    sql("DELETE FROM sys_notice WHERE notice_id BETWEEN 880000000 AND 880000599 AND title='SyntheticCountBenchmark'")

token=request('/system/login',data={'user_id':110228,'password':'review-fixture-password'})[0]['token']
reports=[]
try:
    for count in (120,600):
        cleanup()
        rows=','.join("(%d,110228,110228,'1','SyntheticCountBenchmark','%s','9',0,'0',0,NOW())"%(880000000+i,'synthetic body '*10) for i in range(count))
        sql('INSERT INTO sys_notice(notice_id,from_user_id,to_user_id,type,title,content,source_type,source_id,is_read,is_delete,create_time) VALUES '+rows)
        listed=request('/system/notice',token)[0]
        expected=sum(str(r.get('isRead',r.get('is_read')))=='0' for r in listed)
        assert expected>=count and request('/system/notice?countOnly=true',token)[0]==expected
        for clients in (1,10,30):
            samples=20 if clients<30 else 30
            row={'synthetic_rows':count,'concurrency':clients,'samples_per_mode':samples}
            for label,url in [('legacy_list','/system/notice'),('sql_count','/system/notice?countOnly=true')]:
                for _ in range(2):request(url,token)
                def run(_):
                    body,elapsed,size=request(url,token)
                    actual=sum(str(r.get('isRead',r.get('is_read')))=='0' for r in body) if label=='legacy_list' else body
                    assert actual==expected
                    return elapsed,size
                with concurrent.futures.ThreadPoolExecutor(max_workers=clients) as pool:results=list(pool.map(run,range(samples)))
                timings=sorted(r[0] for r in results)
                row[label]={'p50_ms':timings[len(timings)//2],'p95_ms':timings[math.ceil(len(timings)*.95)-1],
                            'response_bytes':results[0][1],'counts_equal':True}
            reports.append(row)
    report={'scope':'Unread notification count only; disposable synthetic data, one administrator session and short warmed batches. Not full-system, multi-account, cold-start or sustained-load acceptance; no SQL-count instrumentation.',
            'production_changes':False,'results':reports}
    (root/'target/performance-notice-sql-counts.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(report,indent=2))
finally:cleanup()
