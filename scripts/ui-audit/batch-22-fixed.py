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
payload = dict(task,taskId=922101,taskCode='1.1.22',taskName='合成父链测试')
count = sql('SELECT COUNT(*) FROM biz_task')
for invalid_id in (930001,930000):
    sql(f'UPDATE biz_task SET is_delete=1 WHERE task_id={invalid_id}')
    try:
        response = call('/biz/tasks/manage/add',admin,payload)
        assert isinstance(response,dict) and '已删除' in response.get('message',''), response
        assert sql('SELECT COUNT(*) FROM biz_task') == count
    finally:
        sql(f'UPDATE biz_task SET is_delete=0 WHERE task_id={invalid_id}')
assert isinstance(call('/biz/tasks/manage/add',admin,payload),str)
results.append({'id':'22-1','deletedParentRejected':True,'deletedAncestorRejected':True,'validParentCreated':True})

assert isinstance(call('/system/users/add',admin,{'userId':922201,'deptId':920001,'userName':'batch22_deleted','nickName':'合成已删除人员','password':'review-fixture-password','role':'1','status':'0'}),str)
assert isinstance(call('/system/users/delete/922201',admin,{}),str)
before = sql('SELECT COUNT(*) FROM sys_notice')
notice = {'title':'合成接收人校验','content':'合成正文','type':'3','trigger_event':'TASK','source_id':930002}
for recipient in (None,9999222,922201):
    response = call('/system/notice',admin,dict(notice,to_user_id=recipient))
    assert isinstance(response,dict) and '接收人员不存在或已删除' in response.get('message',''), response
    assert sql('SELECT COUNT(*) FROM sys_notice') == before
assert call('/system/notice',admin,dict(notice,to_user_id=910001)) == '发送成功'
assert sql("SELECT to_user_id FROM sys_notice WHERE title='合成接收人校验'") == '910001'
results.append({'id':'22-2','invalidRecipientsRejected':3,'noFailedWrites':True,'validRecipientDelivered':910001})

sql("INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by) VALUES(922001,'batch22.pdf','/synthetic/batch22.pdf','/synthetic/batch22.pdf','pdf',910001)")
assert isinstance(call('/biz/sub',owner,{'task_id':930002,'file_id':922001,'reported_value':3,'data_type':'1'}),str)
sub_id = int(sql('SELECT MAX(sub_id) FROM biz_material_submission'))
for handler in ('910003','NULL','922201'):
    sql(f"INSERT INTO biz_material_submission(task_id,file_id,reported_value,data_type,submit_by,submit_dept_id,manage_dept_id,submit_time,file_suffix,flow_status,current_handler_id,is_delete) SELECT task_id,file_id,reported_value,data_type,submit_by,submit_dept_id,manage_dept_id,submit_time,file_suffix,flow_status,{handler},is_delete FROM biz_material_submission WHERE sub_id={sub_id}")
response = call('/scheduled/month_auditor_trigger',admin,{})
assert isinstance(response,str) and '向1名用户' in response and '跳过 2 条' in response, response
delivered = sql("SELECT to_user_id,content FROM sys_notice WHERE title='月度审核任务提醒'")
assert delivered.startswith('910003\t') and '2 个待审核任务' in delivered, delivered
assert sql("SELECT COUNT(*) FROM sys_notice WHERE title='月度审核任务提醒'") == '1'
sql('UPDATE biz_material_submission SET is_delete=1 WHERE current_handler_id=910003')
empty = call('/scheduled/month_auditor_trigger',admin,{})
assert isinstance(empty,str) and '没有需要发送月度审核提醒' in empty and '跳过 2 条' in empty, empty
assert sql("SELECT COUNT(*) FROM sys_notice WHERE title='月度审核任务提醒'") == '1'
results.append({'id':'22-3','mixedBatch':response,'validHandler':910003,'validPendingCount':2,'allInvalidBatch':empty,'noticeCount':1})
output = ROOT / 'target/ui-audit/evidence-batch-22-fixed'
output.mkdir(parents=True,exist_ok=True)
(output/'results.json').write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'passed':[result['id'] for result in results]}))
