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
class ReviewRoundApiTest {
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
 @Test void loginChoiceAndServerModeCannotBeBypassed()throws Exception{
  String pending=login(110228);assertEquals("SELECT",get("/system/review-context",pending).path("experience").asText());assertEquals(403,call(HttpMethod.GET,"/biz/tasks",pending,null).getStatusCode().value());
  String chosen=switchTo(pending,"BUSINESS");assertEquals(401,call(HttpMethod.GET,"/system/review-context",pending,null).getStatusCode().value());assertEquals(200,call(HttpMethod.GET,"/admin/review-tasks",chosen,null).getStatusCode().value());
  String er=switchTo(chosen,"EXPERT");assertEquals(403,call(HttpMethod.GET,"/admin/users",er,null).getStatusCode().value());assertEquals(403,call(HttpMethod.POST,"/biz/tasks/manage/create",er,Map.of()).getStatusCode().value());assertEquals(200,call(HttpMethod.GET,"/review-work",er,null).getStatusCode().value());
  assertEquals(403,call(HttpMethod.GET,"/biz/tasks",expert,null).getStatusCode().value());assertEquals(403,call(HttpMethod.GET,"/review-work",business,null).getStatusCode().value());
 }
 @Test void creationAndPublicationRequireCompleteConfiguration()throws Exception{
  var r=round(2026,now().minusHours(1),now().plusHours(1));assertEquals(400,call(HttpMethod.POST,"/admin/review-tasks/"+id(r)+"/publish",admin,Map.of("version",0,"reason","publish")).getStatusCode().value());r=add(r,List.of(reviewer));assertEquals(400,call(HttpMethod.POST,"/admin/review-tasks/"+id(r)+"/publish",admin,Map.of("version",r.path("version").asLong(),"reason","publish")).getStatusCode().value());assertFalse(get("/system/review-context",expert).path("expertAvailable").asBoolean());
  r=scope(r,List.of(reviewer),List.of(3L));assertEquals(403,call(HttpMethod.GET,"/review-work/"+id(r)+"/tasks",expert,null).getStatusCode().value());r=publish(r);assertTrue(get("/system/review-context",expert).path("expertAvailable").asBoolean());assertEquals(3,get("/review-work/"+id(r)+"/tasks",expert).size());
 }
 @Test void overlapIsAtomicAcrossAccountsAndAdministratorsAreExempt()throws Exception{
  var a=active();var b=round(2025,now().minusMinutes(10),now().plusHours(2));var response=call(HttpMethod.POST,"/admin/review-tasks/"+id(b)+"/members",admin,Map.of("version",b.path("version").asLong(),"userIds",List.of(other,reviewer),"reason","conflict"));assertEquals(409,response.getStatusCode().value());assertTrue(response.getBody().contains("Synthetic round"));assertEquals(0,db.queryForObject("SELECT COUNT(*) FROM biz_review_member WHERE round_id=?",Integer.class,id(b)));
  b=add(b,List.of(110228L));assertEquals(1,b.path("members").size());var future=round(2026,now().plusHours(2),now().plusHours(3));assertEquals(1,add(future,List.of(reviewer)).path("members").size());assertTrue(a.path("published").asInt()==1);
 }
 @Test void allYearScopeAndNoImplicitBusinessGrant()throws Exception{
  var r=round(null,now().minusHours(1),now().plusHours(1));r=publish(scope(add(r,List.of(reviewer)),List.of(reviewer),List.of(3L,5L)));assertEquals(2025,detail(r,5,expert).path("task").path("year").asInt());assertEquals(2026,detail(r,3,expert).path("task").path("year").asInt());assertEquals(404,call(HttpMethod.GET,"/review-work/"+id(r)+"/tasks/4",expert,null).getStatusCode().value());assertEquals(2,detail(r,1,expert).path("scope").path("tasks").size());assertFalse(get("/system/review-context",expert).path("businessAvailable").asBoolean());
  String reviewerBusiness=business;var before=get("/biz/tasks",reviewerBusiness);r=add(fresh(r),List.of(owner));r=scope(r,List.of(owner),List.of(4L));assertEquals(before,get("/biz/tasks",reviewerBusiness));assertEquals("SELECT",get("/system/review-context",login(owner)).path("experience").asText());
 }
 @Test void formalResultAndRevisionsDoNotTouchTaskApproval()throws Exception{
  var r=active();var draft=save(r,3,expert,false,"","pass");assertEquals(0,draft.path("history").size());assertEquals(400,call(HttpMethod.POST,"/review-work/"+id(r)+"/tasks/3",expert,opinion(draft,true,"","pass")).getStatusCode().value());
  var first=save(r,3,expert,true,"Independent formal opinion","pass");var second=save(r,3,expert,true,"Independent formal opinion","reject");assertEquals(2,second.path("history").size());assertEquals("reject",second.path("history").get(0).path("review_result").asText());assertEquals("pass",second.path("history").get(1).path("review_result").asText());assertEquals(10,db.queryForObject("SELECT progress FROM biz_task WHERE task_id=3",Integer.class));assertEquals(0,db.queryForObject("SELECT COUNT(*) FROM biz_audit_log",Integer.class));assertEquals(409,call(HttpMethod.POST,"/review-work/"+id(r)+"/tasks/3",expert,opinion(first,true,"stale","pass")).getStatusCode().value());
 }
 @Test void sameTaskDifferentRoundsHasIndependentOpinionsAndExpiredReadOnly()throws Exception{
  var r=active();save(r,3,expert,true,"First round","pass");db.update("UPDATE biz_review_round SET starts_at=?,ends_at=? WHERE round_id=?",now().minusDays(2),now().minusDays(1),id(r));var old=detail(r,3,expert);assertFalse(old.path("canWrite").asBoolean());assertEquals(403,call(HttpMethod.POST,"/review-work/"+id(r)+"/tasks/3",expert,opinion(old,true,"late","reject")).getStatusCode().value());assertEquals(0,get("/review-history/rounds",business).size()); // unrelated staff must have no historical membership
  var r2=active();save(r2,3,expert,true,"Second round","reject");assertEquals("First round",detail(r,3,expert).path("formal").path("review_text").asText());assertEquals("Second round",detail(r2,3,expert).path("formal").path("review_text").asText());assertEquals(2,db.queryForObject("SELECT COUNT(*) FROM biz_round_revision",Integer.class));
 }
 @Test void beforeStartAndAdministratorTimeOverride()throws Exception{
  var r=round(2026,now().plusDays(1),now().plusDays(2));r=publish(scope(add(r,List.of(reviewer)),List.of(reviewer),List.of(3L)));assertFalse(get("/system/review-context",expert).path("expertAvailable").asBoolean());assertEquals(403,call(HttpMethod.GET,"/review-work/"+id(r)+"/tasks",expert,null).getStatusCode().value());String a=switchTo(admin,"EXPERT");assertEquals(4,detail(r,4,a).path("task").path("taskId").asInt());assertEquals(1,save(r,4,a,true,"Admin outside time and member scope","pass").path("history").size());
 }
 @Test void scopeRemovalDisableAndOldTokenAreRechecked()throws Exception{
  var r=active();save(r,3,expert,true,"Historical evidence","pass");var d=detail(r,3,expert);r=scope(fresh(r),List.of(reviewer),List.of(4L));assertEquals(404,call(HttpMethod.GET,"/review-work/"+id(r)+"/tasks/3",expert,null).getStatusCode().value());assertEquals(409,call(HttpMethod.POST,"/review-work/"+id(r)+"/tasks/3",expert,opinion(d,true,"stale","reject")).getStatusCode().value());
  r=ok(call(HttpMethod.POST,"/admin/review-tasks/"+id(r)+"/members/"+reviewer+"/remove",admin,Map.of("version",r.path("version").asLong(),"reason","removed")));assertEquals(403,call(HttpMethod.GET,"/review-work/"+id(r)+"/tasks/4",expert,null).getStatusCode().value());assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM biz_round_revision",Integer.class));db.update("UPDATE sys_user SET status='0' WHERE user_id=?",reviewer);assertEquals(403,call(HttpMethod.GET,"/review-work",expert,null).getStatusCode().value());
 }
 @Test void reviewerPrivacyAndOwnFileLinks()throws Exception{
  var r=active();r=publishSecondMember(r);String otherToken=login(other);save(r,3,expert,true,"Private opinion","pass");assertEquals(0,detail(r,3,otherToken).path("history").size());assertEquals(403,call(HttpMethod.GET,"/admin/review-tasks/"+id(r)+"/opinions",expert,null).getStatusCode().value());
  var path=java.nio.file.Files.createTempFile(java.nio.file.Path.of("target"),"round-material-",".pdf");try{java.nio.file.Files.writeString(path,"Synthetic review file");db.update("INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,is_delete) VALUES(91,'review.pdf',?,'/synthetic/review.pdf','pdf',0)","/"+path.toString().replace('\\','/'));db.update("INSERT INTO biz_material_submission(sub_id,task_id,file_id,data_type,submit_by,submit_dept_id,manage_dept_id,file_suffix,flow_status,is_delete) VALUES(81,3,91,'1',?,991001,991001,'pdf',40,0)",owner);assertEquals(200,call(HttpMethod.GET,"/review-work/"+id(r)+"/files/91",expert,null).getStatusCode().value());assertEquals(404,call(HttpMethod.GET,"/review-work/"+id(r)+"/files/999",expert,null).getStatusCode().value());}finally{java.nio.file.Files.deleteIfExists(path);}
 }
 JsonNode publishSecondMember(JsonNode r)throws Exception{return scope(add(fresh(r),List.of(other)),List.of(other),List.of(3L));}
 @Test void staleManagementAndEmptyScopeCannotGrantRoles()throws Exception{
  var r=active();var previous=r;r=scope(r,List.of(reviewer),List.of());assertFalse(get("/system/review-context",expert).path("expertAvailable").asBoolean());assertEquals(0,get("/review-work/"+id(r)+"/tasks",expert).size());assertEquals(409,call(HttpMethod.PUT,"/admin/review-tasks/"+id(r)+"/scope",admin,Map.of("version",previous.path("version").asLong(),"userIds",List.of(reviewer),"taskIds",List.of(3),"reason","stale")).getStatusCode().value());
 }
 @Test void simultaneousAssignmentsCannotCreateOverlappingMembership()throws Exception{
  var first=round(2026,now().minusHours(1),now().plusHours(1));var second=round(2025,now().minusHours(1),now().plusHours(1));
  var pool=Executors.newFixedThreadPool(2);var start=new CountDownLatch(1);
  try{List<Future<Integer>> requests=new ArrayList<>();for(var r:List.of(first,second))requests.add(pool.submit(()->{start.await();return call(HttpMethod.POST,"/admin/review-tasks/"+id(r)+"/members",admin,Map.of("version",r.path("version").asLong(),"userIds",List.of(reviewer),"reason","parallel assignment")).getStatusCode().value();}));start.countDown();var codes=new ArrayList<Integer>();for(var f:requests)codes.add(f.get(30,TimeUnit.SECONDS));Collections.sort(codes);assertEquals(List.of(200,409),codes);assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM biz_review_member WHERE user_id=? AND active=1",Integer.class,reviewer));}finally{pool.shutdownNow();}
 }
 @Test void readdingRemovedMemberRequiresFreshScopeConfirmation()throws Exception{
  var r=active();save(r,3,expert,true,"Retained formal evidence","pass");r=ok(call(HttpMethod.POST,"/admin/review-tasks/"+id(r)+"/members/"+reviewer+"/remove",admin,Map.of("version",r.path("version").asLong(),"reason","temporarily removed")));r=add(r,List.of(reviewer));assertEquals(0,r.path("members").get(0).path("taskCount").asInt());assertFalse(get("/system/review-context",expert).path("expertAvailable").asBoolean());assertEquals(0,get("/review-work/"+id(r)+"/tasks",expert).size());assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM biz_round_revision",Integer.class));r=scope(r,List.of(reviewer),List.of(3L));assertEquals("Retained formal evidence",detail(r,3,expert).path("formal").path("review_text").asText());
 }
 @Test void simultaneousOpinionSubmissionsCreateOnlyOneFormalVersion()throws Exception{
  var r=active();var body=opinion(detail(r,3,expert),true,"One formal version","pass");var pool=Executors.newFixedThreadPool(2);var start=new CountDownLatch(1);
  try{List<Future<Integer>> requests=new ArrayList<>();for(int i=0;i<2;i++)requests.add(pool.submit(()->{start.await();return call(HttpMethod.POST,"/review-work/"+id(r)+"/tasks/3",expert,body).getStatusCode().value();}));start.countDown();var codes=new ArrayList<Integer>();for(var f:requests)codes.add(f.get(30,TimeUnit.SECONDS));Collections.sort(codes);assertEquals(List.of(200,409),codes);assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM biz_round_revision",Integer.class));}finally{pool.shutdownNow();}
 }
 @Test void exportedRevisionsKeepLargeScopeSnapshotsOnTheirOwnSheet()throws Exception{
  var r=active();save(r,3,expert,true,"Exported opinion","reject");List<Map<String,Object>> tasks=new ArrayList<>();for(int i=0;i<500;i++)tasks.add(Map.of("taskId",1000+i,"taskCode","EXPORT"+i,"taskName","Synthetic frozen scope "+i,"year",2026,"targetValue",10,"currentValue",1,"progress",10));db.update("UPDATE biz_round_revision SET scope_json=?",json.writeValueAsString(Map.of("tasks",tasks)));
  HttpHeaders h=new HttpHeaders();h.set("Authorization",admin);var result=http.exchange("http://127.0.0.1:"+port+"/api/admin/review-tasks/"+id(r)+"/opinions/export",HttpMethod.GET,new HttpEntity<>(h),byte[].class);assertEquals(200,result.getStatusCode().value());try(var book=new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(result.getBody()))){assertEquals(2,book.getNumberOfSheets());assertEquals("不通过",book.getSheetAt(0).getRow(1).getCell(6).getStringCellValue());assertEquals(500,book.getSheetAt(1).getLastRowNum());assertEquals("Synthetic frozen scope 499",book.getSheetAt(1).getRow(500).getCell(3).getStringCellValue());}
 }
 @Test void originalTaskTreeIsScopedAndKeepsProfessionalOwnerDistinct()throws Exception{
  db.update("UPDATE biz_task SET leader_id=?,auditor_id=?,principal_id=?,exp_target='Expected outcome',exp_effect='Expected effect',comment='Actual completion' WHERE task_id=3",owner,reviewer,other);
  db.update("UPDATE biz_task SET progress=99,current_value=99,target_value=100 WHERE task_id IN(1,2)");
  var r=active();var tree=get("/review-work/"+id(r)+"/tasks?responseMode=tree",expert);assertEquals(1,tree.path("total").asInt());assertEquals(3,tree.path("items").size());
  var leaf=java.util.stream.StreamSupport.stream(tree.path("items").spliterator(),false).filter(t->t.path("taskId").asLong()==3).findFirst().orElseThrow();
  assertEquals(owner,leaf.path("leaderId").asLong());assertEquals("Round user "+owner,leaf.path("leaderName").asText());assertEquals(other,leaf.path("principalId").asLong());assertEquals("Expected outcome",leaf.path("expTarget").asText());assertTrue(leaf.path("canReview").asBoolean());
  for(var row:tree.path("items"))if(row.path("level").asInt()<3){assertTrue(row.path("currentValue").isNull());assertTrue(row.path("targetValue").isNull());assertEquals(10,row.path("progress").asInt());}
  var page=get("/review-work/"+id(r)+"/tasks/3/page-detail",expert);assertEquals("Actual completion",page.path("task").path("comment").asText());assertEquals("Expected effect",page.path("task").path("expEffect").asText());assertEquals(0,page.path("children").size());assertTrue(page.path("review").path("canWrite").asBoolean());
  assertEquals(404,call(HttpMethod.GET,"/review-work/"+id(r)+"/tasks/4/page-detail",expert,null).getStatusCode().value());assertEquals(403,call(HttpMethod.GET,"/review-work/"+id(r)+"/tasks?responseMode=tree",business,null).getStatusCode().value());
  assertEquals(99,db.queryForObject("SELECT progress FROM biz_task WHERE task_id=1",Integer.class));
 }
 @Test void taskFiltersAndRootOpinionStatusDoNotChangeTheSubmittedScope()throws Exception{
  var r=active();r=scope(fresh(r),List.of(reviewer),List.of(3L,4L));save(r,3,expert,true,"Leaf reviewed","pass");String base="/review-work/"+id(r)+"/tasks?responseMode=tree";
  assertEquals(1,get(base+"&f.reviewStatus=submitted",expert).path("total").asInt());assertEquals(2,get(base+"&f.rootReviewStatus=unreviewed",expert).path("total").asInt());
  assertEquals(1,get(base+"&f.keyword=ROUND3&f.year=2026",expert).path("total").asInt());
  var root=get("/review-work/"+id(r)+"/tasks/1/page-detail",expert);assertEquals(2,root.path("children").size());assertEquals(2,root.path("review").path("scope").path("tasks").size());assertEquals(0,root.path("submissions").size());
  assertTrue(root.path("task").path("targetValue").isNull());assertTrue(root.path("review").path("task").path("targetValue").isNull());
  save(r,1,expert,true,"Root reviewed within the complete grant","reject");assertEquals(2,get(base+"&f.rootReviewStatus=submitted",expert).path("total").asInt());assertEquals(0,get(base+"&f.rootReviewStatus=unreviewed",expert).path("total").asInt());
  assertEquals(400,call(HttpMethod.GET,base+"&f.scope=mine",expert,null).getStatusCode().value());assertEquals(400,call(HttpMethod.GET,base+"&f.progressFrom=90&f.progressTo=10",expert,null).getStatusCode().value());
 }
 @Test void roundFilterOptionsContainOnlyAuthorizedParticipantsAndYears()throws Exception{
  var r=active();String base="/review-work/"+id(r)+"/tasks/filter-options";var definitions=get(base,expert);assertTrue(definitions.path("fields").size()>=15);
  var people=get(base+"?field=leaderId&ids="+owner+"&ids="+other+"&pageSize=50",expert);assertEquals(1,people.path("total").asInt());assertEquals(1,people.path("selected").size());assertEquals(String.valueOf(owner),people.path("selected").get(0).path("value").asText());
  var years=get(base+"?field=year",expert);assertEquals(1,years.path("items").size());assertEquals("2026",years.path("items").get(0).path("value").asText());
  assertEquals(400,call(HttpMethod.GET,base+"?field=password",expert,null).getStatusCode().value());assertEquals(400,call(HttpMethod.GET,"/review-work/"+id(r)+"/tasks?responseMode=tree&f.year=2025",expert,null).getStatusCode().value());
 }
 @Test void pageDetailBatchesGoalsFilesAndAuditRecordsWithoutMutatingEvidence()throws Exception{
  var r=active();db.update("INSERT INTO biz_level4_task(task_id,parent_id,dept_id,task_name,target_value,current_value,progress,data_type,is_delete) VALUES(91,3,991001,'Excess goal',2,3,100,'1',0)");
  db.update("INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,is_delete) VALUES(92,'Evidence.pdf','/synthetic/evidence.pdf','/synthetic/evidence.pdf','pdf',0)");
  db.update("INSERT INTO biz_material_submission(sub_id,task_id,file_id,reported_value,data_type,submit_by,submit_dept_id,manage_dept_id,file_suffix,flow_status,is_delete) VALUES(93,3,92,1,'1',?,991001,991001,'pdf',40,0)",owner);
  db.update("INSERT INTO biz_audit_log(sub_id,operator_id,action_type,pre_status,post_status,comment) VALUES(93,?,'通过',30,40,'Preserved audit evidence')",owner);
  var page=get("/review-work/"+id(r)+"/tasks/3/page-detail",expert);assertEquals(150,page.path("level4Tasks").get(0).path("actualProgress").asInt());assertEquals(100,page.path("level4Tasks").get(0).path("countedProgress").asInt());assertEquals("Evidence.pdf",page.path("submissions").get(0).path("filename").asText());assertEquals("Preserved audit evidence",page.path("auditLogs").get(0).path("comment").asText());assertFalse(page.toString().contains("RoundTest123"));assertFalse(page.toString().contains("file_path"));
  assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM biz_audit_log",Integer.class));assertEquals(3,db.queryForObject("SELECT current_value FROM biz_level4_task WHERE task_id=91",Integer.class));
 }
 @Test void historyTaskPageSuppressesRevisionDraftsEvenForAdministrators()throws Exception{
  var r=active();save(r,3,expert,true,"Formal only","pass");save(r,3,expert,false,"Unsubmitted revision","reject");db.update("UPDATE biz_review_round SET starts_at=?,ends_at=? WHERE round_id=?",now().minusDays(2),now().minusDays(1),id(r));
  var history=get("/review-history/work/"+id(r)+"/tasks/3/page-detail",expert);assertFalse(history.path("review").path("canWrite").asBoolean());assertEquals("Formal only",history.path("review").path("draftText").asText());assertFalse(history.toString().contains("Unsubmitted revision"));
  assertEquals(1,get("/review-history/work/"+id(r)+"/tasks?responseMode=tree",expert).path("total").asInt());
  String a=switchTo(admin,"EXPERT");save(r,4,a,true,"Admin formal","pass");save(r,4,a,false,"Admin private draft","reject");var adminHistory=get("/review-history/work/"+id(r)+"/tasks/4/page-detail",a);assertFalse(adminHistory.path("review").path("canWrite").asBoolean());assertEquals("Admin formal",adminHistory.path("review").path("draftText").asText());assertFalse(adminHistory.toString().contains("Admin private draft"));
  var historyTree=get("/review-history/work/"+id(r)+"/tasks?responseMode=tree&f.reviewStatus=submitted",a);assertEquals(1,historyTree.path("total").asInt());assertEquals(4,historyTree.path("matchedIds").get(0).asLong());
 }
 @Test void fullYearTaskTreeAndSelfProfileAreCompatibleWithBusinessReads()throws Exception{
  var r=round(null,now().minusHours(1),now().plusHours(1));r=publish(scope(add(r,List.of(reviewer)),List.of(reviewer),List.of(3L,5L)));
  String base="/review-work/"+id(r)+"/tasks?responseMode=tree";assertEquals(2,get(base,expert).path("total").asInt());assertEquals(1,get(base+"&f.year=2025",expert).path("total").asInt());assertEquals(1,get(base+"&f.year=2026",expert).path("total").asInt());
  var profile=get("/system/access",expert).path("currentUser");assertEquals(reviewer,profile.path("userId").asLong());assertEquals("Round user "+reviewer,profile.path("nickName").asText());assertFalse(profile.has("password"));
  var ordinary=get("/biz/tasks/3",business);assertEquals(owner,ordinary.path("leaderId").asLong());assertFalse(ordinary.has("leaderName"));assertEquals(10,ordinary.path("targetValue").asInt());assertEquals(10,ordinary.path("progress").asInt());
  var list=java.util.stream.StreamSupport.stream(get("/biz/tasks",business).spliterator(),false).filter(t->t.path("taskId").asLong()==3).findFirst().orElseThrow();assertEquals("round-"+owner,list.path("leaderName").asText());assertEquals(owner,list.path("principalId").asLong());
 }
 @Test void administrativeReportUsesLatestFormalVersionsAndNeverDrafts()throws Exception{
  var r=active();save(r,3,expert,true,"First formal","pass");save(r,3,expert,true,"Second formal","reject");save(r,3,expert,false,"PRIVATE UNSUBMITTED REVISION","pass");save(r,1,expert,true,"Overall formal","pass");
  String base="/admin/review-tasks/"+id(r);
  var report=get(base+"/overview",admin);assertEquals(1,report.path("summary").path("activeExperts").asInt());assertEquals(1,report.path("summary").path("assignedTasks").asInt());assertEquals(1,report.path("summary").path("leafOpinions").asInt());assertEquals(1,report.path("summary").path("overallOpinions").asInt());assertEquals(1,report.path("summary").path("rejected").asInt());assertEquals(3,report.path("summary").path("revisionCount").asInt());assertFalse(report.toString().contains("PRIVATE"));
  var current=get(base+"/opinion-records",admin);assertEquals(2,current.path("total").asInt());assertEquals(3,get(base+"/opinion-records?allVersions=true",admin).path("total").asInt());
  var leaf=get(base+"/opinion-records?level=3",admin).path("items").get(0);assertEquals(2,leaf.path("revision_no").asInt());var detail=get(base+"/opinion-records/"+leaf.path("revision_id").asLong(),admin);assertEquals("Second formal",detail.path("review_text").asText());assertEquals(2,detail.path("revisions").size());assertEquals(1,detail.path("scope").path("tasks").size());assertFalse(detail.toString().contains("PRIVATE"));
  long v1=detail.path("revisions").get(1).path("revision_id").asLong();assertEquals("First formal",get(base+"/opinion-records/"+v1,admin).path("review_text").asText());
  db.update("UPDATE biz_task SET task_name='Renamed current task',current_value=9 WHERE task_id=3");assertEquals(1,get(base+"/opinion-records/"+v1,admin).path("scope").path("tasks").get(0).path("currentValue").asInt());
  r=fresh(r);ok(call(HttpMethod.POST,base+"/members/"+reviewer+"/remove",admin,Map.of("version",r.path("version").asLong(),"reason","retain formal evidence")));
  var after=get(base+"/overview",admin);assertEquals(0,after.path("summary").path("activeExperts").asInt());assertEquals(1,after.path("experts").size());assertEquals(0,after.path("experts").get(0).path("active").asInt());assertEquals(2,get(base+"/opinion-records",admin).path("total").asInt());
 }
 @Test void administrativeOpinionFiltersAndPaginationAreBoundedAndRoundScoped()throws Exception{
  var r=active();save(r,3,expert,true,"Literal 100%_! evidence","reject");save(r,1,expert,true,"Overall evidence","pass");String base="/admin/review-tasks/"+id(r);
  assertEquals(1,get(base+"/opinion-records?result=reject&expert="+reviewer+"&level=3",admin).path("total").asInt());assertEquals(1,get(base+"/opinion-records?keyword=%",admin).path("total").asInt());
  var a=get(base+"/opinion-records?pageSize=1&page=1",admin);var b=get(base+"/opinion-records?pageSize=1&page=2",admin);assertEquals(2,a.path("total").asInt());assertNotEquals(a.path("items").get(0).path("revision_id"),b.path("items").get(0).path("revision_id"));
  for(String query:List.of("level=2","result=unknown","page=0","pageSize=101","expert=-1"))assertEquals(400,call(HttpMethod.GET,base+"/opinion-records?"+query,admin,null).getStatusCode().value());
  var second=round(2026,now().plusDays(1),now().plusDays(2));long revision=a.path("items").get(0).path("revision_id").asLong();assertEquals(404,call(HttpMethod.GET,"/admin/review-tasks/"+id(second)+"/opinion-records/"+revision,admin,null).getStatusCode().value());
  for(String path:List.of("/overview","/opinion-records","/opinion-records/"+revision,"/activity","/activity/submission/"+revision)){
   assertEquals(403,call(HttpMethod.GET,base+path,business,null).getStatusCode().value());assertEquals(403,call(HttpMethod.GET,base+path,expert,null).getStatusCode().value());
  }
  String administratorExpert=switchTo(admin,"EXPERT");assertEquals(403,call(HttpMethod.GET,base+"/overview",administratorExpert,null).getStatusCode().value());
 }
 @Test void administrativeActivityShowsOnlyActualRoundConfigurationAndFormalSubmissions()throws Exception{
  var r=active();String base="/admin/review-tasks/"+id(r);int configured=get(base+"/activity?type=management",admin).path("total").asInt();assertEquals(4,configured);
  save(r,3,expert,false,"Private draft","pass");assertEquals(configured,get(base+"/activity",admin).path("total").asInt());save(r,3,expert,true,"Formal first","pass");save(r,3,expert,true,"Formal revision","reject");
  var submissions=get(base+"/activity?type=submission&pageSize=1",admin);assertEquals(2,submissions.path("total").asInt());assertEquals("OPINION_RESUBMIT",submissions.path("items").get(0).path("action").asText());long revision=submissions.path("items").get(0).path("id").asLong();assertEquals("Formal revision",get(base+"/activity/submission/"+revision,admin).path("review_text").asText());
  var management=get(base+"/activity?type=management",admin).path("items");long log=management.get(0).path("id").asLong();assertTrue(get(base+"/activity/management/"+log,admin).has("after_json"));
  var second=round(2026,now().plusDays(2),now().plusDays(3));assertEquals(404,call(HttpMethod.GET,"/admin/review-tasks/"+id(second)+"/activity/management/"+log,admin,null).getStatusCode().value());assertEquals(400,call(HttpMethod.GET,base+"/activity?type=unknown",admin,null).getStatusCode().value());assertEquals(400,call(HttpMethod.GET,base+"/activity?pageSize=101",admin,null).getStatusCode().value());
 }

}
