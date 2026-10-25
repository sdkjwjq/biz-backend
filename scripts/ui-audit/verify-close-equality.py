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
  old=call(base,"/list-filters/"+domain+"?responseMode=tree&separateOptions=true&"+condition,token);current=call(new,"/list-filters/"+domain+"?responseMode=tree&separateOptions=true&"+condition,token)
  assert old["matchedIds"]==current["matchedIds"],(domain,condition,"IDs/order")
  assert old["total"]==current["total"],(domain,condition,"total")
  checks.append(domain+":"+condition)
for domain,filters in {
 "audits":["tab=todo","tab=done","tab=done&f.type=task","tab=done&f.submitBy=5000000","tab=done&f.submitTimeFrom=2026-09-01"],
 "records":["","f.year=2026&f.keyword=Distinct","f.root=930000","f.updateTimeFrom=2026-09-01"],
 "admin-users":["","f.roles=USER&f.duties=yes","f.personal=yes","f.expert=no","f.keyword=Synthetic"],
 "admin-operations":["","f.module=TASK","f.keyword=Synthetic","f.createTimeFrom=2026-09-01"],
 "budget-versions":["","f.month=10","f.effective=yes","f.createdBy=110228","f.createTimeFrom=2026-09-01"]}.items():
 for condition in filters:
  for page in (1,2,99):
   path="/list-filters/"+domain+"?pageSize=10&page="+str(page)+"&"+condition
   old=call(base,path+"&responseMode=page&includeFields=false&separateOptions=true",token);current=call(new,path+"&responseMode=page&includeFields=false&separateOptions=true",token)
   for key in ("items","total","page","pageSize"):assert old[key]==current[key],(domain,condition,page,key)
   checks.append(domain+":"+condition+":page"+str(page))
for field in ['leaderId','deptId','auditorId','category','root']:
 path='/list-filters/performance/filter-options?field='+field+'&f.year=2026&pageSize=50'
 assert call(base,path,token)==call(new,path,token),('performance-options',field)
 checks.append('performance-options:'+field)
for actor in (5000000,5200000,5300000,5400000,5500000):
 token=call(base,"/system/login",data={"user_id":actor,"password":"review-fixture-password"})["token"]
 domains=["tasks","performance"] if actor<5400000 else ["tasks"] if actor<5500000 else ["budget-versions"]
 for domain in domains:
  mode="page&includeFields=false" if domain=="budget-versions" else "tree"
  path="/list-filters/"+domain+"?responseMode="+mode+"&separateOptions=true&f.year=2026"
  a=call(base,path,token);b=call(new,path,token)
  assert a==b,(actor,domain,"authorized results")
  checks.append(str(actor)+":"+domain)
 audit_path='/list-filters/audits?responseMode=page&includeFields=false&tab=done&pageSize=10'
 try:previous=call(base,audit_path,token)
 except urllib.error.HTTPError as old_error:
  assert old_error.code==403
  try:call(new,audit_path,token);raise AssertionError('Unauthorized audit history became visible')
  except urllib.error.HTTPError as new_error:assert new_error.code==403
 else:assert previous==call(new,audit_path,token),(actor,'audit history scope and wire fields')
 checks.append(str(actor)+':audit-history-scope')
 if actor<5400000:
  option_path='/list-filters/performance/filter-options?field=leaderId&f.year=2026&pageSize=50'
  assert call(base,option_path,token)==call(new,option_path,token),(actor,'performance options scope')
  checks.append(str(actor)+':performance-options-scope')
 if actor in (5200000,5300000):
  for module in ("tasks","performance","records"):
   a=call(base,"/admin/overview/summary?year=2026&month=10&module="+module,token);b=call(new,"/admin/overview/summary?year=2026&month=10&module="+module,token)
   assert a["modules"]==b["modules"],(actor,module,"summary")
   checks.append(str(actor)+":overview:"+module)
  for metric in ("tasks.total","performance.total","records.expected"):
   path="/admin/overview/analysis?year=2026&month=10&page=2&pageSize=10&metric="+metric
   a=call(base,path,token);b=call(new,path,token)
   assert a==b,(actor,metric,"analysis")
   checks.append(str(actor)+":analysis:"+metric)
report={"schema":state["schema"],"checks":checks,"count":len(checks),"allEqual":True};state_path.with_name("close-compatibility-results.json").write_text(json.dumps(report,indent=2),encoding="utf-8");print(json.dumps({"checks":len(checks),"allEqual":True}))
