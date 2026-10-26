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
import socket
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
    global WORK
    parser = argparse.ArgumentParser()
    parser.add_argument("--fixture", choices=["base", "business", "review", "navigation", "convenience", "convenience2", "continuous", "customer", "work-records", "task-stats", "ownership-transfer", "ownership-transfer-scale", "performance-relation", "level4-equal-weight", "responsibility-filters", "leadership", "core", "budgettrends", "admintest", "work-record-scope", "presentation", "overview", "browser-repairs", "usability", "permissions"], default="base")
    parser.add_argument("--frontend-port", type=int, default=15173)
    parser.add_argument("--backend-port", type=int, default=18080)
    parser.add_argument("--run-id")
    parser.add_argument("--production",action="store_true")
    parser.add_argument("--source-root",type=Path)
    parser.add_argument("--frontend-root",type=Path)
    parser.add_argument("--synthetic-scale",type=int,choices=[1,5])
    parser.add_argument("--comparison-backend-root",type=Path)
    parser.add_argument("--comparison-frontend-root",type=Path)
    parser.add_argument("--comparison-backend-port",type=int,default=18382)
    parser.add_argument("--comparison-frontend-port",type=int,default=15382)
    args = parser.parse_args()
    if any(not 1024 <= port <= 65535 for port in (args.frontend_port,args.backend_port)) or args.frontend_port==args.backend_port:
        raise RuntimeError("Invalid audit ports")
    if args.run_id:
        if not re.fullmatch(r"[a-z0-9][a-z0-9-]{0,47}",args.run_id):raise RuntimeError("Invalid audit run ID")
        WORK=ROOT/("target/ui-audit-"+args.run_id)
    for port in (args.frontend_port,args.backend_port):
        with socket.socket() as probe:probe.bind(("127.0.0.1",port))
    frontend_url = "http://127.0.0.1:" + str(args.frontend_port)
    password = os.environ.get("SHUANGGAO_TEST_DB_PASSWORD")
    if password is None:
        raise RuntimeError("Set SHUANGGAO_TEST_DB_PASSWORD")
    WORK.mkdir(parents=True, exist_ok=True)
    stop = WORK / "STOP"
    if stop.exists():
        stop.unlink()
    env = os.environ.copy()
    env["SHUANGGAO_UI_BACKEND_PORT"]=str(args.backend_port)
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
        sql((ROOT / "scripts/password/001_force_password_change.sql").read_text(encoding="utf-8"), schema)
        sql((ROOT / "data/migrations/2026-09-26-budget-import-version.sql").read_text(encoding="utf-8"), schema)
        sql((ROOT / "data/migrations/2026-09-27-admin-management.sql").read_text(encoding="utf-8"), schema)
        sql((ROOT / "data/migrations/2026-10-12-leadership-access.sql").read_text(encoding="utf-8"), schema)
        sql((ROOT / "data/migrations/2026-10-12-task-attributes.sql").read_text(encoding="utf-8"), schema)
        sql((ROOT / "data/migrations/2026-10-12-core-indicators.sql").read_text(encoding="utf-8"), schema)
        sql((ROOT / "data/migrations/2026-10-14-core-indicator-report-mapping.sql").read_text(encoding="utf-8"), schema)
        sql((ROOT / "data/migrations/2026-10-24-permissions-expert-reviews.sql").read_text(encoding="utf-8"), schema)
        sql((ROOT / "scripts/ui-audit/fixture.sql").read_text(encoding="utf-8"), schema)
        sql("UPDATE sys_user SET force_password_change=0", schema)
        if args.fixture == "permissions":
            sql((ROOT / "scripts/ui-audit/fixture-permissions.sql").read_text(encoding="utf-8"), schema)
            (WORK / "uploads").mkdir(exist_ok=True)
            for name in ("permission-file-a.pdf", "permission-file-b.pdf"):
                (WORK / "uploads" / name).write_bytes(b"%PDF-1.4\nSynthetic isolated authorization fixture\n%%EOF")
        if args.fixture == "work-record-scope":
            sql((ROOT / "scripts/ui-audit/fixture-work-record-scope.sql").read_text(encoding="utf-8"), schema)
        if args.fixture in ("core", "budgettrends", "admintest", "presentation", "overview", "browser-repairs", "usability"):
            sql((ROOT / "scripts/ui-audit/fixture-core.sql").read_text(encoding="utf-8"), schema)
        if args.fixture == "admintest":
            sql("UPDATE biz_level4_task SET target_value=10,current_value=0,data_type='1',progress=0;", schema)
            sql("INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,password,role,status,is_delete,force_password_change) VALUES(990009,920001,'test_admin','测试管理员','review-fixture-password','0','1',0,0);", schema)
        if args.fixture == "budgettrends":
            sql((ROOT / "scripts/ui-audit/fixture-budget-trends.sql").read_text(encoding="utf-8"), schema)
        if args.fixture in ("presentation", "overview", "browser-repairs", "usability"):
            sql((ROOT / "scripts/ui-audit/fixture-budget-trends.sql").read_text(encoding="utf-8"), schema)
            sql((ROOT / "scripts/ui-audit/fixture-presentation.sql").read_text(encoding="utf-8"), schema)
            sql((ROOT / "data/migrations/2026-10-23-leadership-schoolwide.sql").read_text(encoding="utf-8"), schema)
        if args.fixture in ("overview", "browser-repairs", "usability"):
            sql((ROOT / "scripts/ui-audit/fixture-overview.sql").read_text(encoding="utf-8"), schema)
        if args.fixture in ("browser-repairs", "usability"):
            sql((ROOT / "scripts/ui-audit/fixture-browser-repairs.sql").read_text(encoding="utf-8"), schema)
        if args.fixture == "usability":
            sql((ROOT / "scripts/ui-audit/fixture-usability.sql").read_text(encoding="utf-8"), schema)
        if args.fixture == "leadership":
            sql((ROOT / "scripts/ui-audit/fixture-leadership.sql").read_text(encoding="utf-8"), schema)
        if args.fixture == "work-records":
            sql("DROP TABLE IF EXISTS biz_work_record_snapshot; DROP TABLE IF EXISTS biz_work_record_entry; DROP TABLE IF EXISTS biz_work_record;", schema)
            sql((ROOT / "scripts/work-records/001_work_records.sql").read_text(encoding="utf-8"), schema)
            sql((ROOT / "scripts/work-records/002_work_record_delete.sql").read_text(encoding="utf-8"), schema)
            sql("UPDATE sys_user SET password='WorkRecords123';", schema)
            sql("INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,status,is_delete) "
                "VALUES(930003,1,930001,2026,'1.1.2','Export synthetic task',3,910001,910002,910002,920001,'1',10,0,'1',0);", schema)
            sql("INSERT INTO sys_dept(dept_id,dept_name,is_delete) VALUES(100,'“双高”建设办公室',0);", schema)
            sql("INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,email,password,role,status,is_delete) "
                "VALUES(910005,100,'audit_office','审计双高办','office@example.invalid','WorkRecords123','1','1',0);", schema)
            env["WORK_RECORDS_VIEWER_USER_IDS"] = "910004"
        if args.fixture in ("business", "review", "convenience", "convenience2", "continuous", "customer"):
            sql((ROOT / "scripts/ui-audit/fixture-business.sql").read_text(encoding="utf-8"), schema)
        if args.fixture in ("review", "continuous", "customer"):
            sql((ROOT / "scripts/ui-audit/fixture-review.sql").read_text(encoding="utf-8"), schema)
        if args.fixture in ("continuous", "customer"):
            sql((ROOT / "scripts/ui-audit/fixture-continuous.sql").read_text(encoding="utf-8"), schema)
        if args.fixture == "customer":
            sql((ROOT / "scripts/ui-audit/fixture-customer.sql").read_text(encoding="utf-8"), schema)
        if args.fixture in ("navigation", "convenience", "convenience2"):
            sql((ROOT / "scripts/ui-audit/fixture-navigation.sql").read_text(encoding="utf-8"), schema)
        if args.fixture in ("convenience", "convenience2"):
            sql((ROOT / "scripts/ui-audit/fixture-convenience.sql").read_text(encoding="utf-8"), schema)
        if args.fixture == "convenience2":
            sql((ROOT / "scripts/ui-audit/fixture-convenience2.sql").read_text(encoding="utf-8"), schema)
        if args.fixture == "task-stats":
            sql((ROOT / "scripts/ui-audit/fixture-task-stats.sql").read_text(encoding="utf-8"), schema)
        if args.fixture in ("ownership-transfer", "ownership-transfer-scale"):
            sql((ROOT / "scripts/ui-audit/fixture-ownership-transfer.sql").read_text(encoding="utf-8"), schema)
        if args.fixture == "ownership-transfer-scale":
            sql((ROOT / "scripts/ui-audit/fixture-ownership-transfer-scale.sql").read_text(encoding="utf-8"), schema)
        if args.fixture == "performance-relation":
            sql((ROOT / "scripts/ui-audit/fixture-performance-relation.sql").read_text(encoding="utf-8"), schema)
        if args.fixture == "level4-equal-weight":
            sql((ROOT / "scripts/ui-audit/fixture-level4-equal-weight.sql").read_text(encoding="utf-8"), schema)
        if args.fixture == "responsibility-filters":
            sql((ROOT / "scripts/ui-audit/fixture-responsibility-filters.sql").read_text(encoding="utf-8"), schema)
        if args.synthetic_scale:
            spec=importlib.util.spec_from_file_location("expanded_fixture",ROOT/"scripts/ui-audit/expanded-fixture.py")
            fixture=importlib.util.module_from_spec(spec);spec.loader.exec_module(fixture)
            for statement in fixture.statements(args.synthetic_scale):sql(statement,schema)
        source=(args.source_root or ROOT).resolve()
        cp = os.pathsep.join([str(WORK), str(source / "target/classes"), str(source / "target/test-classes"),
                              (ROOT / "target/ui-audit-classpath.txt").read_text().strip()])
        subprocess.run([RUNNER.executable("javac"), "-encoding", "UTF-8", "-cp", cp, "-d", str(WORK),
                        str(ROOT / "scripts/ui-audit/IsolatedUiAuditServer.java"),str(ROOT / "scripts/ui-audit/PerformanceProbeConfig.java")], check=True)
        env["SHUANGGAO_TEST_JDBC_URL"] = "jdbc:mysql://127.0.0.1:3306/" + schema + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
        # 所有文件写入留在独立工作目录，避免测试上传和日志覆盖本地业务文件。
        backend_log = open(WORK / "backend.log", "w", encoding="utf-8")
        logs.append(backend_log)
        children.append(subprocess.Popen([RUNNER.executable("java"), *(["-Xms256m","-Xmx768m","-Xlog:gc:file=baseline-gc.log:time,uptime"] if env.get("SHUANGGAO_PERF_METRICS")=="1" else []), "-cp", cp, "org.example.IsolatedUiAuditServer"],
                                          cwd=WORK, env=env, stdout=backend_log, stderr=subprocess.STDOUT,
                                          creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0)))
        frontend = (args.frontend_root or ROOT.parent / "biz").resolve()
        vite = WORK / "vite-audit.mjs"
        if args.production:
            if not (frontend/"dist/index.html").is_file():raise RuntimeError("Production build required")
            vite.write_text("import { preview } from "+json.dumps((ROOT.parent/"biz/node_modules/vite/dist/node/index.js").as_uri())
                +";await preview({root:"+json.dumps(str(frontend))+",preview:{host:'127.0.0.1',port:"+str(args.frontend_port)+",strictPort:true,proxy:{'/api':{target:'http://127.0.0.1:"+str(args.backend_port)+"',changeOrigin:true}}}});",encoding="utf-8")
        else:
            vite.write_text("import { createServer } from " + json.dumps((frontend / "node_modules/vite/dist/node/index.js").as_uri())
                            + "; const server = await createServer({root:" + json.dumps(str(frontend))
                            + ",server:{host:'127.0.0.1',port:" + str(args.frontend_port) + ",strictPort:true,proxy:{'/api':{target:'http://127.0.0.1:"+str(args.backend_port)+"',changeOrigin:true}}}}); await server.listen();", encoding="utf-8")
        frontend_log = open(WORK / "frontend.log", "w", encoding="utf-8")
        logs.append(frontend_log)
        children.append(subprocess.Popen([RUNNER.executable("node"), str(vite)], cwd=WORK,
                                          stdout=frontend_log, stderr=subprocess.STDOUT,
                                          creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0)))
        comparison=None
        if args.comparison_backend_root:
            if not args.production or not args.comparison_frontend_root:raise RuntimeError("Comparison requires two production builds")
            for port in (args.comparison_backend_port,args.comparison_frontend_port):
                if port in (args.frontend_port,args.backend_port) or not 1024<=port<=65535:raise RuntimeError("Invalid comparison ports")
                with socket.socket() as probe:probe.bind(("127.0.0.1",port))
            second=args.comparison_backend_root.resolve();second_front=args.comparison_frontend_root.resolve()
            if not (second_front/"dist/index.html").is_file():raise RuntimeError("Comparison frontend must be built")
            second_env=dict(env,SHUANGGAO_UI_BACKEND_PORT=str(args.comparison_backend_port));second_cp=os.pathsep.join([str(WORK),str(second/"target/classes"),str(second/"target/test-classes"),(ROOT/"target/ui-audit-classpath.txt").read_text().strip()])
            second_log=open(WORK/"comparison-backend.log","w",encoding="utf-8");logs.append(second_log)
            children.append(subprocess.Popen([RUNNER.executable("java"),*(["-Xms256m","-Xmx768m","-Xlog:gc:file=comparison-gc.log:time,uptime"] if env.get("SHUANGGAO_PERF_METRICS")=="1" else []),"-cp",second_cp,"org.example.IsolatedUiAuditServer"],cwd=WORK,env=second_env,stdout=second_log,stderr=subprocess.STDOUT,creationflags=getattr(subprocess,"CREATE_NO_WINDOW",0)))
            second_vite=WORK/"comparison-vite.mjs";second_vite.write_text("import {preview} from "+json.dumps((ROOT.parent/"biz/node_modules/vite/dist/node/index.js").as_uri())+";await preview({root:"+json.dumps(str(second_front))+",preview:{host:'127.0.0.1',port:"+str(args.comparison_frontend_port)+",strictPort:true,proxy:{'/api':{target:'http://127.0.0.1:"+str(args.comparison_backend_port)+"',changeOrigin:true}}}});",encoding="utf-8")
            second_front_log=open(WORK/"comparison-frontend.log","w",encoding="utf-8");logs.append(second_front_log)
            children.append(subprocess.Popen([RUNNER.executable("node"),str(second_vite)],cwd=WORK,stdout=second_front_log,stderr=subprocess.STDOUT,creationflags=getattr(subprocess,"CREATE_NO_WINDOW",0)))
            comparison="http://127.0.0.1:"+str(args.comparison_frontend_port)
        (WORK / "state.json").write_text(json.dumps({"schema": schema, "pids": [p.pid for p in children],
                                                   "frontend": frontend_url,"comparison":comparison}), encoding="utf-8")
        deadline = time.monotonic() + 90
        while time.monotonic() < deadline:
            if any(p.poll() is not None for p in children):
                raise RuntimeError("Audit service exited; inspect isolated logs")
            try:
                with urllib.request.urlopen(frontend_url + "/api/system/login", timeout=2):
                    pass
            except urllib.error.HTTPError as e:
                if e.code == 405:
                    if comparison:
                        try:
                            urllib.request.urlopen(comparison+"/api/system/login",timeout=2)
                        except urllib.error.HTTPError as second_error:
                            if second_error.code!=405:continue
                        except (OSError,urllib.error.URLError):continue
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
