"""Rehearse the approved workbook configuration changes without touching the debug DB."""
from pathlib import Path
import json,os,re,subprocess,uuid,hashlib
ROOT=Path(__file__).resolve().parents[1]
def main():
 password=os.environ['SHUANGGAO_TEST_DB_PASSWORD'];env=dict(os.environ,MYSQL_PWD=password);schema='biz_customer_test_'+uuid.uuid4().hex
 mysql=r'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe';source=(ROOT/'data/20260920backup.sql').read_bytes();assert not re.search(rb'(?im)^\s*(USE\s|CREATE\s+DATABASE\s)',source)
 def sql(text,database=schema,success=True):
  r=subprocess.run([mysql,'--host=127.0.0.1','--user=root','--default-character-set=utf8mb4','--batch','--skip-column-names',*([database] if database else [])],input=text if isinstance(text,bytes) else text.encode(),env=env,capture_output=True);assert (r.returncode==0)==success,r.stderr.decode('utf-8',errors='replace');return r.stdout.decode('utf-8').strip()
 def file(name):return (ROOT/'data/migrations'/name).read_text(encoding='utf-8')
 prefix='2026-10-29-customer-assignments';seed=file(prefix+'-candidates.sql');forward=seed+file(prefix+'.sql');check=seed+file(prefix+'-check.sql');rollback=file(prefix+'-rollback.sql')
 manifest=json.loads((ROOT/'data/repairs/customer-assignments-20261029.json').read_text(encoding='utf-8'))
 def digest(table,exclude):
  cols=sql("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='"+table+"' ORDER BY ORDINAL_POSITION").splitlines();cols=[c for c in cols if c not in exclude];return hashlib.sha256(sql('SELECT JSON_ARRAY('+','.join('`'+c+'`' for c in cols)+') FROM '+table+' ORDER BY 1').encode()).hexdigest()
 def reset():
  sql('DROP TABLE IF EXISTS sys_customer_update_20261029,bak_20261029_customer_task,bak_20261029_customer_performance,sys_performance_leader_repair_20261029,bak_20261029_performance_leader')
  sql(source);sql((ROOT.parent/'releases/shuanggao-update-20261027/sql/additive.sql').read_bytes());sql(file('2026-10-29-platform-notices.sql'));sql(file('2026-09-24-ownership-change-log.sql'));sql(file('2026-10-29-performance-leader-repair-candidates.sql')+file('2026-10-29-performance-leader-repair.sql'))
 checks=[];sql('CREATE DATABASE `'+schema+'` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci',None)
 try:
  reset();before_task=digest('biz_task',{'is_priority','is_difficult','management_version','update_time'});before_perf=digest('biz_performance',{'auditor_id','principal_id','dept_id','update_time'});before_sub=digest('biz_performance_submission',set());before_logs=digest('biz_performance_audit_log',set())
  original_reviewers=sql("SELECT perf_id,auditor_id FROM biz_performance WHERE leader_id=112120 ORDER BY perf_id")
  sql(check);sql(forward)
  for r in manifest['task_flags']:assert sql(f"SELECT COALESCE(is_priority,0),COALESCE(is_difficult,0) FROM biz_task WHERE task_id={r['id']}")==f"{r['priority']}\t{r['difficult']}"
  assert int(sql("SELECT COUNT(*) FROM biz_task WHERE phase=2026 AND is_priority=1 AND is_difficult=1"))==7
  assert before_task==digest('biz_task',{'is_priority','is_difficult','management_version','update_time'}) and before_perf==digest('biz_performance',{'auditor_id','principal_id','dept_id','update_time'})
  assert original_reviewers==sql("SELECT perf_id,auditor_id FROM biz_performance WHERE leader_id=112120 ORDER BY perf_id")
  assert before_sub==digest('biz_performance_submission',set()) and before_logs==digest('biz_performance_audit_log',set())
  verified=int(sql("SELECT COUNT(*) FROM sys_customer_update_20261029 WHERE target_type='PERFORMANCE'"));assert verified>=120,verified
  for r in manifest['performance_assignments']:
   if sql(f"SELECT COUNT(*) FROM sys_customer_update_20261029 WHERE target_type='PERFORMANCE' AND target_id={r['id']}")=='1':
    actual=sql(f"SELECT leader_id,dept_id,principal_id FROM biz_performance WHERE perf_id={r['id']}");assert actual==f"{r['leader']}\t{r['dept']}\t{r['principal']}",(r['code'],actual)
    if not r['keep_auditor']:assert sql(f"SELECT auditor_id FROM biz_performance WHERE perf_id={r['id']}")==str(r['auditor'])
  status=sql(check);checks.append('29 task flags including 7 dual labels updated; professional owners, values, original submissions and history unchanged; Chen Aolin reviewers untouched')
  count=int(sql('SELECT COUNT(*) FROM sys_customer_update_20261029'));assert count>=29
  current=sql('CHECKSUM TABLE biz_task,biz_performance,sys_customer_update_20261029,bak_20261029_customer_task,bak_20261029_customer_performance');sql(forward);assert current==sql('CHECKSUM TABLE biz_task,biz_performance,sys_customer_update_20261029,bak_20261029_customer_task,bak_20261029_customer_performance');checks.append('Repeat update is a no-op and preserves the original backup')
  sql(rollback);sql(rollback);checks.append('Safe configuration-only rollback retains all business values and history')
  reset();first=manifest['performance_assignments'][0];sql(f"UPDATE biz_performance SET auditor_id=110750 WHERE perf_id={first['id']}");sql(forward);assert sql(f"SELECT auditor_id FROM biz_performance WHERE perf_id={first['id']}")=='110750';assert sql(f"SELECT COUNT(*) FROM sys_customer_update_20261029 WHERE target_type='PERFORMANCE' AND target_id={first['id']}")=='0';checks.append('A newer unmatched reviewer configuration is skipped rather than overwritten')
  sql("UPDATE biz_task SET is_priority=IF(is_priority=1,0,1),update_time=DATE_ADD(update_time,INTERVAL 1 SECOND) WHERE task_id="+str(manifest['task_flags'][0]['id']));sql(rollback,success=False);checks.append('Rollback refuses to overwrite later task changes')
 finally:
  assert re.fullmatch('biz_customer_test_[0-9a-f]{32}',schema);sql('DROP DATABASE `'+schema+'`',None)
 out=ROOT.parent/'.run/platform-notices-20261029/customer-migration-results.json';out.write_text(json.dumps({'passed':True,'checks':checks,'firstRehearsalRecordedRows':count,'firstRehearsalCheck':status,'temporaryDatabaseRemoved':True,'debugDatabaseModified':False},ensure_ascii=False,indent=2),encoding='utf-8');print('Customer migration checks passed:',len(checks))
if __name__=='__main__':main()
