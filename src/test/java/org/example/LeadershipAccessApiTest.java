package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="SHUANGGAO_REVIEW_TEST", matches="true")
@Import(ReviewBatchRegressionApiTest.NoAutomaticSchedules.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties={"logging.file.name=target/leadership-test.log", "logging.level.org.springframework=WARN"})
class LeadershipAccessApiTest {
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        String url = System.getenv("SHUANGGAO_TEST_JDBC_URL");
        if (url == null || !url.matches("jdbc:mysql://127\\.0\\.0\\.1:3306/biz_review_test_[0-9a-f]{32}\\?.*")) throw new IllegalStateException("Isolated schema required");
        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.username", () -> System.getenv("SHUANGGAO_TEST_DB_USER"));
        registry.add("spring.datasource.password", () -> System.getenv("SHUANGGAO_TEST_DB_PASSWORD"));
    }
    @LocalServerPort int port;
    @Autowired TestRestTemplate http;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    @Autowired org.example.service.LeadershipDashboardService dashboard;
    private String admin, school, department;

    @BeforeEach
    void seed() throws Exception {
        jdbc.execute((ConnectionCallback<Void>) connection -> {
            assertTrue(connection.getCatalog().matches("biz_review_test_[0-9a-f]{32}"));
            try (Statement statement = connection.createStatement()) {
                List<String> tables = new ArrayList<>();
                try (ResultSet rows = statement.executeQuery("SHOW FULL TABLES WHERE Table_type='BASE TABLE'")) { while (rows.next()) tables.add(rows.getString(1)); }
                statement.execute("SET FOREIGN_KEY_CHECKS=0");
                for (String table : tables) statement.executeUpdate("DELETE FROM `" + table + "`");
                statement.execute("SET FOREIGN_KEY_CHECKS=1");
            } return null;
        });
        jdbc.update("INSERT INTO sys_dept(dept_id,dept_name,status,is_delete) VALUES(991001,'范围A','0',0),(991002,'范围B','0',0)");
        for (long id : List.of(990001L,990002L,990003L,990004L)) jdbc.update("INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,email,password,role,status,is_delete,force_password_change) VALUES(?,991001,?,?,'test@example.invalid','LeaderTest123',?,'1',0,0)", id, "leader-test-"+id,"合成账号"+id,id==990001?"0":"1");
        jdbc.update("INSERT INTO biz_project(project_id,project_name,leader_id) VALUES(1,'隔离权限项目',990001)");
        for (int id=1;id<=4;id++) jdbc.update("INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,dept_id,leader_id,auditor_id,principal_id,target_value,current_value,progress,status,is_delete) VALUES(?,1,?,2026,?,?,?, ?,990004,990004,990004,10,?,?,'1',0)", id,id==1?0:id==2?1:2,"TEST"+id,"范围任务"+id,id<3?id:3,id==4?991002:991001,id==4?9:1,id==4?90:10);
        jdbc.update("INSERT INTO biz_performance(perf_id,project_id,parent_id,perf_code,perf_name,dept_id,leader_id,auditor_id,principal_id,target_value,current_value,is_delete) VALUES(11,1,0,'2.1','绩效A',991001,990004,990004,990004,10,1,0),(12,1,0,'2.2','绩效B',991002,990004,990004,990004,10,9,0)");
        jdbc.update("INSERT INTO biz_performance_year(year_id,perf_id,year,target_value,actual_value,is_delete) VALUES(21,11,2026,10,1,0),(22,12,2026,10,9,0)");
        java.nio.file.Files.writeString(java.nio.file.Path.of("target/leadership-fixture.pdf"), "synthetic attachment");
        jdbc.update("INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by,is_delete) VALUES(41,'synthetic-A.pdf','/target/leadership-fixture.pdf','/api/system/download/41','pdf',990004,0),(42,'synthetic-B.pdf','/target/leadership-fixture.pdf','/api/system/download/42','pdf',990004,0)");
        jdbc.update("INSERT INTO biz_material_submission(sub_id,task_id,file_id,reported_value,submit_by,submit_dept_id,manage_dept_id,file_suffix,flow_status,current_handler_id,is_delete) VALUES(31,3,41,1,990004,991001,991001,'pdf',10,990004,0),(32,4,42,9,990004,991002,991002,'pdf',10,990004,0)");
        admin=login(990001); school=login(990002); department=login(990003);
        assertEquals(200, request(HttpMethod.PUT,"/admin/users/990002/leadership",admin,change("ALL",0,List.of())).getStatusCode().value());
        assertEquals(200, request(HttpMethod.PUT,"/admin/users/990003/leadership",admin,change("DEPARTMENTS",0,List.of(991001))).getStatusCode().value());
    }
    @Test void scopesApplyToListsDetailsAndStatistics() throws Exception {
        assertEquals(4, body(get("/biz/tasks",school)).size());
        JsonNode tasks=body(get("/biz/tasks",department)); assertEquals(3,tasks.size());
        assertEquals(10,tasks.get(0).path("progress").asInt(),"目录不能展示范围外90%的目标");
        assertEquals(403,get("/biz/tasks/4",department).getStatusCode().value());
        assertEquals(403,get("/biz/tasks/forth?parent_id=4",department).getStatusCode().value());
        assertEquals(403,get("/biz/audit/task/4",department).getStatusCode().value());
        assertEquals(1,body(get("/performance",department)).size());
        assertEquals(1,body(get("/performance/year/2026",department)).size());
        assertEquals(403,get("/performance/12",department).getStatusCode().value());
        assertEquals(1,body(get("/dashboard/summary",department)).path("yearCompletion").path("totalTasks").asInt());
        assertEquals(1,body(get("/dashboard/dept/overall",department)).size());
        assertEquals(403,get("/dashboard/dept/991002",department).getStatusCode().value());
    }
    @Test void permissionsUseDatabaseWithOldTokensAndDoNotGrantWrites() throws Exception {
        assertTrue(body(get("/system/access",department)).path("isLeader").asBoolean());
        assertFalse(body(get("/system/access",department)).path("canViewAuditCenter").asBoolean());
        assertEquals(403,get("/admin/users",school).getStatusCode().value());
        assertEquals(403,request(HttpMethod.POST,"/performance/refresh",school,Map.of()).getStatusCode().value());
        assertTrue(body(get("/admin/overview?year=2026&month=9",department)).path("readOnly").asBoolean());
        assertEquals(409,request(HttpMethod.PUT,"/admin/users/990003/leadership",admin,change("ALL",0,List.of())).getStatusCode().value());
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM sys_admin_operation_log WHERE action='LEADERSHIP_ACCESS'",Integer.class));
        assertEquals(200,request(HttpMethod.PUT,"/admin/users/990003/leadership",admin,change("ALL",1,List.of())).getStatusCode().value());
        assertEquals(4,body(get("/biz/tasks",department)).size(),"不重新登录即可扩大范围");
        jdbc.update("UPDATE sys_user SET status='0' WHERE user_id=990003");
        assertEquals(403,get("/biz/tasks",department).getStatusCode().value());
        jdbc.update("UPDATE sys_user SET role='1' WHERE user_id=990001");
        assertEquals(403,request(HttpMethod.PUT,"/admin/users/990002/leadership",admin,change("NONE",1,List.of())).getStatusCode().value());
    }
    @Test void fileLinksAndExportCannotEscapeScope() throws Exception {
        assertEquals(200,get("/system/download/41",department).getStatusCode().value());
        assertEquals(403,get("/system/download/42",department).getStatusCode().value());
        ResponseEntity<String> export=request(HttpMethod.POST,"/biz/tasks/export",department,Map.of("ids",List.of(3,4)));
        assertEquals(200,export.getStatusCode().value());
        assertTrue(export.getHeaders().getContentType().toString().contains("spreadsheet"));
        assertEquals(403,get("/performance/audit/logs/999999",department).getStatusCode().value());
    }
    @Test void cancellingLeadershipImmediatelyRevokesDirectLinksAndKeepsExplicitDuties() throws Exception {
        assertEquals(200,request(HttpMethod.PUT,"/admin/users/990003/leadership",admin,change("NONE",1,List.of())).getStatusCode().value());
        assertFalse(body(get("/system/access",department)).path("isLeader").asBoolean());
        assertEquals(0,body(get("/biz/tasks",department)).size());
        assertEquals(403,get("/biz/tasks/3",department).getStatusCode().value());
        assertEquals(403,get("/system/download/41",department).getStatusCode().value());
        assertEquals(403,get("/admin/overview?year=2026&month=9",department).getStatusCode().value());
        jdbc.update("UPDATE biz_task SET auditor_id=990003 WHERE task_id=3");
        jdbc.update("UPDATE biz_material_submission SET current_handler_id=990003 WHERE sub_id=31");
        assertEquals(200,get("/biz/tasks/3",department).getStatusCode().value());
        assertEquals(200,request(HttpMethod.POST,"/biz/audit",department,Map.of("sub_id",31,"is_pass",true,"title","合成审核","content","保留明确职责")).getStatusCode().value());
        assertEquals(20,jdbc.queryForObject("SELECT flow_status FROM biz_material_submission WHERE sub_id=31",Integer.class));
    }
    @Test void concurrentConfigurationOnlyAllowsOneWriterAndInvalidDepartmentDoesNotWrite() throws Exception {
        assertEquals(400,request(HttpMethod.PUT,"/admin/users/990003/leadership",admin,change("DEPARTMENTS",1,List.of(999999))).getStatusCode().value());
        assertEquals(1,body(get("/admin/users/990003/leadership",admin)).path("version").asInt());
        var pool=java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            var first=pool.submit(() -> request(HttpMethod.PUT,"/admin/users/990003/leadership",admin,change("ALL",1,List.of())).getStatusCode().value());
            var second=pool.submit(() -> request(HttpMethod.PUT,"/admin/users/990003/leadership",admin,change("DEPARTMENTS",1,List.of(991002))).getStatusCode().value());
            var statuses=new ArrayList<>(List.of(first.get(),second.get())); Collections.sort(statuses); assertEquals(List.of(200,409),statuses);
        } finally { pool.shutdownNow(); }
        assertEquals(2,body(get("/admin/users/990003/leadership",admin)).path("version").asInt());
        assertEquals(3,jdbc.queryForObject("SELECT COUNT(*) FROM sys_admin_operation_log WHERE action='LEADERSHIP_ACCESS'",Integer.class));
    }
    @Test void leadershipAloneDoesNotGrantWorkRecordFillingAndDepartmentTrendsNeverUseSchoolHistory() throws Exception {
        assertTrue(body(get("/work-records/capabilities",department)).path("canCreate").asBoolean(),"所属归口部门有任务时，可按部门身份填报");
        jdbc.update("INSERT INTO sys_dept(dept_id,dept_name,status,is_delete) VALUES(991003,'无任务部门','0',0)");
        jdbc.update("UPDATE sys_user SET dept_id=991003 WHERE user_id=990003");
        JsonNode capabilities=body(get("/work-records/capabilities",department));
        assertTrue(capabilities.path("canViewAll").asBoolean()); assertTrue(capabilities.path("canExport").asBoolean()); assertFalse(capabilities.path("canCreate").asBoolean());
        assertEquals(403,request(HttpMethod.POST,"/work-records",department,Map.of("year",2026,"month",1)).getStatusCode().value());
        jdbc.update("INSERT INTO biz_work_record(record_id,owner_id,owner_name,record_year,record_month,status,create_time,update_time) VALUES(51,990004,'合成填报人',2026,1,1,NOW(),NOW()),(52,990004,'合成填报人',2026,2,0,NOW(),NOW())");
        jdbc.update("INSERT INTO biz_work_record_snapshot(record_id,statistics_json,generated_time) VALUES(51,?,NOW())",json.writeValueAsString(new org.example.entity.vo.WorkRecordVO.Statistics(1,2026,1,"2026-01-31","2026-01-31",0,0,0,null,0,"冻结合成快照",List.of(),List.of())));
        assertEquals(1,body(get("/work-records",department)).path("total").asInt());
        assertFalse(body(get("/work-records/51",department)).path("editable").asBoolean());
        assertEquals(404,get("/work-records/52",department).getStatusCode().value());
        jdbc.update("INSERT INTO biz_trend_data(year,month,day,total_tasks,completion_count,completion_rate,is_delete) VALUES(2026,1,1,100,50,50,0)");
        assertEquals(0,body(get("/dashboard/trend/2026",department)).size(),"历史只有全校汇总时不能返回全校值或虚构部门值");
        dashboard.recordDepartmentTrend(); dashboard.recordDepartmentTrend();
        JsonNode scoped=body(get("/dashboard/trend/2026",department));
        assertEquals(1,scoped.size()); assertEquals(1,scoped.get(0).path("totalTasks").asInt()); assertEquals(10,scoped.get(0).path("completionRate").asDouble());
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM biz_department_trend",Integer.class),"各部门每日只留一份快照");
    }
    @Test void migrationInitializesOnlyApprovedIdsAndNeverOverwritesLaterConfiguration() throws Exception {
        jdbc.update("INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,email,password,role,status,is_delete,force_password_change) VALUES(121552,991001,'synthetic_liu','刘莹','test@example.invalid','LeaderTest123','1','1',0,0),(110007,991001,'synthetic_bao','鲍桂楠','test@example.invalid','LeaderTest123','1','1',0,0),(990099,991001,'same_name','刘莹','test@example.invalid','LeaderTest123','1','1',0,0)");
        String migration=java.nio.file.Files.readString(java.nio.file.Path.of("data/migrations/2026-10-12-leadership-access.sql")).replaceAll("(?m)^--.*$", "");
        for (int iteration=0; iteration<2; iteration++) for (String statement : migration.split(";")) if (!statement.isBlank()) jdbc.execute(statement);
        assertEquals("ALL",jdbc.queryForObject("SELECT scope FROM sys_leadership_access WHERE user_id=121552",String.class));
        assertEquals("ALL",jdbc.queryForObject("SELECT scope FROM sys_leadership_access WHERE user_id=110007",String.class));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM sys_leadership_access WHERE user_id=990099",Integer.class));
        jdbc.update("UPDATE sys_leadership_access SET scope='NONE',version=2 WHERE user_id=121552");
        for (String statement : migration.split(";")) if (!statement.isBlank()) jdbc.execute(statement);
        assertEquals("NONE",jdbc.queryForObject("SELECT scope FROM sys_leadership_access WHERE user_id=121552",String.class));
        assertEquals(4,jdbc.queryForObject("SELECT COUNT(*) FROM biz_task",Integer.class));
        assertEquals(9,jdbc.queryForObject("SELECT current_value FROM biz_task WHERE task_id=4",Integer.class));
    }
    @Test void explicitBusinessDutiesRemainAvailableWithoutExpandingOverviewScope() throws Exception {
        jdbc.update("UPDATE biz_task SET auditor_id=990003 WHERE task_id=4");
        jdbc.update("UPDATE biz_material_submission SET current_handler_id=990003 WHERE sub_id=32");
        jdbc.update("UPDATE biz_performance SET leader_id=990003 WHERE perf_id=12");
        assertTrue(body(get("/system/access",department)).path("canViewAuditCenter").asBoolean());
        assertEquals(200,get("/biz/tasks/4",department).getStatusCode().value());
        assertEquals(200,get("/performance/12",department).getStatusCode().value());
        assertEquals(1,body(get("/dashboard/summary",department)).path("yearCompletion").path("totalTasks").asInt(),"概览仍严格按分管部门统计，职责不能扩大领导统计范围");
        assertEquals(200,request(HttpMethod.POST,"/biz/audit",department,Map.of("sub_id",32,"is_pass",true,"title","明确审核职责","content","职责不因领导配置丢失")).getStatusCode().value());
        assertEquals(20,jdbc.queryForObject("SELECT flow_status FROM biz_material_submission WHERE sub_id=32",Integer.class));
    }
    private Map<String,Object> change(String scope,long version,List<Integer> ids) { return Map.of("scope",scope,"version",version,"departmentIds",ids,"reason","隔离权限验证"); }
    private String login(long id) throws Exception { String token=body(request(HttpMethod.POST,"/system/login",null,Map.of("user_id",id,"password","LeaderTest123"))).path("token").asText(); assertFalse(token.isEmpty());return token; }
    private ResponseEntity<String> get(String path,String token) { return request(HttpMethod.GET,path,token,null); }
    private ResponseEntity<String> request(HttpMethod method,String path,String token,Object body) { HttpHeaders headers=new HttpHeaders();headers.setContentType(MediaType.APPLICATION_JSON);if(token!=null)headers.set("Authorization",token);return http.exchange("http://127.0.0.1:"+port+"/api"+path,method,new HttpEntity<>(body,headers),String.class); }
    private JsonNode body(ResponseEntity<String> response) throws Exception { assertEquals(200,response.getStatusCode().value(),response.getBody());return json.readTree(response.getBody()); }
}
