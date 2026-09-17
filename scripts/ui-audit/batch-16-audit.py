"""Reproduce batch 16 using synthetic records in the running disposable UI schema."""
import importlib.util
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import urllib.request

ROOT = Path(__file__).resolve().parents[2]
fixed = '--fixed' in sys.argv
sys.dont_write_bytecode = True
spec = importlib.util.spec_from_file_location('runner', ROOT / 'scripts/run-review-regression.py')
runner = importlib.util.module_from_spec(spec)
spec.loader.exec_module(runner)
schema = json.loads((ROOT / 'target/ui-audit/state.json').read_text())['schema']
assert re.fullmatch(r'biz_review_test_[0-9a-f]{32}', schema)
env = os.environ.copy()
env['MYSQL_PWD'] = env['SHUANGGAO_TEST_DB_PASSWORD']

def sql(statement):
    result = subprocess.run([runner.executable('mysql'), '--host=127.0.0.1', '--port=3306',
        '--user=' + env.get('SHUANGGAO_TEST_DB_USER', 'root'), '--default-character-set=utf8mb4',
        '--batch', '--skip-column-names', schema], input=statement.encode('utf-8'), env=env, capture_output=True, check=True)
    return result.stdout.decode('utf-8').strip()

def call(path, token=None, body=None):
    headers = {'Content-Type': 'application/json'}
    if token:
        headers['Authorization'] = token
    request = urllib.request.Request('http://127.0.0.1:18080/api' + path,
        data=None if body is None else json.dumps(body).encode('utf-8'), headers=headers)
    with urllib.request.urlopen(request, timeout=15) as response:
        raw = response.read().decode('utf-8')
        try:
            return json.loads(raw)
        except json.JSONDecodeError:
            return raw

def login(user):
    return call('/system/login', body={'user_id': user, 'password': 'review-fixture-password'})['token']

sql("""
INSERT INTO sys_file (file_id,file_name,file_path,file_url,file_suffix,file_size,upload_by,is_delete)
VALUES (946001,'batch16-synthetic.pdf','/uploads/batch16-synthetic.pdf','/uploads/batch16-synthetic.pdf','pdf',20,910001,0);
INSERT INTO biz_task (task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete)
VALUES (936001,1,930001,2026,'1.1.16a','合成缺少审核人任务',3,910001,NULL,910002,920001,'1',10,0,0,'1',0),
(936002,1,930001,2026,'1.1.16b','合成四级汇总任务',3,910001,910003,910002,920001,'1',10,5,50,'1',0),
(936003,1,930001,2026,'1.1.16c','合成层级校验任务',3,910001,910003,910002,920001,'1',10,0,0,'1',0);
INSERT INTO biz_level4_task (task_id,parent_id,phase,task_name,leader_id,dept_id,data_type,target_value,current_value,progress,status)
VALUES (966001,936002,2026,'合成子任务A',910001,920001,'1',5,2,40,'1'),
(966002,936002,2026,'合成子任务B',910001,920001,'1',5,3,60,'1');
""")
admin, user, reviewer = login(110228), login(910001), login(910003)
results = []

# Missing reviewer: submission is accepted but even admin cannot process it.
submitted = call('/biz/sub', user, {'task_id':936001,'file_id':946001,'reported_value':3,'comment':'合成缺少审核人'})
if fixed:
    assert submitted['code'] == 500 and '审核人' in submitted['message']
    assert sql('SELECT COUNT(*) FROM biz_material_submission WHERE task_id=936001') == '0'
    assert sql('SELECT status FROM biz_task WHERE task_id=936001') == '1'
    results.append({'id':'16-1','submitted':submitted,'submissionCount':0,'taskStatus':'1'})
else:
    record = json.loads(sql("SELECT JSON_OBJECT('subId',sub_id,'flowStatus',flow_status,'handler',current_handler_id) FROM biz_material_submission WHERE task_id=936001 ORDER BY sub_id DESC LIMIT 1"))
    assert record['flowStatus'] == 10 and record['handler'] is None
    audit = call('/biz/audit', admin, {'sub_id':record['subId'],'is_pass':True,'title':'合成审核','content':'合成审核'})
    assert audit['code'] == 500 and '当前处理人未设置' in audit['message']
    assert sql('SELECT status FROM biz_task WHERE task_id=936001') == '2'
    results.append({'id':'16-1','submitted':submitted,'saved':record,'adminAudit':audit,'taskStatus':'2'})

# Incomplete level-four list discards the other child's contribution.
partial = call('/biz/sub', user, {'third_task_id':936002,'file_id':946001,'comment':'合成部分四级填报',
    'sub_list':[{'task_id':966001,'reported_value':4,'data_type':'1'}]})
values = json.loads(sql("SELECT JSON_OBJECT('parent',current_value,'childSum',(SELECT SUM(current_value) FROM biz_level4_task WHERE parent_id=936002)) FROM biz_task WHERE task_id=936002"))
assert values['parent'] == 4 and values['childSum'] == 7
withdraw = call('/biz/drawback/936002', user, {})
assert sql('SELECT status FROM biz_task WHERE task_id=936002') == '1'
direct = call('/biz/sub', user, {'task_id':936002,'file_id':946001,'reported_value':9,'comment':'合成绕过四级填报'})
direct_values = json.loads(sql("SELECT JSON_OBJECT('parent',current_value,'childSum',(SELECT SUM(current_value) FROM biz_level4_task WHERE parent_id=936002)) FROM biz_task WHERE task_id=936002"))
assert direct_values['parent'] == 9 and direct_values['childSum'] == 5
results.append({'id':'16-2','partialResponse':partial,'partialValues':values,'withdraw':withdraw,
    'directResponse':direct,'directValues':direct_values})

# Task update accepts a self-parent; perform last to avoid touching the corrupted tree.
task = call('/biz/tasks/936003', admin)
for field in ('isDelete', 'createTime', 'updateTime'):
    task.pop(field, None)
task['parentId'] = 936003
updated = call('/biz/tasks/manage/update', admin, task)
saved = call('/biz/tasks/936003', admin)
if fixed:
    assert updated['code'] == 500 and '循环' in updated['message']
    assert saved['parentId'] == 930001
else:
    assert saved['parentId'] == saved['taskId'] == 936003
results.append({'id':'16-3','updated':updated,'savedTaskId':saved['taskId'],'savedParentId':saved['parentId']})
output = ROOT / 'target/ui-audit/evidence-batch-16'
output.mkdir(parents=True, exist_ok=True)
(output / ('task-fixed.json' if fixed else 'task-audit.json')).write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps({'fixed':fixed,'passed':[row['id'] for row in results]}))
