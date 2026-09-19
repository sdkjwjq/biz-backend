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
@Import(WorkRecordApiTest.NoSchedules.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={
        "work-records.viewer-user-ids=910005", "logging.file.name=target/work-record-regression.log",
        "logging.level.org.springframework=WARN", "logging.level.org.example.mapper=WARN"})
class WorkRecordApiTest {
    private static final long ADMIN=110228, OWNER=910001, OTHER=910002, LEADER=910003, VIEWER=910005;
    private static final String PASSWORD="WorkRecords123";
    @Autowired private TestRestTemplate http;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ObjectMapper json;
    @LocalServerPort private int port;
    @MockBean(name="workRecordClock") private Clock clock;

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
        jdbc.update("INSERT INTO sys_dept(dept_id,dept_name,is_delete) VALUES(920001,'Synthetic department',0)");
        for (long id : List.of(ADMIN, OWNER, OTHER, LEADER, VIEWER)) {
            jdbc.update("INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,password,role,status,is_delete) VALUES(?,920001,?,?,?,?, '1',0)",
                    id,"user"+id,"Reporter "+id,PASSWORD,id==ADMIN ? "0" : id==LEADER ? "2" : "1");
        }
        jdbc.update("INSERT INTO biz_project(project_id,project_name,leader_id) VALUES(1,'Synthetic work record project',?)", OWNER);
        jdbc.update("INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by) VALUES(940001,'synthetic.pdf','synthetic.pdf','/uploads/synthetic.pdf','pdf',?)",OWNER);
        jdbc.update("INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,auditor_id,principal_id,dept_id,is_delete) "
                + "VALUES(900000,1,0,2026,'SG01','Synthetic reform',1,?,910003,920001,0),(900001,1,900000,2026,'SG0101','Synthetic directory',2,?,910003,920001,0)", OWNER, OWNER);
        task(900010, OWNER, 2026, 10, "1");
    }

    private void task(long id, long auditor, int year, int target, String status) {
        jdbc.update("INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,auditor_id,leader_id,principal_id,dept_id,target_value,current_value,data_type,status,is_delete) "
                + "VALUES(?,1,900001,?,?,?,3,?,?,910003,920001,?,0,'1',?,0)",id,year,"T"+id,"Task "+id,auditor,OWNER,target,status);
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
    private JsonNode preview(String token, int month) throws Exception { return ok(request(HttpMethod.GET,"/work-records/statistics?year=2026&month="+month,token,null)); }
    private JsonNode create(String token, int month) throws Exception { return ok(request(HttpMethod.POST,"/work-records",token,Map.of("year",2026,"month",month))); }

    @Test void permissionsFollowAuditorNotLeaderAndProtectNewEndpoints() throws Exception {
        assertEquals(401,request(HttpMethod.GET,"/work-records/capabilities",null,null).getStatusCode().value());
        assertEquals(401,request(HttpMethod.POST,"/work-records",null,Map.of("year",2026,"month",1)).getStatusCode().value());
        for (long user : List.of(OWNER, OTHER, LEADER, VIEWER, ADMIN)) {
            String token=login(user);
            JsonNode caps=ok(request(HttpMethod.GET,"/work-records/capabilities",token,null));
            assertEquals(user==OWNER,caps.path("canCreate").asBoolean());
            assertEquals(user==VIEWER || user==ADMIN,caps.path("canExport").asBoolean());
            if (user!=OWNER) assertEquals(403,request(HttpMethod.POST,"/work-records",token,Map.of("year",2026,"month",1)).getStatusCode().value());
        }
        String token=login(OWNER);
        jdbc.update("UPDATE sys_user SET password='weak' WHERE user_id=?",OWNER);
        assertEquals(428,request(HttpMethod.GET,"/work-records/capabilities",token,null).getStatusCode().value());
    }

    @Test void monthAndPayloadValidationRejectsInvalidWithoutWriting() throws Exception {
        String token=login(OWNER);
        for (Map<String,Object> data : List.<Map<String,Object>>of(Map.of("year",2024,"month",1),Map.of("year",2030,"month",1),
                Map.of("year",2026,"month",10),Map.of("year",2026,"month",0),Map.of("year",2026,"month",13),
                Map.of("year",2026,"month",1.5),Map.of("year","2026","month",1),Map.of("month",1))) {
            ResponseEntity<String> response=request(HttpMethod.POST,"/work-records",token,data);
            assertEquals(400,response.getStatusCode().value(),response.getBody());
            assertEquals(400,json.readTree(response.getBody()).path("code").asInt());
        }
        assertEquals(400,request(HttpMethod.GET,"/work-records/statistics?year=2026",token,null).getStatusCode().value());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM biz_work_record",Integer.class));
        assertEquals(403,request(HttpMethod.POST,"/work-records",token,Map.of("year",2025,"month",1)).getStatusCode().value());
    }

    @Test void statisticsUseOwnedAnnualTasksAndFirstQualifyingArchive() throws Exception {
        task(900011,OWNER,2026,10,"1"); task(900012,OWNER,2026,10,"3");
        task(900013,OTHER,2026,10,"1"); task(900014,OWNER,2025,10,"1"); task(900015,OWNER,2026,10,"1");
        jdbc.update("UPDATE biz_task SET is_delete=1 WHERE task_id=900015");
        submission(910010,900010,5,"2026-01-01 01:00:00","2026-01-05 01:00:00");
        submission(910011,900010,10,"2026-01-10 01:00:00","2026-01-31 23:59:59");
        submission(910012,900010,12,"2026-02-01 01:00:00","2026-02-03 01:00:00");
        jdbc.update("UPDATE biz_material_submission SET is_delete=1 WHERE sub_id=910011");
        submission(910013,900011,10,"2026-01-28 01:00:00","2026-02-01 00:00:00");
        log(910011,40,"2026-02-15 00:00:00");
        JsonNode jan=preview(login(OWNER),1),feb=preview(login(OWNER),2);
        assertEquals(3,jan.path("totalTasks").asInt()); assertEquals(1,jan.path("newCompleted").asInt());
        assertEquals(1,jan.path("cumulativeCompleted").asInt()); assertEquals(33.33,jan.path("completionRate").asDouble());
        assertEquals(1,jan.path("unverifiedTasks").asInt());
        assertEquals(1,feb.path("newCompleted").asInt()); assertEquals(2,feb.path("cumulativeCompleted").asInt());
        assertEquals(1,jan.path("reformTasks").size()); assertEquals(900000,jan.path("reformTasks").get(0).path("taskId").asLong());
        assertEquals("2026-01-31T23:59:59.999+08:00",jan.path("cutoffAt").asText());
        assertEquals("2026-09-19T12:00+08:00",preview(login(OWNER),9).path("cutoffAt").asText());
    }

    @Test void fourLevelArchivesNeverDoubleCountAndMissingParentEvidenceIsExplicit() throws Exception {
        for (long id : List.of(900020L,900021L)) {
            task(id,OWNER,2026,5,"3");
            jdbc.update("UPDATE biz_task SET level=4,parent_id=900010 WHERE task_id=?",id);
            jdbc.update("INSERT INTO biz_level4_task(task_id,parent_id,phase,task_name,dept_id,target_value,current_value,is_delete) VALUES(?,900010,2026,'Child',920001,5,5,0)",id);
        }
        submission(910010,900010,10,"2026-01-01 01:00:00","2026-01-05 01:00:00");
        submission(910020,900020,5,"2026-01-01 01:00:00","2026-01-05 01:00:00");
        submission(910021,900021,5,"2026-01-01 01:00:00","2026-01-05 01:00:00");
        String token=login(OWNER);
        assertEquals(1,preview(token,1).path("newCompleted").asInt());
        jdbc.update("DELETE FROM biz_audit_log WHERE sub_id=910010");
        jdbc.update("DELETE FROM biz_material_submission WHERE sub_id=910010");
        JsonNode result=preview(token,1);
        assertEquals(1,result.path("totalTasks").asInt()); assertEquals(0,result.path("newCompleted").asInt());
        assertEquals(1,result.path("unverifiedTasks").asInt());
        assertTrue(result.path("tasks").get(0).path("verificationReason").asText().contains("三级"));
    }

    @Test void previousCompletedSnapshotPreventsInventingFirstCompletionMonth() throws Exception {
        submission(910010,900010,10,"2026-02-01 01:00:00","2026-02-05 01:00:00");
        jdbc.update("INSERT INTO biz_audit_snapshot(sub_id,target_type,target_id,previous_value,previous_status,create_time) VALUES(910010,'TASK',900010,10,'3','2026-02-01')");
        JsonNode result=preview(login(OWNER),2);
        assertEquals(0,result.path("newCompleted").asInt()); assertEquals(1,result.path("unverifiedTasks").asInt());
    }

    @Test void missingEarlierArchiveTimeAndInvalidTargetRemainUnverified() throws Exception {
        submission(910010,900010,10,"2026-01-01 01:00:00",null);
        jdbc.update("UPDATE biz_material_submission SET flow_status=40 WHERE sub_id=910010");
        submission(910011,900010,10,"2026-02-01 01:00:00","2026-02-05 01:00:00");
        task(900011,OWNER,2026,0,"3");
        JsonNode result=preview(login(OWNER),2);
        assertEquals(0,result.path("newCompleted").asInt());
        assertEquals(2,result.path("unverifiedTasks").asInt());
        assertTrue(result.path("notice").asText().contains("可核实统计"));
        assertTrue(result.path("tasks").get(0).path("verificationReason").asText().contains("归档时间证据缺失"));
    }

    @Test void newDraftAndSnapshotAreAtomicOnPersistenceFailure() throws Exception {
        String token=login(OWNER);
        jdbc.execute("CREATE TRIGGER fail_work_create BEFORE INSERT ON biz_work_record_snapshot FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic creation failure'");
        try {
            assertEquals(500,request(HttpMethod.POST,"/work-records",token,Map.of("year",2026,"month",1)).getStatusCode().value());
            assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM biz_work_record",Integer.class));
            assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM biz_work_record_snapshot",Integer.class));
        } finally { jdbc.execute("DROP TRIGGER fail_work_create"); }
        assertEquals(1,create(token,1).path("statistics").path("totalTasks").asInt());
    }

    @Test void concurrentCreateProducesOneDraftAndDoesNotAcceptSpoofedOwner() throws Exception {
        String token=login(OWNER); ExecutorService pool=Executors.newFixedThreadPool(6);
        try {
            List<Callable<JsonNode>> calls=new ArrayList<>();
            for (int i=0;i<6;i++) calls.add(() -> ok(request(HttpMethod.POST,"/work-records",token,Map.of("year",2026,"month",1,"ownerId",OTHER))));
            Set<Long> ids=new HashSet<>();
            for (Future<JsonNode> result : pool.invokeAll(calls)) {
                JsonNode node=result.get(30,TimeUnit.SECONDS);
                ids.add(node.path("record").path("recordId").asLong());
                assertEquals(OWNER,node.path("record").path("ownerId").asLong());
                assertEquals(1,node.path("statistics").path("totalTasks").asInt());
            }
            assertEquals(1,ids.size()); assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM biz_work_record",Integer.class));
            assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM biz_work_record_snapshot",Integer.class));
        } finally { pool.shutdownNow(); }
    }

    @Test void storedSnapshotOnlyChangesOnExplicitVersionedRefresh() throws Exception {
        String token=login(OWNER); JsonNode draft=create(token,1); long id=draft.path("record").path("recordId").asLong();
        task(900011,OWNER,2026,10,"1");
        JsonNode same=create(token,1);
        assertEquals(draft.path("statistics"),same.path("statistics"));
        JsonNode refreshed=ok(request(HttpMethod.POST,"/work-records/"+id+"/statistics/refresh",token,Map.of("version",0)));
        assertEquals(2,refreshed.path("statistics").path("totalTasks").asInt()); assertEquals(1,refreshed.path("record").path("version").asInt());
        assertEquals(409,request(HttpMethod.POST,"/work-records/"+id+"/statistics/refresh",token,Map.of("version",0)).getStatusCode().value());
        assertEquals(400,request(HttpMethod.POST,"/work-records/"+id+"/statistics/refresh",token,Map.of("version",1.5)).getStatusCode().value());
    }

    @Test void draftAndSubmittedVisibilityAndRevokedQualifications() throws Exception {
        String owner=login(OWNER),other=login(OTHER),admin=login(ADMIN),viewer=login(VIEWER);
        long id=create(owner,1).path("record").path("recordId").asLong();
        for (String token: List.of(other,admin,viewer)) {
            assertEquals(404,request(HttpMethod.GET,"/work-records/"+id,token,null).getStatusCode().value());
            assertEquals(0,ok(request(HttpMethod.GET,"/work-records",token,null)).path("total").asInt());
            assertEquals(404,request(HttpMethod.POST,"/work-records/"+id+"/statistics/refresh",token,Map.of("version",0)).getStatusCode().value());
        }
        // 第二批尚未开放提交接口，此处用隔离数据模拟既有已提交记录，验证基础读取及冻结约束。
        jdbc.update("UPDATE biz_work_record SET status=1,submit_time=NOW() WHERE record_id=?",id);
        assertEquals(404,request(HttpMethod.GET,"/work-records/"+id,other,null).getStatusCode().value());
        task(900011,OTHER,2026,10,"1");
        for (String token: List.of(other,admin,viewer)) assertFalse(ok(request(HttpMethod.GET,"/work-records/"+id,token,null)).path("editable").asBoolean());
        JsonNode snapshot=ok(request(HttpMethod.GET,"/work-records/"+id,owner,null)).path("statistics");
        jdbc.update("UPDATE biz_task SET auditor_id=? WHERE auditor_id=?",OTHER,OWNER);
        assertEquals(snapshot,ok(request(HttpMethod.GET,"/work-records/"+id,owner,null)).path("statistics"));
        assertEquals(409,request(HttpMethod.POST,"/work-records/"+id+"/statistics/refresh",owner,Map.of("version",0)).getStatusCode().value());
        assertFalse(ok(request(HttpMethod.GET,"/work-records/capabilities",owner,null)).path("canViewAll").asBoolean());
        assertTrue(ok(request(HttpMethod.GET,"/work-records/capabilities",owner,null)).path("canViewOwnHistory").asBoolean());
    }

    @Test void refreshRechecksQualificationAndRaceAndRollsBackFailedSnapshot() throws Exception {
        String token=login(OWNER); long id=create(token,1).path("record").path("recordId").asLong();
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> call=() -> request(HttpMethod.POST,"/work-records/"+id+"/statistics/refresh",token,Map.of("version",0)).getStatusCode().value();
            List<Integer> codes=new ArrayList<>();
            for (Future<Integer> result: pool.invokeAll(List.of(call,call))) codes.add(result.get(30,TimeUnit.SECONDS));
            Collections.sort(codes); assertEquals(List.of(200,409),codes);
        } finally { pool.shutdownNow(); }
        String snapshot=jdbc.queryForObject("SELECT statistics_json FROM biz_work_record_snapshot WHERE record_id=?",String.class,id);
        jdbc.execute("CREATE TRIGGER fail_work_snapshot BEFORE UPDATE ON biz_work_record_snapshot FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic failure'");
        try {
            assertEquals(500,request(HttpMethod.POST,"/work-records/"+id+"/statistics/refresh",token,Map.of("version",1)).getStatusCode().value());
            assertEquals(1,jdbc.queryForObject("SELECT version FROM biz_work_record WHERE record_id=?",Long.class,id));
            assertEquals(snapshot,jdbc.queryForObject("SELECT statistics_json FROM biz_work_record_snapshot WHERE record_id=?",String.class,id));
        } finally { jdbc.execute("DROP TRIGGER fail_work_snapshot"); }
        jdbc.update("UPDATE biz_task SET auditor_id=? WHERE auditor_id=?",OTHER,OWNER);
        assertEquals(403,request(HttpMethod.POST,"/work-records/"+id+"/statistics/refresh",token,Map.of("version",1)).getStatusCode().value());
        assertFalse(ok(request(HttpMethod.GET,"/work-records/"+id,token,null)).path("editable").asBoolean());
    }

    @Test void listFiltersPaginationAndNoBusinessWritesFromStatistics() throws Exception {
        String token=login(OWNER); create(token,1); create(token,2);
        assertEquals(2,ok(request(HttpMethod.GET,"/work-records?pageSize=1",token,null)).path("total").asInt());
        assertEquals(1,ok(request(HttpMethod.GET,"/work-records?page=2&pageSize=1",token,null)).path("records").size());
        assertEquals(1,ok(request(HttpMethod.GET,"/work-records?month=1&ownerId="+OWNER,token,null)).path("total").asInt());
        assertEquals(0,ok(request(HttpMethod.GET,"/work-records?status=1",token,null)).path("total").asInt());
        assertEquals(400,request(HttpMethod.GET,"/work-records?pageSize=101",token,null).getStatusCode().value());
        List<Map<String,Object>> before=jdbc.queryForList("SELECT * FROM biz_task ORDER BY task_id");
        preview(token,1); preview(token,9);
        assertEquals(before,jdbc.queryForList("SELECT * FROM biz_task ORDER BY task_id"));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM sys_notice",Integer.class));
    }
}
