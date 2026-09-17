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
task.update(currentValue=7,progress=70)
assert isinstance(call('/biz/tasks/manage/update',admin,task),str)
assert float(sql('SELECT current_value FROM biz_performance WHERE perf_id=950001')) == 7
assert float(sql('SELECT actual_value FROM biz_performance_year WHERE year_id=950002')) == 7
before = {table:sql(f'SELECT * FROM {table} ORDER BY 1') for table in ('biz_task','biz_performance','biz_performance_year')}
sql("CREATE TRIGGER batch21_fail BEFORE UPDATE ON biz_performance FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='synthetic failure'")
try:
    failed = call('/biz/tasks/manage/update',admin,dict(task,currentValue=9,progress=90))
    assert isinstance(failed,dict) and failed.get('code') == 500, failed
    assert {table:sql(f'SELECT * FROM {table} ORDER BY 1') for table in before} == before
finally:
    sql('DROP TRIGGER batch21_fail')
results.append({'id':'21-1','taskValue':7,'performanceValue':7,'yearValue':7,'failedRefreshRolledBackAllTables':True})

count = sql('SELECT COUNT(*) FROM biz_task')
for level in (None,0,1,2,4,5):
    response = call('/biz/tasks/manage/add',admin,dict(task,taskId=921101,taskCode='1.1.21',taskName='合成层级测试',level=level))
    assert isinstance(response,dict) and '只能新增三级任务' in response.get('message',''), response
    assert sql('SELECT COUNT(*) FROM biz_task') == count
assert isinstance(call('/biz/tasks/manage/add',admin,dict(task,taskId=921101,taskCode='1.1.21',taskName='合成层级测试',level=3)),str)
assert sql("SELECT level,parent_id FROM biz_task WHERE task_name='合成层级测试'") == '3\t930001'
results.append({'id':'21-2','invalidLevelsRejected':6,'validLevelSaved':3})

for user_id in (921201,921202):
    response = call('/system/users/add',admin,{'userId':user_id,'deptId':920001,'userName':f'batch21_{user_id}',
        'nickName':'合成同名老师','password':'review-fixture-password','role':'1','status':'0'})
    assert isinstance(response,str), response
payload = {'to_user_nick_name':'合成同名老师','title':'合成预警','content':'合成正文','source_id':930002}
before = sql('SELECT COUNT(*) FROM sys_notice')
duplicate = call('/system/alert',admin,payload)
assert isinstance(duplicate,dict) and '存在同名人员' in duplicate.get('message',''), duplicate
assert sql('SELECT COUNT(*) FROM sys_notice') == before
assert call('/system/alert',admin,dict(payload,to_user_id=921202)) == '发送成功'
assert sql("SELECT to_user_id FROM sys_notice WHERE title='合成预警'") == '921202'
for invalid in ({'to_user_id':9999999},{'to_user_nick_name':'不存在的合成人员'},{'to_user_nick_name':''}):
    response = call('/system/alert',admin,dict(payload,**invalid))
    assert isinstance(response,dict) and response.get('code') == 500, response
assert sql("SELECT COUNT(*) FROM sys_notice WHERE title='合成预警'") == '1'
assert isinstance(call('/system/users/delete/921202',admin,{}),str)
rejected = call('/system/alert',admin,dict(payload,to_user_id=921202))
assert isinstance(rejected,dict) and '已删除' in rejected.get('message',''), rejected
assert call('/system/alert',admin,payload) == '发送成功'
assert sql("SELECT to_user_id FROM sys_notice WHERE title='合成预警' ORDER BY notice_id").splitlines() == ['921202','921201']
results.append({'id':'21-3','duplicateNameResponse':duplicate,'explicitIdDeliveredTo':921202,'uniqueActiveNameDeliveredTo':921201,'invalidRecipientsRejected':4})
output = ROOT / 'target/ui-audit/evidence-batch-21-fixed'
output.mkdir(parents=True,exist_ok=True)
(output/'results.json').write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'passed':[result['id'] for result in results]}))
