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
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="SHUANGGAO_REVIEW_TEST",matches="true")
@Import(ReviewBatchRegressionApiTest.NoAutomaticSchedules.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"logging.file.name=target/review-round-test.log","logging.level.org.springframework=WARN"})
class DetailedExportApiTest {
 @DynamicPropertySource static void database(DynamicPropertyRegistry r){String url=System.getenv("SHUANGGAO_TEST_JDBC_URL");if(url==null||!url.matches("jdbc:mysql://127\\.0\\.0\\.1:3306/biz_review_test_[0-9a-f]{32}\\?.*"))throw new IllegalStateException("Isolated schema required");r.add("spring.datasource.url",()->url);r.add("spring.datasource.username",()->System.getenv("SHUANGGAO_TEST_DB_USER"));r.add("spring.datasource.password",()->System.getenv("SHUANGGAO_TEST_DB_PASSWORD"));}
 @LocalServerPort int port;@Autowired TestRestTemplate http;@Autowired JdbcTemplate db;@Autowired ObjectMapper json;
 String admin,expert,business;long owner=90101,reviewer=90103,other=90104;
 LocalDateTime now(){return LocalDateTime.now(ZoneId.of("Asia/Shanghai")).withNano(0);}
 @BeforeEach void seed()throws Exception{
  db.execute((ConnectionCallback<Void>)c->{assertTrue(c.getCatalog().matches("biz_review_test_[0-9a-f]{32}"));try(var s=c.createStatement()){List<String> tables=new ArrayList<>();try(var rs=s.executeQuery("SHOW FULL TABLES WHERE Table_type='BASE TABLE'")){while(rs.next())tables.add(rs.getString(1));}s.execute("SET FOREIGN_KEY_CHECKS=0");for(String t:tables)s.executeUpdate("DELETE FROM `"+t+"`");s.execute("SET FOREIGN_KEY_CHECKS=1");}return null;});
  db.update("INSERT INTO sys_permission_revision VALUES(1,0)");db.update("INSERT INTO sys_dept(dept_id,dept_name,status,is_delete) VALUES(991001,'Review department','0',0)");
  for(long id:List.of(110228L,owner,reviewer,other))db.update("INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,password,role,status,is_delete,force_password_change) VALUES(?,991001,?,?,'RoundTest123',?,'1',0,0)",id,"round-"+id,"Round user "+id,id==110228?"0":"1");
  db.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json) VALUES(?,'CUSTOM','{}','{}'),(?,'CUSTOM','{}','{}')",reviewer,other);db.update("INSERT INTO sys_user_permission_role VALUES(?,'EXPERT'),(?,'EXPERT')",reviewer,other);
  db.update("INSERT INTO biz_project(project_id,project_name,leader_id) VALUES(1,'Round project',110228)");
  for(int id=1;id<=5;id++)db.update("INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,dept_id,leader_id,auditor_id,principal_id,target_value,current_value,progress,status,is_delete) VALUES(?,1,?,?,?, ?,?,991001,?,?,?,10,1,10,'1',0)",id,id==1?0:id==2?1:2,id==5?2025:2026,"ROUND"+id,"Round task "+id,id<3?id:3,owner,owner,owner);
  admin=switchTo(login(110228),"BUSINESS");business=login(owner);expert=login(reviewer);
 }
 ResponseEntity<String> call(HttpMethod method,String path,String token,Object value){var h=new HttpHeaders();if(token!=null)h.set("Authorization",token);h.setContentType(MediaType.APPLICATION_JSON);return http.exchange("http://127.0.0.1:"+port+"/api"+path,method,new HttpEntity<>(value,h),String.class);}
 JsonNode ok(ResponseEntity<String> r)throws Exception{assertEquals(200,r.getStatusCode().value(),r.getBody());return json.readTree(r.getBody());}
 JsonNode get(String path,String token)throws Exception{return ok(call(HttpMethod.GET,path,token,null));}
 String login(long id)throws Exception{return ok(call(HttpMethod.POST,"/system/login",null,Map.of("user_id",id,"password","RoundTest123"))).path("token").asText();}
 String switchTo(String token,String experience)throws Exception{return ok(call(HttpMethod.POST,"/system/review-context",token,Map.of("experience",experience))).path("token").asText();}
 JsonNode round(Integer year,LocalDateTime start,LocalDateTime end)throws Exception{Map<String,Object>b=new LinkedHashMap<>();b.put("name","Synthetic round");b.put("year",year);b.put("startsAt",start.toString());b.put("endsAt",end.toString());return ok(call(HttpMethod.POST,"/admin/review-tasks",admin,b));}
 long id(JsonNode r){return r.path("round_id").asLong();}
 JsonNode fresh(JsonNode r)throws Exception{return get("/admin/review-tasks/"+id(r),admin);}
 JsonNode add(JsonNode r,List<Long> users)throws Exception{return ok(call(HttpMethod.POST,"/admin/review-tasks/"+id(r)+"/members",admin,Map.of("version",r.path("version").asLong(),"userIds",users,"reason","test add")));}
 JsonNode scope(JsonNode r,List<Long> users,List<Long> tasks)throws Exception{return ok(call(HttpMethod.PUT,"/admin/review-tasks/"+id(r)+"/scope",admin,Map.of("version",r.path("version").asLong(),"userIds",users,"taskIds",tasks,"reason","test scope")));}
 JsonNode publish(JsonNode r)throws Exception{return ok(call(HttpMethod.POST,"/admin/review-tasks/"+id(r)+"/publish",admin,Map.of("version",r.path("version").asLong(),"reason","test publish")));}
 JsonNode active()throws Exception{var r=round(2026,now().minusHours(1),now().plusHours(1));return publish(scope(add(r,List.of(reviewer)),List.of(reviewer),List.of(3L)));}
 JsonNode detail(JsonNode r,long task,String token)throws Exception{return get("/review-work/"+id(r)+"/tasks/"+task,token);}
 Map<String,Object> opinion(JsonNode d,boolean submit,String text,String result){return Map.of("roundVersion",d.path("round").path("version").asLong(),"version",d.path("version").asLong(),"text",text,"result",result,"scopeToken",d.path("scopeToken").asText(),"submit",submit);}
 JsonNode save(JsonNode r,long task,String token,boolean submit,String text,String result)throws Exception{return ok(call(HttpMethod.POST,"/review-work/"+id(r)+"/tasks/"+task,token,opinion(detail(r,task,token),submit,text,result)));}

 @Autowired org.example.service.BudgetService budgets;
 org.apache.poi.ss.usermodel.Workbook download(String path,String token)throws Exception{
  var h=new HttpHeaders();h.set("Authorization",token);
  var r=http.exchange("http://127.0.0.1:"+port+"/api"+path,HttpMethod.GET,new HttpEntity<>(h),byte[].class);
  assertEquals(200,r.getStatusCode().value(),new String(r.getBody(),java.nio.charset.StandardCharsets.UTF_8));
  assertTrue(r.getHeaders().getContentType().toString().contains("spreadsheetml"));
  return org.apache.poi.ss.usermodel.WorkbookFactory.create(new java.io.ByteArrayInputStream(r.getBody()));
 }
 String text(org.apache.poi.ss.usermodel.Workbook book){var out=new StringBuilder();for(var sheet:book)for(var row:sheet)for(var cell:row)out.append(cell.toString()).append('\n');return out.toString();}
 String value(org.apache.poi.ss.usermodel.Sheet s,int row,String name){var h=s.getRow(0);for(var c:h)if(c.toString().equals(name))return s.getRow(row).getCell(c.getColumnIndex()).toString();throw new AssertionError(name);}
 void excludesColumns(org.apache.poi.ss.usermodel.Sheet sheet,String... names){var headers=new HashSet<String>();for(var cell:sheet.getRow(0))headers.add(cell.toString());for(String name:names)assertFalse(headers.contains(name),sheet.getSheetName()+" must not contain "+name);}
 void material(){
  db.update("UPDATE biz_task SET principal_id=?,auditor_id=?,leader_id=? WHERE task_id=3",other,reviewer,owner);
  db.update("INSERT INTO biz_level4_task(task_id,parent_id,phase,task_name,leader_id,dept_id,data_type,target_value,current_value,progress,is_delete) VALUES(31,3,2026,'Overcompleted goal',?,991001,'1',10,15,100,0)",owner);
  db.update("INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by) VALUES(41,'Evidence.pdf','not-exported-path','/not-exported-url','pdf',?)",owner);
  db.update("INSERT INTO biz_material_submission(sub_id,task_id,file_id,reported_value,submit_by,submit_dept_id,manage_dept_id,data_type,file_suffix,flow_status,is_delete,submit_time) VALUES(51,3,41,15,?,991001,991001,'1','pdf',40,0,NOW())",owner);
  db.update("INSERT INTO biz_audit_log(log_id,sub_id,operator_id,action_type,pre_status,post_status,comment,create_time) VALUES(61,51,?,'3',30,40,'Archived evidence',NOW())",other);
 }
 @Test void taskExportContainsBusinessDetailsAndCorrectOwnerWithoutExpertOpinions()throws Exception{
  material();try(var book=download("/data-exports/tasks?scope=all&f.keyword=does-not-match&page=999&pageSize=1",admin)){
   assertEquals(6,book.getSheet("任务明细").getPhysicalNumberOfRows());
   assertEquals("150.0",value(book.getSheet("四级目标"),1,"实际完成率(%)"));assertEquals("100.0",value(book.getSheet("四级目标"),1,"计入完成率(%)"));
   assertTrue(text(book).contains("Evidence.pdf"));assertTrue(text(book).contains("Archived evidence"));assertFalse(text(book).contains("not-exported-path"));
   var tasks=book.getSheet("任务明细");assertEquals("Round user "+owner,value(tasks,3,"专业群负责人"));assertEquals("Round user "+other,value(tasks,3,"归口审核人"));
   assertNull(book.getSheet("正式评语及历次修订"));
  }
  try(var filtered=download("/list-filters/tasks/export?f.keyword=ROUND3",admin)){assertEquals(4,filtered.getSheet("任务明细").getPhysicalNumberOfRows());assertTrue(text(filtered).contains("Evidence.pdf"));}
  try(var empty=download("/data-exports/tasks?scope=filtered&f.keyword=no-matches",admin)){assertEquals(1,empty.getSheet("任务明细").getPhysicalNumberOfRows());}
 }
 @Test void taskExportUsesTaskYearAndActualActionsWithoutUnrelatedOrInventedFields()throws Exception{
  material();
  db.update("UPDATE biz_task SET phase=2025,comment='CURRENT TASK DESCRIPTION',exp_level=NULL,actual_completion_date=NULL WHERE task_id=3");
  db.update("UPDATE biz_material_submission SET submit_time='2026-06-05 10:00:00' WHERE sub_id=51");
  db.update("UPDATE biz_audit_log SET action_type='通过' WHERE log_id=61");
  for(String path:List.of("/data-exports/tasks","/list-filters/tasks/export?f.keyword=ROUND3","/data-exports/supervision"))try(var book=download(path,admin)){
   var submitted=book.getSheet("填报记录");var logs=book.getSheet("审核记录");
   assertEquals("2025.0",value(submitted,1,"建设年度"),path);assertTrue(value(submitted,1,"提交时间").startsWith("2026-06-05"));
   assertEquals("2025.0",value(logs,1,"建设年度"));assertEquals("通过",value(logs,1,"操作"));assertEquals("Round task 3",value(logs,1,"任务名称"));
   excludesColumns(submitted,"绩效ID","绩效编号","绩效名称","成果ID","成果名称","绩效填报值","填报说明");
   excludesColumns(logs,"绩效ID","成果ID","核心填报ID","操作代码");
   assertEquals("",value(book.getSheet("任务明细"),3,"预期成果级别"));assertEquals("",value(book.getSheet("任务明细"),3,"实际完成日期"));
   assertEquals("CURRENT TASK DESCRIPTION",value(book.getSheet("任务明细"),3,"完成情况或说明"));
   assertTrue(text(book.getSheet("导出说明").getWorkbook()).contains("旧审批单未保存的原始四级值不推算"));
  }
 }
 @Test void filteredTaskExportShowsBothDirectoryProgressScopes()throws Exception{
  db.update("UPDATE biz_task SET progress=20 WHERE task_id=3");
  db.update("UPDATE biz_task SET progress=80 WHERE task_id=4");
  try(var book=download("/list-filters/tasks/export?f.keyword=ROUND3",admin)){
   var sheet=book.getSheet("任务明细");
   assertEquals(4,sheet.getPhysicalNumberOfRows());
   assertEquals("20.0",value(sheet,1,"当前筛选范围进度(%)"));
   assertNotEquals(value(sheet,1,"当前筛选范围进度(%)"),value(sheet,1,"全部下级任务进度(%)"));
   assertEquals(value(sheet,3,"当前筛选范围进度(%)"),value(sheet,3,"全部下级任务进度(%)"));
  }
 }
 @Test void performanceAndAchievementHistoryHaveApplicableColumnsAndCompleteParentKeys()throws Exception{
  db.update("INSERT INTO biz_performance(perf_id,project_id,parent_id,perf_code,perf_name,target_value,current_value,data_type,dept_id,leader_id,auditor_id,principal_id,is_delete) VALUES(701,1,0,'P-EXPORT','Export performance',10,3,'1',991001,?,?,?,0)",owner,reviewer,other);
  db.update("INSERT INTO biz_performance_year(year_id,perf_id,year,target_value,actual_value,is_delete) VALUES(702,701,2026,10,3,0),(705,701,2025,10,2,0)");
  db.update("INSERT INTO biz_performance_submission(sub_id,perf_id,year_id,year,actual_value,submit_by,flow_status,comment) VALUES(703,701,702,2026,3,?,30,'PERFORMANCE SUBMITTED TEXT'),(706,701,705,2025,2,?,30,'OLDER_YEAR_COMMENT')",owner,owner);
  db.update("INSERT INTO biz_performance_audit_log(log_id,sub_id,operator_id,action_type,pre_status,post_status,comment) VALUES(704,703,?,'通过',20,30,'Performance archived'),(707,706,?,'通过',20,30,'OLDER_YEAR_AUDIT')",other,other);
  try(var book=download("/data-exports/performance?scope=filtered&f.year=2026",admin)){
   var submitted=book.getSheet("填报记录");var logs=book.getSheet("审核记录");assertEquals(2,submitted.getPhysicalNumberOfRows());assertEquals(2,logs.getPhysicalNumberOfRows());
   assertEquals("3.0",value(submitted,1,"绩效填报值"));assertEquals("PERFORMANCE SUBMITTED TEXT",value(submitted,1,"填报说明"));
   assertEquals("701",value(logs,1,"绩效ID"));assertEquals("P-EXPORT",value(logs,1,"绩效编号"));assertEquals("Export performance",value(logs,1,"绩效名称"));
   assertEquals("702",value(logs,1,"绩效年度ID"));assertEquals("2026.0",value(logs,1,"填报年度"));assertEquals("通过",value(logs,1,"操作"));
   excludesColumns(submitted,"任务ID","成果ID","任务填报值","提交部门ID");excludesColumns(logs,"任务ID","成果ID","核心填报ID");assertFalse(text(book).contains("OLDER_YEAR_"));
  }
  db.update("INSERT INTO biz_achievement(ach_id,category,level,ach_name,department,got_time,dept_id,comment,create_by,audit_status,is_delete) VALUES(801,1,'省级','Export achievement','Unit','2026-06-05',991001,'Current achievement',?,30,0)",owner);
  db.update("INSERT INTO biz_achievement_submission(sub_id,ach_id,submit_by,flow_status,comment) VALUES(802,801,?,30,'ACHIEVEMENT SUBMITTED TEXT')",owner);
  db.update("INSERT INTO biz_achievement_audit_log(log_id,sub_id,operator_id,action_type,pre_status,post_status,comment) VALUES(803,802,?,'归档',10,30,'Achievement archived')",other);
  try(var book=download("/data-exports/achievements",admin)){
   var submitted=book.getSheet("填报记录");var logs=book.getSheet("审核记录");assertEquals("801",value(logs,1,"成果ID"));assertEquals("Export achievement",value(logs,1,"成果名称"));assertEquals("归档",value(logs,1,"操作"));
   assertEquals("ACHIEVEMENT SUBMITTED TEXT",value(submitted,1,"填报说明"));excludesColumns(submitted,"任务ID","绩效ID","任务填报值","绩效填报值","填报年度");excludesColumns(logs,"任务ID","绩效ID","核心填报ID");
  }
 }
 @Test void allListModulesProduceWorkbooksAndRejectUnknownOrInvalidRequests()throws Exception{
  for(String module:org.example.service.DetailedExportService.TITLES.keySet())try(var book=download("/data-exports/"+module+"?scope=all",admin)){assertNotNull(book.getSheet("导出说明"),module);}
  assertEquals(400,call(HttpMethod.GET,"/data-exports/tasks?scope=other",admin,null).getStatusCode().value());
  assertEquals(403,call(HttpMethod.GET,"/data-exports/admin-users",business,null).getStatusCode().value());
  String a=switchTo(admin,"EXPERT");assertEquals(403,call(HttpMethod.GET,"/data-exports/tasks",a,null).getStatusCode().value());
 }
 @Autowired org.example.service.AdminManagementService management;
 @Test void operationExportPathsKeepEveryRowAcrossStreamingFlushes()throws Exception{
  var rows=new ArrayList<Map<String,Object>>();
  for(int i=0;i<205;i++){
   db.update("INSERT INTO sys_admin_operation_log(module,action,target_type,target_id,target_name,operator_id,reason) VALUES('TASK','UPDATE','TASK',?,? ,110228,'fontless operation export')",String.valueOf(i),"操作对象 "+i);
   rows.add(Map.of("module","TASK","action","UPDATE","targetType","TASK","targetId",String.valueOf(i),"targetName","操作对象 "+i,"operatorId",110228,"operatorName","管理员","reason","fontless operation export"));
  }
  for(String path:List.of("/admin/operations/export","/admin/operations/export?f.module=TASK")){
   int expected=path.contains("f.module")?205:get("/admin/operations",admin).path("total").asInt();
   var h=new HttpHeaders();h.set("Authorization",admin);
   var result=http.exchange("http://127.0.0.1:"+port+"/api"+path,HttpMethod.POST,new HttpEntity<>(h),byte[].class);
   assertEquals(200,result.getStatusCode().value(),new String(result.getBody(),java.nio.charset.StandardCharsets.UTF_8));
   try(var book=org.apache.poi.ss.usermodel.WorkbookFactory.create(new java.io.ByteArrayInputStream(result.getBody()))){
    assertEquals(expected+1,book.getSheetAt(0).getPhysicalNumberOfRows(),path);assertTrue(text(book).contains("操作对象 204"));
   }
  }
  var response=new org.springframework.mock.web.MockHttpServletResponse();
  management.writeOperationExport(rows,response,110228L);
  try(var book=org.apache.poi.ss.usermodel.WorkbookFactory.create(new java.io.ByteArrayInputStream(response.getContentAsByteArray()))){assertEquals(206,book.getSheetAt(0).getPhysicalNumberOfRows());}
 }
 @Test void taskScopeExportPermissionChangesAndDisabledOldTokensAreEnforced()throws Exception{
  material();db.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json) VALUES(?,'CUSTOM',?,?)",owner,"{\"tasks.read\":true,\"tasks.export\":true,\"tasks.materials\":true,\"tasks.history\":true}","{\"tasks\":{\"mode\":\"CUSTOM\",\"items\":[3],\"years\":[2026]}}");db.update("UPDATE sys_permission_revision SET version=version+1");
  try(var book=download("/data-exports/tasks",business)){assertEquals(4,book.getSheet("任务明细").getPhysicalNumberOfRows());assertFalse(text(book).contains("ROUND4"));assertFalse(text(book).contains("ROUND5"));}
  db.update("UPDATE sys_user_permission SET permissions_json='{\"tasks.read\":true,\"tasks.export\":false}' WHERE user_id=?",owner);db.update("UPDATE sys_permission_revision SET version=version+1");assertEquals(403,call(HttpMethod.GET,"/data-exports/tasks",business,null).getStatusCode().value());
  db.update("UPDATE sys_user SET status='0' WHERE user_id=?",owner);assertEquals(403,call(HttpMethod.GET,"/data-exports/notices",business,null).getStatusCode().value());
 }
 @Test void messagesArePersonalAndAccountsNeverContainPasswords()throws Exception{
  db.update("INSERT INTO sys_notice(notice_id,from_user_id,to_user_id,type,title,content,source_type,source_id,is_read,is_delete,create_time) VALUES(801,110228,?,'1','My notification','My body','9',0,'0',0,NOW()),(802,110228,?,'1','PRIVATE OTHER NOTIFICATION','private','9',0,'0',0,NOW())",owner,other);
  try(var book=download("/data-exports/notices",business)){assertTrue(text(book).contains("My notification"));assertFalse(text(book).contains("PRIVATE OTHER"));}
  try(var book=download("/data-exports/admin-users",admin)){assertFalse(text(book).contains("RoundTest123"));assertFalse(text(book).toLowerCase().contains("password"));assertTrue(text(book).contains("Round user"));}
 }
 @Test void coreHistoryAndWorkRecordFrozenDataExcludeOtherDrafts()throws Exception{
  db.update("INSERT INTO biz_core_indicator VALUES(1,'2.1',1)");db.update("INSERT INTO biz_performance(perf_id,project_id,parent_id,perf_code,perf_name,target_value,current_value,data_type,dept_id,leader_id,auditor_id,principal_id,is_delete) VALUES(701,1,0,'2.1','Analysis indicator',10,3,'1',991001,?,?,?,0)",owner,reviewer,other);
  db.update("INSERT INTO biz_performance_year(year_id,perf_id,year,target_value,actual_value,is_delete) VALUES(702,701,2026,10,3,0)");
  db.update("INSERT INTO biz_core_report(report_id,indicator_id,record_year,record_month,performance_code,indicator_name,target_value,actual_value,comment,submit_by,flow_status,submit_time) VALUES(703,1,2026,5,'2.1','Frozen indicator',10,5,'Formal core value',?,30,NOW()),(704,1,2026,6,'2.1','Private indicator',10,9,'PRIVATE CORE DRAFT',?,0,NULL)",owner,other);
  db.update("INSERT INTO biz_core_audit_log(log_id,report_id,operator_id,action,pre_status,post_status,comment) VALUES(707,703,?,'归档',20,30,'Core archived')",other);
  db.update("INSERT INTO biz_work_record(record_id,owner_id,owner_name,record_year,record_month,status,problems,next_focus,other_matters,delete_marker,create_time,update_time) VALUES(705,?,'Frozen owner',2026,5,1,'Record problems','Next focus','Other',0,NOW(),NOW()),(706,?,'Other owner',2026,6,0,'PRIVATE RECORD DRAFT','','',0,NOW(),NOW())",owner,other);
  db.update("INSERT INTO biz_work_record_entry(record_id,reform_task_id,reform_task_name,sort_order,key_progress,stage_results,typical_practices) VALUES(705,1,'Frozen reform',1,'Frozen progress','Stage results','Practices')");
  db.update("INSERT INTO biz_work_record_snapshot(record_id,statistics_json,generated_time) VALUES(705,?,NOW())","{\"year\":2026,\"month\":5,\"totalTasks\":12,\"newCompleted\":2,\"cumulativeCompleted\":5,\"completionRate\":41.67,\"tasks\":[{\"taskId\":3,\"taskCode\":\"FROZEN-CODE\",\"taskName\":\"Frozen task title\",\"completed\":true}]}");
  try(var book=download("/data-exports/core",admin)){assertTrue(text(book).contains("Formal core value"));assertFalse(text(book).contains("PRIVATE CORE DRAFT"));var logs=book.getSheet("核心审核记录");assertEquals("归档",value(logs,1,"操作"));assertEquals("2.1",value(logs,1,"提交时指标编号"));assertEquals("Frozen indicator",value(logs,1,"提交时指标名称"));assertEquals("2026.0",value(logs,1,"填报年度"));assertEquals("5.0",value(logs,1,"填报月份"));excludesColumns(logs,"任务ID","绩效ID","成果ID","填报单ID","操作代码");}
  try(var book=download("/data-exports/records",admin)){assertTrue(text(book).contains("Frozen progress"));assertTrue(text(book).contains("FROZEN-CODE"));assertFalse(text(book).contains("PRIVATE RECORD DRAFT"));assertEquals("2.0",value(book.getSheet("冻结统计"),1,"本月完成数"));}
  try(var book=download("/data-exports/performance",admin)){assertTrue(text(book).contains("Analysis indicator"));assertEquals(2,book.getSheet("绩效年度值").getPhysicalNumberOfRows());}
 }
 @Test void fullBudgetExportIncludesEverySavedMonthAndBothSections()throws Exception{
  for(int month:List.of(5,6)){db.update("INSERT INTO biz_budget_sheet(sheet_id,year,month,section,locked,is_delete) VALUES(?,2026,?,'annual',0,0)",900+month,month);db.update("INSERT INTO biz_budget_source_item(sheet_id,source_key,source_name,annual_plan) VALUES(?,'school','学校自筹资金',100)",900+month);db.update("INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other) VALUES(?,1,'Budget task',50,5,0,0)",900+month);}
  budgets.copySection(2026,5,"annual","execution",110228L);
  try(var book=download("/data-exports/budget?scope=all&section=annual&f.year=2025&f.month=1",admin)){assertEquals(4,book.getSheet("预算月表").getPhysicalNumberOfRows());assertTrue(text(book).contains("execution"));assertEquals(4,book.getSheet("改革任务资金").getPhysicalNumberOfRows());}
 }
 @Test void fourthGoalIdsCannotLeakUnselectedTaskEvidenceOrOverwriteTaskNames()throws Exception{
  material();
  db.update("INSERT INTO biz_level4_task(task_id,parent_id,phase,dept_id,leader_id,task_name,data_type,target_value,current_value,is_delete) VALUES(3,3,2026,991001,90101,'Goal sharing the selected task ID','1',1,1,0),(4,3,2026,991001,90101,'Goal sharing another task ID','1',1,1,0)");
  db.update("INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by) VALUES(42,'PRIVATE UNSELECTED MATERIAL.pdf','private-path','/private-url','pdf',?)",owner);
  db.update("INSERT INTO biz_material_submission(sub_id,task_id,file_id,reported_value,submit_by,submit_dept_id,manage_dept_id,data_type,file_suffix,flow_status,is_delete) VALUES(52,4,42,7,?,991001,991001,'1','pdf',40,0)",owner);
  db.update("INSERT INTO biz_audit_log(sub_id,operator_id,action_type,pre_status,post_status,comment) VALUES(52,?,'3',30,40,'PRIVATE UNSELECTED HISTORY')",other);
  db.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json) VALUES(?,'CUSTOM',?,?)",owner,"{\"tasks.read\":true,\"tasks.export\":true,\"tasks.materials\":true,\"tasks.history\":true}","{\"tasks\":{\"mode\":\"CUSTOM\",\"items\":[3],\"years\":[2026]}}");
  db.update("UPDATE sys_permission_revision SET version=version+1");
  for(String path:List.of("/data-exports/tasks?scope=all","/list-filters/tasks/export?f.keyword=ROUND3")){
   try(var book=download(path,business)){
    assertFalse(text(book).contains("PRIVATE UNSELECTED"),path);
    assertEquals(2,book.getSheet("填报记录").getPhysicalNumberOfRows(),path);
    assertEquals("Round task 3",value(book.getSheet("填报记录"),1,"任务名称"),path);
    assertEquals("ROUND3",value(book.getSheet("佐证材料清单"),1,"任务编号"),path);
    assertEquals(4,book.getSheet("四级目标").getPhysicalNumberOfRows(),path);
   }
  }
 }
 @Test void largeTaskAndSupervisionExportsDoNotRepeatSubmissionsAcrossSqlChunks()throws Exception{
  material();
  for(int i=0;i<498;i++)db.update("INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,dept_id,leader_id,auditor_id,principal_id,target_value,current_value,progress,status,is_delete) VALUES(?,1,2,2026,?,?,3,991001,?,?,?,1,0,0,'0',0)",1000+i,"BULK"+i,"Bulk export task "+i,owner,reviewer,other);
  db.update("INSERT INTO biz_level4_task(task_id,parent_id,phase,dept_id,leader_id,task_name,data_type,target_value,current_value,is_delete) VALUES(3,3,2026,991001,90101,'Overlapping fourth goal','1',1,1,0)");
  for(String path:List.of("/data-exports/tasks?scope=all","/data-exports/supervision?scope=all","/list-filters/tasks/export")){
   try(var book=download(path,admin)){
    assertEquals(504,book.getSheet("任务明细").getPhysicalNumberOfRows(),path);
    assertEquals(2,book.getSheet("填报记录").getPhysicalNumberOfRows(),path);
    assertEquals(2,book.getSheet("审核记录").getPhysicalNumberOfRows(),path);
    assertEquals(2,book.getSheet("佐证材料清单").getPhysicalNumberOfRows(),path);
    assertEquals("Round task 3",value(book.getSheet("填报记录"),1,"任务名称"),path);
   }
  }
 }
 @Test void reviewManagementExportsFormalRevisionsButNeverPrivateDrafts()throws Exception{
  var r=active();save(r,3,expert,true,"FORMAL OPINION","pass");save(r,3,expert,false,"PRIVATE DRAFT REVISION","reject");
  try(var book=download("/data-exports/review-tasks",admin)){assertTrue(text(book).contains("FORMAL OPINION"));assertTrue(text(book).contains("通过"));assertFalse(text(book).contains("PRIVATE DRAFT REVISION"));}
 }
}
