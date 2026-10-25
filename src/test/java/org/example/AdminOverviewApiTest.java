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
    @Autowired private org.example.service.AdminOverviewService overview;
    @Autowired private org.example.service.PermissionService overviewPermissions;
    @Autowired private org.example.service.AchievementService achievementLists;
    @Autowired private org.example.mapper.AchievementMapper achievementListMapper;
    @Autowired private org.example.mapper.PerformanceMapper performanceListMapper;
    @Autowired private org.example.service.WorkRecordScopeService overviewRecordScopes;
    @Autowired private org.springframework.transaction.PlatformTransactionManager transactionManager;

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
    private void achievement(long id,String date,Integer status,int deleted,long department,long owner) {
        jdbc.update("INSERT INTO biz_achievement(ach_id,ach_name,got_time,audit_status,category,dept_id,yi_deng_jiang,is_delete,level,department,create_by,comment,is_competition,create_time,update_time) VALUES(?,CONCAT('Synthetic ',?),?,?,1,?,3,?,'省级','Synthetic',?,'Fixture',1,NOW(),NOW())",
                id,id,date,status,department,deleted,owner);
    }
    @Test void achievementAggregateMatchesDetailProjectionAcrossDatesStatesAndAllAwardFields() throws Exception {
        jdbc.execute("ALTER TABLE biz_achievement MODIFY got_time datetime NULL");
        try{
            achievement(970001,"2026-01-01 00:00:00",30,0,920001,OWNER);
            achievement(970002,"2026-12-31 23:59:59",10,0,920001,OWNER);
            achievement(970003,"2025-12-31 23:59:59",-20,0,920001,OWNER);
            achievement(970004,"2027-01-01 00:00:00",99,0,920001,OWNER);
            achievement(970005,null,30,0,920001,OWNER);
            achievement(970006,"2026-06-01",30,1,920001,OWNER);
            achievement(970007,"2026-06-01",30,2,920001,OWNER);
            achievement(970008,"2026-06-01",-1,0,920001,OWNER);
            jdbc.update("UPDATE biz_achievement SET te_deng_jiang=2147483647,yi_deng_jiang=2147483647,er_deng_jiang=2,san_deng_jiang=3,jin_jiang=4,yin_jiang=5,tong_jiang=6,you_sheng_jiang=7,bud_deng_deng_ci=8 WHERE ach_id=970001");
            jdbc.update("UPDATE biz_achievement SET yi_deng_jiang=NULL WHERE ach_id=970002");
            Object target=org.springframework.test.util.AopTestUtils.getUltimateTargetObject(overview);String token=login(ADMIN);
            for(int pass=0;pass<2;pass++){
                for(int year:List.of(2025,2026,2027,2029)){
                    Object detail=org.springframework.test.util.ReflectionTestUtils.invokeMethod(target,"data","achievements",year,4,ADMIN);
                    var result=ok(request(HttpMethod.GET,"/admin/overview/summary?year="+year+"&month=4&module=achievements",token,null)).path("modules").path("achievements");
                    assertEquals("ok",result.path("status").asText());
                    for(String key:List.of("title","period","note","metrics","extra"))assertEquals(json.readTree(json.writeValueAsString(org.springframework.test.util.ReflectionTestUtils.getField(detail,key))),result.path(key),year+" "+key);
                }
                jdbc.update("UPDATE biz_achievement SET audit_status=30,yi_deng_jiang=-2 WHERE ach_id=970002");
                jdbc.update("UPDATE biz_achievement SET got_time='2026-06-01' WHERE ach_id=970005");
            }
            jdbc.update("DELETE FROM biz_achievement");
            var empty=ok(request(HttpMethod.GET,"/admin/overview/summary?year=2026&month=4&module=achievements",token,null));
            assertEquals(0,value(empty,"achievements","total").asLong());assertEquals(0,value(empty,"achievements","awards").asLong());
        }finally{jdbc.update("DELETE FROM biz_achievement WHERE got_time IS NULL");jdbc.execute("ALTER TABLE biz_achievement MODIFY got_time datetime NOT NULL");}
    }
    @Test void achievementBatchScopeMatchesIndividualChecksAndReflectsChangesWithOldToken() throws Exception {
        jdbc.execute("ALTER TABLE biz_achievement MODIFY got_time datetime NULL");
        try{
            achievement(970001,"2026-04-01",30,0,920001,OWNER);
            achievement(970002,"2026-04-01",-10,0,920002,VIEWER);
            achievement(970003,"2025-04-01",10,0,920002,OWNER);
            achievement(970004,null,30,0,920001,OWNER);
            achievement(970005,"2026-04-01",30,1,920001,VIEWER);
            var requested=List.of(970001L,970002L,970003L,970004L,970005L,999999L);
            List<Map<String,Object>> rules=List.of(Map.of("mode","ALL"),Map.of("mode","ALL","years",List.of(2026)),
                    Map.of("mode","OWN"),Map.of("mode","BUSINESS"),Map.of("mode","CUSTOM","departments",List.of(920001)),
                    Map.of("mode","CUSTOM","items",List.of(970002,970003)),Map.of("mode","CUSTOM","departments",List.of(920001),"items",List.of(970002)),Map.of("mode","LEADERSHIP"));
            String token=login(VIEWER);
            for(var rule:rules){
                jdbc.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json,version) VALUES(?,'CUSTOM',?,?,1) ON DUPLICATE KEY UPDATE permissions_json=VALUES(permissions_json),scopes_json=VALUES(scopes_json),version=version+1",
                        VIEWER,json.writeValueAsString(Map.of("overview.read",true,"achievements.read",true)),json.writeValueAsString(Map.of("achievements",rule)));
                for(int leadershipPass=0;leadershipPass<("LEADERSHIP".equals(rule.get("mode"))?3:1);leadershipPass++){
                    jdbc.update("DELETE FROM sys_leadership_department");jdbc.update("DELETE FROM sys_leadership_access");
                    if(leadershipPass==0){jdbc.update("INSERT INTO sys_leadership_access(user_id,scope,version) VALUES(?,'DEPARTMENTS',1)",VIEWER);jdbc.update("INSERT INTO sys_leadership_department(user_id,dept_id) VALUES(?,920001)",VIEWER);}
                    if(leadershipPass==1)jdbc.update("INSERT INTO sys_leadership_access(user_id,scope,version) VALUES(?,'ALL',1)",VIEWER);
                    Set<Long> expected=new HashSet<>();for(Long id:requested)if(overviewPermissions.canRead(VIEWER,"achievements",id,2026))expected.add(id);
                    assertEquals(expected,overviewPermissions.readableAchievementIds(VIEWER,requested),rule.toString());
                    var oldList=achievementListMapper.listAllAchievements().stream().filter(a->expected.contains(a.getAchId())).toList();
                    assertEquals(json.valueToTree(oldList),json.valueToTree(achievementLists.listAllAchievements(VIEWER)),
                            "Achievement list must keep order, rejection and archive fields: "+rule);
                    var details=analysis(token,"achievements.total","category");
                    Set<Long> actual=new HashSet<>();details.path("rows").forEach(row->actual.add(row.path("id").asLong()));
                    Set<Long> selected=new HashSet<>(expected);selected.retainAll(Set.of(970001L,970002L));assertEquals(selected,actual);
                    var result=summary(token,4);assertEquals(actual.size(),value(result,"achievements","total").asInt());
                    assertEquals(expected.contains(970004L)?1:0,result.path("modules").path("achievements").path("extra").path("missingDates").asInt());
                }
            }
            jdbc.update("UPDATE sys_user_permission SET permissions_json=? WHERE user_id=?",json.writeValueAsString(Map.of("overview.read",true,"achievements.read",false)),VIEWER);
            assertTrue(overviewPermissions.readableAchievementIds(VIEWER,requested).isEmpty());
            assertEquals("forbidden",summary(token,4).path("modules").path("achievements").path("status").asText());
            jdbc.update("UPDATE sys_user SET status='0' WHERE user_id=?",VIEWER);
            assertEquals(403,request(HttpMethod.GET,"/admin/overview/summary?year=2026&month=4",token,null).getStatusCode().value());
        }finally{jdbc.update("DELETE FROM biz_achievement WHERE got_time IS NULL");jdbc.execute("ALTER TABLE biz_achievement MODIFY got_time datetime NOT NULL");}
    }
    @Test void achievementSummaryAtCurrentAndFiveTimesSyntheticScale() throws Exception {
        Object target=org.springframework.test.util.AopTestUtils.getUltimateTargetObject(overview);String token=login(ADMIN);
        for(int size:List.of(120,600)){
            jdbc.update("DELETE FROM biz_achievement");
            List<Object[]> rows=new ArrayList<>();for(int i=0;i<size;i++)rows.add(new Object[]{9970000+i,i,i%3==0?30:i%3==1?10:-10});
            jdbc.batchUpdate("INSERT INTO biz_achievement(ach_id,ach_name,got_time,audit_status,category,dept_id,yi_deng_jiang,is_delete,level,department,create_by,comment,is_competition,create_time,update_time) VALUES(?,CONCAT('Bench ',?),'2026-04-01',?,1,920001,3,0,'省级','Synthetic',910001,'Fixture',1,NOW(),NOW())",rows);
            jdbc.update("INSERT INTO biz_achievement_submission(ach_id,flow_status,is_delete,submit_by,submit_time) SELECT ach_id,audit_status,0,910001,NOW() FROM biz_achievement");
            jdbc.update("INSERT INTO biz_achievement_audit_log(sub_id,operator_id,action_type,comment,pre_status,post_status,create_time) SELECT sub_id,110228,IF(flow_status<0,'reject','pass'),'Synthetic history',10,flow_status,NOW() FROM biz_achievement_submission");
            List<Long> full=new ArrayList<>(),aggregate=new ArrayList<>();
            try{
                for(int run=0;run<6;run++){
                    var req=new org.springframework.mock.web.MockHttpServletRequest("GET","/admin/overview/summary");req.addHeader("Authorization",token);
                    org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(req));
                    overviewPermissions.state(ADMIN);
                    long start=System.nanoTime();Object original=org.springframework.test.util.ReflectionTestUtils.invokeMethod(target,"data","achievements",2026,4,ADMIN);
                    long middle=System.nanoTime();Object optimized=org.springframework.test.util.ReflectionTestUtils.invokeMethod(target,"achievementSummary",2026);long end=System.nanoTime();
                    assertEquals(org.springframework.test.util.ReflectionTestUtils.getField(original,"metrics"),org.springframework.test.util.ReflectionTestUtils.getField(optimized,"metrics"));
                    if(run>0){full.add((middle-start)/1000);aggregate.add((end-middle)/1000);}
                }
            }finally{org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();}
            Collections.sort(full);Collections.sort(aggregate);
            System.out.println("OVERVIEW_ACHIEVEMENT_DATA_BENCH "+json.writeValueAsString(Map.of("syntheticAchievements",size,"full_projection_median_us",full.get(2),"aggregate_median_us",aggregate.get(2),"equalMetrics",true)));
        }
    }
    @Test void aggregatedTaskSummaryMatchesOriginalDetailProjection() throws Exception {
        task(900011,OWNER,2026,10,"3");task(900012,OWNER,2026,10,"1");
        task(900013,OWNER,2026,10,"2");task(900014,OWNER,2025,10,"1");task(900015,OWNER,2026,10,"3");
        jdbc.update("UPDATE biz_task SET progress=99,is_priority=2,is_difficult=1,is_delete=2 WHERE task_id=900011");
        jdbc.update("UPDATE biz_task SET progress=100 WHERE task_id=900012");
        jdbc.update("UPDATE biz_task SET progress=NULL WHERE task_id=900013");
        jdbc.update("UPDATE biz_task SET is_delete=1 WHERE task_id=900015");
        String token=login(ADMIN);
        Object serviceTarget=org.springframework.test.util.AopTestUtils.getUltimateTargetObject(overview);
        for(int pass=0;pass<2;pass++){
            for(int year:List.of(2025,2026,2029)){
                Object legacy=org.springframework.test.util.ReflectionTestUtils.invokeMethod(serviceTarget,"data","tasks",year,4,ADMIN);
                var result=ok(request(HttpMethod.GET,"/admin/overview/summary?year="+year+"&month=4&module=tasks",token,null)).path("modules").path("tasks");
                assertEquals("ok",result.path("status").asText());
                for(String key:List.of("title","period","note","metrics","extra")){
                    Object expected=org.springframework.test.util.ReflectionTestUtils.getField(legacy,key);
                    assertEquals(json.readTree(json.writeValueAsString(expected)),result.path(key),year+" "+key);
                }
            }
            jdbc.update("UPDATE biz_task SET progress=42,status='9' WHERE task_id=900013");
            jdbc.update("UPDATE biz_task SET status='3' WHERE task_id=900014");
        }
    }
    @Test void taskSummaryAggregationAtCurrentAndFiveTimesSyntheticScale() throws Exception {
        Object target=org.springframework.test.util.AopTestUtils.getUltimateTargetObject(overview);
        String token=login(ADMIN);
        jdbc.update("UPDATE biz_task SET progress=0 WHERE task_id=900010");
        for(int size:List.of(120,600)){
            jdbc.update("DELETE FROM biz_task WHERE task_id BETWEEN 9910000 AND 9910599");
            List<Object[]> rows=new ArrayList<>();
            for(int i=0;i<size;i++)rows.add(new Object[]{9910000+i,"BENCH"+i,"Synthetic "+i,i%4,i%101,i%2,i%3==0?1:0});
            jdbc.batchUpdate("INSERT INTO biz_task(task_id,task_code,task_name,status,progress,is_priority,is_difficult,project_id,parent_id,phase,level,dept_id,leader_id,auditor_id,principal_id,target_value,is_delete) VALUES(?,?,?,?,?,?,?,1,900001,2026,3,920001,910001,910001,910003,10,0)",rows);
            List<Long> legacyTimes=new ArrayList<>(),aggregateTimes=new ArrayList<>();
            try{
                for(int run=0;run<6;run++){
                    var request=new org.springframework.mock.web.MockHttpServletRequest("GET","/admin/overview/summary");
                    request.addHeader("Authorization",token);
                    org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(request));
                    overviewPermissions.state(ADMIN); // Common request authorization is outside this data-phase timing.
                    long start=System.nanoTime();
                    Object legacy=org.springframework.test.util.ReflectionTestUtils.invokeMethod(target,"data","tasks",2026,4,ADMIN);
                    long middle=System.nanoTime();
                    Object aggregate=org.springframework.test.util.ReflectionTestUtils.invokeMethod(target,"taskSummary",2026);
                    long end=System.nanoTime();
                    assertEquals(org.springframework.test.util.ReflectionTestUtils.getField(legacy,"metrics"),org.springframework.test.util.ReflectionTestUtils.getField(aggregate,"metrics"));
                    if(run>0){legacyTimes.add((middle-start)/1000);aggregateTimes.add((end-middle)/1000);}
                }
            }finally{org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();}
            Collections.sort(legacyTimes);Collections.sort(aggregateTimes);
            System.out.println("OVERVIEW_TASK_DATA_BENCH "+json.writeValueAsString(Map.of("syntheticTasks",size,"existingTasks",1,"legacy_median_us",legacyTimes.get(2),"aggregate_median_us",aggregateTimes.get(2),"equalMetrics",true)));
        }
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
    private void comparePerformanceSummaryWithProjection(String token,int year) throws Exception {
        Object target=org.springframework.test.util.AopTestUtils.getUltimateTargetObject(overview);
        Object detail=org.springframework.test.util.ReflectionTestUtils.invokeMethod(target,"data","performance",year,4,ADMIN);
        var result=ok(request(HttpMethod.GET,"/admin/overview/summary?year="+year+"&month=4&module=performance",token,null)).path("modules").path("performance");
        assertEquals("ok",result.path("status").asText());
        for(String key:List.of("title","period","note","metrics","extra"))assertEquals(json.readTree(json.writeValueAsString(org.springframework.test.util.ReflectionTestUtils.getField(detail,key))),result.path(key),year+" "+key);
    }
    @Test void performanceAggregationPreservesAnnualValuesAndAmbiguousConfigurationFallback() throws Exception {
        jdbc.update("UPDATE biz_performance SET ancestors='0,1,1.1'");
        String[] paths={null,"",",,", "0,1", "0,, 1, , 1.1,", "0,1,1.1,", " 0 , 1 , 1.1 ","0,1,2,3","1 2,3,4","0,,1,1.2"};
        for(int i=0;i<paths.length;i++)jdbc.update("UPDATE biz_performance SET ancestors=?,current_value=999,target_value=1 WHERE perf_id=?",paths[i],960001+i);
        jdbc.update("INSERT INTO biz_performance_year(perf_id,year,target_value,actual_value,is_delete) VALUES(960005,2026,10,10,0),(960006,2026,100,25,0),(960007,2026,0,200,0),(960008,2026,10,NULL,0),(960009,2026,NULL,10,0),(960010,2026,-1,10,0)");
        jdbc.update("INSERT INTO biz_performance_year(perf_id,year,target_value,actual_value,is_delete) VALUES(960005,2025,10,0,0),(960006,2025,10,20,1),(960007,2025,10,20,2)");
        jdbc.update("UPDATE biz_performance SET perf_code='1.2.X' WHERE perf_id=960005");
        jdbc.update("UPDATE biz_performance SET perf_code=' 1.1.1' WHERE perf_id=960006");
        jdbc.update("UPDATE biz_performance SET perf_code='2.1.1' WHERE perf_id=960007");
        jdbc.update("UPDATE biz_performance SET is_delete=2 WHERE perf_id=960008");
        jdbc.update("UPDATE biz_performance SET is_delete=1 WHERE perf_id=960009");
        String token=login(ADMIN);
        for(int year:List.of(2025,2026,2029))comparePerformanceSummaryWithProjection(token,year);
        // Do not choose MAX(year_id) or merge columns when old data contains multiple active annual rows.
        jdbc.update("INSERT INTO biz_performance_year(perf_id,year,target_value,actual_value,is_delete) VALUES(960005,2026,10,0,0)");
        comparePerformanceSummaryWithProjection(token,2026);
        jdbc.update("UPDATE biz_performance_year SET is_delete=1 WHERE perf_id=960005 AND actual_value=0 AND year=2026");
        for(String path:List.of("0,\t,1,1.1","0,\u2003,1,1.1","0,\u00a0,1","目录,分类,指标","0,1,1.1\n")){
            jdbc.update("UPDATE biz_performance SET ancestors=? WHERE perf_id=960005",path);
            comparePerformanceSummaryWithProjection(token,2026);
        }
        jdbc.update("UPDATE biz_performance SET is_delete=1");comparePerformanceSummaryWithProjection(token,2026);
    }
    @Test void batchedPerformanceScopeMatchesIndividualChecksAndCurrentDuties() throws Exception {
        jdbc.update("INSERT INTO sys_dept(dept_id,dept_name,leader_id,is_delete) VALUES(920002,'Other synthetic',?,0)",OWNER);
        jdbc.update("UPDATE biz_performance SET ancestors='0,1,1.1',leader_id=?,auditor_id=?,principal_id=?",OWNER,OWNER,OWNER);
        jdbc.update("UPDATE biz_performance SET dept_id=920002 WHERE perf_id IN(960002,960005)");
        jdbc.update("UPDATE biz_performance SET leader_id=? WHERE perf_id=960002",VIEWER);
        jdbc.update("UPDATE biz_performance SET auditor_id=? WHERE perf_id=960003",VIEWER);
        jdbc.update("UPDATE biz_performance SET principal_id=? WHERE perf_id=960004",VIEWER);
        jdbc.update("UPDATE biz_performance SET is_delete=1 WHERE perf_id=960009");
        jdbc.update("UPDATE biz_performance SET is_delete=2 WHERE perf_id=960010");
        List<Long> ids=new ArrayList<>();for(long id=960001;id<=960010;id++)ids.add(id);ids.add(999999L);
        List<Map<String,Object>> rules=List.of(Map.of("mode","ALL"),Map.of("mode","ALL","years",List.of(2026)),Map.of("mode","OWN"),Map.of("mode","BUSINESS"),Map.of("mode","LEADERSHIP"),
                Map.of("mode","CUSTOM","departments",List.of(920001)),Map.of("mode","CUSTOM","items",List.of(960002,960005)),Map.of("mode","CUSTOM","departments",List.of(920001),"items",List.of(960005),"years",List.of(2025)));
        String token=login(VIEWER);
        for(var rule:rules){
            jdbc.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json,version) VALUES(?,'CUSTOM',?,?,1) ON DUPLICATE KEY UPDATE permissions_json=VALUES(permissions_json),scopes_json=VALUES(scopes_json),version=version+1",
                    VIEWER,json.writeValueAsString(Map.of("overview.read",true,"performance.read",true)),json.writeValueAsString(Map.of("performance",rule)));
            for(int pass=0;pass<3;pass++){
                jdbc.update("DELETE FROM sys_leadership_department");jdbc.update("DELETE FROM sys_leadership_access");
                jdbc.update("UPDATE sys_dept SET leader_id=? WHERE dept_id=920001",pass==0?VIEWER:OWNER);
                if(pass==0){jdbc.update("INSERT INTO sys_leadership_access(user_id,scope,version) VALUES(?,'DEPARTMENTS',1)",VIEWER);jdbc.update("INSERT INTO sys_leadership_department(user_id,dept_id) VALUES(?,920001)",VIEWER);}
                if(pass==1)jdbc.update("INSERT INTO sys_leadership_access(user_id,scope,version) VALUES(?,'ALL',1)",VIEWER);
                for(int year:List.of(2025,2026)){
                    Set<Long> expected=new HashSet<>();for(Long id:ids)if(overviewPermissions.canRead(VIEWER,"performance",id,year))expected.add(id);
                    assertEquals(expected,overviewPermissions.readablePerformanceIds(VIEWER,ids,year),rule+" year="+year);
                    var original=performanceListMapper.getAllPerformance().stream().filter(p->expected.contains(p.getPerfId())).toList();
                    assertEquals(json.valueToTree(original),json.valueToTree(overviewPermissions.visiblePerformances(VIEWER,year)),
                            "Leaf-only fixture must keep original order and values: "+rule+" year="+year);
                }
                var detail=analysis(token,"performance.total","department");Set<Long> actual=new HashSet<>();detail.path("rows").forEach(row->actual.add(row.path("id").asLong()));
                assertEquals(overviewPermissions.readablePerformanceIds(VIEWER,ids,2026),actual);
                assertEquals(actual.size(),value(summary(token,4),"performance","total").asInt());
                if(!Set.of("BUSINESS","LEADERSHIP").contains(rule.get("mode")))break;
            }
        }
        jdbc.update("UPDATE sys_user_permission SET permissions_json=? WHERE user_id=?",json.writeValueAsString(Map.of("overview.read",true,"performance.read",false)),VIEWER);
        assertTrue(overviewPermissions.readablePerformanceIds(VIEWER,ids,2026).isEmpty());
        assertEquals("forbidden",summary(token,4).path("modules").path("performance").path("status").asText());
        jdbc.update("UPDATE sys_user SET status='0' WHERE user_id=?",VIEWER);
        assertEquals(403,request(HttpMethod.GET,"/admin/overview/summary?year=2026&month=4",token,null).getStatusCode().value());
    }
    @Test void performanceSummaryAtCurrentAndFiveTimesSyntheticScale() throws Exception {
        Object target=org.springframework.test.util.AopTestUtils.getUltimateTargetObject(overview);String token=login(ADMIN);
        jdbc.update("UPDATE biz_performance SET ancestors='0,1,1.1'");
        for(int size:List.of(120,600)){
            jdbc.update("DELETE FROM biz_performance_year WHERE perf_id BETWEEN 9960000 AND 9960599");
            jdbc.update("DELETE FROM biz_performance WHERE perf_id BETWEEN 9960000 AND 9960599");
            List<Object[]> rows=new ArrayList<>();for(int i=0;i<size;i++)rows.add(new Object[]{9960000+i,(i%2==0?"1.1.":"2.1.")+i,i});
            jdbc.batchUpdate("INSERT INTO biz_performance(perf_id,perf_code,perf_name,project_id,parent_id,ancestors,dept_id,leader_id,auditor_id,principal_id,target_value,current_value,data_type,is_delete) VALUES(?,?,CONCAT('Bench ',?),1,0,'0,1,1.1',920001,910001,910005,910003,999,999,'1',0)",rows);
            jdbc.update("INSERT INTO biz_performance_year(perf_id,year,target_value,actual_value,is_delete) SELECT perf_id,2026,100,MOD(perf_id,125),0 FROM biz_performance WHERE perf_id BETWEEN 9960000 AND 9960599");
            List<Long> full=new ArrayList<>(),aggregate=new ArrayList<>();
            try{
                for(int run=0;run<6;run++){
                    var req=new org.springframework.mock.web.MockHttpServletRequest("GET","/admin/overview/summary");req.addHeader("Authorization",token);
                    org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(req));
                    overviewPermissions.state(ADMIN);
                    long start=System.nanoTime();Object original=org.springframework.test.util.ReflectionTestUtils.invokeMethod(target,"data","performance",2026,4,ADMIN);
                    long middle=System.nanoTime();Object optimized=org.springframework.test.util.ReflectionTestUtils.invokeMethod(target,"performanceSummary",2026,ADMIN);long end=System.nanoTime();
                    assertEquals(org.springframework.test.util.ReflectionTestUtils.getField(original,"metrics"),org.springframework.test.util.ReflectionTestUtils.getField(optimized,"metrics"));
                    if(run>0){full.add((middle-start)/1000);aggregate.add((end-middle)/1000);}
                }
            }finally{org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();}
            Collections.sort(full);Collections.sort(aggregate);
            System.out.println("OVERVIEW_PERFORMANCE_DATA_BENCH "+json.writeValueAsString(Map.of("syntheticPerformance",size,"existingPerformance",10,"full_projection_median_us",full.get(2),"aggregate_median_us",aggregate.get(2),"equalMetrics",true)));
        }
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
    @Test void recordBatchesAndOverviewMatchOriginalScopeChecksAcrossYearsAndDrafts() throws Exception {
        jdbc.update("INSERT INTO sys_dept(dept_id,dept_name,is_delete) VALUES(920002,'Other synthetic',0)");jdbc.update("UPDATE sys_user SET dept_id=920002 WHERE user_id=?",OWNER);
        jdbc.update("INSERT INTO biz_work_record(record_id,owner_id,owner_name,record_year,record_month,status,delete_marker,create_time,update_time) VALUES(993001,910001,'One',2026,4,1,0,NOW(),NOW()),(993002,910002,'Two',2026,4,1,0,NOW(),NOW()),(993003,999999,'Former',2026,4,1,0,NOW(),NOW()),(993004,910005,'Old year',2025,4,1,0,NOW(),NOW()),(993005,910002,'SECRET-DRAFT',2026,5,0,0,NOW(),NOW()),(993006,910001,'Deleted',2026,5,1,993006,NOW(),NOW()),(993007,910005,'Own draft',2026,4,0,0,NOW(),NOW())");
        var ids=List.of(993001L,993002L,993003L,993004L,993005L,993006L,993007L,999999L);
        var rules=List.of(Map.<String,Object>of("mode","ALL"),Map.<String,Object>of("mode","ALL","years",List.of(2025)),Map.<String,Object>of("mode","OWN"),Map.<String,Object>of("mode","BUSINESS"),Map.<String,Object>of("mode","CUSTOM","departments",List.of(920001)),Map.<String,Object>of("mode","CUSTOM","items",List.of(993001,993004)),Map.<String,Object>of("mode","LEADERSHIP"));
        String token=login(VIEWER);
        for(var rule:rules){
            jdbc.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json,version) VALUES(?,'CUSTOM',?,?,1) ON DUPLICATE KEY UPDATE permissions_json=VALUES(permissions_json),scopes_json=VALUES(scopes_json),version=version+1",VIEWER,json.writeValueAsString(Map.of("overview.read",true,"records.read",true)),json.writeValueAsString(Map.of("records",rule)));
            for(int pass=0;pass<("LEADERSHIP".equals(rule.get("mode"))?3:1);pass++){
                jdbc.update("DELETE FROM sys_leadership_department");jdbc.update("DELETE FROM sys_leadership_access");
                if(pass==0){jdbc.update("INSERT INTO sys_leadership_access(user_id,scope,version) VALUES(?,'DEPARTMENTS',1)",VIEWER);jdbc.update("INSERT INTO sys_leadership_department(user_id,dept_id) VALUES(?,920001)",VIEWER);}
                if(pass==1)jdbc.update("INSERT INTO sys_leadership_access(user_id,scope,version) VALUES(?,'ALL',1)",VIEWER);
                var request=new org.springframework.mock.web.MockHttpServletRequest("GET","/admin/overview/summary");request.addHeader("Authorization",token);
                org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(request));
                try{
                    Set<Long> expected=new HashSet<>();for(Long id:ids)if(overviewPermissions.canRead(VIEWER,"records",id,2026))expected.add(id);
                    assertEquals(expected,overviewPermissions.readableRecordIds(VIEWER,ids,2026),rule.toString());
                    var candidates=overviewRecordScopes.eligibleAuthors(2026);Set<Long> owners=new HashSet<>();
                    for(var u:candidates)if(overviewPermissions.canRecordOwner(VIEWER,u.getUserId(),u.getDeptId(),2026))owners.add(u.getUserId());
                    assertEquals(owners,overviewPermissions.readableRecordOwners(VIEWER,candidates,2026));
                    var all=jdbc.queryForList("SELECT r.record_id id,r.owner_id,r.status,r.record_year,u.dept_id FROM biz_work_record r LEFT JOIN sys_user u ON u.user_id=r.owner_id WHERE r.delete_marker=0");
                    List<Long> original=new ArrayList<>();for(var row:all){long id=((Number)row.get("id")).longValue();if(overviewPermissions.canRead(VIEWER,"records",id,null))original.add(id);}
                    assertEquals(original,overviewPermissions.recordIds(VIEWER));
                    long done=owners.stream().filter(owner->expected.stream().anyMatch(id->id==993001&&owner==OWNER||id==993002&&owner==OTHER||id==993003&&owner==999999)).count();
                    long historical=0;for(long[] pair:new long[][]{{993001,OWNER},{993002,OTHER},{993003,999999}})if(expected.contains(pair[0])&&!owners.contains(pair[1]))historical++;
                    var result=summary(token,4);assertEquals(owners.size(),value(result,"records","expected").asLong());assertEquals(done,value(result,"records","submitted").asLong());assertEquals(owners.size()-done,value(result,"records","missing").asLong());assertEquals(historical,value(result,"records","historical").asLong());
                    var details=analysis(token,"records.submitted","department");assertEquals(done,details.path("total").asLong());assertFalse(details.toString().contains("SECRET-DRAFT"));
                }finally{org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();}
            }
        }
        jdbc.update("UPDATE sys_user_permission SET permissions_json=? WHERE user_id=?",json.writeValueAsString(Map.of("overview.read",true,"records.read",false)),VIEWER);
        assertEquals("forbidden",summary(token,4).path("modules").path("records").path("status").asText());assertTrue(overviewPermissions.recordIds(VIEWER).isEmpty());
        jdbc.update("UPDATE sys_user SET status='0' WHERE user_id=?",VIEWER);assertEquals(403,request(HttpMethod.GET,"/admin/overview/summary?year=2026&month=4",token,null).getStatusCode().value());
    }
    @Test void coreScopeBatchPreservesCutoffSnapshotAndMappingWarnings() throws Exception {
        coreResult(2,"1.1.5",5);coreResult(4,"1.1.5",8);coreResult(5,"1.1.5",999);coreResult(3,"obsolete",888);
        jdbc.update("UPDATE biz_core_report SET indicator_name='Frozen snapshot',target_value=7,comment=REPEAT('X',500) WHERE record_month=4");
        jdbc.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json,version) VALUES(?,'CUSTOM',?,?,1)",VIEWER,json.writeValueAsString(Map.of("overview.read",true,"core.read",true)),json.writeValueAsString(Map.of("core",Map.of("mode","CUSTOM","items",List.of(1),"years",List.of(2026)))));
        String token=login(VIEWER);var ids=new ArrayList<Long>();for(long id=1;id<=11;id++)ids.add(id);
        for(int year:List.of(2025,2026)){
            Set<Long> expected=new HashSet<>();for(Long id:ids)if(overviewPermissions.canRead(VIEWER,"core",id,year))expected.add(id);
            assertEquals(expected,overviewPermissions.readableCoreIds(VIEWER,ids,year));
        }
        var detail=analysis(token,"core.total","indicator");assertEquals(1,detail.path("total").asInt());var row=detail.path("rows").get(0);
        assertEquals("Frozen snapshot",row.path("name").asText());assertEquals(8,row.path("value").asInt());assertEquals(7,row.path("target").asInt());assertEquals(4,row.path("month").asInt());assertFalse(detail.toString().contains("XXXXX"));
        assertEquals(1,value(summary(token,4),"core","attained").asInt());
        jdbc.update("INSERT INTO biz_performance(perf_id,project_id,parent_id,perf_code,perf_name,dept_id,leader_id,auditor_id,principal_id,target_value,current_value,data_type,is_delete) VALUES(999001,1,0,'1.1.5','Duplicate',920001,910001,910005,910003,1,0,'1',0)");
        assertEquals(1,value(summary(token,4),"core","invalid").asInt());assertEquals(0,value(summary(token,4),"core","archived").asInt());
        jdbc.update("UPDATE sys_user_permission SET scopes_json=? WHERE user_id=?",json.writeValueAsString(Map.of("core",Map.of("mode","CUSTOM","items",List.of(2),"years",List.of(2026)))),VIEWER);
        detail=analysis(token,"core.total","indicator");assertEquals(2,detail.path("rows").get(0).path("id").asInt());assertTrue(detail.path("rows").get(0).path("value").isNull());
    }
    @Test void deniedBudgetYearMarksAccessFailureWithoutChangingLegacyErrorShape() throws Exception {
        jdbc.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json,version) VALUES(?,'CUSTOM',?,?,1)",VIEWER,json.writeValueAsString(Map.of("overview.read",true,"budget.read",true)),json.writeValueAsString(Map.of("budget",Map.of("mode","ALL","years",List.of(2025)))));
        var section=summary(login(VIEWER),4).path("modules").path("budget");
        assertEquals("error",section.path("status").asText());assertTrue(section.path("accessDenied").asBoolean());assertFalse(section.has("metrics"));
    }
    @Test void recordScopeReadsAtCurrentAndFiveTimesSyntheticScale() throws Exception {
        jdbc.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json,version) VALUES(?,'CUSTOM',?,?,1)",VIEWER,json.writeValueAsString(Map.of("overview.read",true,"records.read",true)),json.writeValueAsString(Map.of("records",Map.of("mode","ALL"))));
        String token=login(VIEWER);var read=new org.springframework.transaction.support.TransactionTemplate(transactionManager);read.setReadOnly(true);read.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_REPEATABLE_READ);
        for(int size:List.of(120,600)){
            jdbc.update("DELETE FROM biz_work_record");List<Object[]> rows=new ArrayList<>();List<Long> ids=new ArrayList<>();
            for(int i=0;i<size;i++){long id=9940000+i;ids.add(id);rows.add(new Object[]{id,id});}
            jdbc.batchUpdate("INSERT INTO biz_work_record(record_id,owner_id,owner_name,record_year,record_month,status,create_time,update_time) VALUES(?,?,'Synthetic',2026,4,1,NOW(),NOW())",rows);
            List<Long> oldTimes=new ArrayList<>(),newTimes=new ArrayList<>();
            try{
                for(int run=0;run<6;run++){
                    var req=new org.springframework.mock.web.MockHttpServletRequest("GET","/admin/overview/summary");req.addHeader("Authorization",token);org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(req));overviewPermissions.state(VIEWER);
                    long start=System.nanoTime();Set<Long> old=read.execute(status->{Set<Long> result=new HashSet<>();for(Long id:ids)if(overviewPermissions.canRead(VIEWER,"records",id,2026))result.add(id);return result;});
                    long middle=System.nanoTime();var batch=read.execute(status->overviewPermissions.readableRecordIds(VIEWER,ids,2026));long end=System.nanoTime();assertEquals(old,batch);
                    if(run>0){oldTimes.add((middle-start)/1000);newTimes.add((end-middle)/1000);}
                }
            }finally{org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();}
            Collections.sort(oldTimes);Collections.sort(newTimes);
            System.out.println("RECORD_SCOPE_READ_BENCH "+json.writeValueAsString(Map.of("records",size,"legacy_median_us",oldTimes.get(2),"batch_median_us",newTimes.get(2),"equalIds",true)));
        }
    }
    @Test void achievementListBatchReadBenchmarkPreservesCompleteOrderedPayload() throws Exception {
        jdbc.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json,version) VALUES(?,'CUSTOM',?,?,1)",
                VIEWER,json.writeValueAsString(Map.of("achievements.read",true)),json.writeValueAsString(Map.of("achievements",Map.of("mode","LEADERSHIP"))));
        jdbc.update("INSERT INTO sys_leadership_access(user_id,scope,version) VALUES(?,'ALL',1)",VIEWER);
        String token=login(VIEWER);var read=new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        read.setReadOnly(true);read.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_REPEATABLE_READ);
        for(int size:List.of(120,600)) {
            jdbc.update("DELETE FROM biz_achievement");List<Object[]> rows=new ArrayList<>();
            for(int i=0;i<size;i++)rows.add(new Object[]{9970000+i,OWNER});
            jdbc.batchUpdate("INSERT INTO biz_achievement(ach_id,category,level,ach_name,department,got_time,comment,dept_id,create_by,is_delete) VALUES(?,1,'省级','Synthetic achievement','Synthetic','2026-04-01','',920001,?,0)",rows);
            List<Long> oldTimes=new ArrayList<>(),newTimes=new ArrayList<>();
            try {
                for(int run=0;run<6;run++) {
                    var request=new org.springframework.mock.web.MockHttpServletRequest("GET","/achievement/");request.addHeader("Authorization",token);
                    org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(request));
                    overviewPermissions.state(VIEWER);
                    long[] times=read.execute(status->{
                        long start=System.nanoTime();
                        var original=achievementListMapper.listAllAchievements().stream().filter(a->overviewPermissions.canRead(VIEWER,"achievements",a.getAchId(),null)).toList();
                        long middle=System.nanoTime();var optimized=achievementLists.listAllAchievements(VIEWER);long end=System.nanoTime();
                        assertEquals(json.valueToTree(original),json.valueToTree(optimized));return new long[]{(middle-start)/1000,(end-middle)/1000};
                    });
                    if(run>0){oldTimes.add(times[0]);newTimes.add(times[1]);}
                }
            } finally { org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes(); }
            Collections.sort(oldTimes);Collections.sort(newTimes);
            System.out.println("ACHIEVEMENT_LIST_SCOPE_BENCH "+json.writeValueAsString(Map.of("records",size,"legacy_median_us",oldTimes.get(2),"batch_median_us",newTimes.get(2),"equalOrderedPayload",true)));
        }
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
