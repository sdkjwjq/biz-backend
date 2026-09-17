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
assert isinstance(detail,dict) and 'overall' in detail, detail
assert sql('SELECT leader_id FROM sys_dept WHERE dept_id=920001') == '910002'
assert 'leaderId' not in detail and 'leaderName' not in detail, detail
results.append({'id':'23-1','configuredLeaderId':910002,'returnedLeaderId':detail.get('leaderId'),'returnedLeaderName':detail.get('leaderName')})

before = detail['overall']['totalTasks']
sql("UPDATE biz_task SET is_delete=1,status='3' WHERE task_id=930002")
deleted = call('/dashboard/dept/920001',admin)
assert deleted['overall']['totalTasks'] == before, deleted
assert deleted['overall']['completedTasks'] == 1, deleted
results.append({'id':'23-2','beforeTotal':before,'afterDeleteTotal':deleted['overall']['totalTasks'],
                'completedTasksFromDeletedRecord':deleted['overall']['completedTasks'],'activeTaskCount':int(sql('SELECT COUNT(*) FROM biz_task WHERE dept_id=920001 AND is_delete=0'))})
sql("UPDATE biz_task SET is_delete=0,status='1',phase=NULL WHERE task_id=930002")
missing_year = call('/dashboard/dept/920001',admin)
assert isinstance(missing_year,dict) and missing_year.get('code') == 500, missing_year
assert 'getPhase()' in missing_year.get('message',''), missing_year
results.append({'id':'23-3','response':missing_year,'taskWithNullYear':930002})
output = ROOT / 'target/ui-audit/evidence-batch-23'
output.mkdir(parents=True,exist_ok=True)
(output/'results.json').write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'reproduced':[result['id'] for result in results]}))
