"""Validate the OR-join replacement on an owned disposable schema, without data writes."""
import json,os,re,shutil,subprocess,time,statistics
from pathlib import Path
root=Path(__file__).resolve().parents[2];state_path=Path(os.environ["UI_AUDIT_STATE_FILE"]);state=json.loads(state_path.read_text());schema=state["schema"];assert re.fullmatch(r"biz_review_test_[0-9a-f]{32}",schema)
mysql=shutil.which("mysql") or "C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe";env=dict(os.environ,MYSQL_PWD=os.environ["SHUANGGAO_TEST_DB_PASSWORD"])
def sql(query):
 p=subprocess.run([mysql,"-h127.0.0.1","-uroot","--default-character-set=utf8mb4","--batch","--raw","--skip-column-names",schema],input=query.encode(),env=env,capture_output=True,check=True);return p.stdout.decode("utf-8",errors="replace")
ids=sql("SELECT task_id FROM biz_task WHERE level=3 AND phase=2026 AND COALESCE(is_delete,0)=0 ORDER BY task_id").split();literal=",".join(str(int(i)) for i in ids)
old=(root/"scripts/ui-audit/legacy-task-state.sql").read_text().replace("${ids}",literal)
source=(root/"src/main/java/org/example/mapper/WorkShortcutMapper.java").read_text(encoding="utf-8");start=source.index('@Select("<script>SELECT DISTINCT link.task_id');end=source.index('List<Map<String, Object>> taskStates',start)
new="".join(re.findall(r'"([^"\\]*(?:\\.[^"\\]*)*)"',source[start:end]));new=new.replace("<script>","").replace("</script>","").replace("&lt;","<");new=re.sub(r"<foreach.*?</foreach>","("+literal+")",new)
original=sql(old).splitlines();optimized=sql(new).splitlines();assert set(original)==set(optimized) and len(original)==len(optimized)
report={"schema":schema,"tasks":len(ids),"rows":len(original),"equalSetAndCardinality":True,"paths":{}}
for label,query in (("legacy",old),("indexedRelationships",new)):
 durations=[]
 for i in range(3):
  start=time.perf_counter();sql(query);durations.append(round((time.perf_counter()-start)*1000,2))
 report["paths"][label]={"clientMs":durations,"medianClientMs":statistics.median(durations),"actualPlan":sql("EXPLAIN ANALYZE "+query)}
state_path.with_name("task-state-query-comparison.json").write_text(json.dumps(report,indent=2),encoding="utf-8");print(json.dumps({"tasks":len(ids),"rows":len(original),"equal":True,"medianClientMs":{k:v["medianClientMs"] for k,v in report["paths"].items()}}))
