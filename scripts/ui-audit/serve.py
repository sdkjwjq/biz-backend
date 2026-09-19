"""Start disposable HTTP / UI audit services; stop with target/ui-audit/STOP.

Requires the regression test classes, target/ui-audit-classpath.txt, existing Vue
dependencies and SHUANGGAO_TEST_DB_PASSWORD. Business records are never copied.
"""
import importlib.util
import argparse
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import time
import urllib.request
import uuid

ROOT = Path(__file__).resolve().parents[2]
WORK = ROOT / "target/ui-audit"
SPEC = importlib.util.spec_from_file_location("review_runner", ROOT / "scripts/run-review-regression.py")
RUNNER = importlib.util.module_from_spec(SPEC)
sys.dont_write_bytecode = True
SPEC.loader.exec_module(RUNNER)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--fixture", choices=["base", "business", "review", "navigation", "convenience", "convenience2", "continuous"], default="base")
    parser.add_argument("--frontend-port", type=int, default=15173)
    args = parser.parse_args()
    if not 1024 <= args.frontend_port <= 65535:
        raise RuntimeError("Invalid frontend port")
    frontend_url = "http://127.0.0.1:" + str(args.frontend_port)
    password = os.environ.get("SHUANGGAO_TEST_DB_PASSWORD")
    if password is None:
        raise RuntimeError("Set SHUANGGAO_TEST_DB_PASSWORD")
    WORK.mkdir(parents=True, exist_ok=True)
    stop = WORK / "STOP"
    if stop.exists():
        stop.unlink()
    env = os.environ.copy()
    env["MYSQL_PWD"] = password
    env["SHUANGGAO_TEST_DB_USER"] = env.get("SHUANGGAO_TEST_DB_USER", "root")
    schema = "biz_review_test_" + uuid.uuid4().hex
    mysql = [RUNNER.executable("mysql"), "--host=127.0.0.1", "--port=3306", "--user=" + env["SHUANGGAO_TEST_DB_USER"], "--default-character-set=utf8mb4"]

    def sql(text, database=None):
        result = subprocess.run(mysql + ([database] if database else []), input=text.encode("utf-8"),
                                env=env, capture_output=True)
        if result.returncode:
            raise RuntimeError(result.stderr.decode("utf-8", errors="replace"))
        return result

    dump = subprocess.run([RUNNER.executable("mysqldump"), *mysql[1:], "--no-data", "--skip-triggers",
                           "--set-gtid-purged=OFF", "biz"], env=env, check=True, capture_output=True).stdout
    if re.search(rb"(?im)^\s*(?:USE\s|CREATE\s+DATABASE\s)", dump):
        raise RuntimeError("Unexpected schema selection in dump")
    sql("CREATE DATABASE `" + schema + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    children = []
    logs = []
    try:
        subprocess.run(mysql + [schema], input=dump, env=env, check=True, capture_output=True)
        sql((ROOT / "scripts/ui-audit/fixture.sql").read_text(encoding="utf-8"), schema)
        if args.fixture in ("business", "review", "convenience", "convenience2", "continuous"):
            sql((ROOT / "scripts/ui-audit/fixture-business.sql").read_text(encoding="utf-8"), schema)
        if args.fixture in ("review", "continuous"):
            sql((ROOT / "scripts/ui-audit/fixture-review.sql").read_text(encoding="utf-8"), schema)
        if args.fixture == "continuous":
            sql((ROOT / "scripts/ui-audit/fixture-continuous.sql").read_text(encoding="utf-8"), schema)
        if args.fixture in ("navigation", "convenience", "convenience2"):
            sql((ROOT / "scripts/ui-audit/fixture-navigation.sql").read_text(encoding="utf-8"), schema)
        if args.fixture in ("convenience", "convenience2"):
            sql((ROOT / "scripts/ui-audit/fixture-convenience.sql").read_text(encoding="utf-8"), schema)
        if args.fixture == "convenience2":
            sql((ROOT / "scripts/ui-audit/fixture-convenience2.sql").read_text(encoding="utf-8"), schema)
        cp = os.pathsep.join([str(WORK), str(ROOT / "target/classes"), str(ROOT / "target/test-classes"),
                              (ROOT / "target/ui-audit-classpath.txt").read_text().strip()])
        subprocess.run([RUNNER.executable("javac"), "-encoding", "UTF-8", "-cp", cp, "-d", str(WORK),
                        str(ROOT / "scripts/ui-audit/IsolatedUiAuditServer.java")], check=True)
        env["SHUANGGAO_TEST_JDBC_URL"] = "jdbc:mysql://127.0.0.1:3306/" + schema + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
        # 所有文件写入留在独立工作目录，避免测试上传和日志覆盖本地业务文件。
        backend_log = open(WORK / "backend.log", "w", encoding="utf-8")
        logs.append(backend_log)
        children.append(subprocess.Popen([RUNNER.executable("java"), "-cp", cp, "org.example.IsolatedUiAuditServer"],
                                          cwd=WORK, env=env, stdout=backend_log, stderr=subprocess.STDOUT,
                                          creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0)))
        frontend = ROOT.parent / "biz"
        vite = WORK / "vite-audit.mjs"
        vite.write_text("import { createServer } from " + json.dumps((frontend / "node_modules/vite/dist/node/index.js").as_uri())
                        + "; const server = await createServer({root:" + json.dumps(str(frontend))
                        + ",server:{host:'127.0.0.1',port:" + str(args.frontend_port) + ",strictPort:true,proxy:{'/api':{target:'http://127.0.0.1:18080',changeOrigin:true}}}}); await server.listen();", encoding="utf-8")
        frontend_log = open(WORK / "frontend.log", "w", encoding="utf-8")
        logs.append(frontend_log)
        children.append(subprocess.Popen([RUNNER.executable("node"), str(vite)], cwd=WORK,
                                          stdout=frontend_log, stderr=subprocess.STDOUT,
                                          creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0)))
        (WORK / "state.json").write_text(json.dumps({"schema": schema, "pids": [p.pid for p in children],
                                                   "frontend": frontend_url}), encoding="utf-8")
        deadline = time.monotonic() + 90
        while time.monotonic() < deadline:
            if any(p.poll() is not None for p in children):
                raise RuntimeError("Audit service exited; inspect isolated logs")
            try:
                with urllib.request.urlopen(frontend_url + "/api/system/login", timeout=2):
                    pass
            except urllib.error.HTTPError as e:
                if e.code == 405:
                    print("UI_AUDIT_READY " + frontend_url + " schema=" + schema, flush=True)
                    break
            except (OSError, urllib.error.URLError):
                pass
            time.sleep(0.5)
        else:
            raise RuntimeError("Audit services did not become ready")
        deadline = time.monotonic() + 3600
        while not stop.exists() and time.monotonic() < deadline:
            if any(p.poll() is not None for p in children):
                raise RuntimeError("Audit service stopped")
            time.sleep(0.5)
    finally:
        for child in children:
            if child.poll() is None:
                child.terminate()
                child.wait(timeout=15)
        for log in logs:
            log.close()
        if not re.fullmatch(r"biz_review_test_[0-9a-f]{32}", schema):
            raise RuntimeError("Unexpected schema name")
        sql("DROP DATABASE `" + schema + "`")
        print("Removed isolated UI schema " + schema, flush=True)


if __name__ == "__main__":
    main()
