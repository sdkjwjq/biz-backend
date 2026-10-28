"""Verify reviewer history migration in one new disposable schema; no business rows copied."""
from pathlib import Path
import json
import os
import re
import subprocess
import uuid

ROOT=Path(__file__).resolve().parents[1]
MYSQL=r'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'

def main():
    password=os.environ.get('SHUANGGAO_TEST_DB_PASSWORD')
    if password is None:raise RuntimeError('Set SHUANGGAO_TEST_DB_PASSWORD')
    schema='biz_review_test_'+uuid.uuid4().hex
    env=dict(os.environ,MYSQL_PWD=password)
    checks=[]
    def sql(source, database=schema, ok=True):
        result=subprocess.run([MYSQL,'--host=127.0.0.1','--user=root','--default-character-set=utf8mb4','--batch','--skip-column-names',*([database] if database else [])],input=source if isinstance(source,bytes) else source.encode(),env=env,capture_output=True)
        assert (result.returncode==0)==ok,result.stderr.decode('utf-8',errors='replace')
        return result
    sql('CREATE DATABASE `'+schema+'` CHARACTER SET utf8mb4',None)
    try:
        sql((ROOT/'data/migrations/2026-09-24-ownership-change-log.sql').read_bytes())
        sql("INSERT INTO biz_ownership_change_log(target_type,target_id,operator_id,reason,leader_before_id,leader_after_id,create_time) VALUES('TASK',1,110228,'Synthetic legacy evidence',10,11,'2026-10-27')")
        projection='SELECT log_id,target_type,target_id,operator_id,reason,leader_before_id,leader_after_id,create_time FROM biz_ownership_change_log ORDER BY log_id'
        before=sql(projection).stdout
        migration=(ROOT/'data/migrations/2026-10-28-transfer-reviewers.sql').read_bytes()
        sql(migration);sql(migration)
        assert sql(projection).stdout==before
        assert sql('SELECT COUNT(*) FROM biz_ownership_change_log WHERE auditor_before_id IS NULL AND auditor_after_id IS NULL').stdout.strip()==b'1'
        checks.append('Idempotent forward migration preserves every old value and leaves old reviewer evidence unknown')
        def count():return int(sql("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_ownership_change_log' AND COLUMN_NAME IN('auditor_before_id','auditor_after_id')").stdout.strip())
        assert count()==2
        sql((ROOT/'data/migrations/2026-10-28-transfer-reviewers-check.sql').read_bytes())
        checks.append('Both new audit fields are present and check script succeeds')
        rollback=(ROOT/'data/migrations/2026-10-28-transfer-reviewers-rollback.sql').read_bytes()
        sql(rollback);assert count()==0 and sql(projection).stdout==before
        checks.append('Rollback with no new reviewer evidence preserves old log rows')
        sql(migration)
        sql("INSERT INTO biz_ownership_change_log(target_type,target_id,operator_id,reason,auditor_before_id,auditor_after_id,create_time) VALUES('TASK',1,110228,'Synthetic new evidence',20,21,NOW())")
        result=sql(rollback,ok=False);assert b'Reviewer transfer history exists' in result.stderr
        assert count()==2 and sql('SELECT auditor_after_id FROM biz_ownership_change_log WHERE auditor_after_id IS NOT NULL').stdout.strip()==b'21'
        checks.append('Rollback refuses to discard recorded reviewer history')
    finally:
        assert re.fullmatch('biz_review_test_[0-9a-f]{32}',schema)
        sql('DROP DATABASE `'+schema+'`',None)
    out=ROOT.parent/'.run/transfer-review-20261028/migration-results.json'
    out.write_text(json.dumps({'checks':checks,'isolatedDatabaseRemoved':True},indent=2)+'\n',encoding='utf-8')
    print(json.dumps({'passed':True,'checks':len(checks),'isolatedDatabaseRemoved':True}))

if __name__=='__main__':main()
