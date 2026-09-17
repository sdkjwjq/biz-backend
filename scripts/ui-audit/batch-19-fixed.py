"""Task/performance workflow concurrency regression in disposable schema."""
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
        if finish_second:
            futures.append(pool.submit(call, *requests[1]))
            deadline = time.monotonic() + 10
            while time.monotonic() < deadline:
                waiting = int(sql("SELECT COUNT(*) FROM information_schema.innodb_trx t JOIN information_schema.processlist p ON t.trx_mysql_thread_id=p.ID WHERE p.DB='" + schema + "' AND t.trx_state='LOCK WAIT'"))
                if waiting == 2:
                    break
                time.sleep(0.1)
            assert waiting == 2, 'Archive must wait for the in-flight withdrawal'
        lock.stdin.write('COMMIT;\nquit\n')
        lock.stdin.flush()
        responses = [future.result(timeout=20) for future in futures]
        return responses
    finally:
        if lock.poll() is None:
            lock.stdin.close()
            lock.wait(timeout=15)
        pool.shutdown(wait=True)


def logs(table, sub_id):
    return [json.loads(row) for row in sql(f"SELECT JSON_OBJECT('action',action_type,'before',pre_status,'after',post_status) FROM {table} WHERE sub_id={sub_id} ORDER BY log_id").splitlines()]


def one_winner(responses):
    winners = [index for index, response in enumerate(responses) if isinstance(response, str)]
    assert len(winners) == 1, responses
    error = responses[1 - winners[0]]
    assert isinstance(error, dict) and error.get('code') == 500, responses
    assert '###' not in error.get('message', ''), responses
    return winners[0]


tokens = {user: call('/system/login', body={'user_id':user,'password':'review-fixture-password'})['token']
          for user in (110228, 910001, 910002, 910003)}
results = []
sql("INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by) VALUES(919101,'batch19-fixed.pdf','/synthetic/batch19-fixed.pdf','/synthetic/batch19-fixed.pdf','pdf',910001)")

for index, decisions in enumerate(([True,False], [True,True], [False,False])):
    task_id = 939100 + index
    sql(f"INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete) VALUES({task_id},1,930001,2026,'1.1.{task_id}','合成并发任务',3,910001,910003,910002,920001,'1',10,0,0,'1',0)")
    submitted = call('/biz/sub',tokens[910001],{'task_id':task_id,'file_id':919101,'reported_value':3,'data_type':'1'})
    assert isinstance(submitted,str) and '提交成功' in submitted, submitted
    sub_id = int(sql(f'SELECT MAX(sub_id) FROM biz_material_submission WHERE task_id={task_id}'))
    responses = race('biz_material_submission',sub_id,[('/biz/audit',tokens[910003],{'sub_id':sub_id,'is_pass':decision,'title':'并发验证'}) for decision in decisions])
    winner = one_winner(responses)
    passed = decisions[winner]
    history = logs('biz_audit_log',sub_id)
    assert len(history) == 2 and history[-1]['after'] == (20 if passed else -10), history
    row = sql(f'SELECT flow_status,is_delete,current_handler_id FROM biz_material_submission WHERE sub_id={sub_id}')
    assert row == ('20\t0\t910002' if passed else '-10\t1\t910001'), row
    value = float(sql(f'SELECT current_value FROM biz_task WHERE task_id={task_id}'))
    assert value == (3 if passed else 0), value
    notices = int(sql(f'SELECT COUNT(*) FROM sys_notice WHERE source_type=0 AND source_id={task_id}'))
    assert notices == 2, notices  # One submission notice and one decision notice.
    retry = call('/biz/audit',tokens[910003],{'sub_id':sub_id,'is_pass':True})
    assert isinstance(retry,dict) and logs('biz_audit_log',sub_id) == history, retry
    results.append({'id':'19-1','decisions':decisions,'responses':responses,'logs':history,'submission':row,'taskValue':value,'noticeCount':notices,'retryRejected':True})
    if index == 1:
        withdrawn = race('biz_material_submission',sub_id,[(f'/biz/drawback/{task_id}',tokens[910001],{}) for _ in range(2)])
        one_winner(withdrawn)
        assert len(logs('biz_audit_log',sub_id)) == 3
        assert float(sql(f'SELECT current_value FROM biz_task WHERE task_id={task_id}')) == 0
        assert sql(f'SELECT is_delete FROM biz_material_submission WHERE sub_id={sub_id}') == '1'
        results.append({'id':'19-1','mode':'duplicate-withdraw','responses':withdrawn,'restoredValue':0,'singleWithdrawalLog':True})

for index, mode in enumerate(('duplicate-pass','conflicting-decisions','duplicate-reject','archive-withdraw','withdraw-first','archive-first')):
    perf_id = 959100 + index
    sql(f"INSERT INTO biz_performance(perf_id,project_id,perf_code,perf_name,target_value,current_value,data_type,dept_id,principal_id,auditor_id,leader_id) VALUES({perf_id},1,'2.19.{perf_id}','合成并发绩效',10,0,'1',920001,910002,910003,910001); INSERT INTO biz_performance_year(year_id,perf_id,year,target_value,actual_value) VALUES({perf_id},{perf_id},2026,10,0)")
    assert call(f'/performance/submit?pref_id={perf_id}&actual_value=3&year=2026',tokens[910001],{}) == '绩效已提交'
    sub_id = int(sql(f'SELECT MAX(sub_id) FROM biz_performance_submission WHERE perf_id={perf_id}'))
    if index < 3:
        decisions = {'duplicate-pass':[True,True],'conflicting-decisions':[True,False],'duplicate-reject':[False,False]}[mode]
        responses = race('biz_performance_submission',sub_id,[('/performance/audit',tokens[910003],{'sub_id':sub_id,'is_pass':decision}) for decision in decisions])
        passed = decisions[one_winner(responses)]
        expected_status, expected_value, expected_logs = (20,3,2) if passed else (-10,0,2)
    else:
        assert call('/performance/audit',tokens[910003],{'sub_id':sub_id,'is_pass':True}) == '绩效已通过专业群审核，待完结归档'
        withdraw = (f'/performance/audit/withdraw/{sub_id}',tokens[910001],{})
        archive = ('/performance/audit',tokens[110228],{'sub_id':sub_id,'is_pass':True})
        if mode == 'archive-first':
            responses = [call(*archive),call(*withdraw)]
            assert one_winner(responses) == 0
            archived = True
        else:
            responses = race('biz_performance_year',perf_id,[withdraw,archive],finish_second=True) if mode == 'withdraw-first' else race('biz_performance_submission',sub_id,[withdraw,archive])
            archived = one_winner(responses) == 1
            if mode == 'withdraw-first':
                assert not archived, responses
        expected_status, expected_value, expected_logs = (30,3,3) if archived else (0,0,3)
    history = logs('biz_performance_audit_log',sub_id)
    assert len(history) == expected_logs and history[-1]['after'] == expected_status, history
    row = sql(f'SELECT flow_status,is_delete FROM biz_performance_submission WHERE sub_id={sub_id}')
    assert row == f'{expected_status}\t{1 if expected_status == 0 else 0}', row
    actual = float(sql(f'SELECT actual_value FROM biz_performance_year WHERE year_id={perf_id}'))
    total = float(sql(f'SELECT current_value FROM biz_performance WHERE perf_id={perf_id}'))
    assert actual == total == expected_value, (actual,total)
    notices = sql(f'SELECT title FROM sys_notice WHERE source_type=2 AND source_id={sub_id} ORDER BY notice_id').splitlines()
    assert len(notices) == (3 if expected_status == 30 else 2), notices
    assert notices.count('绩效已完结归档') == (1 if expected_status == 30 else 0), notices
    retry = call('/performance/audit',tokens[910003],{'sub_id':sub_id,'is_pass':True})
    assert isinstance(retry,dict) and logs('biz_performance_audit_log',sub_id) == history, retry
    results.append({'id':'19-2' if index < 3 else '19-3','mode':mode,'responses':responses,'logs':history,'submission':row,'actualValue':actual,'totalValue':total,'notices':notices,'retryRejected':True})

output = ROOT / 'target/ui-audit/evidence-batch-19-fixed'
output.mkdir(parents=True,exist_ok=True)
(output/'results.json').write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'passed':len(results)}))
