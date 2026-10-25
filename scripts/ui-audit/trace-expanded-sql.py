"""Read-only execution plans on the caller's guarded isolated workload schema."""
import json,os,re,shutil,subprocess
from pathlib import Path
state_path=Path(os.environ["UI_AUDIT_STATE_FILE"]);state=json.loads(state_path.read_text());schema=state["schema"];assert re.fullmatch(r"biz_review_test_[0-9a-f]{32}",schema)
root=Path(__file__).resolve().parents[2];mysql=shutil.which("mysql") or "C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe";env=dict(os.environ,MYSQL_PWD=os.environ["SHUANGGAO_TEST_DB_PASSWORD"])
def sql(text):
 result=subprocess.run([mysql,"-h127.0.0.1","-uroot","--default-character-set=utf8mb4","--batch","--raw","--skip-column-names",schema],input=text.encode(),env=env,capture_output=True)
 if result.returncode:raise RuntimeError(result.stderr.decode("utf-8",errors="replace"))
 return result.stdout.decode("utf-8",errors="replace")
stale=(root/"src/main/resources/sql/stale-review-notices.sql").read_text(encoding="utf-8").strip()
queries={
 "accounts":"SELECT u.user_id,u.user_name,u.nick_name,u.role,u.status FROM sys_user u WHERE COALESCE(u.is_delete,0)=0 ORDER BY u.user_id LIMIT 10",
 "messages":"SELECT n.* FROM sys_notice n WHERE n.to_user_id=5100000 AND COALESCE(n.is_delete,0)=0 AND NOT(COALESCE(("+stale+"),0)) ORDER BY n.create_time DESC LIMIT 10",
 "records":"SELECT r.record_id,r.record_year,r.record_month,r.owner_id FROM biz_work_record r WHERE r.delete_marker=0 ORDER BY r.record_year DESC,r.record_month DESC,r.owner_id,r.record_id LIMIT 10",
 "performanceAudit":"SELECT s.sub_id,p.perf_name,s.actual_value FROM biz_performance_submission s JOIN biz_performance p ON s.perf_id=p.perf_id WHERE s.sub_id IN(4200001,4200061,4200121)",
 "tasks":"SELECT task_id,phase,dept_id,status,progress FROM biz_task WHERE level=3 AND phase=2026 AND COALESCE(is_delete,0)=0"
}
report={"schema":schema,"mysql":sql("SELECT VERSION()" ).strip(),"counts":{},"plans":{}}
for table in ["sys_user","biz_task","biz_material_submission","biz_performance","biz_performance_submission","biz_achievement","sys_notice","biz_work_record","sys_admin_operation_log","biz_budget_import_version","biz_core_report","biz_core_audit_log","biz_budget_task_item","biz_budget_source_item"]:report["counts"][table]=int(sql("SELECT COUNT(*) FROM "+table).strip())
for name,query in queries.items():report["plans"][name]={"explain":sql("EXPLAIN FORMAT=JSON "+query),"actual":sql("EXPLAIN ANALYZE "+query)}
state_path.with_name("execution-plans.json").write_text(json.dumps(report,indent=2),encoding="utf-8");print(json.dumps(report["counts"]))
