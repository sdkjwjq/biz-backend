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

    @Test void administratorCountMatchesAllPagesAndBusinessKinds() throws Exception {
        var first=page(admin,"");assertEquals(34,first.path("total").asInt());assertEquals(34,first.path("summary").path("todoCount").asInt());
        var ids=new HashSet<String>();var types=new HashSet<String>();
        for(int n=1;n<=4;n++)for(var row:page(admin,"&page="+n).path("items")){assertTrue(ids.add(row.path("id").asText()));types.add(row.path("type").asText());}
        assertEquals(34,ids.size());assertEquals(Set.of("task","performance","achievement","core"),types);
        var filtered=page(admin,"&f.type=performance");assertEquals(1,filtered.path("total").asInt());assertEquals(34,filtered.path("summary").path("todoCount").asInt());
        assertEquals(0,page(admin,"&f.keyword=not-found-in-fixtures").path("total").asInt());assertEquals(34,page(admin,"").path("total").asInt());
        var options=ok(HttpMethod.GET,"/list-filters/audits/filter-options?tab=todo&field=type",admin,null);assertEquals(4,options.path("total").asInt());
        assertFalse(ok(HttpMethod.GET,"/list-filters/audits/locate?taskId=930002",admin,null).path("isViewMode").asBoolean());
        long tasks=ok(HttpMethod.GET,"/biz/audit/todo?countOnly=true",admin,null).asLong();
        assertEquals(31,tasks);
    }

    @Test void ordinaryAccountsCannotInheritAdministratorScopeOrSpoofIt() throws Exception {
        String outsider=login(910004),auditor=login(910003);
        assertEquals(0,page(outsider,"").path("total").asInt());assertEquals(32,page(auditor,"").path("total").asInt());
        assertEquals(0,page(outsider,"").path("summary").path("todoCount").asInt());
        assertTrue(Set.of(HttpStatus.FORBIDDEN,HttpStatus.NOT_FOUND).contains(request(HttpMethod.GET,"/list-filters/audits/locate?taskId=930003",outsider,null).getStatusCode()));
        HttpHeaders spoof=new HttpHeaders();spoof.set("Authorization",outsider);spoof.set("X-Admin-Test","1");
        assertEquals(HttpStatus.FORBIDDEN,http.exchange("http://127.0.0.1:"+port+"/api/list-filters/audits?responseMode=page",HttpMethod.GET,new HttpEntity<>(spoof),String.class).getStatusCode());
        jdbc.update("UPDATE sys_user SET role='1' WHERE user_id=110228");
        for(var row:page(admin,"").path("items"))assertFalse(Set.of("task","performance").contains(row.path("type").asText()));
        jdbc.update("UPDATE sys_user SET status='0' WHERE user_id=110228");
        assertEquals(HttpStatus.FORBIDDEN,request(HttpMethod.GET,"/list-filters/audits?responseMode=page",admin,null).getStatusCode());
    }

    @Test void administratorCanProcessVisibleRowAndRefreshWithoutChangingOtherTodos() throws Exception {
        long sub=jdbc.queryForObject("SELECT sub_id FROM biz_material_submission WHERE task_id=930002",Long.class);
        long logs=jdbc.queryForObject("SELECT COUNT(*) FROM biz_audit_log",Long.class);
        ok(HttpMethod.POST,"/biz/audit",admin,Map.of("sub_id",sub,"is_pass",true,"title","Admin scope verification"));
        assertEquals(20,jdbc.queryForObject("SELECT flow_status FROM biz_material_submission WHERE sub_id=?",Integer.class,sub));
        assertEquals(34,page(admin,"").path("total").asInt());
        assertEquals(31,page(login(910003),"").path("total").asInt()); // 30 other tasks plus the performance submission.
        assertEquals(logs+1,jdbc.queryForObject("SELECT COUNT(*) FROM biz_audit_log",Long.class));
        assertEquals(110228L,jdbc.queryForObject("SELECT operator_id FROM biz_audit_log WHERE sub_id=? ORDER BY log_id DESC LIMIT 1",Long.class,sub));
    }
}
