"""Compare legacy and opt-in read results on exactly the same guarded data."""
import json,os,urllib.request,urllib.parse,math
from pathlib import Path
state_path=Path(os.environ["UI_AUDIT_STATE_FILE"]);state=json.loads(state_path.read_text());assert state["schema"].startswith("biz_review_test_")
base=state["frontend"];new=state["comparison"]
def call(host,path,token=None,data=None):
 headers={"Content-Type":"application/json"}
 if token:headers["Authorization"]=token
 req=urllib.request.Request(host+"/api"+path,headers=headers,data=json.dumps(data).encode() if data else None)
 with urllib.request.urlopen(req,timeout=120) as response:return json.load(response)
token=call(base,"/system/login",data={"user_id":5100000,"password":"review-fixture-password"})["token"]
checks=[]
for domain,filters in {
 "tasks":["f.year=2026","f.year=2026&f.deptId=920002","f.year=2026&f.progressFrom=50&f.progressTo=100","f.year=2025"],
 "performance":["f.year=2026","f.year=2026&f.attainment=met","f.year=2026&f.ratioFrom=50"],
 "achievements":["f.year=2026","f.year=2026&f.auditStatus=30","f.year=2026&f.keyword=Synthetic"]}.items():
 for condition in filters:
  old=call(base,"/list-filters/"+domain+"?"+condition,token);current=call(new,"/list-filters/"+domain+"?responseMode=tree&separateOptions=true&"+condition,token)
  assert old["ids"]==current["matchedIds"],(domain,condition,"IDs/order")
  assert old["total"]==current["total"],(domain,condition,"total")
  checks.append(domain+":"+condition)
for domain,filters in {
 "records":["","f.year=2026&f.keyword=Distinct","f.root=930000","f.updateTimeFrom=2026-09-01"],
 "admin-users":["","f.roles=USER&f.duties=yes","f.personal=yes","f.expert=no","f.keyword=Synthetic"],
 "admin-operations":["","f.module=TASK","f.keyword=Synthetic","f.createTimeFrom=2026-09-01"],
 "budget-versions":["","f.month=10","f.effective=yes","f.createdBy=110228","f.createTimeFrom=2026-09-01"]}.items():
 for condition in filters:
  for page in (1,2,99):
   path="/list-filters/"+domain+"?pageSize=10&page="+str(page)+"&"+condition
   old=call(base,path,token);current=call(new,path+"&responseMode=page&includeFields=false&separateOptions=true",token)
   for key in ("items","total","page","pageSize"):assert old[key]==current[key],(domain,condition,page,key)
   checks.append(domain+":"+condition+":page"+str(page))
report={"schema":state["schema"],"checks":checks,"count":len(checks),"allEqual":True};state_path.with_name("compatibility-results.json").write_text(json.dumps(report,indent=2),encoding="utf-8");print(json.dumps({"checks":len(checks),"allEqual":True}))
