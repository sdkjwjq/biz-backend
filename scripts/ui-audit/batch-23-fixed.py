"""Department statistics audit in disposable schema."""
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
detail = call('/dashboard/dept/920001',admin)
assert detail['leaderId'] == 910002 and detail['leaderName'] == '审计部门负责人', detail
sql("INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,password,role,status,is_delete) VALUES(920001,920002,'collision','合成同号用户','review-fixture-password','1','0',0)")
sql('UPDATE sys_dept SET leader_id=910004 WHERE dept_id=920002')
assert call('/dashboard/dept/920001',admin)['leaderId'] == 910002
sql('UPDATE sys_user SET is_delete=1 WHERE user_id=910002')
assert 'leaderId' not in call('/dashboard/dept/920001',admin)
sql('UPDATE sys_user SET is_delete=0 WHERE user_id=910002')
results.append({'id':'23-1','leaderId':910002,'sameNumberUserDoesNotInterfere':True,'deletedLeaderOmitted':True})

sql("UPDATE biz_task SET is_delete=1,status='3' WHERE task_id=930002")
deleted = call('/dashboard/dept/920001',admin)
for kind in ('overall','year','midterm'):
    assert deleted[kind]['totalTasks'] == 2 and deleted[kind]['completedTasks'] == 0, deleted
results.append({'id':'23-2','allThreeStatsExcludeDeleted':True,'activeTotal':2,'completedTotal':0})
sql("UPDATE biz_task SET is_delete=0,status='1',phase=NULL WHERE task_id=930002")
missing = call('/dashboard/dept/920001',admin)
assert missing['overall']['totalTasks'] == 3 and missing['year']['totalTasks'] == missing['midterm']['totalTasks'] == 2, missing
sql('UPDATE biz_task SET is_delete=1 WHERE dept_id=920001')
empty = call('/dashboard/dept/920001',admin)
for kind in ('overall','year','midterm'):
    assert empty[kind]['totalTasks'] == 0 and empty[kind]['completionRate'] == 0, empty
results.append({'id':'23-3','nullYearOverallTotal':3,'nullYearMidtermTotal':2,'emptyDeptZeroRates':True})
output = ROOT / 'target/ui-audit/evidence-batch-23-fixed'
output.mkdir(parents=True,exist_ok=True)
(output/'results.json').write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'passed':[result['id'] for result in results]}))
