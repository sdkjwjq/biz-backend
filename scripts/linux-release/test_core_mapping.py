"""Exercise the corrective migration using synthetic records in a disposable schema."""
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import uuid

ROOT = Path(__file__).resolve().parents[2]
MYSQL = Path(shutil.which('mysql') or r'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe')
env = os.environ.copy()
env['MYSQL_PWD'] = env['SHUANGGAO_TEST_DB_PASSWORD']
schema = 'biz_review_test_' + uuid.uuid4().hex
connection = ['--host=127.0.0.1', '--user=root', '--default-character-set=utf8mb4']


def sql(value, db=schema, success=True):
    result = subprocess.run([str(MYSQL), *connection, '--batch', '--skip-column-names', *([db] if db else [])],
                            input=value if isinstance(value, bytes) else value.encode('utf-8'), env=env, capture_output=True)
    assert (result.returncode == 0) == success, result.stderr.decode('utf-8', errors='replace')
    return result.stdout


def snapshot(query):
    return hashlib.sha256(sql(query)).hexdigest()


def main():
    checks = []
    sql('CREATE DATABASE `' + schema + '` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci', None)
    try:
        dump = subprocess.check_output([str(MYSQL.parent/'mysqldump.exe'), *connection, '--no-data', '--skip-triggers', '--set-gtid-purged=OFF', 'biz'], env=env)
        sql(dump)
        sql((ROOT/'data/migrations/2026-10-12-core-indicators.sql').read_bytes())
        sql((ROOT/'scripts/ui-audit/fixture.sql').read_bytes())
        sql("UPDATE biz_core_indicator SET indicator_code='1.2.15' WHERE indicator_id=10; ALTER TABLE biz_core_report DROP COLUMN performance_code;")
        sql("INSERT INTO biz_performance(perf_id,project_id,perf_code,perf_name,target_value,current_value,data_type,dept_id,leader_id,auditor_id,principal_id,is_delete) VALUES(960015,1,'1.2.15','毕业生就业相关度',85,80,'2',920001,910001,910003,910003,0),(960045,1,'1.2.45','发布《工程机械产业发展报告》数量',5,5,'1',920001,910001,910003,910002,0);")
        for status in [0, 10, 20, 30, -10, -20, -1]:
            report_id = 980000 + status
            draft_key = "'10:2026:8:910001'" if status == 0 else 'NULL'
            sql(f"INSERT INTO biz_core_report(report_id,indicator_id,record_year,record_month,draft_key,indicator_name,target_value,data_type,actual_value,submit_by,principal_id,current_handler_id,flow_status,submit_time) VALUES({report_id},10,2026,8,{draft_key},'毕业生就业相关度',85,'2',85,910001,910003,910003,{status},NOW());")
            sql(f"INSERT INTO biz_core_audit_log(report_id,operator_id,action,pre_status,post_status,comment) VALUES({report_id},910001,'提交',0,{status},'Keep original evidence');")
        sql("INSERT INTO sys_notice(notice_id,to_user_id,source_type,source_id,trigger_event,title,content,is_delete,is_read) VALUES(999001,910003,'4',980010,'CORE_TODO','Old todo','Keep',0,0),(999002,910001,'4',980030,'CORE_RESULT','Old result','Keep',0,0);")
        performances = snapshot('SELECT * FROM biz_performance ORDER BY perf_id')
        evidence_query = 'SELECT report_id,indicator_id,indicator_name,target_value,data_type,actual_value,submit_by,principal_id,current_handler_id,flow_status,submit_time FROM biz_core_report ORDER BY report_id; SELECT * FROM biz_core_audit_log ORDER BY log_id;'
        evidence = snapshot(evidence_query)
        migration = (ROOT/'data/migrations/2026-10-14-core-indicator-report-mapping.sql').read_bytes()
        sql(migration)
        assert sql('SELECT indicator_code FROM biz_core_indicator WHERE indicator_id=10').strip() == b'1.2.45'
        assert sql("SELECT COUNT(*) FROM biz_core_report WHERE performance_code='1.2.15'").strip() == b'7'
        assert sql('SELECT COUNT(*) FROM biz_core_report WHERE draft_key IS NOT NULL').strip() == b'0'
        assert sql('SELECT is_delete FROM sys_notice WHERE notice_id=999001').strip() == b'1'
        assert sql('SELECT is_delete FROM sys_notice WHERE notice_id=999002').strip() == b'0'
        assert snapshot(evidence_query) == evidence
        assert snapshot('SELECT * FROM biz_performance ORDER BY perf_id') == performances
        sql(migration)
        assert snapshot(evidence_query) == evidence
        checks += ['correct-mapping-and-old-source-frozen', 'all-old-workflow-states-and-evidence-retained', 'performance-table-unchanged', 'only-obsolete-todo-retired', 'repeat-migration-idempotent']
        rollback = (ROOT/'data/migrations/2026-10-14-core-indicator-report-mapping-rollback.sql').read_bytes()
        sql(rollback)
        assert sql('SELECT indicator_code FROM biz_core_indicator WHERE indicator_id=10').strip() == b'1.2.15'
        assert sql('SELECT draft_key FROM biz_core_report WHERE flow_status=0').strip() == b'10:2026:8:910001'
        assert sql('SELECT is_delete FROM sys_notice WHERE notice_id=999001').strip() == b'0'
        sql(migration)
        sql("INSERT INTO biz_core_report(indicator_id,record_year,record_month,performance_code,indicator_name,submit_by) VALUES(10,2026,8,'1.2.45','发布《工程机械产业发展报告》数量',910001)")
        sql(rollback, success=False)
        checks += ['safe-manual-rollback-restores-draft-and-notice', 'rollback-refuses-new-business-records']
    finally:
        assert re.fullmatch(r'biz_review_test_[0-9a-f]{32}', schema)
        sql('DROP DATABASE `' + schema + '`', None)
    checks.append('isolated-database-cleaned')
    result = {'checks': checks, 'production_database_accessed': False}
    (ROOT/'target/core-mapping-migration-verification.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
    print(json.dumps(result))


if __name__ == '__main__':
    main()
