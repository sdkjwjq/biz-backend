package org.example;

import org.example.mapper.BizMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="SHUANGGAO_REVIEW_TEST",matches="true")
@Import(ReviewBatchRegressionApiTest.NoAutomaticSchedules.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskAttributesApiTest {
 @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
  String url=System.getenv("SHUANGGAO_TEST_JDBC_URL");
  if(url==null||!url.matches("jdbc:mysql://127\\.0\\.0\\.1:3306/biz_review_test_[0-9a-f]{32}\\?.*"))throw new IllegalStateException("Isolated database required");
  registry.add("spring.datasource.url",()->url);registry.add("spring.datasource.username",()->System.getenv("SHUANGGAO_TEST_DB_USER"));registry.add("spring.datasource.password",()->System.getenv("SHUANGGAO_TEST_DB_PASSWORD"));
 }
 @Autowired JdbcTemplate jdbc; @Autowired TestRestTemplate http; @Autowired BizMapper tasks; @LocalServerPort int port;
 String admin,user;
 @BeforeEach void seed() throws Exception {
  jdbc.update("INSERT IGNORE INTO sys_dept(dept_id,dept_name,status,is_delete) VALUES(1,'合成属性部门','0',0)");
  jdbc.update("DELETE FROM biz_audit_log WHERE sub_id=998001");jdbc.update("DELETE FROM biz_material_submission WHERE sub_id=998001");
  jdbc.update("DELETE FROM biz_task WHERE task_id=998001");
  jdbc.update("DELETE FROM sys_admin_operation_log WHERE target_id='998001'");
  for(long id:List.of(998001L,998002L)){
   jdbc.update("INSERT INTO sys_user(user_id,user_name,nick_name,password,role,status,is_delete,force_password_change) VALUES(?,?,?,'TestPass123',?,'1',0,0) ON DUPLICATE KEY UPDATE password='TestPass123',status='1',is_delete=0",id,"attr"+id,"合成属性账号",id==998001?"0":"1");
  }
  jdbc.update("INSERT IGNORE INTO biz_project(project_id,project_name,leader_id) VALUES(1,'合成属性项目',998001)");
  jdbc.update("INSERT IGNORE INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by,is_delete) VALUES(1,'synthetic.pdf','/target/synthetic.pdf','/api/system/download/1','pdf',998002,0)");
  jdbc.update("INSERT INTO biz_task(task_id,project_id,phase,task_name,level,target_value,current_value,progress,status,is_delete,leader_id,principal_id,auditor_id,dept_id) VALUES(998001,1,2026,'合成管理属性任务',3,10,5,50,'1',0,998002,998001,998001,1)");
  admin=login(998001);user=login(998002);
 }
 String login(long id)throws Exception {
  String body=http.postForObject("http://127.0.0.1:"+port+"/api/system/login",Map.of("user_id",id,"password","TestPass123"),String.class);
  return new com.fasterxml.jackson.databind.ObjectMapper().readTree(body).path("token").asText();
 }
 ResponseEntity<String> save(String token,long version){HttpHeaders headers=new HttpHeaders();headers.set("Authorization",token);return http.exchange("http://127.0.0.1:"+port+"/api/manage/tasks/998001/attributes",HttpMethod.PUT,new HttpEntity<>(Map.of("priority",true,"difficult",true,"plannedCompletionDate","2027-03-01","version",version,"reason","合成变更验证"),headers),String.class);}
 @Test void adminSavePreservesBusinessValuesAndRejectsStaleVersions(){
  assertEquals(403,save(user,0).getStatusCode().value());assertEquals(200,save(admin,0).getStatusCode().value());
  assertEquals(409,save(admin,0).getStatusCode().value());
  var task=tasks.getTaskById(998001L);assertTrue(task.getIsPriority());assertTrue(task.getIsDifficult());assertEquals(1L,task.getManagementVersion());
  assertEquals(5,task.getCurrentValue().intValue());assertEquals(50,task.getProgress());assertEquals("1",task.getStatus());assertNull(task.getActualCompletionDate());
  assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM sys_admin_operation_log WHERE target_id='998001' AND action='ATTRIBUTES'",Integer.class));
 }
 @Test void firstCompletionIsReadOnlyAndNeverOverwritten(){
  tasks.recordActualCompletion(998001L);assertNull(tasks.getTaskById(998001L).getActualCompletionDate());
  jdbc.update("UPDATE biz_task SET current_value=10,status='3' WHERE task_id=998001");tasks.recordActualCompletion(998001L);
  Date first=tasks.getTaskById(998001L).getActualCompletionDate();assertNotNull(first);tasks.recordActualCompletion(998001L);assertEquals(first,tasks.getTaskById(998001L).getActualCompletionDate());
 }
 @Test void migrationIsIdempotentAndRequiresArchiveEvidence()throws Exception {
  jdbc.update("INSERT INTO biz_material_submission(sub_id,task_id,file_id,reported_value,submit_by,submit_dept_id,manage_dept_id,file_suffix,flow_status,submit_time,is_delete) VALUES(998001,998001,1,10,998002,1,1,'pdf',40,'2026-07-01',0)");
  migration();assertNull(tasks.getTaskById(998001L).getActualCompletionDate());
  jdbc.update("INSERT INTO biz_audit_log(sub_id,operator_id,post_status,pre_status,action_type,comment,create_time) VALUES(998001,998001,40,30,'通过','合成归档证据','2026-07-02')");
  migration();assertNotNull(tasks.getTaskById(998001L).getActualCompletionDate());
  assertEquals(200,save(admin,0).getStatusCode().value());migration();assertEquals("2027-03-01",tasks.getTaskById(998001L).getPlannedCompletionDate().toString());
 }
 void migration()throws Exception {for(String sql:Files.readString(Path.of("data/migrations/2026-10-12-task-attributes.sql")).split(";"))if(!sql.isBlank())jdbc.execute(sql);}
}
