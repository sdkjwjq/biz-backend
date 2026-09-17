"""Task/performance concurrent workflow audit in disposable schema."""
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


def race(table, sub_id, requests, finish_second=False):
    assert table in ('biz_material_submission', 'biz_performance_submission', 'biz_performance_year')
    key = 'year_id' if table == 'biz_performance_year' else 'sub_id'
    lock = subprocess.Popen(mysql + ['--unbuffered', schema], stdin=subprocess.PIPE,
        stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, env=env)
    ready = queue.Queue()
    threading.Thread(target=lambda: [ready.put(line.strip()) for line in lock.stdout], daemon=True).start()
    pool = ThreadPoolExecutor(max_workers=2)
    try:
        lock.stdin.write(f"START TRANSACTION; SELECT {key} FROM {table} WHERE {key}={sub_id} FOR UPDATE; SELECT 'READY';\n")
        lock.stdin.flush()
        while ready.get(timeout=10) != 'READY':
            pass
        futures = [pool.submit(call, *request) for request in (requests[:1] if finish_second else requests)]
        deadline = time.monotonic() + 10
        waiting = 0
        while time.monotonic() < deadline:
            waiting = int(sql("SELECT COUNT(*) FROM information_schema.innodb_trx t JOIN information_schema.processlist p ON t.trx_mysql_thread_id=p.ID WHERE p.DB='" + schema + "' AND t.trx_state='LOCK WAIT'"))
            if waiting == (1 if finish_second else 2):
                break
            time.sleep(0.1)
        assert waiting == (1 if finish_second else 2), 'Real HTTP requests must reach row lock wait'
        second_response = call(*requests[1]) if finish_second else None
        lock.stdin.write('COMMIT;\nquit\n')
        lock.stdin.flush()
        responses = [future.result(timeout=20) for future in futures]
        return responses + [second_response] if finish_second else responses
    finally:
        if lock.poll() is None:
            lock.stdin.close()
            lock.wait(timeout=15)
        pool.shutdown(wait=True)


def logs(table, sub_id):
    return [json.loads(row) for row in sql(f"SELECT JSON_OBJECT('action',action_type,'before',pre_status,'after',post_status) FROM {table} WHERE sub_id={sub_id} ORDER BY log_id").splitlines()]


tokens = {user: call('/system/login', body={'user_id':user,'password':'review-fixture-password'})['token']
          for user in (110228, 910001, 910003)}
results = []

# Task approval versus rejection: create the task snapshot through the real submit endpoint.
sql("INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by) VALUES(919001,'batch19.pdf','/synthetic/batch19.pdf','/synthetic/batch19.pdf','pdf',910001)")
submitted = call('/biz/sub', tokens[910001], {'task_id':930002,'file_id':919001,'reported_value':3,'data_type':'1'})
assert isinstance(submitted, str) and '提交成功' in submitted, submitted
sub_id = int(sql('SELECT MAX(sub_id) FROM biz_material_submission'))
responses = race('biz_material_submission', sub_id, [('/biz/audit',tokens[910003],
    {'sub_id':sub_id,'is_pass':decision,'title':'合成并发审核'}) for decision in (True, False)])
assert all(isinstance(response,str) for response in responses), responses
history = logs('biz_audit_log', sub_id)
assert len([row for row in history if row['before'] == 10]) == 2, history
results.append({'id':'19-1','responses':responses,'logs':history,
    'submission':sql(f'SELECT flow_status,is_delete,current_handler_id FROM biz_material_submission WHERE sub_id={sub_id}'),
    'task':sql('SELECT current_value,status FROM biz_task WHERE task_id=930002')})

# Duplicate performance approval, and final archive versus submitter withdrawal.
for case, perf_id in [('19-2',959002), ('19-3',959003)]:
    sql(f"INSERT INTO biz_performance(perf_id,project_id,perf_code,perf_name,target_value,current_value,data_type,dept_id,principal_id,auditor_id,leader_id) VALUES({perf_id},1,'2.19.{perf_id}','合成绩效并发',10,0,'1',920001,910002,910003,910001); INSERT INTO biz_performance_year(year_id,perf_id,year,target_value,actual_value) VALUES({perf_id},{perf_id},2026,10,0)")
    submitted = call(f'/performance/submit?pref_id={perf_id}&actual_value=3&year=2026',tokens[910001],{})
    assert submitted == '绩效已提交', submitted
    sub_id = int(sql(f'SELECT MAX(sub_id) FROM biz_performance_submission WHERE perf_id={perf_id}'))
    if case == '19-2':
        requests = [('/performance/audit',tokens[910003],{'sub_id':sub_id,'is_pass':True,'title':'合成绩效审核'}) for _ in range(2)]
    else:
        assert call('/performance/audit',tokens[910003],{'sub_id':sub_id,'is_pass':True}) == '绩效已通过专业群审核，待完结归档'
        requests = [(f'/performance/audit/withdraw/{sub_id}',tokens[910001],{}),
                    ('/performance/audit',tokens[110228],{'sub_id':sub_id,'is_pass':True,'title':'合成归档'})]
    responses = race('biz_performance_submission',sub_id,requests) if case == '19-2' else race('biz_performance_year',perf_id,requests,finish_second=True)
    assert all(isinstance(response,str) for response in responses), responses
    history = logs('biz_performance_audit_log',sub_id)
    expected_before = 10 if case == '19-2' else 20
    assert len([row for row in history if row['before'] == expected_before]) == 2, history
    actual = sql(f'SELECT actual_value FROM biz_performance_year WHERE year_id={perf_id}')
    assert float(actual) == (3 if case == '19-2' else 0), actual
    results.append({'id':case,'responses':responses,'logs':history,'yearActualValue':actual,
        'submission':sql(f'SELECT flow_status,is_delete,current_handler_id FROM biz_performance_submission WHERE sub_id={sub_id}'),
        'notices':sql(f'SELECT title FROM sys_notice WHERE source_type=2 AND source_id={sub_id} ORDER BY notice_id').splitlines()})

output = ROOT / 'target/ui-audit/evidence-batch-19'
output.mkdir(parents=True, exist_ok=True)
(output/'results.json').write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'reproduced':[result['id'] for result in results]}))
