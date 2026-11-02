"""Verify transfer DDL and guarded rollback in a new, disposable schema only."""
from pathlib import Path
import os,subprocess,uuid,json
ROOT=Path(__file__).resolve().parents[1]
def main():
 schema='biz_review_test_'+uuid.uuid4().hex
 env=dict(os.environ,MYSQL_PWD=os.environ['SHUANGGAO_TEST_DB_PASSWORD'])
 client=['C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe','--host=127.0.0.1','--user='+env.get('SHUANGGAO_TEST_DB_USER','root'),'--default-character-set=utf8mb4','--batch','--skip-column-names']
 def sql(body,database=True,check=True):return subprocess.run(client+([schema] if database else []),input=body.encode('utf-8'),env=env,capture_output=True,check=check)
 forward=(ROOT/'data/migrations/2026-11-02-task-transfer-request.sql').read_text(encoding='utf-8');rollback=(ROOT/'data/migrations/2026-11-02-task-transfer-request-rollback.sql').read_text(encoding='utf-8');checks=[]
 sql('CREATE DATABASE `'+schema+'` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci',False)
 try:
  sql(forward);sql(forward);sql(rollback);checks.append('Repeated migration and empty-table rollback')
  sql(forward);sql("INSERT INTO biz_task_transfer_request(task_id,field_type,from_user,to_user,reason) VALUES(1,'leader',1,2,'synthetic')")
  duplicate=sql("INSERT INTO biz_task_transfer_request(task_id,field_type,from_user,to_user,reason) VALUES(1,'leader',1,3,'synthetic')",check=False);assert duplicate.returncode!=0 and b'Duplicate entry' in duplicate.stderr;checks.append('Database uniqueness prevents duplicate pending requests')
  sql(forward);guard=sql(rollback,check=False);assert guard.returncode!=0 and b'Transfer requests exist' in guard.stderr
  assert sql('SELECT COUNT(*) FROM biz_task_transfer_request').stdout.strip()==b'1';checks.append('Rollback refuses to discard business records')
  result=sql((ROOT/'data/migrations/2026-11-02-task-transfer-request-check.sql').read_text(encoding='utf-8')).stdout.decode();assert all(line.split('\t')[-1]=='0' for line in result.splitlines());checks.append('Verification script reports no errors')
  print(json.dumps({'passed':True,'checks':checks},ensure_ascii=False))
 finally:sql('DROP DATABASE `'+schema+'`',False)
if __name__=='__main__':main()
