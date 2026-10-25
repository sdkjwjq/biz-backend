"""Readonly endpoint concurrency runs against one disposable production-preview server."""
import concurrent.futures,json,math,os,statistics,time,urllib.request,platform
from pathlib import Path
state_path=Path(os.environ["UI_AUDIT_STATE_FILE"]);state=json.loads(state_path.read_text());base=state["frontend"]
assert state["schema"].startswith("biz_review_test_") and base.startswith("http://127.0.0.1:")
label=os.environ.get("PERF_BUILD","optimized")
if label=="optimized" and state.get("comparison"):base=state["comparison"]
def call(path,token=None,data=None):
    headers={"Content-Type":"application/json"}
    if token:headers["Authorization"]=token
    request=urllib.request.Request(base+"/api"+path,headers=headers,data=json.dumps(data).encode() if data else None)
    start=time.perf_counter()
    with urllib.request.urlopen(request,timeout=120) as response:
        body=response.read();metrics={key.lower():value for key,value in response.headers.items()}
    result=json.loads(body)
    if isinstance(result,dict) and int(result.get("code",200))>=400:raise RuntimeError(path+": "+str(result.get("code")))
    if isinstance(result,dict) and any(section.get("status")=="error" for section in result.get("modules",{}).values()):raise RuntimeError(path+": overview module error")
    return {"ms":(time.perf_counter()-start)*1000,"bytes":len(body),"sql":float(metrics.get("x-perf-sql-count",-1)),"sqlMs":float(metrics.get("x-perf-sql-ms",-1)),"serverMs":float(metrics.get("x-perf-server-ms",-1))},result
tokens=[call("/system/login",data={"user_id":5100000+i,"password":"review-fixture-password"})[1]["token"] for i in range(30)]
token=tokens[0]
workloads={
 "accounts":"/list-filters/admin-users?responseMode=page&includeFields=false&pageSize=10",
 "accountRoles":"/list-filters/admin-users?responseMode=page&includeFields=false&pageSize=10&f.roles=USER&f.duties=yes",
 "auditTodo":"/list-filters/audits?tab=todo&pageSize=10"+("&responseMode=page&includeFields=false" if label!="baseline" else ""),
 "auditHistory":"/list-filters/audits?tab=done&pageSize=10"+("&responseMode=page&includeFields=false" if label!="baseline" else ""),
 "messages":"/list-filters/notices?pageSize=10"+("&responseMode=page&includeFields=false" if label!="baseline" else ""),
 "versions":"/list-filters/budget-versions?pageSize=10"+("&responseMode=page&separateOptions=true" if label!="baseline" else ""),
 "recordText":"/list-filters/records?responseMode=page&includeFields=false&pageSize=10&f.year=2026&f.keyword=Distinct",
 "tasks":"/list-filters/tasks?f.year=2026"+("&responseMode=tree&separateOptions=true" if label!="baseline" else ""),
 "performance":"/list-filters/performance?f.year=2026"+("&responseMode=tree&separateOptions=true" if label!="baseline" else ""),
 "achievements":"/list-filters/achievements?f.year=2026"+("&responseMode=tree&separateOptions=true" if label!="baseline" else ""),
 "expert":"/expert-reviews?year=2026&pageSize=20",
 "supervision":"/list-filters/supervision?f.year=2026"+("&responseMode=tree&separateOptions=true" if label!="baseline" else ""),
 "core":"/list-filters/core?f.year=2026&f.month=10"+("&responseMode=page&separateOptions=true" if label!="baseline" else ""),
 "budget":"/list-filters/budget?f.year=2026&f.month=10",
 "operations":"/list-filters/admin-operations?responseMode=page&includeFields=false&pageSize=10",
 "departments":"/list-filters/admin-departments?pageSize=10",
 "roles":"/list-filters/admin-roles?pageSize=10",
 "taskOptions":("/list-filters/tasks/filter-options?field=leaderId&f.year=2026&pageSize=50" if label!="baseline" else "/list-filters/tasks?f.year=2026"),
 "overview":"/admin/overview/summary?year=2026&month=10&module=tasks",
 "overviewPerformance":"/admin/overview/summary?year=2026&month=10&module=performance",
 "overviewCore":"/admin/overview/summary?year=2026&month=10&module=core",
 "overviewAchievements":"/admin/overview/summary?year=2026&month=10&module=achievements",
 "overviewBudget":"/admin/overview/summary?year=2026&month=10&module=budget",
 "overviewRecords":"/admin/overview/summary?year=2026&month=10&module=records",
 "overviewAnalysis":"/admin/overview/analysis?year=2026&month=10&metric=tasks.total&page=2&pageSize=10",
 "overviewRecordAnalysis":"/admin/overview/analysis?year=2026&month=10&metric=records.expected&page=2&pageSize=10"
}
def percentile(values,p):return sorted(values)[max(0,math.ceil(len(values)*p)-1)]
report={"build":label,"environment":"Loopback production preview; synthetic isolated schema; no user or production data changed","schema":state["schema"],"distinctAccounts":30,"cpuCount":os.cpu_count(),"platform":platform.platform(),"results":[]}
for name,path in workloads.items():
    cold,body=call(path,token);call(path,token)
    for concurrency in (1,10,30):
        count=30
        with concurrent.futures.ThreadPoolExecutor(max_workers=concurrency) as pool:
            rows=list(pool.map(lambda i:call(path,tokens[i%concurrency])[0],range(count)))
        ms=[r["ms"] for r in rows]
        result={"workload":name,"concurrency":concurrency,"samples":count,"coldMs":round(cold["ms"],2),"p50Ms":round(statistics.median(ms),2),"p95Ms":round(percentile(ms,.95),2),"bytes":round(statistics.mean(r["bytes"] for r in rows)),"sqlCount":round(statistics.mean(r["sql"] for r in rows),2),"sqlMs":round(statistics.mean(r["sqlMs"] for r in rows),2),"total":body.get("total") if isinstance(body,dict) else len(body)}
        report["results"].append(result);print(json.dumps(result),flush=True)
        state_path.with_name("performance-"+label+".json").write_text(json.dumps(report,indent=2),encoding="utf-8")
