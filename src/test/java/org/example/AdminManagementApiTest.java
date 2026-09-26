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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** 管理中心真实 HTTP / MySQL 闭环；仅在随机隔离库中运行。 */
@EnabledIfEnvironmentVariable(named = "SHUANGGAO_REVIEW_TEST", matches = "true")
@Import(ReviewBatchRegressionApiTest.NoAutomaticSchedules.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"logging.file.name=target/admin-management-test.log", "logging.level.org.springframework=WARN"})
class AdminManagementApiTest {
    private static final long ADMIN = 990001L;
    private static final long USER = 990002L;
    private static final long DEPT = 991001L;
    private static final String PASSWORD = "AdminTest123";

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        String url = System.getenv("SHUANGGAO_TEST_JDBC_URL");
        if (url == null || !url.matches("jdbc:mysql://127\\.0\\.0\\.1:3306/biz_review_test_[0-9a-f]{32}\\?.*")) {
            throw new IllegalStateException("Admin tests require an isolated local schema");
        }
        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.username", () -> System.getenv("SHUANGGAO_TEST_DB_USER"));
        registry.add("spring.datasource.password", () -> System.getenv("SHUANGGAO_TEST_DB_PASSWORD"));
    }

    @LocalServerPort private int port;
    @Autowired private TestRestTemplate http;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ObjectMapper json;

    @BeforeEach
    void seed() {
        jdbc.execute((ConnectionCallback<Void>) connection -> {
            assertTrue(connection.getCatalog().matches("biz_review_test_[0-9a-f]{32}"));
            try (Statement statement = connection.createStatement()) {
                List<String> tables = new ArrayList<>();
                try (ResultSet rows = statement.executeQuery("SHOW FULL TABLES WHERE Table_type='BASE TABLE'")) {
                    while (rows.next()) tables.add(rows.getString(1));
                }
                statement.execute("SET FOREIGN_KEY_CHECKS=0");
                for (String table : tables) statement.executeUpdate("DELETE FROM `" + table + "`");
                statement.execute("SET FOREIGN_KEY_CHECKS=1");
            }
            return null;
        });
        jdbc.update("INSERT INTO sys_dept(dept_id,dept_name,status,is_delete,create_time,update_time) VALUES(?,?,'0',0,NOW(),NOW())", DEPT, "管理测试部门");
        seedUser(ADMIN, "0", DEPT);
        seedUser(USER, "1", DEPT);
    }

    @Test
    void accessUsesCurrentDatabaseRoleAndStatus() throws Exception {
        String userToken = login(USER);
        assertEquals(HttpStatus.FORBIDDEN, request(HttpMethod.GET, "/admin/users", userToken, null).getStatusCode());

        String oldAdminToken = login(ADMIN);
        assertEquals(HttpStatus.OK, request(HttpMethod.GET, "/admin/users", oldAdminToken, null).getStatusCode());
        jdbc.update("UPDATE sys_user SET role='1' WHERE user_id=?", ADMIN);
        assertEquals(HttpStatus.FORBIDDEN, request(HttpMethod.GET, "/admin/users", oldAdminToken, null).getStatusCode(),
                "旧 token 在数据库角色变化后必须立即失去管理权限");
    }

    @Test
    void createResetAndDisableUserAreAuditedWithoutPasswords() throws Exception {
        String token = login(ADMIN);
        Map<String, Object> create = new LinkedHashMap<>();
        create.put("userId", 990010L); create.put("deptId", DEPT); create.put("userName", "new_admin_test");
        create.put("nickName", "新增账号"); create.put("email", "new@example.invalid"); create.put("role", "1");
        JsonNode created = body(request(HttpMethod.POST, "/admin/users", token, create));
        assertEquals("1", created.path("status").asText());
        assertEquals(1, created.path("forcePasswordChange").asInt());
        assertEquals("990010", jdbc.queryForObject("SELECT password FROM sys_user WHERE user_id=990010", String.class));

        JsonNode reset = body(request(HttpMethod.POST, "/admin/users/990010/reset-password", token,
                Map.of("reason", "测试重置", "expectedUpdateTime", created.path("updateTime").asText())));
        assertTrue(reset.path("message").asText().contains("重置"));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM sys_admin_operation_log WHERE target_id='990010'", Integer.class));
        String logs = jdbc.queryForObject("SELECT GROUP_CONCAT(COALESCE(before_json,''),COALESCE(after_json,'')) FROM sys_admin_operation_log WHERE target_id='990010'", String.class);
        assertFalse(logs.toLowerCase().contains("\"password\":"), "日志不得保存密码字段");

        JsonNode refreshed = body(request(HttpMethod.GET, "/admin/users?keyword=990010", token, null)).path("items").get(0);
        body(request(HttpMethod.POST, "/admin/users/990010/status", token,
                Map.of("status", "0", "reason", "测试停用", "expectedUpdateTime", refreshed.path("updateTime").asText())));
        JsonNode deniedLogin = body(request(HttpMethod.POST, "/system/login", null,
                Map.of("user_id", 990010L, "password", "990010")));
        assertEquals(500, deniedLogin.path("code").asInt());
        assertTrue(deniedLogin.path("message").asText().contains("停用"));
    }

    @Test
    void impactLastAdministratorAndOptimisticLockAreEnforced() throws Exception {
        String token = login(ADMIN);
        JsonNode admin = body(request(HttpMethod.GET, "/admin/users?keyword=" + ADMIN, token, null)).path("items").get(0);
        ResponseEntity<String> last = request(HttpMethod.POST, "/admin/users/" + ADMIN + "/status", token,
                Map.of("status", "0", "reason", "不能停用最后管理员", "expectedUpdateTime", admin.path("updateTime").asText()));
        assertEquals(HttpStatus.CONFLICT, last.getStatusCode());

        jdbc.update("INSERT INTO biz_project(project_id,project_name,is_delete) VALUES(1,'管理测试项目',0)");
        jdbc.update("INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,status,is_delete) VALUES(992001,1,0,2026,'A','活动职责',3,?,?,?,?,'1',1,'1',0)", USER, ADMIN, ADMIN, DEPT);
        JsonNode user = body(request(HttpMethod.GET, "/admin/users?keyword=" + USER, token, null)).path("items").get(0);
        JsonNode impact = body(request(HttpMethod.GET, "/admin/users/" + USER + "/impact", token, null));
        assertTrue(impact.path("blockingCount").asLong() > 0);
        assertEquals(HttpStatus.CONFLICT, request(HttpMethod.POST, "/admin/users/" + USER + "/status", token,
                Map.of("status", "0", "reason", "职责未移交", "expectedUpdateTime", user.path("updateTime").asText())).getStatusCode());

        Map<String, Object> edit = new LinkedHashMap<>();
        edit.put("deptId", DEPT); edit.put("userName", "changed-user"); edit.put("nickName", "第一次修改");
        edit.put("email", "changed@example.invalid"); edit.put("role", "1"); edit.put("reason", "第一次修改");
        edit.put("expectedUpdateTime", user.path("updateTime").asText());
        assertEquals(HttpStatus.OK, request(HttpMethod.PUT, "/admin/users/" + USER, token, edit).getStatusCode());
        edit.put("nickName", "过期窗口修改");
        assertEquals(HttpStatus.CONFLICT, request(HttpMethod.PUT, "/admin/users/" + USER, token, edit).getStatusCode());
    }

    @Test
    void overviewOperationsExportAndDeletedRecordsAreAvailable() throws Exception {
        String token = login(ADMIN);
        JsonNode overview = body(request(HttpMethod.GET, "/admin/overview?year=2026&month=9", token, null));
        assertEquals(2, overview.path("activeUsers").asInt());
        assertTrue(overview.has("taskIssues"));
        JsonNode operations = body(request(HttpMethod.GET, "/admin/operations?page=1&pageSize=10", token, null));
        assertTrue(operations.has("items"));
        ResponseEntity<String> export = request(HttpMethod.POST, "/admin/operations/export", token, Map.of());
        assertEquals(HttpStatus.OK, export.getStatusCode());
        assertEquals("PK", export.getBody().substring(0, 2));
        JsonNode deleted = body(request(HttpMethod.GET, "/admin/work-records/deleted", token, null));
        assertEquals(0, deleted.path("total").asInt());
    }

    @Test
    void departmentLeaderMustBeActiveMemberAndActiveDepartmentCannotBeDisabled() throws Exception {
        String token = login(ADMIN);
        JsonNode department = body(request(HttpMethod.GET, "/admin/departments", token, null)).get(0);
        ResponseEntity<String> wrongLeader = request(HttpMethod.PUT, "/admin/departments/" + DEPT, token,
                Map.of("leaderId", 123456L, "status", "0", "reason", "无效负责人",
                        "expectedUpdateTime", department.path("updateTime").asText()));
        assertEquals(HttpStatus.BAD_REQUEST, wrongLeader.getStatusCode());

        JsonNode impact = body(request(HttpMethod.GET, "/admin/departments/" + DEPT + "/impact", token, null));
        assertTrue(impact.path("blockingCount").asLong() >= 2);
        ResponseEntity<String> disable = request(HttpMethod.PUT, "/admin/departments/" + DEPT, token,
                Map.of("leaderId", USER, "status", "1", "reason", "仍有关联账号",
                        "expectedUpdateTime", department.path("updateTime").asText()));
        assertEquals(HttpStatus.CONFLICT, disable.getStatusCode());

        JsonNode updated = body(request(HttpMethod.PUT, "/admin/departments/" + DEPT, token,
                Map.of("leaderId", USER, "status", "0", "reason", "调整部门负责人",
                        "expectedUpdateTime", department.path("updateTime").asText())));
        assertEquals(USER, updated.path("leaderId").asLong());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM sys_admin_operation_log WHERE module='DEPARTMENT'", Integer.class));
    }

    private void seedUser(long id, String role, long dept) {
        jdbc.update("INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,email,password,role,status,is_delete,force_password_change,create_time,update_time) VALUES(?,?,?,?,?,?,?,'1',0,0,NOW(),NOW())",
                id, dept, "admin-test-" + id, "测试" + id, "test@example.invalid", PASSWORD, role);
    }

    private String login(long id) throws Exception {
        return body(request(HttpMethod.POST, "/system/login", null, Map.of("user_id", id, "password", PASSWORD))).path("token").asText();
    }

    private ResponseEntity<String> request(HttpMethod method, String path, String token, Object body) {
        HttpHeaders headers = new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) headers.set("Authorization", token);
        return http.exchange("http://127.0.0.1:" + port + "/api" + path, method, new HttpEntity<>(body, headers), String.class);
    }

    private JsonNode body(ResponseEntity<String> response) throws Exception {
        assertNotNull(response.getBody());
        return json.readTree(response.getBody());
    }
}
