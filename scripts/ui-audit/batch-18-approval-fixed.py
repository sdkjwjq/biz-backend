"""Verify achievement approval serialization using real HTTP and MySQL row locks.

Run after serve.py --fixture review; only the disposable schema is writable.
"""
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


def snapshot(sub_id):
    return {
        'submission': sql(f'SELECT flow_status,current_handler_id,comment FROM biz_achievement_submission WHERE sub_id={sub_id}'),
        'achievement': sql(f'SELECT audit_status,current_handler_id FROM biz_achievement WHERE ach_id=(SELECT ach_id FROM biz_achievement_submission WHERE sub_id={sub_id})'),
        'logs': [json.loads(row) for row in sql(f"SELECT JSON_OBJECT('action',action_type,'before',pre_status,'after',post_status) FROM biz_achievement_audit_log WHERE sub_id={sub_id} AND action_type<>'submit' ORDER BY log_id").splitlines()],
        'notices': sql(f'SELECT title FROM sys_notice WHERE source_type=3 AND source_id={sub_id} AND to_user_id=1910001 ORDER BY notice_id').splitlines(),
    }


def rejected(response):
    return isinstance(response, dict) and '当前状态不可审核' in json.dumps(response, ensure_ascii=False)


admin = call('/system/login', body={'user_id':110228,'password':'review-fixture-password'})['token']
uploader = call('/system/login', body={'user_id':1910001,'password':'review-fixture-password'})['token']
results = []
for decisions in ([True, False], [True, True], [False, False]):
    added = call('/achievement/add', uploader, {
        'category':1,'level':'省级','achName':'合成并发成果','department':'合成颁发单位',
        'comment':'合成并发测试','gotTime':'2026-09-17 10:00:00','isCompetition':1,'yiDengJiang':1})
    assert isinstance(added, str) and '添加成功' in added, added
    sub_id = int(sql('SELECT MAX(sub_id) FROM biz_achievement_submission'))
    lock = subprocess.Popen(mysql + ['--unbuffered', schema], stdin=subprocess.PIPE,
        stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, env=env)
    ready = queue.Queue()
    threading.Thread(target=lambda: [ready.put(line.strip()) for line in lock.stdout], daemon=True).start()
    pool = ThreadPoolExecutor(max_workers=2)
    try:
        lock.stdin.write(f"START TRANSACTION; SELECT sub_id FROM biz_achievement_submission WHERE sub_id={sub_id} FOR UPDATE; SELECT 'READY';\n")
        lock.stdin.flush()
        while ready.get(timeout=10) != 'READY':
            pass
        futures = [pool.submit(call, '/achievement/audit', admin, {
            'sub_id':sub_id,'is_pass':decision,'title':'合成并发审核','content':'合成并发审核'}) for decision in decisions]
        deadline = time.monotonic() + 10
        waiting = 0
        while time.monotonic() < deadline:
            waiting = int(sql("SELECT COUNT(*) FROM information_schema.innodb_trx t JOIN information_schema.processlist p ON t.trx_mysql_thread_id=p.ID WHERE p.DB='" + schema + "' AND t.trx_state='LOCK WAIT'"))
            if waiting == 2:
                break
            time.sleep(0.1)
        assert waiting == 2, 'Both HTTP requests must overlap at the locked submission'
        lock.stdin.write('COMMIT;\nquit\n')
        lock.stdin.flush()
        responses = [future.result(timeout=20) for future in futures]
    finally:
        if lock.poll() is None:
            lock.stdin.close()
            lock.wait(timeout=15)
        pool.shutdown(wait=True)
    winners = [index for index, response in enumerate(responses) if isinstance(response, str) and response in ('成果已归档', '成果已退回')]
    assert len(winners) == 1 and sum(rejected(response) for response in responses) == 1, responses
    passed = decisions[winners[0]]
    status = 30 if passed else -10
    expected_title = '成果已归档' if passed else '成果已退回'
    assert responses[winners[0]] == expected_title
    state = snapshot(sub_id)
    assert state['submission'] == f"{status}\t{110228 if passed else 1910001}\t合成并发审核", state
    assert state['achievement'] == f"{status}\t{'NULL' if passed else 1910001}", state
    assert state['logs'] == [{'action':'pass' if passed else 'reject','before':10,'after':status}], state
    assert state['notices'] == [expected_title], state
    retried = call('/achievement/audit', admin, {'sub_id':sub_id,'is_pass':not passed,'title':'重试不应写入'})
    assert rejected(retried), retried
    assert snapshot(sub_id) == state, 'Sequential retry must not alter state, logs or notifications'
    results.append({'decisions':decisions,'overlappingRequests':waiting,'responses':responses,
                    'state':state,'retryResponse':retried,'retryUnchanged':True})
output = ROOT / 'target/ui-audit/evidence-batch-18-fixed'
output.mkdir(parents=True, exist_ok=True)
(output / 'results.json').write_text(json.dumps(results, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'passed':len(results),'sequentialRetries':len(results)}))
