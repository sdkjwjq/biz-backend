package org.example;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.jdbc.core.*;
import org.springframework.test.context.*;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="SHUANGGAO_REVIEW_TEST",matches="true")
@Import(ReviewBatchRegressionApiTest.NoAutomaticSchedules.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"logging.file.name=target/platform-notice-test.log","logging.level.org.springframework=WARN"})
class PlatformNoticeApiTest {
    static final long ADMIN=110228, USER=910001, OTHER=910002, DEPT=920001;
    @LocalServerPort int port;
    @Autowired TestRestTemplate http;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    @DynamicPropertySource static void db(DynamicPropertyRegistry r){String url=System.getenv("SHUANGGAO_TEST_JDBC_URL");if(url==null||!url.matches("jdbc:mysql://127\\.0\\.0\\.1:3306/biz_review_test_[0-9a-f]{32}\\?.*"))throw new IllegalStateException("Requires disposable database");r.add("spring.datasource.url",()->url);r.add("spring.datasource.username",()->System.getenv("SHUANGGAO_TEST_DB_USER"));r.add("spring.datasource.password",()->System.getenv("SHUANGGAO_TEST_DB_PASSWORD"));}
    @BeforeEach void seed(){
        http.getRestTemplate().setRequestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory());
        jdbc.execute((ConnectionCallback<Void>)c->{assertTrue(c.getCatalog().matches("biz_review_test_[0-9a-f]{32}"));try(var s=c.createStatement()){List<String> tables=new ArrayList<>();try(var rs=s.executeQuery("SHOW FULL TABLES WHERE Table_type='BASE TABLE'")){while(rs.next())tables.add(rs.getString(1));}s.execute("SET FOREIGN_KEY_CHECKS=0");try{for(String t:tables){assertTrue(t.matches("[A-Za-z0-9_]+"));s.execute("TRUNCATE TABLE `"+t+"`");}}finally{s.execute("SET FOREIGN_KEY_CHECKS=1");}}return null;});
        jdbc.update("INSERT INTO sys_dept(dept_id,dept_name,status,is_delete) VALUES(?,'通知测试部门','0',0)",DEPT);
        account(ADMIN,"0");account(USER,"1");account(OTHER,"1");
    }
    void account(long id,String role){jdbc.update("INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,password,role,status,is_delete,force_password_change) VALUES(?,? ,?,?,'Notice-fixture-123',?,'1',0,0)",id,DEPT,"notice"+id,"通知用户"+id,role);}
    ResponseEntity<String> call(HttpMethod method,String path,String token,Object body){HttpHeaders h=new HttpHeaders();h.setContentType(MediaType.APPLICATION_JSON);if(token!=null)h.set("Authorization",token);return http.exchange("http://127.0.0.1:"+port+"/api"+path,method,new HttpEntity<>(body,h),String.class);}
    JsonNode body(ResponseEntity<String> r)throws Exception{return json.readTree(r.getBody());}
    String login(long id)throws Exception{var r=call(HttpMethod.POST,"/system/login",null,Map.of("user_id",id,"password","Notice-fixture-123"));assertEquals(200,r.getStatusCode().value());return body(r).path("token").asText();}
    Map<String,Object> draft(boolean important){Map<String,Object> d=new LinkedHashMap<>();d.put("title","通知测试");d.put("content","第一段内容\n第二段内容");d.put("important",important);d.put("audienceType","USERS");d.put("audienceIds",List.of(USER));d.put("fileIds",List.of());return d;}
    JsonNode save(String token,Map<String,Object> d)throws Exception{var r=call(HttpMethod.POST,"/admin/platform-notices",token,d);assertEquals(200,r.getStatusCode().value(),r.getBody());return body(r);}
    long publish(String token,boolean important)throws Exception{var n=save(token,draft(important));var r=call(HttpMethod.POST,"/admin/platform-notices/"+n.path("id").asLong()+"/publish",token,Map.of("version",n.path("version").asLong()));assertEquals(200,r.getStatusCode().value(),r.getBody());return n.path("id").asLong();}
    JsonNode open(long id,String token,String key)throws Exception{var r=call(HttpMethod.POST,"/platform-notices/"+id+"/open",token,Map.of("requestId",key));assertEquals(200,r.getStatusCode().value(),r.getBody());return body(r);}
    void confirm(JsonNode n,String token)throws Exception{var r=call(HttpMethod.POST,"/platform-notices/"+n.path("id").asLong()+"/confirm",token,Map.of("contentVersion",n.path("contentVersion").asInt(),"viewKey",n.path("viewKey").asText()));assertEquals(200,r.getStatusCode().value(),r.getBody());}
    int pending(String token)throws Exception{return body(call(HttpMethod.GET,"/platform-notices/status",token,null)).path("total").asInt();}

    @Test void allImportantNoticesMustBeConfirmedAndCannotBeBypassed()throws Exception{
        String admin=login(ADMIN),oldSession=login(USER);long a=publish(admin,true),b=publish(admin,true);publish(admin,false);
        assertEquals(0,pending(oldSession));assertEquals(200,call(HttpMethod.GET,"/system/notice",oldSession,null).getStatusCode().value());
        String token=login(USER);assertEquals(2,pending(token));
        assertEquals(423,call(HttpMethod.GET,"/biz/tasks",token,null).getStatusCode().value());
        long inbox=jdbc.queryForObject("SELECT inbox_id FROM sys_platform_notice_recipient WHERE notice_id=? AND user_id=?",Long.class,a,USER);
        var mark=call(HttpMethod.POST,"/system/notice/"+inbox,oldSession,null);assertTrue(mark.getStatusCode().isError()||body(mark).path("code").asInt()>=400);
        assertEquals("0",jdbc.queryForObject("SELECT is_read FROM sys_notice WHERE notice_id=?",String.class,inbox));
        assertEquals(409,call(HttpMethod.POST,"/platform-notices/"+a+"/confirm",token,Map.of("contentVersion",1,"viewKey","fake")).getStatusCode().value());
        JsonNode opened=open(a,token,"same-request");open(a,token,"same-request");assertEquals(1,jdbc.queryForObject("SELECT open_count FROM sys_platform_notice_recipient WHERE notice_id=? AND user_id=?",Integer.class,a,USER));
        confirm(opened,token);confirm(opened,token);assertEquals(1,pending(token));assertEquals(423,call(HttpMethod.POST,"/system/notice",token,Map.of("to_user_id",OTHER,"title","不能绕过","content","内容")).getStatusCode().value());
        confirm(open(b,token,"second-notice"),token);assertEquals(0,pending(token));assertEquals(200,call(HttpMethod.GET,"/biz/tasks",token,null).getStatusCode().value());
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM sys_platform_notice_view_log WHERE action='CONFIRM'",Integer.class));assertEquals(0,pending(login(USER)));
    }
    @Test void recipientIsolationAndCurrentAdministratorRoleAreEnforced()throws Exception{
        String admin=login(ADMIN);long id=publish(admin,true);String outsider=login(OTHER);
        assertEquals(0,pending(outsider));assertEquals(404,call(HttpMethod.POST,"/platform-notices/"+id+"/open",outsider,Map.of("requestId","other")).getStatusCode().value());
        assertEquals(403,call(HttpMethod.POST,"/admin/platform-notices",outsider,draft(false)).getStatusCode().value());
        jdbc.update("UPDATE sys_user SET role='1' WHERE user_id=?",ADMIN);
        assertEquals(403,call(HttpMethod.GET,"/admin/platform-notices",admin,null).getStatusCode().value());
        jdbc.update("UPDATE sys_user SET status='0' WHERE user_id=?",OTHER);
        assertEquals(403,call(HttpMethod.GET,"/platform-notices/status",outsider,null).getStatusCode().value());
    }
    @Test void usersWithoutMessagesPermissionCanReadTheirOwnMandatoryNotice()throws Exception{
        String admin=login(ADMIN);long id=publish(admin,true);
        jdbc.update("INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json,version) VALUES(?,'CUSTOM','{}','{}',1)",USER);
        String token=login(USER);assertEquals(1,pending(token));confirm(open(id,token,"restricted-account"),token);assertEquals(0,pending(token));
        assertEquals(403,call(HttpMethod.GET,"/system/notice",token,null).getStatusCode().value());
    }
    @Test void recipientSnapshotWithdrawalAndReadEvidenceArePreserved()throws Exception{
        String admin=login(ADMIN);var d=draft(true);d.put("audienceType","ALL");JsonNode n=save(admin,d);long id=n.path("id").asLong();
        jdbc.update("UPDATE sys_user SET status='0' WHERE user_id=?",OTHER);
        assertEquals(200,call(HttpMethod.POST,"/admin/platform-notices/"+id+"/publish",admin,Map.of("version",1)).getStatusCode().value());
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM sys_platform_notice_recipient WHERE notice_id=?",Integer.class,id));account(910010,"1");assertEquals(0,pending(login(910010)));
        String token=login(USER);open(id,token,"before-withdraw");
        var current=body(call(HttpMethod.GET,"/admin/platform-notices/"+id,admin,null));assertEquals(200,call(HttpMethod.POST,"/admin/platform-notices/"+id+"/withdraw",admin,Map.of("version",current.path("version").asLong(),"reason","测试撤回")).getStatusCode().value());
        assertEquals(0,pending(token));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM sys_platform_notice_view_log",Integer.class));assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM sys_platform_notice_recipient WHERE notice_id=?",Integer.class,id));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM sys_notice WHERE source_type='5' AND is_delete=0",Integer.class));
    }
    @Test void draftsUseVersionChecksAndPublishedContentCannotBeOverwritten()throws Exception{
        String admin=login(ADMIN);Map<String,Object>d=draft(false);JsonNode n=save(admin,d);d.put("id",n.path("id").asLong());d.put("version",0);
        assertEquals(409,call(HttpMethod.POST,"/admin/platform-notices",admin,d).getStatusCode().value());
        d.put("version",1);n=save(admin,d);long id=n.path("id").asLong();long version=n.path("version").asLong();
        assertEquals(200,call(HttpMethod.POST,"/admin/platform-notices/"+id+"/publish",admin,Map.of("version",version)).getStatusCode().value());
        assertEquals(200,call(HttpMethod.POST,"/admin/platform-notices/"+id+"/publish",admin,Map.of("version",version)).getStatusCode().value());
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM sys_platform_notice_recipient WHERE notice_id=?",Integer.class,id));
        assertEquals(409,call(HttpMethod.POST,"/admin/platform-notices",admin,d).getStatusCode().value());
        assertEquals(400,call(HttpMethod.POST,"/admin/platform-notices",admin,Map.of("title","缺正文")).getStatusCode().value());
    }
    @Test void failedPublicationDoesNotLeavePartialMessagesOrSuccessLogs()throws Exception{
        String admin=login(ADMIN);var n=save(admin,draft(true));long id=n.path("id").asLong();
        jdbc.execute("CREATE TRIGGER fail_notice_recipient BEFORE INSERT ON sys_platform_notice_recipient FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic recipient failure'");
        try{assertTrue(call(HttpMethod.POST,"/admin/platform-notices/"+id+"/publish",admin,Map.of("version",1)).getStatusCode().is5xxServerError());}
        finally{jdbc.execute("DROP TRIGGER fail_notice_recipient");}
        assertEquals("DRAFT",jdbc.queryForObject("SELECT status FROM sys_platform_notice WHERE notice_id=?",String.class,id));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM sys_notice",Integer.class));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM sys_platform_notice_recipient",Integer.class));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM sys_admin_operation_log WHERE module='PLATFORM_NOTICE' AND action='PUBLISH'",Integer.class));
    }
    @Test void forcedPasswordChangeAndLogoutPrecedeBusinessAccess()throws Exception{
        String admin=login(ADMIN);long id=publish(admin,true);jdbc.update("UPDATE sys_user SET force_password_change=1 WHERE user_id=?",USER);String token=login(USER);
        assertEquals(428,call(HttpMethod.GET,"/platform-notices/status",token,null).getStatusCode().value());
        assertEquals(200,call(HttpMethod.GET,"/system/password/status",token,null).getStatusCode().value());
        jdbc.update("UPDATE sys_user SET force_password_change=0 WHERE user_id=?",USER);
        assertEquals(1,pending(token));assertEquals(200,call(HttpMethod.POST,"/system/logout",token,null).getStatusCode().value());
        assertEquals(401,call(HttpMethod.POST,"/platform-notices/"+id+"/open",token,Map.of("requestId","old-token")).getStatusCode().value());assertEquals(1,pending(login(USER)));
    }
    @Test void logsAndExportDistinguishOpenedFromConfirmed()throws Exception{
        String admin=login(ADMIN);long id=publish(admin,false);String token=login(USER);JsonNode n=open(id,token,"one");open(id,token,"two");
        var page=body(call(HttpMethod.GET,"/admin/platform-notices/"+id+"/receipts",admin,null));assertEquals(2,page.path("items").get(0).path("openCount").asInt());assertEquals(0,page.path("summary").path("confirmed").asInt());
        confirm(n,token);page=body(call(HttpMethod.GET,"/admin/platform-notices/"+id+"/receipts?state=confirmed",admin,null));assertEquals(1,page.path("total").asInt());
        assertEquals(3,body(call(HttpMethod.GET,"/admin/platform-notices/"+id+"/events/"+USER,admin,null)).size());
        HttpHeaders h=new HttpHeaders();h.set("Authorization",admin);var exported=http.exchange("http://127.0.0.1:"+port+"/api/admin/platform-notices/"+id+"/export",HttpMethod.GET,new HttpEntity<>(h),byte[].class);assertEquals(200,exported.getStatusCode().value());
        try(var book=new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(exported.getBody()))){assertEquals("910001",book.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());}
        String logs=jdbc.queryForObject("SELECT GROUP_CONCAT(COALESCE(after_json,'')) FROM sys_admin_operation_log WHERE module='PLATFORM_NOTICE'",String.class);assertFalse(logs.contains("第一段内容"));
    }
    @Test void correctedVersionsRequireFreshConfirmationAndRetainPreviousEvidence()throws Exception{
        String admin=login(ADMIN);long original=publish(admin,true);String first=login(USER);confirm(open(original,first,"version-one"),first);
        var d=draft(true);d.put("title","更正后的通知");d.put("revisionOf",original);var saved=save(admin,d);long revised=saved.path("id").asLong();
        assertEquals(2,saved.path("contentVersion").asInt());assertEquals(200,call(HttpMethod.POST,"/admin/platform-notices/"+revised+"/publish",admin,Map.of("version",1)).getStatusCode().value());
        assertEquals(0,pending(first));String second=login(USER);assertEquals(1,pending(second));
        assertEquals("WITHDRAWN",jdbc.queryForObject("SELECT status FROM sys_platform_notice WHERE notice_id=?",String.class,original));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM sys_platform_notice_recipient WHERE notice_id=? AND confirmed_at IS NOT NULL",Integer.class,original));
        var viewed=open(revised,second,"version-two");assertEquals(409,call(HttpMethod.POST,"/platform-notices/"+revised+"/confirm",second,Map.of("contentVersion",1,"viewKey",viewed.path("viewKey").asText())).getStatusCode().value());confirm(viewed,second);
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM sys_platform_notice_view_log WHERE action='CONFIRM'",Integer.class));
    }
    @Test void expiredNoticeReleasesTheLoginGateWithoutDeletingHistory()throws Exception{
        String admin=login(ADMIN);var d=draft(true);d.put("expiresAt",java.time.LocalDateTime.now().plusDays(1).toString());var n=save(admin,d);long id=n.path("id").asLong();
        assertEquals(200,call(HttpMethod.POST,"/admin/platform-notices/"+id+"/publish",admin,Map.of("version",1)).getStatusCode().value());String token=login(USER);assertEquals(1,pending(token));open(id,token,"before-expiry");
        jdbc.update("UPDATE sys_platform_notice SET expires_at=DATE_SUB(NOW(),INTERVAL 1 MINUTE) WHERE notice_id=?",id);
        assertEquals(0,pending(token));assertEquals(200,call(HttpMethod.GET,"/biz/tasks",token,null).getStatusCode().value());assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM sys_platform_notice_view_log",Integer.class));
    }
    @Test void attachmentsAreAvailableOnlyToRecipientsAndAdministrators()throws Exception{
        String admin=login(ADMIN);var parts=new org.springframework.util.LinkedMultiValueMap<String,Object>();
        parts.add("file",new org.springframework.core.io.ByteArrayResource("%PDF-1.4\nSynthetic notice attachment".getBytes(java.nio.charset.StandardCharsets.UTF_8)){@Override public String getFilename(){return "notice-test.pdf";}});
        HttpHeaders headers=new HttpHeaders();headers.set("Authorization",admin);headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        var upload=http.exchange("http://127.0.0.1:"+port+"/api/admin/platform-notices/attachments",HttpMethod.POST,new HttpEntity<>(parts,headers),String.class);assertEquals(200,upload.getStatusCode().value(),upload.getBody());long file=body(upload).path("id").asLong();
        var path=java.nio.file.Path.of(System.getProperty("user.dir"),jdbc.queryForObject("SELECT file_path FROM sys_file WHERE file_id=?",String.class,file).substring(1));
        try{
            var d=draft(true);d.put("fileIds",List.of(file));var n=save(admin,d);long id=n.path("id").asLong();assertEquals(200,call(HttpMethod.POST,"/admin/platform-notices/"+id+"/publish",admin,Map.of("version",1)).getStatusCode().value());
            String recipient=login(USER);HttpHeaders own=new HttpHeaders();own.set("Authorization",recipient);var response=http.exchange("http://127.0.0.1:"+port+"/api/platform-notices/"+id+"/attachments/"+file,HttpMethod.GET,new HttpEntity<>(own),byte[].class);assertEquals(200,response.getStatusCode().value());assertTrue(new String(response.getBody(),java.nio.charset.StandardCharsets.UTF_8).startsWith("%PDF-1.4"));
            assertEquals(404,call(HttpMethod.GET,"/platform-notices/"+id+"/attachments/"+file,login(OTHER),null).getStatusCode().value());
        }finally{java.nio.file.Files.deleteIfExists(path);}
    }
}
