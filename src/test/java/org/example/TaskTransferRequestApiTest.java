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
class TaskTransferRequestApiTest {
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
  admin=switchTo(login(110228),"BUSINESS");business=login(owner);expert=login(reviewer);recipients();
 }
 ResponseEntity<String> call(HttpMethod method,String path,String token,Object value){var h=new HttpHeaders();if(token!=null)h.set("Authorization",token);h.setContentType(MediaType.APPLICATION_JSON);return http.exchange("http://127.0.0.1:"+port+"/api"+path,method,new HttpEntity<>(value,h),String.class);}
 JsonNode ok(ResponseEntity<String> r)throws Exception{assertEquals(200,r.getStatusCode().value(),r.getBody());return json.readTree(r.getBody());}
 JsonNode get(String path,String token)throws Exception{return ok(call(HttpMethod.GET,path,token,null));}
 String login(long id)throws Exception{return ok(call(HttpMethod.POST,"/system/login",null,Map.of("user_id",id,"password","RoundTest123"))).path("token").asText();}
 String switchTo(String token,String experience)throws Exception{return ok(call(HttpMethod.POST,"/system/review-context",token,Map.of("experience",experience))).path("token").asText();}

 String recipient;
 void recipients()throws Exception{
  db.update("INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,password,role,status,is_delete,force_password_change) VALUES(90105,991001,'recipient','Synthetic recipient','RoundTest123','1','1',0,0)");
  db.update("UPDATE biz_task SET leader_id=90105,auditor_id=90105,principal_id=90105 WHERE task_id=4");recipient=login(90105);
 }
 JsonNode create(String field)throws Exception{return ok(call(HttpMethod.POST,"/task-transfer/request",business,Map.of("taskId",3,"fieldType",field,"toUserId",90105,"reason","Synthetic reassignment")));}
 String cleared(long user)throws Exception{String t=user==110228?switchTo(login(user),"BUSINESS"):login(user);for(var n:get("/platform-notices/status",t).path("pending")){var opened=ok(call(HttpMethod.POST,"/platform-notices/"+n.path("id").asLong()+"/open",t,Map.of("requestId",UUID.randomUUID().toString())));ok(call(HttpMethod.POST,"/platform-notices/"+n.path("id").asLong()+"/confirm",t,Map.of("contentVersion",opened.path("contentVersion").asInt(),"viewKey",opened.path("viewKey").asText())));}return t;}
 void activeAudit(){db.update("INSERT IGNORE INTO sys_file(file_id,file_name,file_path,file_url,upload_by,is_delete) VALUES(41,'synthetic.pdf','/uploads/synthetic.pdf','/uploads/synthetic.pdf',90101,0)");db.update("INSERT INTO biz_material_submission(task_id,file_id,submit_by,submit_dept_id,manage_dept_id,file_suffix,flow_status,current_handler_id,is_delete) VALUES(3,41,90101,991001,991001,'pdf',10,90101,0)");}
 @Test void threeDutiesUseRecipientConfirmationAndOnlyChangeTheSelectedField()throws Exception{
  for(String field:List.of("leader","auditor","principal")){
   business=cleared(owner);recipient=cleared(90105);var request=create(field);long id=request.path("requestId").asLong();
   assertEquals(owner,db.queryForObject("SELECT "+field+"_id FROM biz_task WHERE task_id=3",Long.class));
   assertEquals(1,get("/task-transfer/pending",recipient).size());
   var accepted=ok(call(HttpMethod.POST,"/task-transfer/"+id+"/accept",cleared(90105),Map.of()));assertEquals("ACCEPTED",accepted.path("status").asText());
   assertEquals(90105L,db.queryForObject("SELECT "+field+"_id FROM biz_task WHERE task_id=3",Long.class));
   assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM biz_ownership_change_log WHERE "+field+"_after_id=90105",Integer.class));
  }
  assertEquals(3,db.queryForObject("SELECT COUNT(*) FROM biz_ownership_change_log",Integer.class));
 }
 @Test void rejectKeepsOwnershipAndRecordsReasonAndNotice()throws Exception{
  var request=create("leader");var result=ok(call(HttpMethod.POST,"/task-transfer/"+request.path("requestId").asLong()+"/reject",cleared(90105),Map.of("remark","Workload requires discussion")));
  assertEquals("REJECTED",result.path("status").asText());assertEquals(owner,db.queryForObject("SELECT leader_id FROM biz_task WHERE task_id=3",Long.class));assertEquals(0,db.queryForObject("SELECT COUNT(*) FROM biz_ownership_change_log",Integer.class));assertEquals(0,get("/task-transfer/pending",cleared(90105)).size());
 }
 @Test void duplicateAndForeignProcessingAreRejected()throws Exception{
  long id=create("leader").path("requestId").asLong();business=cleared(owner);
  assertEquals(409,call(HttpMethod.POST,"/task-transfer/request",business,Map.of("taskId",3,"fieldType","leader","toUserId",90105,"reason","duplicate")).getStatusCode().value());
  assertEquals(403,call(HttpMethod.POST,"/task-transfer/"+id+"/accept",business,Map.of()).getStatusCode().value());
  assertEquals(403,call(HttpMethod.POST,"/task-transfer/request",recipient,Map.of("taskId",3,"fieldType","auditor","toUserId",owner,"reason","not mine")).getStatusCode().value());
  assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM biz_task_transfer_request",Integer.class));
 }
 @Test void activeAuditsBlockBothRequestAndAcceptanceWithoutPartialEffects()throws Exception{
  activeAudit();assertEquals(409,call(HttpMethod.POST,"/task-transfer/request",business,Map.of("taskId",3,"fieldType","leader","toUserId",90105,"reason","audit active")).getStatusCode().value());
  assertEquals(0,db.queryForObject("SELECT COUNT(*) FROM sys_platform_notice",Integer.class));db.update("DELETE FROM biz_material_submission");long id=create("auditor").path("requestId").asLong();activeAudit();
  assertEquals(409,call(HttpMethod.POST,"/task-transfer/"+id+"/accept",cleared(90105),Map.of()).getStatusCode().value());assertEquals(owner,db.queryForObject("SELECT auditor_id FROM biz_task WHERE task_id=3",Long.class));assertEquals("PENDING",db.queryForObject("SELECT status FROM biz_task_transfer_request WHERE request_id=?",String.class,id));
 }
 @Test void recipientsNeedActiveAccountsSameDutyAndProspectiveScope()throws Exception{
  for(String clause:List.of("UPDATE sys_user SET is_delete=1 WHERE user_id=90105","UPDATE sys_user SET status='0' WHERE user_id=90105","UPDATE biz_task SET leader_id=90103 WHERE task_id=4")){
   db.update(clause);assertTrue(call(HttpMethod.POST,"/task-transfer/request",business,Map.of("taskId",3,"fieldType","leader","toUserId",90105,"reason","invalid recipient")).getStatusCode().value()>=400);db.update("UPDATE sys_user SET status='1',is_delete=0 WHERE user_id=90105");db.update("UPDATE biz_task SET leader_id=90105 WHERE task_id=4");
  }
  db.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json) VALUES(90105,'CUSTOM','{\"tasks.read\":true,\"tasks.fill\":true}','{\"tasks\":{\"mode\":\"CUSTOM\",\"items\":[4],\"years\":[2026]}}')");
  assertEquals(409,call(HttpMethod.POST,"/task-transfer/request",business,Map.of("taskId",3,"fieldType","leader","toUserId",90105,"reason","outside scope")).getStatusCode().value());assertEquals(0,db.queryForObject("SELECT COUNT(*) FROM biz_task_transfer_request",Integer.class));
 }
 @Test void scopePrivacyAndSignedExpertModeProtectNewEndpoints()throws Exception{
  assertEquals(401,call(HttpMethod.GET,"/task-transfer/pending",null,null).getStatusCode().value());
  assertEquals(403,call(HttpMethod.GET,"/task-transfer/logs?taskId=3",expert,null).getStatusCode().value());
  assertEquals(403,call(HttpMethod.GET,"/task-transfer/users?taskId=3&fieldType=leader",expert,null).getStatusCode().value());
  String mode=switchTo(login(110228),"EXPERT");assertEquals(403,call(HttpMethod.GET,"/task-transfer/pending",mode,null).getStatusCode().value());
  db.update("UPDATE sys_user SET force_password_change=1 WHERE user_id=?",owner);assertEquals(428,call(HttpMethod.GET,"/task-transfer/pending",business,null).getStatusCode().value());
 }
 @Test void permissionsAndOwnershipAreRecheckedAtAcceptance()throws Exception{
  long id=create("leader").path("requestId").asLong();recipient=cleared(90105);db.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json) VALUES(90105,'CUSTOM','{\"tasks.read\":true}','{}')");
  assertEquals(409,call(HttpMethod.POST,"/task-transfer/"+id+"/accept",recipient,Map.of()).getStatusCode().value());assertEquals(owner,db.queryForObject("SELECT leader_id FROM biz_task WHERE task_id=3",Long.class));
  db.update("DELETE FROM sys_user_permission WHERE user_id=90105");db.update("UPDATE biz_task SET leader_id=90103 WHERE task_id=3");
  assertEquals(409,call(HttpMethod.POST,"/task-transfer/"+id+"/accept",recipient,Map.of()).getStatusCode().value());assertEquals(0,db.queryForObject("SELECT COUNT(*) FROM biz_ownership_change_log",Integer.class));
 }
 @Test void concurrentAcceptIsExactlyOnceAndRetainsOtherDutyFields()throws Exception{
  long id=create("leader").path("requestId").asLong();String token=cleared(90105);var pool=Executors.newFixedThreadPool(2);
  try{var first=pool.submit(()->call(HttpMethod.POST,"/task-transfer/"+id+"/accept",token,Map.of()).getStatusCode().value());var second=pool.submit(()->call(HttpMethod.POST,"/task-transfer/"+id+"/accept",token,Map.of()).getStatusCode().value());var statuses=new ArrayList<>(List.of(first.get(),second.get()));Collections.sort(statuses);assertEquals(List.of(200,409),statuses);}finally{pool.shutdownNow();}
  assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM biz_ownership_change_log",Integer.class));assertEquals(owner,db.queryForObject("SELECT auditor_id FROM biz_task WHERE task_id=3",Long.class));assertEquals(owner,db.queryForObject("SELECT principal_id FROM biz_task WHERE task_id=3",Long.class));
 }
 @Test void concurrentRequestsHaveOnePendingAndOnlyOneNotificationSet()throws Exception{
  var pool=Executors.newFixedThreadPool(2);try{var first=pool.submit(()->call(HttpMethod.POST,"/task-transfer/request",business,Map.of("taskId",3,"fieldType","leader","toUserId",90105,"reason","concurrent")).getStatusCode().value());var second=pool.submit(()->call(HttpMethod.POST,"/task-transfer/request",business,Map.of("taskId",3,"fieldType","leader","toUserId",90105,"reason","concurrent")).getStatusCode().value());var statuses=new ArrayList<>(List.of(first.get(),second.get()));Collections.sort(statuses);assertEquals(List.of(200,409),statuses);}finally{pool.shutdownNow();}assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM biz_task_transfer_request",Integer.class));assertEquals(2,db.queryForObject("SELECT COUNT(*) FROM sys_platform_notice",Integer.class));
 }
 @Test void candidatesAreTaskBoundAndCannotLeakOtherTaskLogs()throws Exception{
  var candidates=get("/task-transfer/users?taskId=3&fieldType=leader",business);assertTrue(candidates.toString().contains("90105"));assertFalse(candidates.toString().contains("90101"));var missing=call(HttpMethod.GET,"/task-transfer/users?fieldType=leader",business,null);assertEquals(400,missing.getStatusCode().value(),missing.getBody());
  var directory=call(HttpMethod.POST,"/task-transfer/request",business,Map.of("taskId",1,"fieldType","leader","toUserId",90105,"reason","directory"));assertEquals(400,directory.getStatusCode().value(),directory.getBody());
 }
}
