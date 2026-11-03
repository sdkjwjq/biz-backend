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
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.sql.Statement;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Real HTTP checks for the administrator count/list mismatch; synthetic isolated data only. */
@EnabledIfEnvironmentVariable(named="SHUANGGAO_REVIEW_TEST",matches="true")
@Import(ReviewBatchRegressionApiTest.NoAutomaticSchedules.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties={"logging.level.root=WARN","logging.file.name=target/audit-scope-tests.log"})
class AuditScopeConsistencyApiTest {
    @DynamicPropertySource static void database(DynamicPropertyRegistry r){
        String url=System.getenv("SHUANGGAO_TEST_JDBC_URL");
        if(url==null||!url.matches("jdbc:mysql://127\\.0\\.0\\.1:3306/biz_review_test_[0-9a-f]{32}\\?.*"))throw new IllegalStateException("Isolated schema required");
        r.add("spring.datasource.url",()->url);r.add("spring.datasource.username",()->System.getenv("SHUANGGAO_TEST_DB_USER"));r.add("spring.datasource.password",()->System.getenv("SHUANGGAO_TEST_DB_PASSWORD"));
    }
    @LocalServerPort int port;
    @Autowired TestRestTemplate http;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    String admin;

    @BeforeEach void seed() throws Exception {
        jdbc.execute((ConnectionCallback<Void>) c->{assertTrue(c.getCatalog().matches("biz_review_test_[0-9a-f]{32}"));
            try(Statement s=c.createStatement()){
                var tables=new ArrayList<String>();try(var rows=s.executeQuery("SHOW FULL TABLES WHERE Table_type='BASE TABLE'")){while(rows.next())tables.add(rows.getString(1));}
                s.execute("SET FOREIGN_KEY_CHECKS=0");try{for(String table:tables){assertTrue(table.matches("[A-Za-z0-9_]+"));s.executeUpdate("DELETE FROM `"+table+"`");}}finally{s.execute("SET FOREIGN_KEY_CHECKS=1");}
            }return null;});
        new ResourceDatabasePopulator(new FileSystemResource("scripts/ui-audit/fixture.sql"),
                new FileSystemResource("data/migrations/2026-10-12-core-indicators.sql"),
                new FileSystemResource("scripts/ui-audit/fixture-core.sql")).execute(Objects.requireNonNull(jdbc.getDataSource()));
        jdbc.update("UPDATE sys_user SET force_password_change=0");
        jdbc.update("UPDATE biz_level4_task SET target_value=10,current_value=0,progress=0,data_type='1'");
        jdbc.update("DELETE FROM biz_core_report"); // Remove deliberately invalid historical fixture reports only.
        jdbc.update("INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by) VALUES(940001,'audit.pdf','audit.pdf','/uploads/audit.pdf','pdf',910001)");
        for(int i=0;i<31;i++){
            long task=930002+i;
            if(i>0)jdbc.update("INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete) VALUES(?,1,930001,2026,?, ?,3,910001,910003,910002,920001,'1',10,0,0,'2',0)",task,"1.1."+(i+1),"Audit task "+(i+1));
            jdbc.update("INSERT INTO biz_material_submission(task_id,file_id,reported_value,data_type,submit_by,submit_dept_id,manage_dept_id,file_suffix,submit_time,flow_status,current_handler_id,is_delete) VALUES(?,940001,3,'1',910001,920001,920001,'pdf',NOW(),10,910003,0)",task);
        }
        admin=login(110228);
        ok(HttpMethod.POST,"/achievement/add",admin,Map.of("category",1,"level","省级","achName","Audit scope achievement","gotTime",1767225600000L,"department","Audit organization","comment","Scope fixture","isCompetition",0));
        String owner=login(910001);
        JsonNode draft=ok(HttpMethod.POST,"/core-indicators/1/draft",owner,Map.of("year",2026,"month",9));
        draft=ok(HttpMethod.PUT,"/core-indicators/reports/"+draft.path("reportId").asLong(),owner,Map.of("version",draft.path("version").asLong(),"actualValue",3,"previousYearValue",0,"comment","Audit scope"));
        ok(HttpMethod.POST,"/core-indicators/reports/"+draft.path("reportId").asLong()+"/submit",owner,Map.of("version",draft.path("version").asLong()));
    }
    ResponseEntity<String> request(HttpMethod method,String path,String token,Object input){
        HttpHeaders headers=new HttpHeaders();headers.setContentType(MediaType.APPLICATION_JSON);if(token!=null)headers.set("Authorization",token);
        return http.exchange("http://127.0.0.1:"+port+"/api"+path,method,new HttpEntity<>(input,headers),String.class);
    }
    JsonNode ok(HttpMethod method,String path,String token,Object input) throws Exception {
        var r=request(method,path,token,input);assertEquals(HttpStatus.OK,r.getStatusCode(),r.getBody());
        JsonNode body;try{body=json.readTree(r.getBody());}catch(Exception text){body=json.getNodeFactory().textNode(r.getBody());}
        assertFalse(body.has("code")&&body.path("code").asInt()>=400,body.toString());return body;
    }
    String login(long id) throws Exception {return ok(HttpMethod.POST,"/system/login",null,Map.of("user_id",id,"password","review-fixture-password")).path("token").asText();}
    JsonNode page(String token,String extra) throws Exception {return ok(HttpMethod.GET,"/list-filters/audits?tab=todo&responseMode=page&includeFields=false&pageSize=10"+extra,token,null);}
    JsonNode allPage(String token,String extra) throws Exception {return ok(HttpMethod.GET,"/list-filters/audits?tab=all&responseMode=page&includeFields=false&pageSize=10"+extra,token,null);}

    @Test void administratorAllPendingIsSeparateFromPersonalBadgeAcrossPages() throws Exception {
        var mine=page(admin,"");assertEquals(1,mine.path("total").asInt());assertEquals(1,mine.path("summary").path("todoCount").asInt());
        assertTrue(mine.path("items").get(0).path("assignedToMe").asBoolean());
        var first=allPage(admin,"");assertEquals(34,first.path("total").asInt());assertEquals(1,first.path("summary").path("todoCount").asInt());
        var ids=new HashSet<String>();var types=new HashSet<String>();
        for(int n=1;n<=4;n++)for(var row:allPage(admin,"&page="+n).path("items")){assertTrue(ids.add(row.path("id").asText()));types.add(row.path("type").asText());}
        assertEquals(34,ids.size());assertEquals(Set.of("task","performance","achievement","core"),types);
        var filtered=allPage(admin,"&f.type=performance");assertEquals(1,filtered.path("total").asInt());assertEquals(1,filtered.path("summary").path("todoCount").asInt());
        assertEquals(0,allPage(admin,"&f.keyword=not-found-in-fixtures").path("total").asInt());assertEquals(34,allPage(admin,"").path("total").asInt());
        var options=ok(HttpMethod.GET,"/list-filters/audits/filter-options?tab=all&field=type",admin,null);assertEquals(4,options.path("total").asInt());
        assertFalse(ok(HttpMethod.GET,"/list-filters/audits/locate?taskId=930002",admin,null).path("isViewMode").asBoolean());
        long tasks=ok(HttpMethod.GET,"/biz/audit/todo?countOnly=true",admin,null).asLong();
        assertEquals(0,tasks);
    }

    @Test void ordinaryAccountsCannotInheritAdministratorScopeOrSpoofIt() throws Exception {
        String outsider=login(910004),auditor=login(910003);
        assertEquals(0,page(outsider,"").path("total").asInt());assertEquals(32,page(auditor,"").path("total").asInt());
        assertEquals(0,page(outsider,"").path("summary").path("todoCount").asInt());
        for(String path:List.of("/list-filters/audits?tab=all&responseMode=page","/list-filters/audits/filter-options?tab=all&definitions=true","/data-exports/audits?tab=all"))assertEquals(HttpStatus.FORBIDDEN,request(HttpMethod.GET,path,auditor,null).getStatusCode());
        assertTrue(Set.of(HttpStatus.FORBIDDEN,HttpStatus.NOT_FOUND).contains(request(HttpMethod.GET,"/list-filters/audits/locate?taskId=930003",outsider,null).getStatusCode()));
        HttpHeaders spoof=new HttpHeaders();spoof.set("Authorization",outsider);spoof.set("X-Admin-Test","1");
        assertEquals(HttpStatus.FORBIDDEN,http.exchange("http://127.0.0.1:"+port+"/api/list-filters/audits?responseMode=page",HttpMethod.GET,new HttpEntity<>(spoof),String.class).getStatusCode());
        jdbc.update("UPDATE sys_user SET role='1' WHERE user_id=110228");
        assertEquals(HttpStatus.FORBIDDEN,request(HttpMethod.GET,"/list-filters/audits?tab=all&responseMode=page",admin,null).getStatusCode());
        for(var row:page(admin,"").path("items"))assertFalse(Set.of("task","performance").contains(row.path("type").asText()));
        jdbc.update("UPDATE sys_user SET status='0' WHERE user_id=110228");
        assertEquals(HttpStatus.FORBIDDEN,request(HttpMethod.GET,"/list-filters/audits?responseMode=page",admin,null).getStatusCode());
    }

    @Test void administratorCanProcessVisibleRowAndRefreshWithoutChangingOtherTodos() throws Exception {
        long sub=jdbc.queryForObject("SELECT sub_id FROM biz_material_submission WHERE task_id=930002",Long.class);
        long logs=jdbc.queryForObject("SELECT COUNT(*) FROM biz_audit_log",Long.class);
        ok(HttpMethod.POST,"/biz/audit",admin,Map.of("sub_id",sub,"is_pass",true,"title","Admin scope verification"));
        assertEquals(20,jdbc.queryForObject("SELECT flow_status FROM biz_material_submission WHERE sub_id=?",Integer.class,sub));
        assertEquals(34,allPage(admin,"").path("total").asInt());
        assertEquals(1,page(admin,"").path("total").asInt());
        assertEquals(31,page(login(910003),"").path("total").asInt()); // 30 other tasks plus the performance submission.
        assertEquals(logs+1,jdbc.queryForObject("SELECT COUNT(*) FROM biz_audit_log",Long.class));
        assertEquals(110228L,jdbc.queryForObject("SELECT operator_id FROM biz_audit_log WHERE sub_id=? ORDER BY log_id DESC LIMIT 1",Long.class,sub));
    }

    @Test void personalListAndSidebarCountsUseActualHandlerForEveryBusinessKind() throws Exception {
        jdbc.update("UPDATE biz_material_submission SET current_handler_id=110228,flow_status=30 WHERE task_id=930002");
        jdbc.update("UPDATE biz_performance_submission SET current_handler_id=110228,flow_status=20 WHERE sub_id=971001");
        jdbc.update("UPDATE biz_core_report SET current_handler_id=110228,flow_status=20 WHERE flow_status=10");
        var mine=page(admin,"");assertEquals(4,mine.path("total").asInt());assertEquals(4,mine.path("summary").path("todoCount").asInt());
        var kinds=new HashSet<String>();for(var row:mine.path("items")){assertEquals(110228,row.path("currentHandlerId").asLong());assertTrue(row.path("assignedToMe").asBoolean());kinds.add(row.path("type").asText());}
        assertEquals(Set.of("task","performance","achievement","core"),kinds);
        long sum=0;for(String prefix:List.of("/biz","/performance","/achievement","/core-indicators"))sum+=ok(HttpMethod.GET,prefix+"/audit/todo?countOnly=true",admin,null).asLong();
        assertEquals(4,sum);assertEquals(34,allPage(admin,"").path("total").asInt());assertEquals(4,allPage(admin,"").path("summary").path("todoCount").asInt());
        assertEquals(0,page(admin,"&f.type=task&f.keyword=no-such-task").path("total").asInt());
        assertEquals(4,page(admin,"&f.type=task&f.keyword=no-such-task").path("summary").path("todoCount").asInt());
    }

    @Test void allPendingReadsNeverGenerateMessagesAndStaleForwardedTodosDoNotAlertAdmin() throws Exception {
        long core=jdbc.queryForObject("SELECT report_id FROM biz_core_report WHERE flow_status=10",Long.class);
        // A legacy/broadcast copy must not count as a real personal approval notification.
        jdbc.update("INSERT INTO sys_notice(from_user_id,to_user_id,type,trigger_event,title,content,source_type,source_id,is_read,is_delete,create_time) VALUES(910001,110228,'1','任务审核','任务审核','obsolete broadcast','0',930003,'0',0,NOW()),(910001,110228,'1','CORE_TODO','核心指标待归口审核','obsolete broadcast','4',?,'0',0,NOW()),(910001,110228,'1','绩效审核','绩效待审核','obsolete broadcast','2',971001,'0',0,NOW())",core);
        long notices=jdbc.queryForObject("SELECT COUNT(*) FROM sys_notice",Long.class);
        long unread=ok(HttpMethod.GET,"/system/notice?countOnly=true",admin,null).asLong();
        for(int i=0;i<2;i++){allPage(admin,"");ok(HttpMethod.GET,"/list-filters/audits/filter-options?tab=all&field=type",admin,null);}
        assertEquals(notices,jdbc.queryForObject("SELECT COUNT(*) FROM sys_notice",Long.class));
        assertEquals(unread,ok(HttpMethod.GET,"/system/notice?countOnly=true",admin,null).asLong());
        var messages=ok(HttpMethod.GET,"/system/notice",admin,null);
        assertFalse(messages.toString().contains("obsolete broadcast"));
        // A result notification remains visible even though the associated business is no longer pending.
        jdbc.update("INSERT INTO sys_notice(from_user_id,to_user_id,type,trigger_event,title,content,source_type,source_id,is_read,is_delete,create_time) VALUES(910002,110228,'1','CORE_RESULT','核心指标已归档','keep result notification','4',?,'0',0,NOW())",core);
        assertEquals(unread+1,ok(HttpMethod.GET,"/system/notice?countOnly=true",admin,null).asLong());
    }

    @Test void administratorReceivesReminderOnlyAfterRealReviewersForwardToArchive() throws Exception {
        long sub=jdbc.queryForObject("SELECT sub_id FROM biz_material_submission WHERE task_id=930002",Long.class);
        long initial=page(admin,"").path("total").asLong();
        assertEquals(0,ok(HttpMethod.GET,"/biz/audit/todo?countOnly=true",admin,null).asLong());
        ok(HttpMethod.POST,"/biz/audit",login(910003),Map.of("sub_id",sub,"is_pass",true,"title","专业群审核通过"));
        assertEquals(initial,page(admin,"").path("total").asLong());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM sys_notice WHERE to_user_id=110228 AND source_type='0' AND source_id=930002 AND trigger_event='任务审核' AND is_delete=0",Long.class));
        long unread=ok(HttpMethod.GET,"/system/notice?countOnly=true",admin,null).asLong();
        ok(HttpMethod.POST,"/biz/audit",login(910002),Map.of("sub_id",sub,"is_pass",true,"title","归口审核通过"));
        assertEquals(initial+1,page(admin,"").path("total").asLong());
        assertEquals(1,ok(HttpMethod.GET,"/biz/audit/todo?countOnly=true",admin,null).asLong());
        assertEquals(unread+1,ok(HttpMethod.GET,"/system/notice?countOnly=true",admin,null).asLong());
        ok(HttpMethod.POST,"/biz/audit",admin,Map.of("sub_id",sub,"is_pass",true,"title","双高办归档"));
        assertEquals(initial,page(admin,"").path("total").asLong());
        assertEquals(0,ok(HttpMethod.GET,"/biz/audit/todo?countOnly=true",admin,null).asLong());
        assertEquals(unread,ok(HttpMethod.GET,"/system/notice?countOnly=true",admin,null).asLong());
        assertEquals(33,allPage(admin,"").path("total").asInt());
    }

    @Test void taskHistoryExcludesEarlierAndOrphanEvidenceWithoutChangingRawLogs() throws Exception {
        long sub=jdbc.queryForObject("SELECT sub_id FROM biz_material_submission WHERE task_id=930002",Long.class);
        jdbc.update("UPDATE biz_material_submission SET submit_time='2026-06-24 12:53:13',flow_status=20,current_handler_id=910002 WHERE sub_id=?",sub);
        jdbc.update("DELETE FROM biz_audit_log WHERE sub_id=?",sub);
        jdbc.update("INSERT INTO biz_audit_log(sub_id,operator_id,action_type,pre_status,post_status,comment,create_time) VALUES(?,110228,'通过',30,40,'old archive','2026-05-20 17:13:05'),(?,910003,'通过',10,20,'current group approval','2026-06-25 09:28:17')",sub,sub);
        JsonNode logs=ok(HttpMethod.GET,"/biz/audit/logs/"+sub,admin,null);
        assertEquals(1,logs.size());assertEquals(20,logs.get(0).path("postStatus").asInt());
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM biz_audit_log WHERE sub_id=?",Integer.class,sub));
        jdbc.update("INSERT INTO biz_audit_log(sub_id,operator_id,action_type,pre_status,post_status,comment,create_time) VALUES(999900,110228,'通过',30,40,'orphan archive','2026-05-20 17:13:05')");
        jdbc.update("INSERT INTO biz_material_submission(sub_id,task_id,file_id,reported_value,data_type,submit_by,submit_dept_id,manage_dept_id,file_suffix,submit_time,flow_status,current_handler_id,is_delete) VALUES(999900,930003,940001,3,'1',910001,920001,920001,'pdf','2026-07-01',20,910002,0)");
        assertEquals(0,ok(HttpMethod.GET,"/biz/audit/logs/999900",admin,null).size());
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM biz_audit_log WHERE sub_id=999900",Integer.class));
    }

    @Test void realPrincipalReturnRetainsReadableHistoryWithoutReopeningTheTodo() throws Exception {
        long sub=jdbc.queryForObject("SELECT sub_id FROM biz_material_submission WHERE task_id=930002",Long.class);
        String group=login(910003),principal=login(910002),owner=login(910001);
        ok(HttpMethod.POST,"/biz/audit",group,Map.of("sub_id",sub,"is_pass",true,"title","Professional approved"));
        ok(HttpMethod.POST,"/biz/audit",principal,Map.of("sub_id",sub,"is_pass",false,"title","Please supplement the supporting material"));
        assertEquals(10,jdbc.queryForObject("SELECT flow_status FROM biz_material_submission WHERE sub_id=?",Integer.class,sub));
        ok(HttpMethod.POST,"/biz/audit",group,Map.of("sub_id",sub,"is_pass",false,"title","Please supplement the supporting material"));
        assertEquals(-10,jdbc.queryForObject("SELECT flow_status FROM biz_material_submission WHERE sub_id=?",Integer.class,sub));
        assertEquals(1,jdbc.queryForObject("SELECT is_delete FROM biz_material_submission WHERE sub_id=?",Integer.class,sub));
        JsonNode history=ok(HttpMethod.GET,"/biz/audit/task/930002",owner,null);assertEquals(1,history.size());assertEquals(sub,history.get(0).path("subId").asLong());
        JsonNode logs=ok(HttpMethod.GET,"/biz/audit/logs/"+sub,owner,null);assertEquals(3,logs.size());assertTrue(logs.toString().contains("Please supplement the supporting material"));
        assertEquals(10,logs.get(0).path("preStatus").asInt());assertEquals(-10,logs.get(0).path("postStatus").asInt());
        assertEquals(0,ok(HttpMethod.GET,"/biz/audit/todo?countOnly=true",principal,null).asInt());
        // The base fixture shares a first-level task, so its second reporter can
        // legitimately read peer tasks. Constrain this account to a distinct task.
        jdbc.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json,version) VALUES(910004,'CUSTOM','{\"tasks.read\":true,\"tasks.history\":true}','{\"tasks\":{\"mode\":\"CUSTOM\",\"items\":[930003]}}',1) ON DUPLICATE KEY UPDATE access_mode=VALUES(access_mode),permissions_json=VALUES(permissions_json),scopes_json=VALUES(scopes_json),version=version+1");
        var denied=request(HttpMethod.GET,"/biz/audit/logs/"+sub,login(910004),null);
        assertTrue(Set.of(HttpStatus.FORBIDDEN,HttpStatus.NOT_FOUND).contains(denied.getStatusCode()) || (denied.getStatusCode().is2xxSuccessful() && json.readTree(denied.getBody()).path("code").asInt()>=400),denied.getStatusCode()+" "+denied.getBody());
        assertFalse(denied.getBody().contains("Please supplement the supporting material"));
    }

    @Test void stepBackAtBothReviewLevelsKeepsOriginalSubmissionAndResumesFullChain()throws Exception{
        long sub=jdbc.queryForObject("SELECT sub_id FROM biz_material_submission WHERE task_id=930002",Long.class);
        String group=login(910003),principal=login(910002);
        for(var decision:List.of(new Object[]{group,true,20},new Object[]{principal,true,30},new Object[]{admin,false,20},new Object[]{principal,false,10},new Object[]{group,true,20},new Object[]{principal,true,30},new Object[]{admin,true,40})){
            ok(HttpMethod.POST,"/biz/audit",(String)decision[0],Map.of("sub_id",sub,"is_pass",decision[1],"title","Step-back acceptance"));
            assertEquals(decision[2],jdbc.queryForObject("SELECT flow_status FROM biz_material_submission WHERE sub_id=?",Integer.class,sub));
            assertEquals(0,jdbc.queryForObject("SELECT is_delete FROM biz_material_submission WHERE sub_id=?",Integer.class,sub));
        }
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM biz_material_submission WHERE task_id=930002",Integer.class));
        assertEquals(7,ok(HttpMethod.GET,"/biz/audit/logs/"+sub,login(910001),null).size());
    }
}
