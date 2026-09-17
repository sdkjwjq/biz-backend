"""Task management consistency and nickname alert audit in disposable schema."""
import importlib.util
import json
import os
from pathlib import Path
import queue
import re
import subprocess
import sys
import threading
import time
import urllib.request
from concurrent.futures import ThreadPoolExecutor

ROOT = Path(__file__).resolve().parents[2]
sys.dont_write_bytecode = True
spec = importlib.util.spec_from_file_location('runner', ROOT / 'scripts/run-review-regression.py')
runner = importlib.util.module_from_spec(spec)
spec.loader.exec_module(runner)
schema = json.loads((ROOT / 'target/ui-audit/state.json').read_text())['schema']
assert re.fullmatch(r'biz_review_test_[0-9a-f]{32}', schema)
env = os.environ.copy()
env['MYSQL_PWD'] = env['SHUANGGAO_TEST_DB_PASSWORD']
mysql = [runner.executable('mysql'), '--host=127.0.0.1', '--port=3306',
         '--user=' + env.get('SHUANGGAO_TEST_DB_USER', 'root'), '--default-character-set=utf8mb4',
         '--batch', '--skip-column-names']


def sql(statement):
    result = subprocess.run(mysql + [schema], input=statement.encode('utf-8'),
                            env=env, capture_output=True, check=True)
    return result.stdout.decode('utf-8').strip()


def call(path, token=None, body=None):
    headers = {'Content-Type': 'application/json'}
    if token:
        headers['Authorization'] = token
    request = urllib.request.Request('http://127.0.0.1:18080/api' + path,
        data=None if body is None else json.dumps(body).encode('utf-8'), headers=headers)
    with urllib.request.urlopen(request, timeout=20) as response:
        raw = response.read().decode('utf-8')
        try:
            return json.loads(raw)
        except json.JSONDecodeError:
            return raw


admin = call('/system/login',body={'user_id':110228,'password':'review-fixture-password'})['token']
results = []
task = call('/biz/tasks/930002',admin)
for field in ('isDelete','createTime','updateTime'):
    task.pop(field,None)
call('/performance/refresh',admin,{})
assert float(sql('SELECT current_value FROM biz_performance WHERE perf_id=950001')) == 0
task['currentValue'] = 7
task['progress'] = 70
updated = call('/biz/tasks/manage/update',admin,task)
assert isinstance(updated,str), updated
assert float(sql('SELECT current_value FROM biz_task WHERE task_id=930002')) == 7
stale = float(sql('SELECT current_value FROM biz_performance WHERE perf_id=950001'))
assert stale == 0, stale
call('/performance/refresh',admin,{})
refreshed = float(sql('SELECT current_value FROM biz_performance WHERE perf_id=950001'))
assert refreshed == 7, refreshed
results.append({'id':'21-1','updateResponse':updated,'taskActualValue':7,'performanceAfterUpdate':stale,'performanceAfterRefresh':refreshed})

added_task = dict(task,taskId=921101,taskCode='1.1.21',taskName='合成错误层级新增',level=1,currentValue=0,progress=0)
added = call('/biz/tasks/manage/add',admin,added_task)
assert isinstance(added,str), added
row = sql("SELECT level,parent_id FROM biz_task WHERE task_name='合成错误层级新增'")
assert row == '1\t930001', row
assert sql('SELECT level FROM biz_task WHERE task_id=930001') == '2'
results.append({'id':'21-2','addResponse':added,'savedLevel':1,'parentLevel':2,'parentId':930001})

def add_user(user_id):
    response = call('/system/users/add',admin,{'userId':user_id,'deptId':920001,'userName':f'batch21_{user_id}',
        'nickName':'合成同名老师','password':'review-fixture-password','role':'1','status':'0'})
    assert isinstance(response,str) and '添加成功' in response, response
    return response

add_user(921201)
payload = {'to_user_nick_name':'合成同名老师','title':'合成预警','content':'合成正文','source_id':930002}
first = call('/system/alert',admin,payload)
assert first == '发送成功', first
second_user = add_user(921202)
before = sql('SELECT COUNT(*) FROM sys_notice')
second = call('/system/alert',admin,payload)
assert isinstance(second,dict) and second.get('code') == 500 and 'MyBatisSystemException' in second.get('message',''), second
assert sql('SELECT COUNT(*) FROM sys_notice') == before
results.append({'id':'21-3','uniqueNameAlert':first,'sameNameUserCreated':second_user,'duplicateNameAlert':second,'noNoticeWritten':True})
output = ROOT / 'target/ui-audit/evidence-batch-21'
output.mkdir(parents=True,exist_ok=True)
(output/'results.json').write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'reproduced':[result['id'] for result in results]}))
