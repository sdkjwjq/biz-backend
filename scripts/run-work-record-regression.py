"""工作纪实真实 HTTP/MySQL 回归：仅复制结构，应用新迁移，结束后删除随机隔离库。"""
import importlib.util
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import uuid

ROOT = Path(__file__).resolve().parents[1]
SPEC = importlib.util.spec_from_file_location("review_runner", ROOT / "scripts/run-review-regression.py")
RUNNER = importlib.util.module_from_spec(SPEC)
sys.dont_write_bytecode = True
SPEC.loader.exec_module(RUNNER)


def main():
    password = os.environ.get("SHUANGGAO_TEST_DB_PASSWORD")
    if password is None:
        raise RuntimeError("Set SHUANGGAO_TEST_DB_PASSWORD")
    env = os.environ.copy()
    env["MYSQL_PWD"] = password
    env["SHUANGGAO_TEST_DB_USER"] = env.get("SHUANGGAO_TEST_DB_USER", "root")
    schema = "biz_review_test_" + uuid.uuid4().hex
    args = ["--host=127.0.0.1", "--port=3306", "--user=" + env["SHUANGGAO_TEST_DB_USER"], "--default-character-set=utf8mb4"]
    mysql = [RUNNER.executable("mysql"), *args]
    dump = subprocess.run([RUNNER.executable("mysqldump"), *args, "--no-data", "--skip-triggers", "--set-gtid-purged=OFF", "biz"],
                          env=env, check=True, capture_output=True).stdout
    if re.search(rb"(?im)^\s*(USE\s|CREATE\s+DATABASE\s)", dump):
        raise RuntimeError("Unexpected database selection")

    def sql(text, database=None):
        subprocess.run(mysql + ([database] if database else []), input=text if isinstance(text, bytes) else text.encode("utf-8"),
                       env=env, check=True)

    sql("CREATE DATABASE `" + schema + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    try:
        sql(dump, schema)
        # 本地业务库将来可能已经部署纪实；只在已验证的随机隔离库重建新增表。
        sql("DROP TABLE IF EXISTS biz_work_record_snapshot; DROP TABLE IF EXISTS biz_work_record_entry; DROP TABLE IF EXISTS biz_work_record;", schema)
        migration = (ROOT / "scripts/work-records/001_work_records.sql").read_bytes()
        sql(migration, schema)
        sql((ROOT / "scripts/work-records/001_work_records_rollback.sql").read_bytes(), schema)
        sql(migration, schema)
        env["SHUANGGAO_REVIEW_TEST"] = "true"
        env["SHUANGGAO_TEST_JDBC_URL"] = "jdbc:mysql://127.0.0.1:3306/" + schema + "?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai"
        # Connector/J 的 Java 字符集名称使用 UTF-8。
        env["SHUANGGAO_TEST_JDBC_URL"] = env["SHUANGGAO_TEST_JDBC_URL"].replace("characterEncoding=utf8mb4", "characterEncoding=utf8")
        print("Work record regression schema=" + schema, flush=True)
        return subprocess.run([shutil.which("mvn.cmd") or RUNNER.executable("mvn"), "-B", "-Dtest=WorkRecordApiTest", "test"],
                              cwd=ROOT, env=env).returncode
    finally:
        if not re.fullmatch(r"biz_review_test_[0-9a-f]{32}", schema):
            raise RuntimeError("Unexpected cleanup target")
        sql("DROP DATABASE `" + schema + "`")
        print("Removed isolated schema " + schema, flush=True)


if __name__ == "__main__":
    sys.exit(main())
