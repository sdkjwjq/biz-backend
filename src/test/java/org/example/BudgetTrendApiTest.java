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
@Import(BudgetTrendApiTest.NoSchedules.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={
        "work-records.viewer-user-ids=910005", "logging.file.name=target/work-record-regression.log",
        "logging.level.org.springframework=WARN", "logging.level.org.example.mapper=WARN"})
class BudgetTrendApiTest {
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
        String[] codes={"1.1.5","1.1.7","1.1.55","1.1.14","1.1.20","1.1.28","1.1.63","1.1.66","1.1.45","1.2.15"};
        for(int i=0;i<codes.length;i++) {
            jdbc.update("INSERT INTO biz_core_indicator(indicator_id,indicator_code,sort_order) VALUES(?,?,?)",i+1,codes[i],i+1);
            jdbc.update("INSERT INTO biz_performance(perf_id,project_id,parent_id,perf_code,perf_name,dept_id,leader_id,auditor_id,principal_id,target_value,current_value,data_type,is_delete) VALUES(?,1,0,?,'Synthetic indicator',920001,?,?,?,10,7,'1',0)",960001+i,codes[i],OWNER,VIEWER,LEADER);
        }
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
    @Test void continuousMonthsGapsAndNegativeAdjustments() throws Exception {
        month(1,1);month(2,3);month(4,5);month(5,4);
        JsonNode p=trend(0);assertEquals(12,p.size());
        assertEquals(10,p.get(0).path("cumulative").asInt());assertEquals(10,p.get(0).path("incremental").asInt());
        assertEquals(20,p.get(1).path("incremental").asInt());assertTrue(p.get(2).path("cumulative").isNull());
        assertTrue(p.get(3).path("incremental").isNull());assertEquals(-10,p.get(4).path("incremental").asInt());assertTrue(p.get(4).path("adjustment").asBoolean());
        assertEquals(25,p.get(1).path("executionRate").asDouble());
        assertEquals(30,trend(1).get(1).path("executionRate").asDouble());
    }
    @Test void zeroMissingValuesAndDenominatorsRemainDistinct() throws Exception {
        month(1,0);month(2,1);assertEquals(0,trend(0).get(0).path("cumulative").asInt());
        jdbc.update("UPDATE biz_budget_task_item SET goods=NULL WHERE sheet_id=970002 AND task_index=1");
        assertTrue(trend(0).get(1).path("cumulative").isNull());assertTrue(trend(1).get(1).path("cumulative").isNull());
        assertEquals(1,trend(2).get(1).path("cumulative").asInt());
        jdbc.update("UPDATE biz_budget_source_item SET annual_plan=0 WHERE sheet_id=970001");
        jdbc.update("UPDATE biz_budget_task_item SET amount=NULL WHERE sheet_id=970001 AND task_index=1");
        assertTrue(trend(0).get(0).path("executionRate").isNull());assertTrue(trend(1).get(0).path("executionRate").isNull());
    }
    @Test void accessAndInvalidPeriodsAreRejected() throws Exception {
        assertEquals(401,request(HttpMethod.GET,"/budget/trends?year=2026",null,null).getStatusCode().value());
        assertEquals(403,request(HttpMethod.GET,"/budget/trends?year=2026",login(OWNER),null).getStatusCode().value());
        assertEquals(400,request(HttpMethod.GET,"/budget/trends?year=2024",login(ADMIN),null).getStatusCode().value());
        assertEquals(400,request(HttpMethod.GET,"/budget/trends?year=2026&taskIndex=11",login(ADMIN),null).getStatusCode().value());
        assertEquals(12,trend(0).size());assertTrue(trend(0).get(0).path("cumulative").isNull());
    }
    @Test void archivedCoreValuesNeverReferenceFutureMonthsOrMutateImports() throws Exception {
        month(1,1);month(2,2);month(3,3);
        String owner=login(OWNER);JsonNode january=audit(audit(submit(owner,1),LEADER,true),ADMIN,true);
        audit(audit(submit(owner,3),LEADER,true),ADMIN,true);
        JsonNode pending=submit(owner,2);
        JsonNode shown=ok(request(HttpMethod.GET,"/budget?year=2026&month=2",login(ADMIN),null));
        assertEquals(january.path("reportId"),shown.path("archivedIndicators").path("1").path("reportId"));
        assertEquals(1,shown.path("archivedIndicators").path("1").path("month").asInt());
        assertFalse(shown.path("archivedIndicators").has("2"));
        assertEquals("Imported value must stay raw",shown.path("tasks").get(0).path("achieved").asText());
        org.springframework.mock.web.MockHttpServletResponse response=new org.springframework.mock.web.MockHttpServletResponse();budgets.exportBudget(2026,2,response);
        try(var workbook=org.apache.poi.ss.usermodel.WorkbookFactory.create(new java.io.ByteArrayInputStream(response.getContentAsByteArray()))) {
            assertTrue(workbook.getSheetAt(0).getRow(14).getCell(4).getStringCellValue().contains("2026年1月"));
            assertEquals("",workbook.getSheetAt(0).getRow(14).getCell(5).getStringCellValue());
        }
        assertEquals("Imported value must stay raw",jdbc.queryForObject("SELECT achieved FROM biz_budget_task_item WHERE sheet_id=970002 AND task_index=1",String.class));
        assertEquals(10,pending.path("flowStatus").asInt());
    }
    @Test void restorationRefreshesTrendWithoutDuplicatingHistory() throws Exception {
        month(1,1);month(2,2);
        String first=json.writeValueAsString(budgets.getBudget(2026,2));
        jdbc.update("UPDATE biz_budget_task_item SET goods=9 WHERE sheet_id=970002");
        String second=json.writeValueAsString(budgets.getBudget(2026,2));
        jdbc.update("INSERT INTO biz_budget_import_version(version_id,sheet_id,year,month,version_no,operation_type,snapshot_json,created_by,is_delete) VALUES(980001,970002,2026,2,1,'IMPORT',?,?,0),(980002,970002,2026,2,2,'IMPORT',?,?,0)",first,ADMIN,second,ADMIN);
        assertEquals(90,trend(0).get(1).path("cumulative").asInt());
        ok(request(HttpMethod.POST,"/budget/versions/980001/restore",login(ADMIN),Map.of("remark","Synthetic restore")));
        assertEquals(20,trend(0).get(1).path("cumulative").asInt());
        assertEquals(3,jdbc.queryForObject("SELECT COUNT(*) FROM biz_budget_import_version",Integer.class));
        assertFalse(jdbc.queryForObject("SELECT snapshot_json FROM biz_budget_import_version ORDER BY version_no DESC LIMIT 1",String.class).contains("archivedIndicators"),"Raw versions must remain readable by the previous application");
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM biz_budget_sheet",Integer.class));
    }
}
