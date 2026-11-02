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
class DashboardAccessApiTest {
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


 @Autowired org.example.service.PermissionService permissions;
 void account(long id,long dept,String role){db.update("INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,password,role,status,is_delete,force_password_change) VALUES(?,?,?,?,'RoundTest123',?,'1',0,0)",id,dept,"synthetic-"+id,"Synthetic "+id,role);}
 @Test void dashboardIsLimitedToCurrentSchoolLeadersOfficeAndAdministrators()throws Exception{
  db.update("INSERT INTO sys_dept(dept_id,dept_name,status,is_delete) VALUES(100,'Double high office','0',0),(104,'Finance','0',0),(106,'Ordinary department','0',0)");
  account(121362,106,"1");account(90110,106,"2");account(90111,100,"1");account(90112,104,"1");account(90113,106,"1");
  db.update("INSERT INTO sys_leadership_access(user_id,scope,version) VALUES(90113,'ALL',0)");
  for(long id:List.of(121362L,90110L,90112L)){
   String token=login(id);var access=get("/system/access",token);assertFalse(access.path("canViewDashboard").asBoolean());assertFalse(access.path("permissions").toString().contains("dashboard.read"));
   for(String url:List.of("/dashboard/summary","/dashboard/completion/overall","/dashboard/trend/2026"))assertEquals(403,call(HttpMethod.GET,url,token,null).getStatusCode().value(),url);
   assertEquals(200,call(HttpMethod.GET,"/biz/tasks",token,null).getStatusCode().value());
  }
  for(long id:List.of(110228L,90111L,90113L)){
   String token=id==110228?admin:login(id);assertTrue(get("/system/access",token).path("canViewDashboard").asBoolean());assertEquals(200,call(HttpMethod.GET,"/dashboard/summary",token,null).getStatusCode().value());
  }
 }
 @Test void oldStoredRolesAndPersonalGrantsCannotReopenOrdinaryDashboardAccess()throws Exception{
  db.update("INSERT INTO sys_permission_role(role_code,role_name,permissions_json,scopes_json,enabled,version) VALUES('USER','Old ordinary template',?,'{}',1,1)","[\"tasks.read\",\"dashboard.read\"]");
  assertFalse(get("/system/access",business).path("permissions").toString().contains("dashboard.read"));
  db.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json) VALUES(?,'CUSTOM','{\"dashboard.read\":true,\"tasks.read\":true}','{}')",owner);
  assertEquals(403,call(HttpMethod.GET,"/dashboard/summary",business,null).getStatusCode().value());
  db.update("INSERT INTO sys_user_permission_role(user_id,role_code) VALUES(?,'LEADERSHIP')",owner);
  assertFalse(get("/system/access",business).path("canViewDashboard").asBoolean());
 }
 @Test void changingIdentityOrDisablingDepartmentRevokesAnExistingTokenImmediately()throws Exception{
  db.update("INSERT INTO sys_leadership_access(user_id,scope,version) VALUES(?,'ALL',0)",owner);
  assertTrue(get("/system/access",business).path("canViewDashboard").asBoolean());
  db.update("UPDATE sys_leadership_access SET scope='NONE',version=version+1 WHERE user_id=?",owner);
  assertEquals(403,call(HttpMethod.GET,"/dashboard/summary",business,null).getStatusCode().value());
  db.update("INSERT INTO sys_dept(dept_id,dept_name,status,is_delete) VALUES(100,'Office','0',0)");db.update("UPDATE sys_user SET dept_id=100 WHERE user_id=?",owner);
  assertTrue(get("/system/access",business).path("canViewDashboard").asBoolean());db.update("UPDATE sys_dept SET status='1' WHERE dept_id=100");
  assertEquals(403,call(HttpMethod.GET,"/dashboard/summary",business,null).getStatusCode().value());
 }
 void passwordMigration()throws Exception{
  String script=java.nio.file.Files.readString(java.nio.file.Path.of("scripts/password/001_force_password_change.sql"));
  db.execute((org.springframework.jdbc.core.ConnectionCallback<Void>)connection->{assertTrue(connection.getCatalog().matches("biz_review_test_[0-9a-f]{32}"));try(var statement=connection.createStatement()){for(String sql:script.replaceAll("(?m)^--.*$","").split(";"))if(!sql.isBlank())statement.execute(sql);}return null;});
 }
 @Test void successfulPasswordChangeSurvivesRepeatedUpdateMigrationsAndLogin()throws Exception{
  db.update("UPDATE sys_user SET force_password_change=1 WHERE user_id=?",owner);
  var first=ok(call(HttpMethod.POST,"/system/login",null,Map.of("user_id",owner,"password","RoundTest123")));assertTrue(first.path("requiresPasswordChange").asBoolean());String token=first.path("token").asText();
  ok(call(HttpMethod.POST,"/system/password",token,Map.of("new_password","ChangedOnlyOnce123")));
  assertEquals(0,db.queryForObject("SELECT force_password_change FROM sys_user WHERE user_id=?",Integer.class,owner));
  passwordMigration();passwordMigration();
  var again=ok(call(HttpMethod.POST,"/system/login",null,Map.of("user_id",owner,"password","ChangedOnlyOnce123")));assertFalse(again.path("requiresPasswordChange").asBoolean());
  assertFalse(get("/system/password/status",again.path("token").asText()).path("requiresPasswordChange").asBoolean());
 }
 @Test void strongHistoricalPasswordDoesNotProveTheForcedChangeFlagWasCleared()throws Exception{
  db.update("UPDATE sys_user SET force_password_change=1 WHERE user_id=?",owner);passwordMigration();passwordMigration();
  assertEquals(1,db.queryForObject("SELECT force_password_change FROM sys_user WHERE user_id=?",Integer.class,owner));
  assertTrue(ok(call(HttpMethod.POST,"/system/login",null,Map.of("user_id",owner,"password","RoundTest123"))).path("requiresPasswordChange").asBoolean());
 }
}
