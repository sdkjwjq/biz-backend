package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.config.TaskManagementConfigUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@EnabledIfEnvironmentVariable(named="SHUANGGAO_REVIEW_TEST", matches="true")
@Import(AdminTestApiTest.NoSchedules.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={
        "work-records.viewer-user-ids=910005", "logging.file.name=target/work-record-regression.log",
        "logging.level.org.springframework=WARN", "logging.level.org.example.mapper=WARN"})
class AdminTestApiTest {
    private static final long ADMIN=110228, OWNER=910001, OTHER=910002, LEADER=910003, VIEWER=910005, OFFICE=910006;
    private static final String PASSWORD="WorkRecords123";
    @Autowired private TestRestTemplate http;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ObjectMapper json;
    @LocalServerPort private int port;
    @MockBean(name="workRecordClock") private Clock clock;
    @Autowired private org.example.service.CoreIndicatorService core;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        String url = System.getenv("SHUANGGAO_TEST_JDBC_URL");
        if (url == null || !url.matches("jdbc:mysql://127\\.0\\.0\\.1:3306/biz_review_test_[0-9a-f]{32}\\?.*")) {
            throw new IllegalStateException("Only isolated local databases are allowed");
        }
        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.username", () -> System.getenv("SHUANGGAO_TEST_DB_USER"));
        registry.add("spring.datasource.password", () -> System.getenv("SHUANGGAO_TEST_DB_PASSWORD"));
    }

    @TestConfiguration(proxyBeanMethods=false)
    static class NoSchedules {
        @Bean static BeanFactoryPostProcessor disableSchedules() {
            return factory -> {
                BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory;
                if (registry.containsBeanDefinition(TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
                    registry.removeBeanDefinition(TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME);
                }
            };
        }
    }

    @BeforeEach
    void seed() {
        http.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
        Clock fixed = Clock.fixed(Instant.parse("2026-09-19T04:00:00Z"), ZoneId.of("Asia/Shanghai"));
        when(clock.instant()).thenReturn(fixed.instant());
        when(clock.getZone()).thenReturn(fixed.getZone());
        when(clock.withZone(any())).thenAnswer(call -> fixed.withZone(call.getArgument(0)));
        jdbc.execute((ConnectionCallback<Void>) connection -> {
            assertTrue(connection.getCatalog().matches("biz_review_test_[0-9a-f]{32}"));
            try (Statement statement = connection.createStatement()) {
                List<String> tables = new ArrayList<>();
                try (ResultSet rows = statement.executeQuery("SHOW FULL TABLES WHERE Table_type='BASE TABLE'")) {
                    while (rows.next()) tables.add(rows.getString(1));
                }
                statement.execute("SET FOREIGN_KEY_CHECKS=0");
                try { for (String table : tables) { assertTrue(table.matches("[A-Za-z0-9_]+")); statement.executeUpdate("DELETE FROM `" + table + "`"); } }
                finally { statement.execute("SET FOREIGN_KEY_CHECKS=1"); }
            }
            return null;
        });
        jdbc.update("INSERT INTO sys_dept(dept_id,dept_name,is_delete) VALUES(920001,'Synthetic department',0),(100,'“双高”建设办公室',0)");
        for (long id : List.of(ADMIN, OWNER, OTHER, LEADER, VIEWER)) {
            jdbc.update("INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,password,role,status,is_delete) VALUES(?,920001,?,?,?,?, '1',0)",
                    id,"user"+id,"Reporter "+id,PASSWORD,id==ADMIN ? "0" : id==LEADER ? "2" : "1");
        }
        jdbc.update("INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,password,role,status,is_delete) VALUES(?,100,?,?,?,?,'1',0)",
                OFFICE,"user"+OFFICE,"Reporter "+OFFICE,PASSWORD,"1");
        jdbc.update("INSERT INTO biz_project(project_id,project_name,leader_id) VALUES(1,'Synthetic work record project',?)", OWNER);
        jdbc.update("INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by) VALUES(940001,'synthetic.pdf','synthetic.pdf','/uploads/synthetic.pdf','pdf',?)",OWNER);
        jdbc.update("INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,auditor_id,principal_id,dept_id,is_delete) "
                + "VALUES(900000,1,0,2026,'SG01','Synthetic reform',1,?,910003,920001,0),(900001,1,900000,2026,'SG0101','Synthetic directory',2,?,910003,920001,0)", OWNER, OWNER);
        jdbc.update("UPDATE sys_user SET force_password_change=0");
        task(900010, OWNER, 2026, 10, "1");
        jdbc.update("INSERT INTO biz_level4_task(task_id,parent_id,task_name,leader_id,dept_id,is_delete) VALUES(950001,900010,'Goal',?,920001,0)",OTHER);
        String[] codes={"1.1.5","1.1.7","1.1.55","1.1.14","1.1.20","1.1.28","1.1.63","1.1.66","1.1.45","1.2.45"};
        for(int i=0;i<codes.length;i++) {
            jdbc.update("INSERT INTO biz_core_indicator(indicator_id,indicator_code,sort_order) VALUES(?,?,?)",i+1,codes[i],i+1);
            jdbc.update("INSERT INTO biz_performance(perf_id,project_id,parent_id,perf_code,perf_name,dept_id,leader_id,auditor_id,principal_id,target_value,current_value,data_type,is_delete) VALUES(?,1,0,?,'Synthetic indicator',920001,?,?,?,10,7,'1',0)",960001+i,codes[i],OWNER,VIEWER,LEADER);
        }
        jdbc.update("UPDATE biz_performance SET perf_name='发布《工程机械产业发展报告》数量',target_value=5 WHERE perf_id=960010");
    }

    private void task(long id, long auditor, int year, int target, String status) {
        jdbc.update("INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,auditor_id,leader_id,principal_id,dept_id,target_value,current_value,data_type,status,is_delete) "
                + "VALUES(?,1,900001,?,?,?,3,?,?,910003,920001,?,0,'1',?,0)",id,year,"T"+id,"Task "+id,auditor,auditor,target,status);
    }

    private void submission(long sub, long task, int value, String submitted, String archived) {
        jdbc.update("INSERT INTO biz_material_submission(sub_id,task_id,reported_value,submit_by,submit_time,flow_status,current_handler_id,file_id,submit_dept_id,manage_dept_id,file_suffix,is_delete) VALUES(?,?,?,?,?, ?,?,940001,920001,920001,'pdf',0)",
                sub,task,value,OWNER,submitted,archived==null ? 10 : 40,OWNER);
        log(sub,10,submitted);
        if (archived != null) log(sub,40,archived);
    }

    private void log(long sub, int status, String time) {
        jdbc.update("INSERT INTO biz_audit_log(sub_id,operator_id,action_type,pre_status,post_status,comment,create_time) VALUES(?,?,'通过',30,?,'Synthetic',?)",sub,ADMIN,status,time);
    }

    private ResponseEntity<String> request(HttpMethod method, String path, String token, Object data) {
        HttpHeaders headers = new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) headers.set("Authorization", token);
        return http.exchange("http://127.0.0.1:"+port+"/api"+path,method,new HttpEntity<>(data,headers),String.class);
    }
    private JsonNode ok(ResponseEntity<String> response) throws Exception {
        assertEquals(200,response.getStatusCode().value(),response.getBody()); return json.readTree(response.getBody());
    }
    private String login(long user) throws Exception {
        return ok(request(HttpMethod.POST,"/system/login",null,Map.of("user_id",user,"password",PASSWORD))).path("token").asText();
    }

    private ResponseEntity<String> testing(HttpMethod method,String path,String token,Object body) {
        HttpHeaders headers=new HttpHeaders();headers.setContentType(MediaType.APPLICATION_JSON);
        if(token!=null)headers.set("Authorization",token);
        headers.set("X-Admin-Test","1");
        headers.set("X-Admin-Test-Reason",java.net.URLEncoder.encode("管理员闭环测试",java.nio.charset.StandardCharsets.UTF_8));
        return http.exchange("http://127.0.0.1:"+port+"/api"+path,method,new HttpEntity<>(body,headers),String.class);
    }
    private JsonNode success(ResponseEntity<String> response) throws Exception {
        assertEquals(200,response.getStatusCode().value(),response.getBody());
        String content=response.getBody();
        JsonNode body=content!=null && (content.startsWith("{")||content.startsWith("[")) ? json.readTree(content) : json.getNodeFactory().textNode(content);
        assertTrue(body.path("code").asInt()<400,response.getBody());return body;
    }
    private void failed(ResponseEntity<String> response) throws Exception {
        assertTrue(response.getStatusCode().value()>=400 || json.readTree(response.getBody()).path("code").asInt()>=400,response.getBody());
    }
    private int count(String table) {assertTrue(table.matches("[a-z_]+"));return jdbc.queryForObject("SELECT COUNT(*) FROM "+table,Integer.class);}
    private Map<String,Object> fillTask() {
        jdbc.update("DELETE FROM biz_level4_task");
        return Map.of("third_task_id",900010,"task_id",900010,"file_id",940001,"data_type","1","reported_value",5,"comment","Synthetic test");
    }
    private long pendingTask(String token,boolean mode) throws Exception {
        Map<String,Object> body=fillTask();
        success(mode?testing(HttpMethod.POST,"/biz/sub",token,body):request(HttpMethod.POST,"/biz/sub",token,body));
        return jdbc.queryForObject("SELECT MAX(sub_id) FROM biz_material_submission",Long.class);
    }
    @Test void testHeadersRequireCurrentActiveAdminAndReason() throws Exception {
        String admin=login(ADMIN),normal=login(OWNER);
        assertEquals(403,testing(HttpMethod.GET,"/biz/audit/todo",normal,null).getStatusCode().value());
        HttpHeaders headers=new HttpHeaders();headers.set("Authorization",admin);headers.set("X-Admin-Test","1");
        assertEquals(400,http.exchange("http://127.0.0.1:"+port+"/api/biz/tasks",HttpMethod.GET,new HttpEntity<>(headers),String.class).getStatusCode().value());
        jdbc.update("UPDATE sys_user SET role='1' WHERE user_id=?",ADMIN);
        assertEquals(403,testing(HttpMethod.GET,"/biz/tasks",admin,null).getStatusCode().value());
        jdbc.update("UPDATE sys_user SET role='0',status='0' WHERE user_id=?",ADMIN);
        assertEquals(403,testing(HttpMethod.GET,"/biz/tasks",admin,null).getStatusCode().value());
        jdbc.update("UPDATE sys_user SET status='1',force_password_change=1 WHERE user_id=?",ADMIN);
        assertEquals(428,testing(HttpMethod.GET,"/biz/tasks",admin,null).getStatusCode().value());
        jdbc.update("UPDATE sys_user SET force_password_change=0 WHERE user_id=?",ADMIN);
        success(request(HttpMethod.POST,"/system/logout",admin,null));
        assertEquals(401,testing(HttpMethod.GET,"/biz/tasks",admin,null).getStatusCode().value());
        assertEquals(64,jdbc.queryForObject("SELECT LENGTH(token) FROM token_blacklist LIMIT 1",Integer.class));
        String legacy=com.auth0.jwt.JWT.create().withIssuer("auth0").withClaim("id",ADMIN)
                .withExpiresAt(new Date(System.currentTimeMillis()+600000)).sign(com.auth0.jwt.algorithms.Algorithm.HMAC256("secretKey"));
        jdbc.update("INSERT INTO token_blacklist(token,expiry_time) VALUES(?,NOW()+INTERVAL 10 MINUTE)",legacy);
        assertEquals(401,testing(HttpMethod.GET,"/biz/tasks",legacy,null).getStatusCode().value());
        assertEquals(401,request(HttpMethod.GET,"/biz/tasks",legacy,null).getStatusCode().value());
        assertEquals(0,count("sys_admin_operation_log"));
    }
    @Test void delegatedTaskFillAndThreeReviewStagesRecordRealAdmin() throws Exception {
        String admin=login(ADMIN);Map<String,Object> body=fillTask();
        failed(request(HttpMethod.POST,"/biz/sub",admin,body));
        assertEquals(0,count("biz_material_submission"));
        long id=pendingTask(admin,true);
        assertEquals(ADMIN,jdbc.queryForObject("SELECT submit_by FROM biz_material_submission WHERE sub_id=?",Long.class,id));
        failed(request(HttpMethod.POST,"/biz/audit",admin,Map.of("sub_id",id,"is_pass",true)));
        assertEquals(1,success(testing(HttpMethod.GET,"/biz/audit/todo",admin,null)).size());
        for(int stage:List.of(20,30,40)) {
            success(testing(HttpMethod.POST,"/biz/audit",admin,Map.of("sub_id",id,"is_pass",true,"title","Synthetic approval")));
            assertEquals(stage,jdbc.queryForObject("SELECT flow_status FROM biz_material_submission WHERE sub_id=?",Integer.class,id));
        }
        assertEquals(4,count("sys_admin_operation_log"));
        assertEquals(4,jdbc.queryForObject("SELECT COUNT(*) FROM biz_audit_log WHERE sub_id=? AND operator_id=?",Integer.class,id,ADMIN));
        assertEquals(0,success(testing(HttpMethod.GET,"/biz/audit/todo",admin,null)).size());
        failed(testing(HttpMethod.POST,"/biz/audit",admin,Map.of("sub_id",id,"is_pass",true)));
        assertEquals(4,count("sys_admin_operation_log"));
        String logs=success(request(HttpMethod.GET,"/admin/operations?module=ADMIN_TEST",admin,null)).toString();
        assertTrue(logs.contains("管理员闭环测试"));assertFalse(logs.contains(PASSWORD));assertFalse(logs.contains(admin));
    }
    @Test void delegatedWithdrawRestoresTaskAndDoesNotLeakToNextRequest() throws Exception {
        String admin=login(ADMIN),owner=login(OWNER);long id=pendingTask(owner,false);
        failed(request(HttpMethod.POST,"/biz/drawback/900010",admin,null));
        success(testing(HttpMethod.POST,"/biz/drawback/900010",admin,null));
        assertEquals(1,jdbc.queryForObject("SELECT is_delete FROM biz_material_submission WHERE sub_id=?",Integer.class,id));
        failed(request(HttpMethod.POST,"/biz/sub",admin,fillTask()));
        assertEquals(1,count("sys_admin_operation_log"));
    }
    @Test void allPerformanceTodosAvailableAndBusinessActorRetained() throws Exception {
        jdbc.update("INSERT INTO biz_performance(perf_id,project_id,perf_code,perf_name,dept_id,leader_id,auditor_id,principal_id,target_value,current_value,data_type,is_delete) VALUES(970001,1,'2.test','Manual',920001,?,?,?,10,0,'1',0)",OWNER,OTHER,LEADER);
        jdbc.update("INSERT INTO biz_performance_year(year_id,perf_id,year,target_value,data_type) VALUES(970002,970001,2026,10,'1')");
        String owner=login(OWNER),admin=login(ADMIN);
        success(request(HttpMethod.POST,"/performance/submit?pref_id=970001&year=2026&actual_value=3",owner,null));
        assertEquals(0,success(request(HttpMethod.GET,"/performance/audit/todo",admin,null)).size());
        assertEquals(1,success(testing(HttpMethod.GET,"/performance/audit/todo",admin,null)).size());
        long id=jdbc.queryForObject("SELECT MAX(sub_id) FROM biz_performance_submission",Long.class);
        for(int stage:List.of(20,30)) {
            success(testing(HttpMethod.POST,"/performance/audit",admin,Map.of("sub_id",id,"is_pass",true,"title","Synthetic")));
            assertEquals(stage,jdbc.queryForObject("SELECT flow_status FROM biz_performance_submission WHERE sub_id=?",Integer.class,id));
        }
        assertEquals(2,count("sys_admin_operation_log"));
    }
    @Test void anyCurrentAdminCanCreateAndReviewAchievementsOnlyInTestMode() throws Exception {
        jdbc.update("UPDATE sys_user SET role='0' WHERE user_id=?",OTHER);
        String admin=login(OTHER);
        Map<String,Object> payload=Map.of("category",1,"level","省级","achName","Synthetic test","gotTime",1767225600000L,"department","Synthetic department","isCompetition",1,"comment","Synthetic","deptId",100);
        failed(request(HttpMethod.POST,"/achievement/add",admin,payload));
        success(testing(HttpMethod.POST,"/achievement/add",admin,payload));
        long id=jdbc.queryForObject("SELECT MAX(sub_id) FROM biz_achievement_submission",Long.class);
        success(testing(HttpMethod.POST,"/achievement/audit",admin,Map.of("sub_id",id,"is_pass",true,"title","Synthetic")));
        assertEquals(OTHER,jdbc.queryForObject("SELECT create_by FROM biz_achievement LIMIT 1",Long.class));
        assertEquals(100,jdbc.queryForObject("SELECT dept_id FROM biz_achievement LIMIT 1",Long.class));
        assertEquals(2,count("sys_admin_operation_log"));
    }
    @Test void delegatedWorkRecordKeepsOwnerScopeNameAndSubmittedImmutability() throws Exception {
        String admin=login(ADMIN),owner=login(OWNER);
        assertTrue(success(request(HttpMethod.GET,"/work-records/capabilities",admin,null)).path("canCreate").asBoolean()); // 正常管理员也可按所属归口部门填报。
        JsonNode record=success(request(HttpMethod.POST,"/work-records",owner,Map.of("year",2026,"month",1)));
        long id=record.path("record").path("recordId").asLong();
        assertEquals(404,request(HttpMethod.GET,"/work-records/"+id,admin,null).getStatusCode().value());
        assertTrue(success(testing(HttpMethod.GET,"/work-records/"+id,admin,null)).path("editable").asBoolean());
        assertEquals(1,success(testing(HttpMethod.GET,"/work-records?status=0",admin,null)).path("total").asInt());
        long reform=record.path("statistics").path("reformTasks").path(0).path("taskId").asLong();
        Map<String,Object> body=Map.of("version",0,"entries",List.of(Map.of("reformTaskId",reform,"keyProgress","Delegated synthetic")));
        JsonNode saved=success(testing(HttpMethod.POST,"/work-records/"+id+"/submit",admin,body));
        assertEquals(OWNER,saved.path("record").path("ownerId").asLong());assertEquals("Reporter "+OWNER,saved.path("record").path("ownerName").asText());
        assertFalse(saved.path("editable").asBoolean());
        assertEquals(409,testing(HttpMethod.POST,"/work-records/"+id+"/save",admin,Map.of("version",1,"entries",List.of())).getStatusCode().value());
        assertEquals(1,count("sys_admin_operation_log"));
        assertTrue(success(testing(HttpMethod.POST,"/work-records",admin,Map.of("year",2026,"month",1))).path("statistics").path("notice").asText().startsWith("管理员测试统计"));
    }
    @Test void delegatedDepartmentWorkRecordKeepsOriginalDepartmentScopeDuringAdminTest() throws Exception {
        jdbc.update("INSERT INTO sys_dept(dept_id,dept_name,status,is_delete) VALUES(920002,'Other department','0',0)");
        jdbc.update("INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,principal_id,dept_id,target_value,current_value,data_type,status,is_delete) VALUES(900100,1,0,2026,'R2','Other reform',1,?,910003,920002,10,0,'1','1',0),(900101,1,900100,2026,'D2','Other directory',2,?,910003,920002,10,0,'1','1',0),(900110,1,900101,2026,'T2','Other task',3,?,910003,920002,10,0,'1','1',0)",OWNER,OWNER,OWNER);
        String admin=login(ADMIN),department=login(VIEWER);
        JsonNode draft=success(request(HttpMethod.POST,"/work-records",department,Map.of("year",2026,"month",1)));
        assertEquals(1,draft.path("statistics").path("totalTasks").asInt());
        long id=draft.path("record").path("recordId").asLong();String path="/work-records/"+id;
        assertEquals(400,testing(HttpMethod.POST,path+"/submit",admin,Map.of("version",0,"entries",List.of(Map.of("reformTaskId",900100,"keyProgress","Outside original department")))).getStatusCode().value());
        assertEquals(0,jdbc.queryForObject("SELECT version FROM biz_work_record WHERE record_id=?",Integer.class,id));
        JsonNode saved=success(testing(HttpMethod.POST,path+"/submit",admin,Map.of("version",0,"entries",List.of(Map.of("reformTaskId",900000,"keyProgress","Delegated department report")))));
        assertEquals(VIEWER,saved.path("record").path("ownerId").asLong());
        assertEquals(1,saved.path("statistics").path("totalTasks").asInt());
        assertTrue(saved.path("statistics").path("notice").asText().startsWith("归口部门统计"));
        assertEquals(ADMIN,jdbc.queryForObject("SELECT operator_id FROM sys_admin_operation_log WHERE module='ADMIN_TEST' ORDER BY log_id DESC LIMIT 1",Long.class));
        JsonNode otherDraft=success(request(HttpMethod.POST,"/work-records",department,Map.of("year",2026,"month",2)));
        long draftId=otherDraft.path("record").path("recordId").asLong();
        String snapshot=jdbc.queryForObject("SELECT statistics_json FROM biz_work_record_snapshot WHERE record_id=?",String.class,draftId);
        jdbc.update("UPDATE sys_user SET status='0' WHERE user_id=?",VIEWER);
        assertEquals(401,testing(HttpMethod.POST,"/work-records/"+draftId+"/statistics/refresh",admin,Map.of("version",0)).getStatusCode().value());
        assertEquals(snapshot,jdbc.queryForObject("SELECT statistics_json FROM biz_work_record_snapshot WHERE record_id=?",String.class,draftId));
        assertEquals(0,jdbc.queryForObject("SELECT version FROM biz_work_record WHERE record_id=?",Integer.class,draftId));
    }
    @Test void delegatedCoreDraftAndReviewUseActualOperatorAndKeepStages() throws Exception {
        String owner=login(OWNER),admin=login(ADMIN);
        JsonNode draft=success(request(HttpMethod.POST,"/core-indicators/1/draft",owner,Map.of("year",2026,"month",1)));
        long id=draft.path("reportId").asLong();String path="/core-indicators/reports/"+id;
        failed(request(HttpMethod.PUT,path,admin,Map.of("version",0,"actualValue",2,"previousYearValue",1)));
        success(testing(HttpMethod.PUT,path,admin,Map.of("version",0,"actualValue",2,"previousYearValue",1)));
        JsonNode submitted=success(testing(HttpMethod.POST,path+"/submit",admin,Map.of("version",1)));
        assertEquals(ADMIN,submitted.path("submitBy").asLong());
        String review="/core-indicators/reports/"+submitted.path("reportId").asLong()+"/audit";
        assertEquals(1,success(testing(HttpMethod.GET,"/core-indicators/audit/todo",admin,null)).size());
        assertEquals(403,request(HttpMethod.POST,review,admin,Map.of("version",0,"pass",true)).getStatusCode().value());
        assertEquals(20,success(testing(HttpMethod.POST,review,admin,Map.of("version",0,"pass",true))).path("flowStatus").asInt());
        assertEquals(409,testing(HttpMethod.POST,review,admin,Map.of("version",0,"pass",true)).getStatusCode().value());
        assertEquals(30,success(testing(HttpMethod.POST,review,admin,Map.of("version",1,"pass",true))).path("flowStatus").asInt());
        assertEquals(4,count("sys_admin_operation_log"));
        assertEquals(3,jdbc.queryForObject("SELECT COUNT(*) FROM biz_core_audit_log WHERE operator_id=?",Integer.class,ADMIN));
        assertEquals(7,jdbc.queryForObject("SELECT current_value FROM biz_performance WHERE perf_id=960001",Integer.class));
    }
    @Test void failedTraceRollsBackBusinessValuesAuditsAndNotifications() throws Exception {
        String admin=login(ADMIN);Map<String,Object> body=fillTask();
        jdbc.execute("CREATE TRIGGER reject_admin_test_trace BEFORE INSERT ON sys_admin_operation_log FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic trace failure'");
        try {
            assertEquals(500,testing(HttpMethod.POST,"/biz/sub",admin,body).getStatusCode().value());
            assertEquals(0,count("biz_material_submission"));assertEquals(0,count("biz_audit_log"));assertEquals(0,count("sys_notice"));
            assertEquals(0,jdbc.queryForObject("SELECT current_value FROM biz_task WHERE task_id=900010",Integer.class));
            assertEquals(0,count("sys_admin_operation_log"));
        } finally {jdbc.execute("DROP TRIGGER reject_admin_test_trace");}
    }
}
