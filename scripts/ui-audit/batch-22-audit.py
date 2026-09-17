"""Invalid references audit in disposable schema."""
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
owner = call('/system/login',body={'user_id':910001,'password':'review-fixture-password'})['token']
results = []
task = call('/biz/tasks/930002',admin)
for field in ('isDelete','createTime','updateTime'):
    task.pop(field,None)
sql('UPDATE biz_task SET is_delete=1 WHERE task_id=930001')
added = call('/biz/tasks/manage/add',admin,dict(task,taskId=922101,taskCode='1.1.22',taskName='合成已删除父节点下新增'))
assert isinstance(added,str), added
row = sql("SELECT t.level,t.is_delete,p.is_delete FROM biz_task t JOIN biz_task p ON t.parent_id=p.task_id WHERE t.task_name='合成已删除父节点下新增'")
assert row == '3\t0\t1', row
results.append({'id':'22-1','response':added,'childLevel':3,'childDeleted':False,'parentDeleted':True})
sql('UPDATE biz_task SET is_delete=0 WHERE task_id=930001')

before = int(sql('SELECT COUNT(*) FROM sys_notice'))
assert isinstance(call('/system/users/add',admin,{'userId':922201,'deptId':920001,'userName':'batch22_deleted','nickName':'合成已删除接收人','password':'review-fixture-password','role':'1','status':'0'}),str)
assert isinstance(call('/system/users/delete/922201',admin,{}),str)
assert sql('SELECT is_delete FROM sys_user WHERE user_id=922201') == '1'
sent = call('/system/notice',admin,{'to_user_id':922201,'title':'合成已删除接收人','content':'合成正文','type':'3','trigger_event':'TASK','source_id':930002})
assert sent == '发送成功', sent
assert int(sql('SELECT COUNT(*) FROM sys_notice')) == before+1
assert sql("SELECT to_user_id FROM sys_notice WHERE title='合成已删除接收人'") == '922201'
assert 'token' not in call('/system/login',body={'user_id':922201,'password':'review-fixture-password'})
results.append({'id':'22-2','response':sent,'recipientDeleted':True,'savedRecipientId':922201,'recipientLoginRejected':True})

sql("INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by) VALUES(922001,'batch22.pdf','/synthetic/batch22.pdf','/synthetic/batch22.pdf','pdf',910001)")
assert isinstance(call('/biz/sub',owner,{'task_id':930002,'file_id':922001,'reported_value':3,'data_type':'1'}),str)
sub_id = int(sql('SELECT MAX(sub_id) FROM biz_material_submission'))
normal = call('/scheduled/month_auditor_trigger',admin,{})
assert isinstance(normal,str) and '成功' in normal, normal
sql(f"INSERT INTO biz_material_submission(task_id,file_id,reported_value,data_type,submit_by,submit_dept_id,manage_dept_id,submit_time,file_suffix,flow_status,current_handler_id,is_delete) SELECT task_id,file_id,reported_value,data_type,submit_by,submit_dept_id,manage_dept_id,submit_time,file_suffix,flow_status,NULL,is_delete FROM biz_material_submission WHERE sub_id={sub_id}")
before = sql('SELECT COUNT(*) FROM sys_notice')
broken = call('/scheduled/month_auditor_trigger',admin,{})
assert isinstance(broken,dict) and broken.get('code') == 500, broken
assert sql('SELECT COUNT(*) FROM sys_notice') == before
results.append({'id':'22-3','normalResponse':normal,'missingHandlerResponse':broken,'validPendingSubmissionRetained':sub_id,'newNotices':0})
output = ROOT / 'target/ui-audit/evidence-batch-22'
output.mkdir(parents=True,exist_ok=True)
(output/'results.json').write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'reproduced':[result['id'] for result in results]}))
