package org.example;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.jdbc.core.*;
import org.springframework.test.context.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="SHUANGGAO_REVIEW_TEST",matches="true")
@Import(ReviewBatchRegressionApiTest.NoAutomaticSchedules.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"logging.file.name=target/expert-test.log","logging.level.org.springframework=WARN"})
class ExpertReviewApiTest {
 @DynamicPropertySource static void database(DynamicPropertyRegistry r){String url=System.getenv("SHUANGGAO_TEST_JDBC_URL");if(url==null||!url.matches("jdbc:mysql://127\\.0\\.0\\.1:3306/biz_review_test_[0-9a-f]{32}\\?.*"))throw new IllegalStateException("Isolated schema required");r.add("spring.datasource.url",()->url);r.add("spring.datasource.username",()->System.getenv("SHUANGGAO_TEST_DB_USER"));r.add("spring.datasource.password",()->System.getenv("SHUANGGAO_TEST_DB_PASSWORD"));}
 @LocalServerPort int port;@Autowired TestRestTemplate http;@Autowired JdbcTemplate jdbc;@Autowired ObjectMapper json;
 String admin,a,b,owner;
 @BeforeEach void seed()throws Exception{
  jdbc.execute((ConnectionCallback<Void>)c->{assertTrue(c.getCatalog().matches("biz_review_test_[0-9a-f]{32}"));try(Statement s=c.createStatement()){List<String> tables=new ArrayList<>();try(ResultSet rs=s.executeQuery("SHOW FULL TABLES WHERE Table_type='BASE TABLE'")){while(rs.next())tables.add(rs.getString(1));}s.execute("SET FOREIGN_KEY_CHECKS=0");for(String t:tables)s.executeUpdate("DELETE FROM `"+t+"`");s.execute("SET FOREIGN_KEY_CHECKS=1");}return null;});
  jdbc.update("INSERT INTO sys_permission_revision VALUES(1,0)");jdbc.update("INSERT INTO sys_dept(dept_id,dept_name,status,is_delete) VALUES(991001,'合成部门甲','0',0),(991002,'合成部门乙','0',0)");
  for(long id:List.of(110228L,90101L,90102L,90103L,90104L))jdbc.update("INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,password,role,status,is_delete,force_password_change) VALUES(?,991001,?,?,'ExpertTest123',?,'1',0,0)",id,"expert-test-"+id,"合成用户"+id,id==110228?"0":"1");jdbc.update("INSERT INTO biz_project(project_id,project_name,leader_id) VALUES(1,'隔离专家项目',110228)");
  for(int id=1;id<=5;id++)jdbc.update("INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,dept_id,leader_id,auditor_id,principal_id,target_value,current_value,progress,status,is_delete) VALUES(?,1,?,?,?, ?,?,?,90101,90102,90102,10,1,10,'1',0)",id,id==1?0:id==2?1:2,id==5?2025:2026,"EXPERT"+id,"合成评审任务"+id,id<3?id:3,id==4?991002:991001);
  jdbc.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json) VALUES(90103,'CUSTOM','{}','{\"tasks\":{\"mode\":\"CUSTOM\",\"items\":[3]}}'),(90104,'CUSTOM','{}','{}')");jdbc.update("INSERT INTO sys_user_permission_role VALUES(90103,'EXPERT'),(90104,'EXPERT')");admin=login(110228);a=login(90103);b=login(90104);owner=login(90101);
 }
 ResponseEntity<String> call(HttpMethod method,String path,String token,Object data,boolean test){HttpHeaders h=headers(token,test);return http.exchange("http://127.0.0.1:"+port+"/api"+path,method,new HttpEntity<>(data,h),String.class);}
 HttpHeaders headers(String token,boolean test){HttpHeaders h=new HttpHeaders();if(token!=null)h.set("Authorization",token);h.setContentType(MediaType.APPLICATION_JSON);if(test){h.set("X-Admin-Test","1");h.set("X-Admin-Test-Reason",java.net.URLEncoder.encode("隔离专家代填验证",java.nio.charset.StandardCharsets.UTF_8));}return h;}
 ResponseEntity<String> get(String path,String token){return call(HttpMethod.GET,path,token,null,false);}
 JsonNode body(ResponseEntity<String> r)throws Exception{assertEquals(200,r.getStatusCode().value(),r.getBody());return json.readTree(r.getBody());}
 String login(long id)throws Exception{return body(call(HttpMethod.POST,"/system/login",null,Map.of("user_id",id,"password","ExpertTest123"),false)).get("token").asText();}
 JsonNode detail(long task,String actor)throws Exception{return body(get("/expert-reviews/tasks/"+task+"?year=2026",actor));}
 Map<String,Object> input(long task,String text,JsonNode detail){return new LinkedHashMap<>(Map.of("taskId",task,"year",2026,"version",detail.path("version").asLong(),"text",text,"scopeToken",detail.path("scopeToken").asText()));}
 JsonNode submit(long task,String text,String actor)throws Exception{return body(call(HttpMethod.POST,"/expert-reviews/submit",actor,input(task,text,detail(task,actor)),false));}
 @Autowired org.example.service.PermissionService permissionService;
 @Autowired org.example.mapper.SysMapper accountMapper;
 @Test void accountOptionEndpointMatchesExistingFieldsAndProtectsAccess()throws Exception{
  var legacy=body(get("/list-filters/admin-users",admin));var options=body(get("/list-filters/admin-users/filter-options",admin));
  assertEquals(legacy.path("fields"),options.path("fields"));assertEquals(1,options.size());
  assertEquals(403,get("/list-filters/admin-users/filter-options",owner).getStatusCode().value());
 }
 @Test void pagedAccountSqlMatchesLegacyFiltersAndKeepsAuthorization()throws Exception{
  jdbc.update("UPDATE sys_user SET nick_name='MiXeD %_ name',force_password_change=1 WHERE user_id=90101");
  for(String filter:List.of("","f.status=1","f.status=0","f.deptId=991001&f.deptId=991002","f.deptId=bad","f.keyword=MiXeD","f.keyword=%25_","f.role=0","f.force=yes","f.force=no","f.leadership=NONE","f.roles=EXPERT","f.personal=yes","f.duties=yes")){
   String url="/list-filters/admin-users?page=2&pageSize=2&"+filter;
   var legacy=body(get(url,admin));var paged=body(get(url+"&responseMode=page&includeFields=false",admin));
   assertEquals(legacy.path("total"),paged.path("total"),filter);assertEquals(legacy.path("page"),paged.path("page"),filter);
   assertEquals(legacy.path("items"),paged.path("items"),filter);assertFalse(paged.has("ids"));
  }
  assertEquals(403,get("/list-filters/admin-users?responseMode=page&includeFields=false",owner).getStatusCode().value());
  for(String invalid:List.of("f.status=9","f.keyword=x&f.keyword=y","f.deptIdFrom=1","f.password=x","pageSize=0"))assertEquals(400,get("/list-filters/admin-users?responseMode=page&includeFields=false&"+invalid,admin).getStatusCode().value());
 }
 @Test void batchAccountPermissionsMatchUnbatchedEvaluation()throws Exception{
  for(int i=0;i<50;i++)jdbc.update("INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,password,role,status,is_delete) VALUES(?,991001,?,?,'synthetic','1','1',0)",980000+i,"perf-"+i,"Performance fixture "+i);
  var accounts=accountMapper.getAllUsers();var before=new LinkedHashMap<Long,Object>();var after=new LinkedHashMap<Long,Object>();long baseline,optimized;
  try{
   var request=new org.springframework.mock.web.MockHttpServletRequest("GET","/list-filters/admin-users");request.addHeader("Authorization",admin);
   org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(request));
   long start=System.nanoTime();for(var user:accounts)before.put(user.getUserId(),List.of(permissionService.assignedRoleCodes(user),permissionService.policy(user.getUserId())));baseline=System.nanoTime()-start;
   var next=new org.springframework.mock.web.MockHttpServletRequest("GET","/list-filters/admin-users");next.addHeader("Authorization",admin);
   org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(next));
   start=System.nanoTime();permissionService.prepareAccountList(accounts);for(var user:accounts)after.put(user.getUserId(),List.of(permissionService.assignedRoleCodes(user),permissionService.policy(user.getUserId())));optimized=System.nanoTime()-start;
   assertEquals(before,after);
   System.out.println("ACCOUNT_BATCH_BASELINE users="+accounts.size()+" baseline_ms="+baseline/1_000_000+" optimized_ms="+optimized/1_000_000);
  }finally{org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();}
 }
 @Test void taskFilterExportRechecksAuthorizationAndExportsOnlyMatchingLeaves()throws Exception{
  jdbc.update("UPDATE sys_user_permission SET permissions_json='{\"tasks.export\":true}' WHERE user_id=90103");
  var response=http.exchange("http://127.0.0.1:"+port+"/api/list-filters/tasks/export?f.year=2026",HttpMethod.GET,new HttpEntity<>(null,headers(a,false)),byte[].class);assertEquals(200,response.getStatusCode().value());
  try(var book=new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(response.getBody()))){assertEquals(1,book.getSheetAt(0).getLastRowNum());assertEquals("EXPERT3",book.getSheetAt(0).getRow(1).getCell(2).getStringCellValue());}
  assertEquals(400,get("/list-filters/tasks/export?f.deptId=991002",a).getStatusCode().value());
  jdbc.update("UPDATE sys_user_permission SET permissions_json='{\"tasks.export\":false}' WHERE user_id=90103");assertEquals(403,get("/list-filters/tasks/export",a).getStatusCode().value());
 }
 @Test void administrativeFiltersArePrivateAndRespectRoleAssignments()throws Exception{
  for(String domain:List.of("admin-users","admin-roles"))assertEquals(403,get("/list-filters/"+domain,a).getStatusCode().value());
  var experts=body(get("/list-filters/admin-users?f.roles=EXPERT",admin));assertEquals(2,experts.path("total").asInt());assertFalse(experts.toString().contains("ExpertTest123"));assertFalse(experts.toString().contains("password"));
  assertEquals(2,body(get("/list-filters/admin-users?f.expert=yes&f.role=1",admin)).path("total").asInt());
  var assigned=body(get("/list-filters/admin-roles?f.assigned=yes&f.keyword=EXPERT",admin));assertEquals(1,assigned.path("total").asInt());
  jdbc.update("UPDATE sys_user SET status='0' WHERE user_id=90103");assertEquals(1,body(get("/list-filters/admin-users?f.roles=EXPERT&f.status=0",admin)).path("total").asInt());
 }
 @Test void overviewFiltersApplyToBothChartAndDetail()throws Exception{
  var data=body(get("/admin/overview/analysis?year=2026&month=10&metric=tasks.total&f.department=合成部门甲",admin));assertEquals(1,data.path("total").asInt());assertEquals(1,data.path("rows").size());assertEquals(1,data.path("chart").get(0).path("value").asInt());
  assertEquals(400,get("/admin/overview/analysis?year=2026&month=10&metric=tasks.total&f.notAllowed=1",admin).getStatusCode().value());
 }
 @Test void budgetFiltersUseWholeExpenseAndOriginalDenominator()throws Exception{
  jdbc.update("INSERT INTO biz_budget_sheet(sheet_id,year,month,locked,is_delete) VALUES(1,2026,10,1,0)");
  jdbc.update("INSERT INTO biz_budget_source_item(sheet_id,source_key,source_name,display_order,annual_plan) VALUES(1,'school','学校资金',1,100)");
  jdbc.update("INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other) VALUES(1,1,'改革任务一',100,20,30,0),(1,2,'改革任务二',0,0,0,0)");
  var filtered=body(get("/list-filters/budget?f.year=2026&f.month=10&f.expense=goods&f.expense=capital&f.ratioFrom=50&f.ratioTo=50",admin));assertEquals(1,filtered.path("ids").size());assertEquals(1,filtered.path("ids").get(0).asInt());assertEquals(2,filtered.path("budget").path("tasks").size());
  assertTrue(filtered.path("imported").asBoolean());assertFalse(body(get("/list-filters/budget?f.year=2026&f.month=9",admin)).path("imported").asBoolean());assertEquals(403,get("/list-filters/budget",a).getStatusCode().value());assertEquals(400,get("/list-filters/budget?f.month=13",admin).getStatusCode().value());
 }
 @Autowired org.example.service.AuditHistoryTimeService historyTimes;
 @Test void boundedAuditTimesMatchFullAggregationAndIgnoreOtherRecords()throws Exception{
  jdbc.update("INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by,is_delete) VALUES(41,'audit.pdf','/target/audit','/uploads/audit','pdf',90101,0)");
  jdbc.update("INSERT INTO biz_material_submission(sub_id,task_id,file_id,reported_value,submit_by,submit_dept_id,manage_dept_id,file_suffix,flow_status,is_delete,submit_time) VALUES(31,3,41,1,90101,991001,991001,'pdf',40,0,'2026-10-01'),(32,4,41,1,90101,991002,991002,'pdf',40,0,'2026-10-01')");
  jdbc.update("INSERT INTO biz_audit_log(sub_id,operator_id,action_type,pre_status,post_status,comment,create_time) VALUES(31,110228,'pass',10,20,'','2026-10-01'),(31,110228,'REJECT',20,-20,'','2026-10-02'),(31,110228,'submit',0,10,'','2026-10-03'),(32,110228,'pass',30,40,'','2026-10-05')");
  Map<Long,Object> expected=new HashMap<>();
  for(var row:jdbc.queryForList("SELECT sub_id id,MAX(create_time) handledTime FROM biz_audit_log WHERE LOWER(action_type) IN('pass','reject') GROUP BY sub_id"))if(((Number)row.get("id")).longValue()==31)expected.put(31L,row.get("handledTime"));
  assertEquals(expected,historyTimes.latest("tasks",List.of(31L,31L,999L)));
  assertTrue(historyTimes.latest("tasks",List.of()).isEmpty());
  jdbc.update("INSERT INTO biz_core_audit_log(report_id,operator_id,action,pre_status,post_status,comment,create_time) VALUES(31,110228,'通过',10,20,'','2026-10-01'),(31,110228,'退回',20,-20,'','2026-10-02'),(31,110228,'提交',0,10,'','2026-10-03'),(32,110228,'通过',20,30,'','2026-10-05')");
  var coreTime=jdbc.queryForObject("SELECT MAX(create_time) FROM biz_core_audit_log WHERE report_id=31 AND action IN('通过','退回')",Object.class);
  assertEquals(Map.of(31L,coreTime),historyTimes.latest("core",List.of(31L,31L,999L)));
  assertThrows(IllegalArgumentException.class,()->historyTimes.latest("not-a-domain",List.of(31L)));

  var filtered=body(get("/list-filters/audits?tab=done&f.type=task&f.handledTimeFrom=2026-10-02&f.handledTimeTo=2026-10-02",admin));
  assertEquals(1,filtered.path("total").asInt());assertEquals("task:31",filtered.path("ids").get(0).asText());
 }
 @Autowired org.example.mapper.AdminManagementMapper auditDepartments;
 @Test void lightweightAuditDepartmentNamesMatchManagementProjection(){
  jdbc.update("INSERT INTO sys_dept(dept_id,dept_name,status,is_delete) VALUES(991003,'Deleted fixture','0',1),(991004,'Disabled fixture','1',0)");
  var expected=auditDepartments.departments().stream().map(d->Map.of("deptId",d.get("deptId"),"deptName",d.get("deptName"))).toList();
  assertEquals(expected,auditDepartments.departmentNames());
 }
 @Autowired org.example.service.BizService auditProjection;
 @Test void batchAuditProjectionMatchesSingleRecordQueries()throws Exception{
  jdbc.update("INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by,is_delete) VALUES(41,'retained.pdf','/target/retained','/uploads/retained','pdf',90101,1)");
  jdbc.update("INSERT INTO biz_level4_task(task_id,parent_id,phase,task_name,leader_id,dept_id,data_type,target_value,current_value,progress,status,is_delete) VALUES(101,3,2026,'First',90101,991001,'0',10,15,100,'1',0),(102,3,2026,'Second',90101,991001,'0',10,5,50,'1',NULL),(103,3,2026,'Deleted',90101,991001,'0',10,9,90,'1',1)");
  var source=new ArrayList<org.example.entity.BizMaterialSubmission>();
  for(int i=0;i<20;i++){var item=new org.example.entity.BizMaterialSubmission();item.setSubId((long)i);item.setFileId(41L);item.setTaskId(i%2==0?3L:4L);source.add(item);}
  var original=source.stream().map(auditProjection::auditToAuditVo).toList();
  var batched=auditProjection.auditListToAuditVoList(source);
  assertEquals(json.valueToTree(original),json.valueToTree(batched));
  assertEquals(2,batched.get(0).getSub_list().size());assertEquals(101L,batched.get(0).getSub_list().get(0).getTask_id());
  assertTrue(batched.get(1).getSub_list().isEmpty());
 }
 @Test void auditFiltersRespectCurrentHandlerAndCannotExposeOtherModules()throws Exception{
  jdbc.update("INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by,is_delete) VALUES(41,'testA.pdf','/target/testA','/uploads/testA','pdf',90101,0),(42,'testB.pdf','/target/testB','/uploads/testB','pdf',90101,0)");
  jdbc.update("INSERT INTO biz_material_submission(sub_id,task_id,file_id,reported_value,submit_by,submit_dept_id,manage_dept_id,file_suffix,flow_status,current_handler_id,is_delete,submit_time) VALUES(31,3,41,1,90101,991001,991001,'pdf',10,90102,0,'2026-10-01'),(32,4,42,9,90101,991002,991002,'pdf',10,90104,0,'2026-10-02')");
  var reviewer=login(90102);var filtered=body(get("/list-filters/audits?tab=todo&f.type=task&f.submitBy=90101",reviewer));assertEquals(1,filtered.path("total").asInt());assertEquals("task:31",filtered.path("ids").get(0).asText());assertEquals(403,get("/list-filters/audits?tab=todo",a).getStatusCode().value());
  jdbc.update("UPDATE biz_material_submission SET current_handler_id=90104 WHERE sub_id=31");assertEquals(0,body(get("/list-filters/audits?tab=todo",reviewer)).path("total").asInt());
 }
 @Test void listFiltersCombineScopeYearMultiSelectAndProgressBeforeCounting()throws Exception{
  var allowed=body(get("/list-filters/tasks?f.year=2026&f.deptId=991001&f.deptId=991002&f.progressFrom=10&f.progressTo=10",a));assertEquals(1,allowed.path("total").asInt());assertEquals(3,allowed.path("ids").get(0).asInt());
  assertEquals(0,body(get("/list-filters/tasks?f.year=2026&f.deptId=991002",a)).path("total").asInt());
  assertEquals(2,body(get("/list-filters/tasks?f.year=2026&f.deptId=991001&f.deptId=991002",b)).path("total").asInt());assertFalse(allowed.path("fields").toString().contains("合成部门乙"));
 }
 @Test void listFiltersRejectInvalidRangesEnumsAndUnknownFields()throws Exception{
  for(String query:List.of("f.progressFrom=50&f.progressTo=10","f.progressFrom=nope","f.status=9","f.plannedCompletionDateFrom=2026-02-30","f.password=anything","f.year=2031","f.statusFrom=1","f.status=1&f.status=2","f.plannedCompletionDateFrom=missing","f.plannedCompletionDate=missing&f.plannedCompletionDateFrom=2026-01-01"))assertEquals(400,get("/list-filters/tasks?"+query,admin).getStatusCode().value(),query);
 }
 @Test void listFiltersDoNotTreatMissingDatesAsZero()throws Exception{
  jdbc.update("UPDATE biz_task SET planned_completion_date='2026-10-24',is_priority=1,is_difficult=1 WHERE task_id=3");
  assertEquals(1,body(get("/list-filters/tasks?f.year=2026&f.kind=priority&f.plannedCompletionDateFrom=2026-10-24&f.plannedCompletionDateTo=2026-10-24",admin)).path("total").asInt());
  assertEquals(1,body(get("/list-filters/tasks?f.year=2026&f.plannedCompletionDate=missing",admin)).path("total").asInt());
  assertEquals(0,body(get("/list-filters/tasks?f.year=2026&f.actualCompletionDateFrom=2020-01-01",admin)).path("total").asInt());
 }
 @Test void expertAdvancedFiltersRunBeforePaginationWithoutChangingReviewScope()throws Exception{
  jdbc.update("UPDATE biz_task SET progress=80,is_priority=1 WHERE task_id=3");
  var result=body(get("/expert-reviews?year=2026&f.progressFrom=80&pageSize=1",b));assertEquals(2,result.path("total").asInt());assertEquals(1,result.path("items").size());assertEquals(1,result.path("items").get(0).path("matchedTaskCount").asInt());
  assertEquals(3,body(get("/expert-reviews?year=2026&f.progressFrom=80&f.level=3",b)).path("items").get(0).path("taskId").asInt());assertEquals(2,detail(1,b).path("scope").path("tasks").size());
 }
 @Test void listFiltersRespectRevocationAndDoNotGrantOtherModules()throws Exception{
  assertEquals(403,get("/list-filters/achievements",a).getStatusCode().value());jdbc.update("UPDATE sys_user_permission SET permissions_json='{\"tasks.read\":false}',version=version+1 WHERE user_id=90103");assertEquals(403,get("/list-filters/tasks",a).getStatusCode().value());
 }
 @Test void departmentFiltersRemainAdminOnlyAndUseRealRelationships()throws Exception{
  assertEquals(403,get("/list-filters/admin-departments",a).getStatusCode().value());
  assertEquals(2,body(get("/list-filters/admin-departments?f.missingLeader=yes",admin)).path("total").asInt());
  assertEquals(1,body(get("/list-filters/admin-departments?f.hasUsers=yes&f.hasBusiness=yes",admin)).path("total").asInt());
 }
 @Autowired org.example.service.BudgetVersionListService budgetVersionRows;
 @Test void groupedBudgetVersionsMatchLegacyAndRecheckYearScope()throws Exception{
  int key=100;
  for(int year:List.of(2025,2026))for(int month:List.of(9,10))for(int version=1;version<=30;version++){
   jdbc.update("INSERT INTO biz_budget_import_version(version_id,sheet_id,year,month,version_no,operation_type,snapshot_json,created_by,is_delete) VALUES(?,?,?,?,?,?,'{\"privateSnapshot\":true}',?,?)",key++,year*100+month,year,month,version,version%2==0?"RESTORE":"IMPORT",version%3==0?999999:110228,version==30?1:0);
  }
  var expected=jdbc.queryForList("SELECT v.version_id versionId,v.sheet_id sheetId,v.year,v.month,v.version_no versionNo,v.operation_type operationType,v.source_version_id sourceVersionId,v.file_id fileId,v.file_name fileName,v.created_by createdBy,u.nick_name createdByName,v.create_time createTime,v.remark,(v.version_no=(SELECT MAX(x.version_no) FROM biz_budget_import_version x WHERE x.year=v.year AND x.month=v.month AND x.is_delete=0)) isCurrent FROM biz_budget_import_version v LEFT JOIN sys_user u ON u.user_id=v.created_by WHERE v.is_delete=0 ORDER BY v.year DESC,v.month DESC,v.version_no DESC");
  var optimized=budgetVersionRows.rows(110228L);assertEquals(expected.size(),optimized.size());
  for(int i=0;i<expected.size();i++)for(var field:expected.get(i).entrySet()){
   Object value=optimized.get(i).get(field.getKey());
   assertEquals(field.getValue(),value,field.getKey()+" originalType="+(field.getValue()==null?"null":field.getValue().getClass())+" newType="+(value==null?"null":value.getClass()));
  }
  assertEquals(116,expected.size());
  var current=body(get("/list-filters/budget-versions?f.effective=yes&pageSize=1&page=2",admin));
  assertEquals(4,current.path("total").asInt());assertEquals(29,current.path("items").get(0).path("versionNo").asInt());
  assertFalse(current.toString().contains("privateSnapshot"));
  jdbc.update("UPDATE sys_user_permission SET permissions_json='{\"budget.read\":true,\"budget.versions\":true}',scopes_json='{\"budget\":{\"mode\":\"ALL\",\"years\":[2025]}}' WHERE user_id=90103");
  var limited=body(get("/list-filters/budget-versions?f.effective=yes",a));
  assertEquals(2,limited.path("total").asInt());for(var item:limited.path("items"))assertEquals(2025,item.path("year").asInt());
  jdbc.update("UPDATE sys_user_permission SET scopes_json='{\"budget\":{\"mode\":\"ALL\",\"years\":[2026]}}' WHERE user_id=90103");
  var changed=body(get("/list-filters/budget-versions?f.effective=yes",a));
  assertEquals(2,changed.path("total").asInt());for(var item:changed.path("items"))assertEquals(2026,item.path("year").asInt());
  jdbc.update("UPDATE sys_user_permission SET permissions_json='{\"budget.read\":true,\"budget.versions\":false}' WHERE user_id=90103");
  assertEquals(403,get("/list-filters/budget-versions",a).getStatusCode().value());
 }
 @Test void budgetVersionFiltersKeepCurrentPerMonthBeforePaging()throws Exception{
  for(int n=1;n<=3;n++)jdbc.update("INSERT INTO biz_budget_import_version(version_id,sheet_id,year,month,version_no,operation_type,snapshot_json,created_by,is_delete) VALUES(?,?,2026,?,?,?,'{}',110228,0)",n,n==3?2:1,n==3?9:10,n==3?1:n,n==2?"RESTORE":"IMPORT");
  assertEquals(403,get("/list-filters/budget-versions",a).getStatusCode().value());
  var all=body(get("/list-filters/budget-versions?f.effective=yes&pageSize=1",admin));assertEquals(2,all.path("total").asInt());assertEquals(1,all.path("items").size());assertTrue(all.path("items").get(0).path("current").asBoolean());
  assertEquals(1,body(get("/list-filters/budget-versions?f.month=10&f.effective=no",admin)).path("total").asInt());
 }
 @Test void noticeFiltersUseCurrentBusinessAndNeverOtherRecipients()throws Exception{
  jdbc.update("INSERT INTO biz_core_indicator VALUES(1,'1.1.5',1)");jdbc.update("INSERT INTO biz_core_report(report_id,indicator_id,record_year,record_month,indicator_name,performance_code,submit_by,flow_status,submit_time) VALUES(1,1,2026,10,'合成指标','1.1.5',90101,10,NOW())");
  jdbc.update("INSERT INTO sys_notice(notice_id,from_user_id,to_user_id,type,title,content,source_type,source_id,is_read,is_delete,create_time) VALUES(1,90101,110228,'1','相同标题','合成','4',1,'0',0,NOW()),(2,90101,90102,'1','相同标题','合成','4',1,'0',0,NOW()),(3,90101,110228,'1','相同标题','合成','4',999,'0',0,NOW())");
  assertEquals(1,body(get("/list-filters/notices?f.pending=yes",admin)).path("total").asInt());
  jdbc.update("UPDATE biz_core_report SET flow_status=30 WHERE report_id=1");assertEquals(0,body(get("/list-filters/notices?f.pending=yes",admin)).path("total").asInt());assertEquals(1,body(get("/list-filters/notices?f.pending=no",admin)).path("total").asInt());assertEquals(1,body(get("/list-filters/notices?f.pending=unknown",admin)).path("total").asInt());
 }
 @Test void operationFiltersAndExportUseTheSameRowsAcrossPages()throws Exception{
  for(int n=1;n<=3;n++)jdbc.update("INSERT INTO sys_admin_operation_log(module,action,target_type,target_id,target_name,batch_id,operator_id,reason,create_time) VALUES(?,'UPDATE','USER',?,'合成对象',?,110228,'合成原因',NOW())",n==3?"TASK":"USER",String.valueOf(n),n==1?"batch-a":"batch-b");
  assertEquals(403,get("/list-filters/admin-operations",a).getStatusCode().value());
  var selected=body(get("/list-filters/admin-operations?f.module=USER&pageSize=1",admin));assertEquals(2,selected.path("total").asInt());assertEquals(1,selected.path("items").size());
  assertEquals(1,body(get("/list-filters/admin-operations?f.module=USER&f.batchId=batch-a",admin)).path("total").asInt());
  var exported=http.exchange("http://127.0.0.1:"+port+"/api/admin/operations/export?f.module=USER",HttpMethod.POST,new HttpEntity<>(Map.of(),headers(admin,false)),byte[].class);assertEquals(200,exported.getStatusCode().value());try(var book=org.apache.poi.ss.usermodel.WorkbookFactory.create(new java.io.ByteArrayInputStream(exported.getBody()))){assertEquals(3,book.getSheetAt(0).getPhysicalNumberOfRows());}
 }
 @Test void performanceFiltersUseAnnualRatiosAndKeepAuditSeparate()throws Exception{
  jdbc.update("INSERT INTO biz_performance(perf_id,project_id,perf_code,perf_name,target_value,data_type,dept_id,leader_id,principal_id,auditor_id,is_delete) VALUES(101,1,'2.1.1','合成数量指标',100,'1',991001,90101,90102,90102,0),(102,1,'2.1.2','合成百分比指标',90,'2',991001,90101,90102,90102,0),(103,1,'2.1.3','合成缺失指标',10,'1',991001,90101,90102,90102,0)");
  jdbc.update("INSERT INTO biz_performance_year(year_id,perf_id,year,target_value,actual_value,is_delete) VALUES(101,101,2026,10,5,0),(102,102,2026,80,40,0),(103,103,2026,0,NULL,0),(104,101,2025,1,2,0)");
  assertEquals(2,body(get("/list-filters/performance?f.year=2026&f.ratioFrom=50&f.ratioTo=50",admin)).path("total").asInt());
  assertEquals(0,body(get("/list-filters/performance?f.year=2026&f.attainment=met",admin)).path("total").asInt());
  assertEquals(1,body(get("/list-filters/performance?f.year=2025&f.attainment=met&f.auditStatus=none",admin)).path("total").asInt());
  assertEquals(1,body(get("/list-filters/performance?f.year=2026&f.attainment=unknown",admin)).path("total").asInt());
 }
 @Test void formalFiltersUseLatestSubmittedOpinionAndKeepDraftsPrivate()throws Exception{
  submit(3,"old-keyword",a);submit(3,"new-keyword",a);submit(1,"partial-range",a);
  body(call(HttpMethod.POST,"/expert-reviews/draft",a,input(3,"private-draft-keyword",detail(3,a)),false));
  assertEquals(403,get("/expert-reviews/admin?filterMode=advanced",a).getStatusCode().value());
  assertEquals(0,body(get("/expert-reviews/admin?filterMode=advanced&f.text=old-keyword",admin)).path("total").asInt());
  assertEquals(0,body(get("/expert-reviews/admin?filterMode=advanced&f.text=private-draft-keyword",admin)).path("total").asInt());
  assertEquals(1,body(get("/expert-reviews/admin?filterMode=advanced&f.text=new-keyword&f.expertId=90103&f.level=3",admin)).path("total").asInt());
  assertEquals(1,body(get("/expert-reviews/admin?filterMode=advanced&f.range=partial",admin)).path("total").asInt());
 }
 @Test void onlyOwnStatusAndDraftAreReturnedAndAdminListNeverReturnsDrafts()throws Exception{
  body(call(HttpMethod.POST,"/expert-reviews/draft",b,input(3,"另一专家的私密草稿",detail(3,b)),false));assertFalse(body(get("/expert-reviews?year=2026",a)).toString().contains("私密草稿"));assertEquals("unreviewed",body(get("/expert-reviews?year=2026",a)).path("items").get(0).path("reviewStatus").asText());assertEquals(0,body(get("/expert-reviews/admin",admin)).path("total").asInt());assertEquals(403,get("/expert-reviews/tasks/3?year=2026&expertId=90104",a).getStatusCode().value());assertEquals(403,get("/expert-reviews/admin",a).getStatusCode().value());assertEquals(403,get("/expert-reviews?year=2026",owner).getStatusCode().value());
 }
 @Test void partialLevelOneScopeIsExplicitAndFrozenWithFormalRevision()throws Exception{
  var result=submit(1,"只对获准任务形成总体评语",a);var formal=result.path("formal");assertTrue(formal.path("scope").path("partial").asBoolean());assertEquals(1,formal.path("scope").path("tasks").size());assertEquals(3,formal.path("scope").path("tasks").get(0).path("taskId").asInt());jdbc.update("UPDATE biz_task SET task_name='已变更的业务名称' WHERE task_id=3");var history=body(get("/expert-reviews/tasks/1/history?year=2026",a));assertEquals("合成评审任务3",history.get(0).path("scope").path("tasks").get(0).path("taskName").asText());assertFalse(history.toString().contains("合成评审任务4"));
 }
 @Test void fullScopeFirstLevelAndThirdLevelReviewsDoNotChangeBusinessState()throws Exception{
  String before=jdbc.queryForList("SELECT task_id,current_value,progress,status FROM biz_task ORDER BY task_id").toString();assertFalse(submit(1,"完整范围评语",b).path("formal").path("scope").path("partial").asBoolean());submit(3,"三级任务评语",b);assertEquals(before,jdbc.queryForList("SELECT task_id,current_value,progress,status FROM biz_task ORDER BY task_id").toString());assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM biz_audit_log",Integer.class));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM biz_material_submission",Integer.class));
 }
 @Test void savedRevisionDraftDoesNotOverwritePreviousFormalOpinion()throws Exception{
  var first=submit(3,"第一版正式意见",a);var draft=body(call(HttpMethod.POST,"/expert-reviews/draft",a,input(3,"尚未正式提交的修订",first),false));assertEquals("第一版正式意见",draft.path("formal").path("text").asText());assertEquals(1,body(get("/expert-reviews/tasks/3/history?year=2026",a)).size());var second=body(call(HttpMethod.POST,"/expert-reviews/submit",a,input(3,"第二版正式意见",draft),false));assertEquals(2,second.path("formal").path("revisionNo").asInt());assertEquals(2,body(get("/expert-reviews/tasks/3/history?year=2026",a)).size());assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM biz_expert_review",Integer.class));
 }
 @Test void blankAndOverlongContentAreRejectedButUnicodeLimitIsByCharacters()throws Exception{
  var d=detail(3,a);assertEquals(400,call(HttpMethod.POST,"/expert-reviews/submit",a,input(3," \u00a0\n",d),false).getStatusCode().value());assertEquals(400,call(HttpMethod.POST,"/expert-reviews/draft",a,input(3,"评".repeat(2001),d),false).getStatusCode().value());var saved=body(call(HttpMethod.POST,"/expert-reviews/draft",a,input(3,"😀".repeat(2000),d),false));assertEquals(2000,saved.path("draftText").asText().codePointCount(0,saved.path("draftText").asText().length()));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM biz_expert_review_revision",Integer.class));
 }
 @Test void unsupportedDirectoryWrongYearAndOutOfScopeTaskAreRejected()throws Exception{
  assertEquals(404,get("/expert-reviews/tasks/2?year=2026",a).getStatusCode().value());assertEquals(404,get("/expert-reviews/tasks/4?year=2026",a).getStatusCode().value());assertEquals(400,get("/expert-reviews/tasks/3?year=2025",a).getStatusCode().value());assertEquals(400,get("/expert-reviews?year=2030",a).getStatusCode().value());
 }
 @Test void scopeChangeRejectsOldFormAndAllowsNewlyConfirmedScope()throws Exception{
  var old=detail(1,b);jdbc.update("INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,dept_id,leader_id,auditor_id,principal_id,target_value,current_value,progress,status,is_delete) VALUES(6,1,2,2026,'NEW6','新增获准任务',3,991001,90101,90102,90102,10,0,0,'0',0)");assertEquals(409,call(HttpMethod.POST,"/expert-reviews/submit",b,input(1,"旧范围意见",old),false).getStatusCode().value());var next=submit(1,"确认后的新范围意见",b);assertEquals(3,next.path("formal").path("scope").path("tasks").size());
 }
 @Test void changingReviewerFilterDoesNotChangeActualAuthorizedScope()throws Exception{
  var filtered=body(get("/expert-reviews?year=2026&deptId=991001&keyword=1",b));assertTrue(filtered.path("total").asInt()>0);var root=submit(1,"列表筛选不改变授权范围",b);assertEquals(2,root.path("formal").path("scope").path("tasks").size());
 }
 @Test void duplicateAndConcurrentSubmitOnlyCreateOneFormalRevision()throws Exception{
  var data=input(3,"并发正式提交",detail(3,a));var pool=Executors.newFixedThreadPool(2);try{var first=pool.submit(()->call(HttpMethod.POST,"/expert-reviews/submit",a,data,false).getStatusCode().value());var second=pool.submit(()->call(HttpMethod.POST,"/expert-reviews/submit",a,data,false).getStatusCode().value());var result=new ArrayList<>(List.of(first.get(20,TimeUnit.SECONDS),second.get(20,TimeUnit.SECONDS)));Collections.sort(result);assertEquals(List.of(200,409),result);}finally{pool.shutdownNow();}assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM biz_expert_review_revision",Integer.class));assertEquals(409,call(HttpMethod.POST,"/expert-reviews/submit",a,data,false).getStatusCode().value());
 }
 @Test void revocationAndDeletedTaskDenyExpertButAdminKeepsHistory()throws Exception{
  var formal=submit(3,"权限撤销前的意见",a).path("formal");long review=formal.path("reviewId").asLong();jdbc.update("UPDATE sys_user_permission SET scopes_json='{\"tasks\":{\"mode\":\"NONE\"}}',version=version+1 WHERE user_id=90103");assertEquals(404,get("/expert-reviews/tasks/3?year=2026",a).getStatusCode().value());assertEquals(404,get("/expert-reviews/tasks/3/history?year=2026",a).getStatusCode().value());jdbc.update("UPDATE biz_task SET is_delete=1 WHERE task_id=3");assertEquals("权限撤销前的意见",body(get("/expert-reviews/admin/"+review+"/history?year=2026",admin)).get(0).path("text").asText());jdbc.update("UPDATE sys_user SET status='0' WHERE user_id=90103");assertEquals(403,get("/expert-reviews?year=2026",a).getStatusCode().value());assertEquals(1,body(get("/expert-reviews/admin?expertId=90103",admin)).path("total").asInt());
 }
 @Test void adminCanReviewDirectlyAndDelegationRecordsBothIdentities()throws Exception{
  var own=body(call(HttpMethod.POST,"/expert-reviews/submit",admin,input(3,"管理员直接评审",detail(3,admin)),false));assertFalse(own.path("formal").path("delegated").asBoolean());assertEquals(110228,own.path("formal").path("operatorId").asLong());var d=body(call(HttpMethod.GET,"/expert-reviews/tasks/3?year=2026&expertId=90103",admin,null,false));var data=input(3,"管理员明确代填的正式意见",d);data.put("expertId",90103);assertEquals(403,call(HttpMethod.POST,"/expert-reviews/submit",b,data,false).getStatusCode().value());var saved=body(call(HttpMethod.POST,"/expert-reviews/submit",admin,data,false));assertEquals(90103,saved.path("formal").path("expertId").asLong());assertEquals(110228,saved.path("formal").path("operatorId").asLong());assertTrue(saved.path("formal").path("delegated").asBoolean());assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM sys_admin_operation_log WHERE module='ADMIN_OPERATION' AND target_type='EXPERT_REVIEW'",Integer.class));assertTrue(jdbc.queryForList("SELECT after_json FROM sys_admin_operation_log WHERE module='ADMIN_OPERATION'",String.class).stream().noneMatch(value->value.contains("管理员明确代填的正式意见")));
 }
 @Test void adminExportContainsFormalRevisionsAndCompleteScopeButNoDraft()throws Exception{
  var first=submit(1,"第一版正式",a);submit(1,"第二版正式",a);var d=detail(1,a);body(call(HttpMethod.POST,"/expert-reviews/draft",a,input(1,"不可导出的未提交草稿",d),false));long revision=first.path("formal").path("revisionId").asLong();var r=http.exchange("http://127.0.0.1:"+port+"/api/expert-reviews/admin/export",HttpMethod.POST,new HttpEntity<>(Map.of("revisionIds",List.of(revision),"includeHistory",true),headers(admin,false)),byte[].class);assertEquals(200,r.getStatusCode().value());try(var book=new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(r.getBody()))){assertEquals(2,book.getSheetAt(0).getLastRowNum());assertEquals(2,book.getSheetAt(1).getLastRowNum());String values=book.getSheetAt(0).getRow(1).getCell(7).getStringCellValue()+book.getSheetAt(0).getRow(2).getCell(7).getStringCellValue();assertTrue(values.contains("第一版正式")&&values.contains("第二版正式"));assertFalse(values.contains("不可导出"));}assertEquals(403,call(HttpMethod.POST,"/expert-reviews/admin/export",a,Map.of("revisionIds",List.of(revision)),false).getStatusCode().value());
 }
 @Test void shrinkingLevelOneScopeDoesNotLeakRemovedTasksThroughOldOpinionOrDraft()throws Exception{
  var formal=submit(1,"涉及获准任务3和4的总体评语",b);body(call(HttpMethod.POST,"/expert-reviews/draft",b,input(1,"包含任务4资料的未提交修订",formal),false));long reviewId=formal.path("formal").path("reviewId").asLong();
  jdbc.update("UPDATE sys_user_permission SET scopes_json='{\"tasks\":{\"mode\":\"CUSTOM\",\"items\":[3]}}',version=version+1 WHERE user_id=90104");var restricted=detail(1,b);assertTrue(restricted.path("formal").isNull());assertEquals("",restricted.path("draftText").asText());assertTrue(restricted.path("draftRestricted").asBoolean());assertEquals(0,body(get("/expert-reviews/tasks/1/history?year=2026",b)).size());assertEquals(1,body(get("/expert-reviews/admin/"+reviewId+"/history?year=2026",admin)).size());var next=submit(1,"仅针对现有任务3的总体评语",b);assertEquals(2,next.path("formal").path("revisionNo").asInt());assertEquals(1,next.path("formal").path("scope").path("tasks").size());
 }
 @Test void expertSelectorIncludesDirectAndCustomRoleGrantsAndKeepsHistoricalInactiveExperts()throws Exception{
  var initial=body(get("/expert-reviews/admin/experts",admin));assertTrue(initial.toString().contains("90103"));assertFalse(initial.toString().contains("90101"));
  jdbc.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json) VALUES(90101,'LEGACY','{\"expert.read\":true}','{}')");
  jdbc.update("INSERT INTO sys_permission_role(role_code,role_name,permissions_json,scopes_json,enabled) VALUES('CUSTOM_REVIEW','Custom read role','[\"tasks.read\",\"expert.read\"]','{\"tasks\":{\"mode\":\"ALL\"}}',1)");
  jdbc.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json) VALUES(90102,'CUSTOM','{}','{}')");jdbc.update("INSERT INTO sys_user_permission_role VALUES(90102,'CUSTOM_REVIEW')");
  var granted=body(get("/expert-reviews/admin/experts",admin));assertTrue(granted.toString().contains("90101"));assertTrue(granted.toString().contains("90102"));
  submit(3,"Retained formal opinion",a);jdbc.update("UPDATE sys_user SET status='0' WHERE user_id=90103");
  var history=body(get("/expert-reviews/admin/experts",admin));boolean found=false;for(var row:history)if(row.path("userId").asLong()==90103){assertFalse(row.path("active").asBoolean());found=true;}assertTrue(found);
 }

}
