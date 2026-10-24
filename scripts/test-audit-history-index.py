"""Exercise index migration/rollback using a disposable schema and synthetic logs."""
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import uuid

root=Path(__file__).resolve().parents[1]
schema="biz_review_test_"+uuid.uuid4().hex
env=dict(os.environ,MYSQL_PWD=os.environ["SHUANGGAO_TEST_DB_PASSWORD"])
mysql=shutil.which("mysql") or "C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"
command=[mysql,"--host=127.0.0.1","--port=3306","--user=root","--default-character-set=utf8mb4","--batch","--raw","--skip-column-names"]

def sql(statement, database=True, check=True):
    return subprocess.run(command+([schema] if database else []),input=statement.encode("utf-8"),
                          env=env,check=check,capture_output=True)

def migration(suffix=""):
    return (root/f"data/migrations/2026-10-24-audit-history-index{suffix}.sql").read_text(encoding="utf-8")

def fingerprint():
    return sql("SELECT COUNT(*),SUM(CRC32(CONCAT_WS('|',log_id,sub_id,operator_id,action_type,create_time))) FROM biz_audit_log").stdout

query="SELECT sub_id,MAX(create_time) FROM biz_audit_log WHERE LOWER(action_type) IN('pass','reject') AND sub_id IN(1,2,3) GROUP BY sub_id"
sql(f"CREATE DATABASE `{schema}` CHARACTER SET utf8mb4",False)
try:
    sql("CREATE TABLE biz_audit_log(log_id BIGINT PRIMARY KEY,sub_id BIGINT NOT NULL,operator_id BIGINT NOT NULL,action_type VARCHAR(20),create_time DATETIME,KEY fk_log_op(operator_id))")
    for start in range(0,10000,500):
        rows=",".join(f"({i+1},{i//10+1},1,'pass','2026-10-01')" for i in range(start,start+500))
        sql("INSERT INTO biz_audit_log VALUES "+rows)
    sql("ANALYZE TABLE biz_audit_log")
    before=fingerprint();expected=sql(query).stdout
    plan_before=sql("EXPLAIN "+query).stdout.decode().strip()
    sql(migration());sql(migration());sql(migration("-check"));sql("ANALYZE TABLE biz_audit_log")
    plan_after=sql("EXPLAIN "+query).stdout.decode().strip()
    assert "idx_audit_history_sub_time" in plan_after
    assert before==fingerprint() and expected==sql(query).stdout
    sql(migration("-rollback"));sql(migration("-rollback"))
    assert before==fingerprint()
    assert sql("SHOW INDEX FROM biz_audit_log WHERE Key_name='idx_audit_history_sub_time'").stdout==b""
    # An independently existing index must not be marked or removed.
    sql("ALTER TABLE biz_audit_log ADD INDEX custom_existing_sub(sub_id)")
    sql(migration());sql(migration("-check"));sql(migration("-rollback"))
    assert sql("SHOW INDEX FROM biz_audit_log WHERE Key_name='custom_existing_sub'").stdout
    sql("ALTER TABLE biz_audit_log DROP INDEX custom_existing_sub")
    sql(migration())
    sql("ALTER TABLE biz_audit_log DROP INDEX idx_audit_history_sub_time,ADD INDEX idx_audit_history_sub_time(sub_id,operator_id) COMMENT 'performance-audit-history-20261024'")
    refused=sql(migration("-rollback"),check=False)
    assert refused.returncode!=0 and b"rollback refused" in refused.stderr
    assert before==fingerprint()
    report={"synthetic_rows":10000,"explain_before":plan_before,"explain_after":plan_after,
            "result_and_log_fingerprint_unchanged":True,"idempotent_apply_rollback":True,
            "preexisting_index_preserved":True,"modified_index_rollback_refused":True}
    (root/"target/performance-audit-history-index.json").write_text(json.dumps(report,indent=2),encoding="utf-8")
    print(json.dumps(report,indent=2))
finally:
    if not re.fullmatch(r"biz_review_test_[0-9a-f]{32}",schema):raise RuntimeError("Unsafe cleanup target")
    sql(f"DROP DATABASE `{schema}`",False)
