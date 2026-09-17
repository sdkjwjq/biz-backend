"""Task completion and validation regression in disposable schema."""
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
    try:
        response = urllib.request.urlopen(request, timeout=20)
    except urllib.error.HTTPError as error:
        if error.code != 400:
            raise
        response = error
    with response:
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


def rejected(response):
    assert isinstance(response,dict) and response.get('code') in (400,500), response
    assert '###' not in response.get('message',''), response


tokens = {user: call('/system/login',body={'user_id':user,'password':'review-fixture-password'})['token']
          for user in (110228,910001,1910001)}
results = []
sql("INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by) VALUES(920101,'batch20.pdf','/synthetic/batch20.pdf','/synthetic/batch20.pdf','pdf',910001)")
submitted = call('/biz/sub',tokens[910001],{'task_id':930002,'file_id':920101,'reported_value':3,'data_type':'1'})
assert isinstance(submitted,str) and '提交成功' in submitted, submitted
sub_id = int(sql('SELECT MAX(sub_id) FROM biz_material_submission'))
responses = race('biz_material_submission',sub_id,[('/biz/tasks/manage/finish/930002',tokens[110228],{}) for _ in range(2)])
assert sum(isinstance(response,str) and '已完成' in response for response in responses) == 1, responses
for response in responses:
    if isinstance(response,dict):
        rejected(response)
history = logs('biz_audit_log',sub_id)
assert len(history) == 4 and [row['after'] for row in history] == [10,20,30,40], history
notices = int(sql("SELECT COUNT(*) FROM sys_notice WHERE source_type=0 AND source_id=930002 AND title='任务已完成'"))
assert notices == 1, notices
assert sql(f'SELECT flow_status,is_delete FROM biz_material_submission WHERE sub_id={sub_id}') == '40\t0'
assert float(sql('SELECT current_value FROM biz_task WHERE task_id=930002')) == 10
rejected(call('/biz/drawback/930002',tokens[910001],{}))
assert logs('biz_audit_log',sub_id) == history
results.append({'id':'20-1','responses':responses,'logs':history,'completionNotices':notices,'withdrawRejected':True})

payload = {'category':1,'level':'省级','achName':'合成必填校验成果','department':'合成颁发单位','comment':'合成校验','gotTime':'2026-09-17 10:00:00','isCompetition':1,'yiDengJiang':1}
assert '添加成功' in call('/achievement/add',tokens[1910001],payload)
ach_id = int(sql('SELECT MAX(ach_id) FROM biz_achievement'))
tables = ('biz_achievement','biz_achievement_submission','biz_achievement_audit_log','sys_notice')
before = {table:sql(f'SELECT * FROM {table} ORDER BY 1') for table in tables}
cases = [('category',None),('level',''),('level','   '),('achName',''),('achName','   '),('gotTime',None)]
for field, value in cases:
    for endpoint in ('/achievement/add',f'/achievement/update/{ach_id}'):
        rejected(call(endpoint,tokens[1910001],dict(payload,**{field:value})))
        assert {table:sql(f'SELECT * FROM {table} ORDER BY 1') for table in tables} == before
updated = call(f'/achievement/update/{ach_id}',tokens[1910001],dict(payload,achName='合成修改后成果'))
assert isinstance(updated,str) and '修改成功' in updated, updated
assert sql(f'SELECT ach_name FROM biz_achievement WHERE ach_id={ach_id}') == '合成修改后成果'
results.append({'id':'20-2','invalidCases':len(cases)*2,'failedWritesUnchanged':True,'validUpdate':updated})

def user(user_id,password):
    return {'userId':user_id,'deptId':920001,'userName':f'batch20_{user_id}','nickName':'合成密码校验账号','password':password,'role':'1','status':'0'}

assert '添加成功' in call('/system/users/add',tokens[110228],user(920202,'review-fixture-password'))
before = sql('SELECT * FROM sys_user WHERE user_id=920202')
invalid_passwords = [None,'','      ','\t\n','12345']
for password in invalid_passwords:
    rejected(call('/system/users/add',tokens[110228],user(920201,password)))
    assert sql('SELECT COUNT(*) FROM sys_user WHERE user_id=920201') == '0'
    rejected(call('/system/users/update',tokens[110228],user(920202,password)))
    assert sql('SELECT * FROM sys_user WHERE user_id=920202') == before
    assert 'token' in call('/system/login',body={'user_id':920202,'password':'review-fixture-password'})
valid = ' abcde '
assert '添加成功' in call('/system/users/add',tokens[110228],user(920201,valid))
assert '更新成功' in call('/system/users/update',tokens[110228],user(920202,valid))
for user_id in (920201,920202):
    assert 'token' in call('/system/login',body={'user_id':user_id,'password':valid})
    assert 'token' not in call('/system/login',body={'user_id':user_id,'password':valid.strip()})
results.append({'id':'20-3','invalidCases':len(invalid_passwords)*2,'failedWritesUnchanged':True,'validPasswordsPreserveSpaces':True})
output = ROOT / 'target/ui-audit/evidence-batch-20-fixed'
output.mkdir(parents=True,exist_ok=True)
(output/'results.json').write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'passed':[result['id'] for result in results],'invalidInputCases':22}))
