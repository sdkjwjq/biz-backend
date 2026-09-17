"""Synthetic reminder/trend audit; requires a fresh disposable review fixture."""
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
    req = urllib.request.Request('http://127.0.0.1:18080/api' + path,
        data=None if body is None else json.dumps(body).encode('utf-8'), headers=headers)
    with urllib.request.urlopen(req, timeout=15) as response:
        raw = response.read().decode('utf-8')
        try:
            return json.loads(raw)
        except json.JSONDecodeError:
            return raw

admin = call('/system/login', body={'user_id':110228,'password':'review-fixture-password'})['token']
results = []
assert sql('SELECT leader_id IS NULL FROM sys_dept WHERE dept_id=920002') == '1'
# Existing fixture has one valid leader and a department whose leader is unset.
monthly = call('/scheduled/month_leader_trigger', admin, {})
annual = call('/scheduled/year_trigger', admin, {})
if fixed:
    assert '成功' in monthly and '成功' in annual
else:
    assert monthly['code'] == annual['code'] == 500
count = int(sql('SELECT COUNT(*) FROM sys_notice WHERE to_user_id=910002'))
assert count == (2 if fixed else 0)
results.append({'id':'17-1','monthly':monthly,'annual':annual,'validLeaderNoticeCount':count})
previous_notice = int(sql('SELECT COALESCE(MAX(notice_id),0) FROM sys_notice'))

# One person manages A and B, but their own account belongs to A.
sql("""
UPDATE sys_dept SET leader_id=910002 WHERE dept_id=920002;
INSERT INTO biz_task (task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete)
VALUES (937001,1,0,YEAR(CURDATE()),'17.1','合成部门B任务一',3,910004,910003,910002,920002,'1',10,0,0,'1',0),
(937002,1,0,YEAR(CURDATE()),'17.2','合成部门B任务二',3,910004,910003,910002,920002,'1',10,0,0,'1',0);
""")
monthly = call('/scheduled/month_leader_trigger', admin, {})
annual = call('/scheduled/year_trigger', admin, {})
notices = [json.loads(row) for row in sql("SELECT JSON_OBJECT('title',title,'content',content) FROM sys_notice WHERE to_user_id=910002 AND notice_id>" + str(previous_notice) + " ORDER BY notice_id").splitlines()]
assert len(notices) == (4 if fixed else 2)
if fixed:
    assert sum('审计测试部门A' in notice['content'] and '3 个双高建设任务' in notice['content'] for notice in notices) == 2
    assert sum('审计测试部门B' in notice['content'] and '2 个双高建设任务' in notice['content'] for notice in notices) == 2
else:
    assert all('3 个双高建设任务' in notice['content'] for notice in notices)
assert sql('SELECT COUNT(*) FROM biz_task WHERE dept_id IN (920001,920002) AND phase=YEAR(CURDATE())') == '5'
results.append({'id':'17-2','monthly':monthly,'annual':annual,'managedTaskCount':5,'ownDepartmentTaskCount':3,'notices':notices})

# A nullable value prevents the daily sample, but the real endpoint says success.
sql('UPDATE biz_task SET current_value=NULL WHERE task_id=930002')
failed = call('/dashboard/trend/record', admin, {})
assert failed == '手动记录成功'
null_count = int(sql('SELECT COUNT(*) FROM biz_trend_data'))
assert null_count == (1 if fixed else 0)
sql('UPDATE biz_task SET current_value=0 WHERE task_id=930002')
control = call('/dashboard/trend/record', admin, {})
assert control == ('今天已记录趋势数据，跳过' if fixed else '手动记录成功')
assert sql('SELECT COUNT(*) FROM biz_trend_data') == '1'
results.append({'id':'17-3','withNullValue':failed,'nullValueRecordCount':null_count,'afterZeroValue':control,'controlRecordCount':1})
output = ROOT / 'target/ui-audit/evidence-batch-17'
output.mkdir(parents=True, exist_ok=True)
(output / ('reminder-trend-fixed.json' if fixed else 'reminder-trend.json')).write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps({'fixed':fixed,'passed':[row['id'] for row in results]}))
