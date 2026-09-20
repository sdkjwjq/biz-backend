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
        // 模拟已有提交记录，单独验证读取及资格撤销约束；真实提交另有闭环用例。
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

    private ResponseEntity<byte[]> export(String token, Object body) {
        HttpHeaders headers=new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON);
        if(token!=null) headers.set("Authorization",token);
        return http.exchange("http://127.0.0.1:"+port+"/api/work-records/export",HttpMethod.POST,new HttpEntity<>(body,headers),byte[].class);
    }

    private String wordText(byte[] bytes) throws Exception {
        try(var document=new org.apache.poi.xwpf.usermodel.XWPFDocument(new java.io.ByteArrayInputStream(bytes));
            var extractor=new org.apache.poi.xwpf.extractor.XWPFWordExtractor(document)) { return extractor.getText(); }
    }

    @Test void exportRequiresExplicitPermissionAndRejectsEntireInvalidSelection() throws Exception {
        String owner=login(OWNER), admin=login(ADMIN), viewer=login(VIEWER);
        long id=create(owner,1).path("record").path("recordId").asLong();
        assertEquals(401,export(null,Map.of("ids",List.of(id))).getStatusCode().value());
        assertEquals(403,export(owner,Map.of("ids",List.of(id))).getStatusCode().value());
        assertEquals(404,export(admin,Map.of("ids",List.of(id))).getStatusCode().value());
        ok(request(HttpMethod.POST,"/work-records/"+id+"/submit",owner,narrative(0,"Submitted text")));
        for(Object body:List.of(Map.of("ids",List.of()),Map.of("ids",List.of(1.5)),Map.of("ids",List.of("1")),Map.of("ids",List.of(-1)),Map.of("ids","invalid")))
            assertEquals(400,export(admin,body).getStatusCode().value());
        assertEquals(404,export(admin,Map.of("ids",List.of(id,99999999))).getStatusCode().value());
        long draft=create(owner,2).path("record").path("recordId").asLong();
        assertEquals(404,export(admin,Map.of("ids",List.of(id,draft))).getStatusCode().value());
        for(String token:List.of(admin,viewer)) {
            var response=export(token,Map.of("ids",List.of(id)));
            assertEquals(200,response.getStatusCode().value());
            assertTrue(response.getHeaders().getContentType().toString().contains("wordprocessingml"));
            assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("attachment"));
            assertEquals("no-store",response.getHeaders().getCacheControl());
            assertTrue(wordText(response.getBody()).contains("Submitted text"));
        }
    }

    @Test void wordUsesFrozenSnapshotRemovesExamplesAndPreservesFiveSections() throws Exception {
        jdbc.update("INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,auditor_id,principal_id,dept_id,is_delete) VALUES(900100,1,0,2026,'SG02','Empty reform omitted',1,?,910003,920001,0)",OWNER);
        task(900101,OWNER,2026,10,"1"); jdbc.update("UPDATE biz_task SET parent_id=900100 WHERE task_id=900101");
        String token=login(OWNER),admin=login(ADMIN); long id=create(token,1).path("record").path("recordId").asLong();
        ok(request(HttpMethod.POST,"/work-records/"+id+"/submit",token,Map.of("version",0,"entries",List.of(
                Map.of("reformTaskId",900000,"keyProgress","Saved <XML> & text\n第二行"),Map.of("reformTaskId",900100,"keyProgress","\u00a0")))));
        String before=wordText(export(admin,Map.of("ids",List.of(id))).getBody());
        assertFalse(before.contains("Empty reform omitted"));
        assertTrue(before.contains("2026年1月份工作纪实")); assertTrue(before.contains("填报人：Reporter "+OWNER));
        for(String heading:List.of("一、基本情况","二、主要工作亮点和成果","三、存在问题","四、下阶段工作重点","五、其它事项")) assertTrue(before.contains(heading),heading);
        assertTrue(before.contains("2026年09月19日")); assertTrue(before.contains("2026-01-31 23:59:59"));
        assertTrue(before.contains("Saved <XML> & text\n第二行")); assertTrue(before.contains("关键进展：")); assertTrue(before.contains("典型做法："));
        assertFalse(before.contains("****")); assertFalse(before.contains("{{")); assertFalse(before.contains("（说明"));
        assertFalse(before.contains("直接提取指定时间段")); assertFalse(before.contains("三维联动"));
        jdbc.update("UPDATE biz_task SET task_name='MUTATED',auditor_id=?,target_value=999 WHERE auditor_id=?",OTHER,OWNER);
        jdbc.update("UPDATE sys_user SET nick_name='CHANGED USER' WHERE user_id=?",OWNER);
        assertEquals(before,wordText(export(admin,Map.of("ids",List.of(id))).getBody()));
    }

    @Test void mergedWordSortsDeduplicatesAndSupportsLongTablesAndNarratives() throws Exception {
        for(long taskId=900100;taskId<900150;taskId++) task(taskId,OWNER,2026,10,"1");
        task(900011,OTHER,2026,10,"1");
        String owner=login(OWNER),other=login(OTHER),admin=login(ADMIN);
        long feb=create(owner,2).path("record").path("recordId").asLong();
        long janOther=create(other,1).path("record").path("recordId").asLong();
        long jan=create(owner,1).path("record").path("recordId").asLong();
        String longText="长文本内容".repeat(60);
        Map<String,Object> body=Map.of("version",0,"entries",List.of(Map.of("reformTaskId",900000,"keyProgress",longText,"stageResults",longText,"typicalPractices",longText)),
                "problems",longText,"nextFocus",longText,"otherMatters",longText);
        ok(request(HttpMethod.POST,"/work-records/"+jan+"/submit",owner,body));
        ok(request(HttpMethod.POST,"/work-records/"+feb+"/submit",owner,narrative(0,"FEB_OWNER")));
        ok(request(HttpMethod.POST,"/work-records/"+janOther+"/submit",other,narrative(0,"JAN_OTHER")));
        var response=export(admin,Map.of("ids",List.of(feb,janOther,jan,jan)));
        assertEquals(200,response.getStatusCode().value()); String text=wordText(response.getBody());
        assertTrue(text.indexOf(longText)<text.indexOf("JAN_OTHER")); assertTrue(text.indexOf("JAN_OTHER")<text.indexOf("FEB_OWNER"));
        try(var doc=new org.apache.poi.xwpf.usermodel.XWPFDocument(new java.io.ByteArrayInputStream(response.getBody()))) {
            assertEquals(6,doc.getTables().size()); assertEquals(52,doc.getTables().get(0).getNumberOfRows());
            for(var table:doc.getTables()) {
                assertTrue(table.getRow(0).isRepeatHeader());
                for(var row:table.getRows()) for(var cell:row.getTableCells()) for(var paragraph:cell.getParagraphs()) for(var run:paragraph.getRuns()) {
                    for(var range:org.apache.poi.xwpf.usermodel.XWPFRun.FontCharRange.values()) assertEquals("仿宋_GB2312",run.getFontFamily(range));
                    assertEquals(12.0,run.getFontSizeAsDouble());
                    assertEquals(java.math.BigInteger.valueOf(24),run.getCTR().getRPr().getSzCsArray(0).getVal());
                }
            }
            assertEquals(2,doc.getParagraphs().stream().filter(org.apache.poi.xwpf.usermodel.XWPFParagraph::isPageBreak).count());
            assertTrue(doc.getTables().get(1).getText().contains(longText));
        }
        java.nio.file.Path output=java.nio.file.Path.of("target/work-record-export-samples"); java.nio.file.Files.createDirectories(output);
        java.nio.file.Files.write(output.resolve("merged-long.docx"),response.getBody());
        java.nio.file.Files.write(output.resolve("single.docx"),export(admin,Map.of("ids",List.of(jan))).getBody());
    }

    private Map<String,Object> narrative(long version, String text) {
        return Map.of("version",version,"entries",List.of(Map.of("reformTaskId",900000,"keyProgress",text,
                "reformTaskName","Spoofed name")),"problems","Synthetic problem","nextFocus","Next","otherMatters","");
    }

    @Test void draftRoundTripAndSubmitRecomputeThenFreezeAllContent() throws Exception {
        String token=login(OWNER); JsonNode draft=create(token,1); long id=draft.path("record").path("recordId").asLong();
        JsonNode empty=ok(request(HttpMethod.POST,"/work-records/"+id+"/save",token,Map.of("version",0,"entries",List.of())));
        assertEquals(1,empty.path("record").path("version").asInt());
        task(900011,OWNER,2026,10,"1");
        JsonNode saved=ok(request(HttpMethod.POST,"/work-records/"+id+"/save",token,narrative(1,"😀".repeat(300))));
        assertEquals(draft.path("statistics"),saved.path("statistics"));
        assertEquals("Synthetic reform",saved.path("entries").get(0).path("reformTaskName").asText());
        assertEquals("😀".repeat(300),saved.path("entries").get(0).path("keyProgress").asText());
        assertEquals(saved,ok(request(HttpMethod.GET,"/work-records/"+id,token,null)));
        submission(910010,900010,10,"2026-01-01 01:00:00","2026-01-05 01:00:00");
        JsonNode submitted=ok(request(HttpMethod.POST,"/work-records/"+id+"/submit",token,narrative(2,"Final content")));
        assertEquals(1,submitted.path("record").path("status").asInt());
        assertEquals(3,submitted.path("record").path("version").asInt());
        assertFalse(submitted.path("editable").asBoolean());
        assertEquals(2,submitted.path("statistics").path("totalTasks").asInt());
        assertEquals(1,submitted.path("statistics").path("newCompleted").asInt());
        jdbc.update("UPDATE biz_task SET task_name='Changed',target_value=999,auditor_id=? WHERE auditor_id=?",OTHER,OWNER);
        assertEquals(submitted,ok(request(HttpMethod.GET,"/work-records/"+id,token,null)));
        for(String action:List.of("save","submit","statistics/refresh"))
            assertEquals(409,request(HttpMethod.POST,"/work-records/"+id+"/"+action,token,narrative(3,"Overwrite")).getStatusCode().value());
        assertEquals(submitted.path("entries"),ok(request(HttpMethod.GET,"/work-records/"+id,login(ADMIN),null)).path("entries"));
    }

    @Test void textAndEntryValidationDoesNotPartiallyWrite() throws Exception {
        String token=login(OWNER); long id=create(token,1).path("record").path("recordId").asLong();
        List<Object> invalid=new ArrayList<>();
        invalid.add(narrative(0,"x".repeat(301)));
        for(String field:List.of("problems","nextFocus","otherMatters")) {
            Map<String,Object> body=new HashMap<>(narrative(0,"Valid")); body.put(field,"😀".repeat(301)); invalid.add(body);
            body=new HashMap<>(narrative(0,"Valid")); body.put(field,123); invalid.add(body);
        }
        for(String field:List.of("keyProgress","stageResults","typicalPractices"))
            invalid.add(Map.of("version",0,"entries",List.of(Map.of("reformTaskId",900000,field,"x".repeat(301)))));
        invalid.add(Map.of("version",0,"entries",List.of(Map.of("reformTaskId",900010))));
        invalid.add(Map.of("version",0,"entries",List.of(Map.of("reformTaskId",900000),Map.of("reformTaskId",900000))));
        invalid.add(Map.of("version",0,"entries","wrong type"));
        invalid.add(Map.of("version",0.5,"entries",List.of()));
        invalid.add(Map.of("entries",List.of()));
        invalid.add(Map.of("version",0,"entries",List.of("invalid")));
        for(Object body:invalid) assertEquals(400,request(HttpMethod.POST,"/work-records/"+id+"/save",token,body).getStatusCode().value(),body.toString());
        for(String blank:List.of("","  ","\n\t","\u00a0")) assertEquals(400,request(HttpMethod.POST,"/work-records/"+id+"/submit",token,narrative(0,blank)).getStatusCode().value());
        assertEquals(0,jdbc.queryForObject("SELECT version FROM biz_work_record WHERE record_id=?",Long.class,id));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM biz_work_record_entry",Integer.class));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM sys_notice",Integer.class));
    }

    @Test void saveAndSubmitRequireOwnerAndCurrentAnnualQualification() throws Exception {
        String token=login(OWNER); long id=create(token,1).path("record").path("recordId").asLong();
        for(String action:List.of("save","submit")) {
            assertEquals(401,request(HttpMethod.POST,"/work-records/"+id+"/"+action,null,narrative(0,"Valid")).getStatusCode().value());
            for(long user:List.of(ADMIN,OTHER,VIEWER)) assertEquals(404,request(HttpMethod.POST,"/work-records/"+id+"/"+action,login(user),narrative(0,"Valid")).getStatusCode().value());
        }
        assertEquals(0,ok(request(HttpMethod.GET,"/work-records/authors",login(ADMIN),null)).size());
        jdbc.update("UPDATE biz_task SET auditor_id=? WHERE auditor_id=?",OTHER,OWNER);
        for(String action:List.of("save","submit")) assertEquals(403,request(HttpMethod.POST,"/work-records/"+id+"/"+action,token,narrative(0,"Valid")).getStatusCode().value());
        assertTrue(ok(request(HttpMethod.GET,"/work-records/capabilities",token,null)).path("canViewOwnRecords").asBoolean());
        assertFalse(ok(request(HttpMethod.GET,"/work-records/"+id,token,null)).path("editable").asBoolean());
    }

    @Test void concurrentSubmitCommitsExactlyOnceAndOldWindowCannotOverwrite() throws Exception {
        String token=login(OWNER); long id=create(token,1).path("record").path("recordId").asLong();
        ok(request(HttpMethod.POST,"/work-records/"+id+"/save",token,narrative(0,"First window")));
        assertEquals(409,request(HttpMethod.POST,"/work-records/"+id+"/save",token,narrative(0,"Old window")).getStatusCode().value());
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> call=() -> request(HttpMethod.POST,"/work-records/"+id+"/submit",token,narrative(1,"Final")).getStatusCode().value();
            List<Integer> codes=new ArrayList<>();
            for(Future<Integer> result:pool.invokeAll(List.of(call,call))) codes.add(result.get(30,TimeUnit.SECONDS));
            Collections.sort(codes); assertEquals(List.of(200,409),codes);
        } finally { pool.shutdownNow(); }
        assertEquals(2,jdbc.queryForObject("SELECT version FROM biz_work_record WHERE record_id=?",Long.class,id));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM biz_work_record_entry WHERE record_id=?",Integer.class,id));
        assertEquals(1,ok(request(HttpMethod.GET,"/work-records/authors",login(VIEWER),null)).size());
    }

    @Test void failedSubmissionRollsBackTextEntriesVersionAndSnapshot() throws Exception {
        String token=login(OWNER); long id=create(token,1).path("record").path("recordId").asLong();
        JsonNode before=ok(request(HttpMethod.POST,"/work-records/"+id+"/save",token,narrative(0,"Original")));
        jdbc.execute("CREATE TRIGGER fail_work_submit BEFORE UPDATE ON biz_work_record_snapshot FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic submission failure'");
        try {
            assertEquals(500,request(HttpMethod.POST,"/work-records/"+id+"/submit",token,narrative(1,"Must rollback")).getStatusCode().value());
            assertEquals(before,ok(request(HttpMethod.GET,"/work-records/"+id,token,null)));
        } finally { jdbc.execute("DROP TRIGGER fail_work_submit"); }
    }

    private long submittedForDelete(String ownerToken, int month) throws Exception {
        long id=create(ownerToken,month).path("record").path("recordId").asLong();
        ok(request(HttpMethod.POST,"/work-records/"+id+"/submit",ownerToken,narrative(0,"Retained narrative")));
        return id;
    }

    @Test void adminDeleteRetainsContentAndSnapshotButBlocksEveryReadAndExport() throws Exception {
        String owner=login(OWNER),admin=login(ADMIN);long id=submittedForDelete(owner,1);
        JsonNode before=ok(request(HttpMethod.GET,"/work-records/"+id,owner,null));
        String snapshot=jdbc.queryForObject("SELECT statistics_json FROM biz_work_record_snapshot WHERE record_id=?",String.class,id);
        List<Map<String,Object>> entries=jdbc.queryForList("SELECT * FROM biz_work_record_entry WHERE record_id=?",id);
        List<Map<String,Object>> tasks=jdbc.queryForList("SELECT * FROM biz_task ORDER BY task_id");
        ok(request(HttpMethod.POST,"/work-records/"+id+"/delete",admin,Map.of("version",1,"reason","  Duplicate report  ")));
        Map<String,Object> row=jdbc.queryForMap("SELECT * FROM biz_work_record WHERE record_id=?",id);
        assertEquals(id,((Number)row.get("delete_marker")).longValue());assertEquals(ADMIN,((Number)row.get("deleted_by")).longValue());
        assertEquals("Duplicate report",row.get("delete_reason"));assertNotNull(row.get("deleted_time"));assertEquals(2,((Number)row.get("version")).longValue());
        assertEquals(before.path("record").path("problems").asText(),row.get("problems"));
        assertEquals(snapshot,jdbc.queryForObject("SELECT statistics_json FROM biz_work_record_snapshot WHERE record_id=?",String.class,id));
        assertEquals(entries,jdbc.queryForList("SELECT * FROM biz_work_record_entry WHERE record_id=?",id));
        assertEquals(tasks,jdbc.queryForList("SELECT * FROM biz_task ORDER BY task_id"));
        for(String token:List.of(owner,admin,login(VIEWER))) {
            assertEquals(404,request(HttpMethod.GET,"/work-records/"+id,token,null).getStatusCode().value());
            assertEquals(0,ok(request(HttpMethod.GET,"/work-records",token,null)).path("total").asInt());
            assertEquals(0,ok(request(HttpMethod.GET,"/work-records/authors",token,null)).size());
        }
        assertFalse(ok(request(HttpMethod.GET,"/work-records/capabilities",owner,null)).path("canViewOwnRecords").asBoolean());
        for(String action:List.of("save","submit","statistics/refresh")) assertEquals(404,request(HttpMethod.POST,"/work-records/"+id+"/"+action,owner,narrative(2,"Old window")).getStatusCode().value());
        long other=submittedForDelete(owner,2);
        assertEquals(404,request(HttpMethod.POST,"/work-records/export",admin,Map.of("ids",List.of(other,id))).getStatusCode().value());
    }

    @Test void deleteRequiresCurrentAdminAndDoesNotExposeOthersDrafts() throws Exception {
        String owner=login(OWNER),admin=login(ADMIN);long id=submittedForDelete(owner,1);
        assertTrue(ok(request(HttpMethod.GET,"/work-records/capabilities",admin,null)).path("canDelete").asBoolean());
        assertEquals(401,request(HttpMethod.POST,"/work-records/"+id+"/delete",null,Map.of("version",1,"reason","Delete")).getStatusCode().value());
        for(long account:List.of(OWNER,OTHER,LEADER,VIEWER)) {
            String token=login(account);assertFalse(ok(request(HttpMethod.GET,"/work-records/capabilities",token,null)).path("canDelete").asBoolean());
            assertEquals(403,request(HttpMethod.POST,"/work-records/"+id+"/delete",token,Map.of("version",1,"reason","Delete")).getStatusCode().value());
        }
        long draft=create(owner,2).path("record").path("recordId").asLong();
        assertEquals(404,request(HttpMethod.POST,"/work-records/"+draft+"/delete",admin,Map.of("version",0,"reason","Delete")).getStatusCode().value());
        jdbc.update("UPDATE sys_user SET role='1' WHERE user_id=?",ADMIN);
        assertEquals(403,request(HttpMethod.POST,"/work-records/"+id+"/delete",admin,Map.of("version",1,"reason","Delete")).getStatusCode().value());
        assertEquals(0,jdbc.queryForObject("SELECT SUM(delete_marker) FROM biz_work_record",Long.class));
    }

    @Test void deleteValidationAndStaleVersionLeaveRecordUntouched() throws Exception {
        String owner=login(OWNER),admin=login(ADMIN);long id=submittedForDelete(owner,1);
        Map<String,Object> before=jdbc.queryForMap("SELECT * FROM biz_work_record WHERE record_id=?",id);
        for(Object reason:List.of(""," \n\t","\u00a0","😀".repeat(301),42))
            assertEquals(400,request(HttpMethod.POST,"/work-records/"+id+"/delete",admin,Map.of("version",1,"reason",reason)).getStatusCode().value());
        for(Object version:List.of(-1,1.5,"1"))
            assertEquals(400,request(HttpMethod.POST,"/work-records/"+id+"/delete",admin,Map.of("version",version,"reason","Delete")).getStatusCode().value());
        assertEquals(409,request(HttpMethod.POST,"/work-records/"+id+"/delete",admin,Map.of("version",0,"reason","Delete")).getStatusCode().value());
        assertEquals(before,jdbc.queryForMap("SELECT * FROM biz_work_record WHERE record_id=?",id));
        ok(request(HttpMethod.POST,"/work-records/"+id+"/delete",admin,Map.of("version",1,"reason","😀".repeat(300))));
    }

    @Test void deletedMonthCanBeRecreatedConcurrentlyAndRepeatedHistoryIsRetained() throws Exception {
        String owner=login(OWNER),admin=login(ADMIN);long first=submittedForDelete(owner,1);
        ok(request(HttpMethod.POST,"/work-records/"+first+"/delete",admin,Map.of("version",1,"reason","Replace")));
        ExecutorService pool=Executors.newFixedThreadPool(2);long second;
        try {
            Callable<Long> call=()->create(owner,1).path("record").path("recordId").asLong();
            List<Future<Long>> results=pool.invokeAll(List.of(call,call));second=results.get(0).get(30,TimeUnit.SECONDS);
            assertEquals(second,results.get(1).get(30,TimeUnit.SECONDS));assertNotEquals(first,second);
        } finally {pool.shutdownNow();}
        ok(request(HttpMethod.POST,"/work-records/"+second+"/submit",owner,narrative(0,"Replacement")));
        ok(request(HttpMethod.POST,"/work-records/"+second+"/delete",admin,Map.of("version",1,"reason","Replace again")));
        long third=create(owner,1).path("record").path("recordId").asLong();assertNotEquals(second,third);
        assertEquals(3,jdbc.queryForObject("SELECT COUNT(*) FROM biz_work_record",Integer.class));
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM biz_work_record WHERE delete_marker<>0",Integer.class));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM biz_work_record WHERE delete_marker=0",Integer.class));
        assertEquals(3,jdbc.queryForObject("SELECT COUNT(*) FROM biz_work_record_snapshot",Integer.class));
    }

    @Test void concurrentDeleteCommitsOnceAndCannotAffectReplacement() throws Exception {
        String owner=login(OWNER),admin=login(ADMIN);long id=submittedForDelete(owner,1);
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> call=()->request(HttpMethod.POST,"/work-records/"+id+"/delete",admin,Map.of("version",1,"reason","Concurrent delete")).getStatusCode().value();
            List<Integer> codes=new ArrayList<>();for(Future<Integer> result:pool.invokeAll(List.of(call,call))) codes.add(result.get(30,TimeUnit.SECONDS));
            Collections.sort(codes);assertEquals(List.of(200,404),codes);
        } finally {pool.shutdownNow();}
        long replacement=create(owner,1).path("record").path("recordId").asLong();
        assertEquals(404,request(HttpMethod.POST,"/work-records/"+id+"/delete",admin,Map.of("version",1,"reason","Old click")).getStatusCode().value());
        assertEquals(0,jdbc.queryForObject("SELECT delete_marker FROM biz_work_record WHERE record_id=?",Long.class,replacement));
    }

    @Test void failedDeleteRollsBackAndPreservesSubmittedRecord() throws Exception {
        String owner=login(OWNER),admin=login(ADMIN);long id=submittedForDelete(owner,1);
        Map<String,Object> before=jdbc.queryForMap("SELECT * FROM biz_work_record WHERE record_id=?",id);
        jdbc.execute("CREATE TRIGGER fail_work_delete BEFORE UPDATE ON biz_work_record FOR EACH ROW BEGIN IF NEW.delete_marker<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic delete failure'; END IF; END");
        try {
            assertEquals(500,request(HttpMethod.POST,"/work-records/"+id+"/delete",admin,Map.of("version",1,"reason","Fail")).getStatusCode().value());
            assertEquals(before,jdbc.queryForMap("SELECT * FROM biz_work_record WHERE record_id=?",id));
            assertEquals(200,request(HttpMethod.GET,"/work-records/"+id,admin,null).getStatusCode().value());
        } finally {jdbc.execute("DROP TRIGGER fail_work_delete");}
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
