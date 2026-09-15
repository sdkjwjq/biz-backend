"""Run HTTP regression tests in a temporary MySQL schema, never the business schema.

Requires MySQL client tools, Java/Maven and SHUANGGAO_TEST_DB_PASSWORD in the environment.
Usage: python scripts/run-review-regression.py [methodName]
Only the schema of the local biz database is copied; no business records are copied.
"""
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import uuid


def executable(name):
    found = shutil.which(name)
    if found:
        return found
    candidate = Path(os.environ.get("ProgramFiles", "C:/Program Files")) / "MySQL/MySQL Server 8.0/bin" / (name + ".exe")
    if candidate.is_file():
        return str(candidate)
    raise RuntimeError("Missing executable: " + name)


def main():
    root = Path(__file__).resolve().parents[1]
    password = os.environ.get("SHUANGGAO_TEST_DB_PASSWORD")
    if password is None:
        raise RuntimeError("Set SHUANGGAO_TEST_DB_PASSWORD before running this script")
    method = sys.argv[1] if len(sys.argv) > 1 else None
    if method and not re.fullmatch(r"[A-Za-z][A-Za-z0-9_]*", method):
        raise RuntimeError("Expected a single regression test method name")
    database = "biz_review_test_" + uuid.uuid4().hex
    env = os.environ.copy()
    env["MYSQL_PWD"] = password
    user = env.get("SHUANGGAO_TEST_DB_USER", "root")
    connection = ["--host=127.0.0.1", "--port=3306", "--user=" + user, "--default-character-set=utf8mb4"]
    mysql = executable("mysql")
    dump = subprocess.run([executable("mysqldump"), *connection, "--no-data", "--skip-triggers",
                           "--set-gtid-purged=OFF", "biz"], env=env, check=True, capture_output=True).stdout
    if re.search(rb"(?im)^\s*(?:USE\s|CREATE\s+DATABASE\s)", dump):
        raise RuntimeError("Schema dump unexpectedly selects a database")

    def sql(statement):
        subprocess.run([mysql, *connection], input=statement.encode("utf-8"), env=env, check=True)

    sql("CREATE DATABASE `" + database + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    try:
        subprocess.run([mysql, *connection, database], input=dump, env=env, check=True)
        env["SHUANGGAO_REVIEW_TEST"] = "true"
        env["SHUANGGAO_TEST_DB_USER"] = user
        env["SHUANGGAO_TEST_JDBC_URL"] = ("jdbc:mysql://127.0.0.1:3306/" + database
                                         + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai")
        selector = "ReviewBatchRegressionApiTest" + ("#" + method if method else "")
        print("Running " + selector + " in isolated schema " + database, flush=True)
        result = subprocess.run([shutil.which("mvn.cmd") or executable("mvn"), "-B", "-Dtest=" + selector, "test"],
                                cwd=root, env=env)
        return result.returncode
    finally:
        # Only the schema name created above can reach this cleanup.
        if not re.fullmatch(r"biz_review_test_[0-9a-f]{32}", database):
            raise RuntimeError("Refusing to drop unexpected database")
        sql("DROP DATABASE `" + database + "`")
        print("Removed isolated schema " + database, flush=True)


if __name__ == "__main__":
    sys.exit(main())
