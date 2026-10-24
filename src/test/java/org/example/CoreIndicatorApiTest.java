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
@Import(CoreIndicatorApiTest.NoSchedules.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={
        "work-records.viewer-user-ids=910005", "logging.file.name=target/work-record-regression.log",
        "logging.level.org.springframework=WARN", "logging.level.org.example.mapper=WARN"})
class CoreIndicatorApiTest {
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
    private JsonNode preview(String token, int month) throws Exception { return ok(request(HttpMethod.GET,"/work-records/statistics?year=2026&month="+month,token,null)); }
    private JsonNode create(String token, int month) throws Exception { return ok(request(HttpMethod.POST,"/work-records",token,Map.of("year",2026,"month",month))); }


    private JsonNode draft(String token,int month) throws Exception {
        return ok(request(HttpMethod.POST,"/core-indicators/1/draft",token,Map.of("year",2026,"month",month)));
    }

    @Test
    void reportIndicatorUsesCorrectMetadataAndPrincipalWithoutChangingPerformance() throws Exception {
        jdbc.update("INSERT INTO biz_performance(perf_id,project_id,perf_code,perf_name,target_value,data_type,dept_id,leader_id,auditor_id,principal_id,is_delete) VALUES(960015,1,'1.2.15','毕业生就业相关度',85,'2',920001,?,?,?,0)",OWNER,VIEWER,OTHER);
        String token=login(OWNER);
        JsonNode items=ok(request(HttpMethod.GET,"/core-indicators?year=2026",token,null));
        JsonNode item=items.get(9);
        assertEquals("1.2.45",item.path("indicatorCode").asText());
        assertEquals("发布《工程机械产业发展报告》数量",item.path("indicatorName").asText());
        assertEquals(5,item.path("targetValue").asInt());
        assertEquals("1",item.path("dataType").asText());
        JsonNode d=ok(request(HttpMethod.POST,"/core-indicators/10/draft",token,Map.of("year",2026,"month",8)));
        d=ok(request(HttpMethod.PUT,"/core-indicators/reports/"+d.path("reportId").asLong(),token,Map.of("version",d.path("version").asLong(),"previousYearValue",0,"actualValue",2,"comment","Report progress")));
        JsonNode submitted=ok(request(HttpMethod.POST,"/core-indicators/reports/"+d.path("reportId").asLong()+"/submit",token,Map.of("version",d.path("version").asLong())));
        assertEquals("1.2.45",submitted.path("performanceCode").asText());
        assertEquals(LEADER,submitted.path("currentHandlerId").asLong());
        assertEquals(403,request(HttpMethod.POST,"/core-indicators/reports/"+submitted.path("reportId").asLong()+"/audit",login(OTHER),Map.of("version",0,"pass",true)).getStatusCode().value());
        assertEquals("毕业生就业相关度",jdbc.queryForObject("SELECT perf_name FROM biz_performance WHERE perf_id=960015",String.class));
        assertEquals(85,jdbc.queryForObject("SELECT target_value FROM biz_performance WHERE perf_id=960015",Integer.class));
        assertEquals(7,jdbc.queryForObject("SELECT current_value FROM biz_performance WHERE perf_id=960010",Integer.class));
    }

    @Test
    void obsoleteReportsRemainReadableButCannotAffectCurrentResultsOrWorkflow() throws Exception {
        for(int status:List.of(0,10,30,-10)) {
            jdbc.update("INSERT INTO biz_core_report(indicator_id,record_year,record_month,performance_code,indicator_name,target_value,data_type,actual_value,submit_by,principal_id,current_handler_id,flow_status,submit_time,archive_time) VALUES(10,2026,8,'1.2.15','毕业生就业相关度',85,'2',85,?,?,?, ?,NOW(),NOW())",OWNER,LEADER,LEADER,status);
        }
        String token=login(OWNER);
        JsonNode item=ok(request(HttpMethod.GET,"/core-indicators?year=2026",token,null)).get(9);
        assertEquals(0,item.path("recentReports").size());
        assertTrue(item.path("effectiveResult").isNull());
        assertNull(core.effective(10L,2026,8));
        assertTrue(core.audits(LEADER,true).isEmpty());
        JsonNode history=ok(request(HttpMethod.GET,"/core-indicators/10/history",token,null));
        long id=history.get(0).path("reportId").asLong();
        JsonNode detail=ok(request(HttpMethod.GET,"/core-indicators/reports/"+id,token,null));
        assertEquals("毕业生就业相关度",detail.path("indicatorName").asText());
        assertTrue(detail.path("mappingObsolete").asInt()==1||detail.path("mappingObsolete").asBoolean());
        long pending=jdbc.queryForObject("SELECT report_id FROM biz_core_report WHERE flow_status=10",Long.class);
        assertEquals(409,request(HttpMethod.POST,"/core-indicators/reports/"+pending+"/audit",login(LEADER),Map.of("version",0,"pass",true)).getStatusCode().value());
        JsonNode d=ok(request(HttpMethod.POST,"/core-indicators/10/draft",token,Map.of("year",2026,"month",8)));
        assertTrue(d.path("actualValue").isNull());
        d=ok(request(HttpMethod.PUT,"/core-indicators/reports/"+d.path("reportId").asLong(),token,Map.of("version",d.path("version").asLong(),"previousYearValue",0,"actualValue",2)));
        assertEquals(10,ok(request(HttpMethod.POST,"/core-indicators/reports/"+d.path("reportId").asLong()+"/submit",token,Map.of("version",d.path("version").asLong()))).path("flowStatus").asInt());
        assertEquals(4,jdbc.queryForObject("SELECT COUNT(*) FROM biz_core_report WHERE performance_code='1.2.15'",Integer.class));
    }

    @Test
    void reportIndicatorRefusesAUniqueButWrongPerformanceName() throws Exception {
        jdbc.update("UPDATE biz_performance SET perf_name='毕业生就业相关度' WHERE perf_id=960010");
        String token=login(OWNER);
        JsonNode item=ok(request(HttpMethod.GET,"/core-indicators",token,null)).get(9);
        assertTrue(item.path("configurationError").asText().contains("关联名称不符"));
        assertEquals(409,request(HttpMethod.POST,"/core-indicators/10/draft",token,Map.of("year",2026,"month",8)).getStatusCode().value());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM biz_core_report",Integer.class));
    }
    private JsonNode saved(String token,int month) throws Exception {
        JsonNode d=draft(token,month);
        return ok(request(HttpMethod.PUT,"/core-indicators/reports/"+d.path("reportId").asLong(),token,
            Map.of("version",d.path("version").asLong(),"previousYearValue",0,"actualValue",15,"comment","Synthetic progress")));
    }
    private JsonNode submit(String token,int month) throws Exception {
        JsonNode d=saved(token,month);
        return ok(request(HttpMethod.POST,"/core-indicators/reports/"+d.path("reportId").asLong()+"/submit",token,Map.of("version",d.path("version").asLong())));
    }
    private JsonNode audit(JsonNode r,long actor,boolean pass) throws Exception {
        return ok(request(HttpMethod.POST,"/core-indicators/reports/"+r.path("reportId").asLong()+"/audit",login(actor),
            Map.of("version",r.path("version").asLong(),"pass",pass,"comment",pass?"Verified":"Return to reporter")));
    }
    @Test void eligibilityUsesLevel3OrLevel4OwnerAndKeepsDraftPrivate() throws Exception {
        for(long id:List.of(OWNER,OTHER,ADMIN,OFFICE,LEADER,VIEWER)) {
            JsonNode caps=ok(request(HttpMethod.GET,"/core-indicators/capabilities",login(id),null));
            assertEquals(id==OWNER||id==OTHER||id==ADMIN||id==OFFICE,caps.path("canFill").asBoolean());
        }
        JsonNode d=draft(login(OWNER),1);
        assertEquals(200,request(HttpMethod.GET,"/core-indicators/reports/"+d.path("reportId").asLong(),login(ADMIN),null).getStatusCode().value());
        assertEquals(404,request(HttpMethod.GET,"/core-indicators/reports/"+d.path("reportId").asLong(),login(OTHER),null).getStatusCode().value());
        assertEquals(200,request(HttpMethod.POST,"/core-indicators/1/draft",login(ADMIN),Map.of("year",2026,"month",1)).getStatusCode().value());
        assertEquals(401,request(HttpMethod.GET,"/core-indicators",null,null).getStatusCode().value());
    }
    @Test void adminAndOfficeCanFillWithoutTaskResponsibilitiesAndKeepWorkflowStages() throws Exception {
        int month=1;
        for(long actor:List.of(ADMIN,OFFICE)) {
            String token=login(actor);
            assertEquals(5,ok(request(HttpMethod.GET,"/core-indicators/capabilities",token,null)).path("fillableYears").size());
            assertTrue(ok(request(HttpMethod.GET,"/system/access",token,null)).path("canViewCoreIndicators").asBoolean());
            JsonNode r=submit(token,month++);
            assertEquals(actor,r.path("submitBy").asLong());
            assertEquals(LEADER,r.path("currentHandlerId").asLong());
            if(actor!=ADMIN)assertEquals(403,request(HttpMethod.POST,"/core-indicators/reports/"+r.path("reportId").asLong()+"/audit",token,Map.of("version",0,"pass",true)).getStatusCode().value());
            r=audit(r,LEADER,true);r=audit(r,ADMIN,true);
            assertEquals(30,r.path("flowStatus").asInt());
        }
        assertEquals(403,request(HttpMethod.POST,"/core-indicators/1/draft",login(VIEWER),Map.of("year",2026,"month",3)).getStatusCode().value());
    }
    @Test void adminOrOfficeFillPermissionIsRevokedImmediatelyWithExistingToken() throws Exception {
        String admin=login(ADMIN),office=login(OFFICE);
        JsonNode a=draft(admin,1),o=draft(office,2);
        jdbc.update("UPDATE sys_user SET role='1' WHERE user_id=?",ADMIN);
        jdbc.update("UPDATE sys_user SET dept_id=920001 WHERE user_id=?",OFFICE);
        for(var entry:List.of(Map.entry(admin,a),Map.entry(office,o))) {
            assertFalse(ok(request(HttpMethod.GET,"/core-indicators/capabilities",entry.getKey(),null)).path("canFill").asBoolean());
            assertEquals(403,request(HttpMethod.PUT,"/core-indicators/reports/"+entry.getValue().path("reportId").asLong(),entry.getKey(),Map.of("version",0,"previousYearValue",0,"actualValue",1)).getStatusCode().value());
        }
        jdbc.update("UPDATE sys_user SET dept_id=100 WHERE user_id=?",OFFICE);
        jdbc.update("UPDATE sys_dept SET status='1' WHERE dept_id=100");
        assertFalse(ok(request(HttpMethod.GET,"/core-indicators/capabilities",office,null)).path("canFill").asBoolean());
        jdbc.update("UPDATE sys_dept SET status='0' WHERE dept_id=100");
        jdbc.update("UPDATE sys_user SET status='0' WHERE user_id=?",OFFICE);
        assertEquals(403,request(HttpMethod.POST,"/core-indicators/1/draft",office,Map.of("year",2026,"month",3)).getStatusCode().value());
    }
    @Test void workflowUsesPrincipalThenArchiveWithoutChangingPerformance() throws Exception {
        JsonNode r=submit(login(OWNER),1);
        assertEquals(LEADER,r.path("currentHandlerId").asLong());
        String path="/core-indicators/reports/"+r.path("reportId").asLong()+"/audit";
        assertEquals(403,request(HttpMethod.POST,path,login(OTHER),Map.of("version",0,"pass",true)).getStatusCode().value());
        assertEquals(403,request(HttpMethod.POST,path,login(VIEWER),Map.of("version",0,"pass",true)).getStatusCode().value());
        assertNull(core.effective(1L,2026,1));
        r=audit(r,LEADER,true);
        assertEquals(20,r.path("flowStatus").asInt());
        assertEquals(1,ok(request(HttpMethod.GET,"/core-indicators/audit/records",login(LEADER),null)).size());
        assertEquals(1,ok(request(HttpMethod.GET,"/core-indicators/audit/todo",login(ADMIN),null)).size());
        r=audit(r,ADMIN,true);
        assertEquals(30,r.path("flowStatus").asInt());
        assertEquals(3,r.path("logs").size());
        assertEquals(0,ok(request(HttpMethod.GET,"/core-indicators/audit/todo",login(ADMIN),null)).size());
        assertEquals(15,((java.math.BigDecimal)core.effective(1L,2026,1).get("actualValue")).intValue());
        assertEquals(7,jdbc.queryForObject("SELECT current_value FROM biz_performance WHERE perf_id=960001",Integer.class));
    }
    @Test void validationAndVersionConflictsDoNotWrite() throws Exception {
        String token=login(OWNER);JsonNode d=draft(token,1);String path="/core-indicators/reports/"+d.path("reportId").asLong();
        for(Object value:List.of(-1,"0.00001","10000000000000000")) {
            assertEquals(400,request(HttpMethod.PUT,path,token,Map.of("version",0,"previousYearValue",0,"actualValue",value)).getStatusCode().value());
        }
        assertEquals(400,request(HttpMethod.PUT,path,token,Map.of("version",0,"comment","字".repeat(501))).getStatusCode().value());
        assertEquals(400,request(HttpMethod.POST,path+"/submit",token,Map.of("version",0)).getStatusCode().value());
        JsonNode saved=saved(token,1);
        assertEquals(409,request(HttpMethod.PUT,path,token,Map.of("version",0,"comment","stale")).getStatusCode().value());
        assertEquals(1,saved.path("version").asInt());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM biz_core_audit_log",Integer.class));
    }
    @Test void rejectedAndWithdrawnSubmissionsRemainInHistoryAndRestartFlow() throws Exception {
        String owner=login(OWNER);JsonNode r=submit(owner,1);
        assertEquals(403,request(HttpMethod.POST,"/core-indicators/reports/"+r.path("reportId").asLong()+"/withdraw",login(OTHER),Map.of("version",0)).getStatusCode().value());
        ok(request(HttpMethod.POST,"/core-indicators/reports/"+r.path("reportId").asLong()+"/withdraw",owner,Map.of("version",0)));
        r=submit(owner,1);r=audit(r,LEADER,false);
        assertEquals(-10,r.path("flowStatus").asInt());
        JsonNode restarted=draft(owner,1);
        assertEquals(15,restarted.path("actualValue").asInt());
        assertEquals("Synthetic progress",restarted.path("comment").asText());
        r=submit(owner,1);r=audit(r,LEADER,true);
        assertEquals(409,request(HttpMethod.POST,"/core-indicators/reports/"+r.path("reportId").asLong()+"/withdraw",owner,Map.of("version",1)).getStatusCode().value());
        r=audit(r,ADMIN,false);assertEquals(-20,r.path("flowStatus").asInt());
        r=submit(owner,1);assertEquals(10,r.path("flowStatus").asInt());
        assertEquals(4,ok(request(HttpMethod.GET,"/core-indicators/1/history",owner,null)).size());
    }
    @Test void recentTwoCrossMonthsAndFrozenEffectiveResults() throws Exception {
        String token=login(OWNER);
        for(int month=1;month<=3;month++)audit(audit(submit(token,month),LEADER,true),ADMIN,true);
        jdbc.update("UPDATE biz_performance SET perf_name='Changed',target_value=999 WHERE perf_id=960001");
        JsonNode item=ok(request(HttpMethod.GET,"/core-indicators?year=2026",token,null)).get(0);
        assertEquals(2,item.path("recentReports").size());
        assertEquals(3,item.path("recentReports").get(0).path("month").asInt());
        assertEquals("Synthetic indicator",item.path("effectiveResult").path("indicatorName").asText());
        assertEquals(3,ok(request(HttpMethod.GET,"/core-indicators/1/history",token,null)).size());
        assertEquals(2,((Number)core.effective(1L,2026,2).get("month")).intValue());
        assertNull(core.effective(1L,2025,12));
        for(int month=1;month<=4;month++){
            assertEquals(json.readTree(json.writeValueAsString(core.effective(1L,2026,month))),json.readTree(json.writeValueAsString(core.effectiveResults(OWNER,2026,month).get(1L))));
            var history=core.history(1L,OWNER,2026,month);assertEquals(json.readTree(json.writeValueAsString(history.isEmpty()?null:history.get(0))),json.readTree(json.writeValueAsString(core.selectedMonthReports(OWNER,2026,month).get(1L))));
        }
        assertEquals(json.readTree(json.writeValueAsString(core.history(1L,OWNER,2026,null).subList(0,2))),json.readTree(json.writeValueAsString(core.list(OWNER,2026,null).get(0).get("recentReports"))));

    }
    @Test void concurrentReportersProduceOnlyOneActiveSubmission() throws Exception {
        String a=login(OWNER),b=login(OTHER);JsonNode da=saved(a,1),db=saved(b,1);
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
        try {
            List<Future<Integer>> jobs=new ArrayList<>();
            for(var entry:List.of(Map.entry(a,da),Map.entry(b,db)))jobs.add(pool.submit(()->{start.await();return request(HttpMethod.POST,"/core-indicators/reports/"+entry.getValue().path("reportId").asLong()+"/submit",entry.getKey(),Map.of("version",1)).getStatusCode().value();}));
            start.countDown();List<Integer> statuses=new ArrayList<>();for(var job:jobs)statuses.add(job.get(20,TimeUnit.SECONDS));Collections.sort(statuses);
            assertEquals(List.of(200,409),statuses);
            assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM biz_core_report WHERE flow_status IN(10,20)",Integer.class));
            assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM biz_core_audit_log",Integer.class));
        } finally {pool.shutdownNow();}
    }
    @Test void withdrawalAndApprovalCannotBothCommit() throws Exception {
        String owner=login(OWNER),principal=login(LEADER);JsonNode report=submit(owner,1);
        String path="/core-indicators/reports/"+report.path("reportId").asLong();
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
        try {
            Future<Integer> withdrawal=pool.submit(()->{start.await();return request(HttpMethod.POST,path+"/withdraw",owner,Map.of("version",0)).getStatusCode().value();});
            Future<Integer> approval=pool.submit(()->{start.await();return request(HttpMethod.POST,path+"/audit",principal,Map.of("version",0,"pass",true)).getStatusCode().value();});
            start.countDown();List<Integer> results=new ArrayList<>(List.of(withdrawal.get(20,TimeUnit.SECONDS),approval.get(20,TimeUnit.SECONDS)));Collections.sort(results);
            assertEquals(List.of(200,409),results);
            assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM biz_core_audit_log",Integer.class));
        } finally {pool.shutdownNow();}
    }
    @Test void invalidMappingOrReviewerPreventsSubmissionWithoutSideEffects() throws Exception {
        String token=login(OWNER);JsonNode d=saved(token,1);
        jdbc.update("UPDATE sys_user SET status='0' WHERE user_id=?",LEADER);
        assertEquals(409,request(HttpMethod.POST,"/core-indicators/reports/"+d.path("reportId").asLong()+"/submit",token,Map.of("version",1)).getStatusCode().value());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM biz_core_audit_log",Integer.class));
        jdbc.update("UPDATE biz_performance SET is_delete=1 WHERE perf_id=960001");
        JsonNode items=ok(request(HttpMethod.GET,"/core-indicators",token,null));assertEquals(10,items.size());
        assertTrue(items.get(0).has("configurationError"));
    }
}
