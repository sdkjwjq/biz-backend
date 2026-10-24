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
