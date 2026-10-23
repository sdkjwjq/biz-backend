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
@Import(AdminOverviewApiTest.NoSchedules.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={
        "work-records.viewer-user-ids=910005", "logging.file.name=target/work-record-regression.log",
        "logging.level.org.springframework=WARN", "logging.level.org.example.mapper=WARN"})
class AdminOverviewApiTest {
    private static final long ADMIN=110228, OWNER=910001, OTHER=910002, LEADER=910003, VIEWER=910005, OFFICE=910006;
    private static final String PASSWORD="WorkRecords123";
    @Autowired private TestRestTemplate http;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ObjectMapper json;
    @LocalServerPort private int port;
    @MockBean(name="workRecordClock") private Clock clock;
    @Autowired private org.example.service.CoreIndicatorService core;
    @Autowired private org.example.service.BudgetService budgets;

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

    private void month(int month,int cumulative) {
        jdbc.update("INSERT INTO biz_budget_sheet(sheet_id,year,month,locked,is_delete) VALUES(?,2026,?,1,0)",970000+month,month);
        jdbc.update("INSERT INTO biz_budget_source_item(sheet_id,source_key,source_name,display_order,annual_plan) VALUES(?,'school','School budget',1,120)",970000+month);
        for(int index=1;index<=10;index++)jdbc.update("INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,core_output,target_value,achieved) VALUES(?,?,'Reform task',10,?,0,0,'Imported original indicator','Original target','Imported value must stay raw')",970000+month,index,cumulative);
    }
    private JsonNode trend(int index) throws Exception {
        return ok(request(HttpMethod.GET,"/budget/trends?year=2026&taskIndex="+index,login(ADMIN),null)).path("points");
    }
    private JsonNode summary(String token,int month) throws Exception {
        return ok(request(HttpMethod.GET,"/admin/overview/summary?year=2026&month="+month,token,null));
    }
    private JsonNode analysis(String token,String metric,String dimension) throws Exception {
        return ok(request(HttpMethod.GET,"/admin/overview/analysis?year=2026&month=4&metric="+metric+"&dimension="+dimension,token,null));
    }
    private JsonNode value(JsonNode summary,String module,String key) {
        for(JsonNode m:summary.path("modules").path(module).path("metrics"))if(key.equals(m.path("key").asText()))return m.path("value");
        fail("Missing metric "+module+"."+key+": "+summary);return null;
    }
    @Test void taskCountsDeduplicateGoalsAndKeepOverlappingFlags() throws Exception {
        task(900011,OWNER,2026,10,"3");task(900012,OWNER,2025,10,"1");
        jdbc.update("UPDATE biz_task SET progress=50,is_priority=1,is_difficult=1 WHERE task_id=900010");
        jdbc.update("UPDATE biz_task SET progress=100 WHERE task_id=900011");
        jdbc.update("INSERT INTO biz_level4_task(task_id,parent_id,task_name,leader_id,dept_id,is_delete) VALUES(950002,900010,'Another goal',?,920001,0)",OWNER);
        String token=login(ADMIN);JsonNode s=summary(token,4);
        assertEquals(2,value(s,"tasks","total").asInt());assertEquals(50,value(s,"tasks","completionRate").asInt());assertEquals(75,value(s,"tasks","averageProgress").asInt());
        assertEquals(1,value(s,"tasks","priority").asInt());assertEquals(1,value(s,"tasks","difficult").asInt());
        JsonNode a=analysis(token,"tasks.total","reform");assertEquals(2,a.path("total").asInt());assertEquals(2,a.path("chart").get(0).path("value").asInt());
    }
    @Test void annualPerformanceExcludesDirectoriesAndDoesNotSumDifferentUnits() throws Exception {
        jdbc.update("UPDATE biz_performance SET ancestors='0,1,1.1'");
        jdbc.update("INSERT INTO biz_performance_year(year_id,perf_id,year,target_value,actual_value,is_delete) VALUES(980001,960001,2026,10,15,0),(980002,960002,2026,100,50,0),(980003,960003,2026,0,5,0)");
        jdbc.update("UPDATE biz_performance SET ancestors='0,1' WHERE perf_id=960010");
        JsonNode s=summary(login(ADMIN),4);
        assertEquals(9,value(s,"performance","total").asInt());assertEquals(1,value(s,"performance","attained").asInt());
        assertEquals(1,value(s,"performance","unattained").asInt());assertEquals(7,value(s,"performance","uncertain").asInt());
    }
    private void coreResult(int month,String code,int value) {
        jdbc.update("INSERT INTO biz_core_report(indicator_id,record_year,record_month,performance_code,indicator_name,target_value,actual_value,submit_by,flow_status,submit_time,archive_time) VALUES(1,2026,?,?,'Frozen indicator',10,?,?,30,NOW(),NOW())",month,code,value,OWNER);
    }
    @Test void coreCutoffAndMappingKeepFutureAndObsoleteResultsOut() throws Exception {
        coreResult(2,"1.1.5",5);coreResult(5,"1.1.5",20);coreResult(3,"obsolete",99);
        JsonNode s=summary(login(ADMIN),4);assertEquals(1,value(s,"core","archived").asInt());assertEquals(0,value(s,"core","attained").asInt());
        JsonNode a=analysis(login(ADMIN),"core.archived","indicator");assertEquals(5,a.path("rows").get(0).path("value").asInt());assertEquals(2,a.path("rows").get(0).path("month").asInt());
        jdbc.update("UPDATE biz_core_indicator SET indicator_code='missing' WHERE indicator_id=1");
        s=summary(login(ADMIN),4);assertEquals(1,value(s,"core","invalid").asInt());assertEquals(0,value(s,"core","archived").asInt());
    }
    @Test void achievementYearAwardsAndRejectionReasonStayDistinct() throws Exception {
        // 隔离库模拟历史兼容数据中缺少取得日期；正式库结构不改变。
        jdbc.execute("ALTER TABLE biz_achievement MODIFY got_time datetime NULL");
        jdbc.update("INSERT INTO biz_achievement(ach_id,ach_name,got_time,audit_status,category,dept_id,yi_deng_jiang,is_delete,level,department,create_by,comment,is_competition,create_time,update_time) VALUES(970001,'Archived','2026-03-01',30,1,920001,3,0,'省级','Synthetic',910001,'Fixture',1,NOW(),NOW()),(970002,'Rejected','2026-03-02',-10,2,920001,9,0,'省级','Synthetic',910001,'Fixture',1,NOW(),NOW()),(970003,'No date',NULL,30,2,920001,7,0,'省级','Synthetic',910001,'Fixture',1,NOW(),NOW()),(970004,'Old','2025-01-01',30,1,920001,8,0,'省级','Synthetic',910001,'Fixture',1,NOW(),NOW())");
        JsonNode s=summary(login(ADMIN),4);assertEquals(2,value(s,"achievements","total").asInt());assertEquals(3,value(s,"achievements","awards").asInt());
        assertEquals(1,s.path("modules").path("achievements").path("extra").path("missingDates").asInt());
        JsonNode a=analysis(login(ADMIN),"achievements.awards","category");assertEquals(1,a.path("total").asInt());assertEquals(3,a.path("chart").get(0).path("value").asInt());
        jdbc.update("DELETE FROM biz_achievement WHERE got_time IS NULL");
        jdbc.execute("ALTER TABLE biz_achievement MODIFY got_time datetime NOT NULL");
    }
    @Test void recordDenominatorIntersectionAndDraftPrivacy() throws Exception {
        jdbc.update("INSERT INTO biz_work_record(owner_id,owner_name,record_year,record_month,status,problems,create_time,update_time,submit_time) VALUES(?,'Current',2026,4,1,'submitted',NOW(),NOW(),NOW()),(999999,'Former',2026,4,1,'historical',NOW(),NOW(),NOW()),(?,'Draft',2026,4,0,'SECRET-DRAFT',NOW(),NOW(),NULL)",OWNER,OTHER);
        String token=login(ADMIN);JsonNode s=summary(token,4);assertEquals(1,value(s,"records","submitted").asInt());assertEquals(1,value(s,"records","historical").asInt());
        int expected=value(s,"records","expected").asInt();assertTrue(expected>=2);assertEquals(expected-1,value(s,"records","missing").asInt());
        JsonNode a=analysis(token,"records.missing","department");assertEquals(expected-1,a.path("total").asInt());assertFalse(a.toString().contains("SECRET-DRAFT"));assertFalse(a.toString().contains("problems"));
        jdbc.update("UPDATE sys_user SET status='0' WHERE user_id=?",OWNER);
        s=summary(token,4);assertEquals(0,value(s,"records","submitted").asInt());assertEquals(2,value(s,"records","historical").asInt());
    }
    @Test void budgetMissingMonthsZeroAndNegativeAdjustments() throws Exception {
        for(int month:List.of(1,2,4)) {
            jdbc.update("INSERT INTO biz_budget_sheet(sheet_id,year,month,is_delete) VALUES(?,?,?,0)",990000+month,2026,month);
            jdbc.update("INSERT INTO biz_budget_source_item(sheet_id,source_key,source_name,annual_plan) VALUES(?,'one','Source',100)",990000+month);
            for(int i=1;i<=10;i++)jdbc.update("INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other) VALUES(?,?,'Reform',10,?,0,0)",990000+month,i,month==1?2:month==2?1:0);
        }
        String token=login(ADMIN);assertTrue(value(summary(token,3),"budget","expense").isNull());
        assertEquals(0,value(summary(token,4),"budget","expense").asInt());
        JsonNode points=analysis(token,"budget.expense","trend").path("chart");assertEquals(4,points.size());assertTrue(points.get(1).path("adjustment").asBoolean());assertTrue(points.get(3).path("incremental").isNull());
    }
    @Test void exactReadOnlyPermissionAndCurrentDatabaseScope() throws Exception {
        jdbc.update("INSERT INTO sys_leadership_access(user_id,scope,version) VALUES(?,'DEPARTMENTS',1)",VIEWER);
        jdbc.update("INSERT INTO sys_leadership_department(user_id,dept_id) VALUES(?,920001)",VIEWER);
        String leader=login(VIEWER),ordinary=login(OTHER);
        assertEquals(403,request(HttpMethod.GET,"/admin/overview/summary?year=2026&month=4",ordinary,null).getStatusCode().value());
        assertEquals(1,value(summary(leader,4),"tasks","total").asInt());
        assertEquals(403,request(HttpMethod.GET,"/admin/users",leader,null).getStatusCode().value());
        assertEquals(403,request(HttpMethod.POST,"/admin/overview/summary",leader,Map.of()).getStatusCode().value());
        jdbc.update("DELETE FROM sys_leadership_department WHERE user_id=?",VIEWER);
        assertEquals(0,value(summary(leader,4),"tasks","total").asInt());
        assertEquals(0,analysis(leader,"tasks.total","department").path("total").asInt());
        jdbc.update("UPDATE sys_leadership_access SET scope='NONE',version=version+1 WHERE user_id=?",VIEWER);
        assertEquals(403,request(HttpMethod.GET,"/admin/overview/analysis?year=2026&month=4&metric=tasks.total",leader,null).getStatusCode().value());
    }
    @Test void allowlistsPaginationAndReadOnlyNoBusinessUpdates() throws Exception {
        String token=login(ADMIN);String before=jdbc.queryForList("SELECT * FROM biz_task").toString();
        for(String suffix:List.of("metric=tasks.bad","metric=tasks.total&dimension=SQL","metric=tasks.total&pageSize=101","metric=tasks.total&page=0"))
            assertEquals(400,request(HttpMethod.GET,"/admin/overview/analysis?year=2026&month=4&"+suffix,token,null).getStatusCode().value());
        JsonNode a=ok(request(HttpMethod.GET,"/admin/overview/analysis?year=2026&month=4&metric=tasks.total&page=999",token,null));assertEquals(1,a.path("page").asInt());
        assertEquals(before,jdbc.queryForList("SELECT * FROM biz_task").toString());
        assertTrue(value(summary(token,4),"core","archived").isNumber());assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM biz_core_report",Integer.class));
    }
    @Test void moduleFailureIsNotAFalseZeroAndOtherModulesRemainAvailable() throws Exception {
        jdbc.execute("RENAME TABLE biz_achievement TO overview_test_achievement");
        try {JsonNode s=summary(login(ADMIN),4);assertEquals("error",s.path("modules").path("achievements").path("status").asText());assertFalse(s.path("modules").path("achievements").has("metrics"));assertEquals(1,value(s,"tasks","total").asInt());}
        finally {jdbc.execute("RENAME TABLE overview_test_achievement TO biz_achievement");}
    }
}
