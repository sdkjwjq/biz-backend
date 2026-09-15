package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.service.ScheduledTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.sql.ResultSet;
import java.sql.Statement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** 本批修复的真实 HTTP / MySQL 回归；仅由隔离库脚本显式开启。 */
@EnabledIfEnvironmentVariable(named = "SHUANGGAO_REVIEW_TEST", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"logging.file.name=target/review-regression.log",
                "logging.level.org.springframework=WARN", "logging.level.org.springframework.web=WARN",
                "logging.level.org.example=WARN", "logging.level.org.example.mapper=WARN"})
class ReviewBatchRegressionApiTest {
    private static final long ADMIN = 110228L;
    private static final long USER = 910001L;
    private static final long LEADER = 910002L;
    private static final long AUDITOR = 910003L;
    private static final long DEPT = 920001L;
    private static final String PASSWORD = "review-fixture-password";

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        String url = System.getenv("SHUANGGAO_TEST_JDBC_URL");
        if (url == null || !url.matches("jdbc:mysql://127\\.0\\.0\\.1:3306/biz_review_test_[0-9a-f]{32}\\?.*")) {
            throw new IllegalStateException("Regression tests require an isolated local schema");
        }
        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.username", () -> System.getenv("SHUANGGAO_TEST_DB_USER"));
        registry.add("spring.datasource.password", () -> System.getenv("SHUANGGAO_TEST_DB_PASSWORD"));
    }

    @LocalServerPort private int port;
    @Autowired private TestRestTemplate http;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ObjectMapper json;
    @MockBean private ScheduledTaskService scheduledTaskService;

    @BeforeEach
    void seed() {
        http.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
        jdbc.execute((ConnectionCallback<Void>) connection -> {
            assertTrue(connection.getCatalog().matches("biz_review_test_[0-9a-f]{32}"));
            try (Statement statement = connection.createStatement()) {
                List<String> tables = new ArrayList<>();
                try (ResultSet rows = statement.executeQuery("SHOW FULL TABLES WHERE Table_type = 'BASE TABLE'")) {
                    while (rows.next()) tables.add(rows.getString(1));
                }
                statement.execute("SET FOREIGN_KEY_CHECKS=0");
                try {
                    for (String table : tables) {
                        assertTrue(table.matches("[A-Za-z0-9_]+"));
                        statement.executeUpdate("DELETE FROM `" + table + "`");
                    }
                } finally {
                    statement.execute("SET FOREIGN_KEY_CHECKS=1");
                }
            }
            return null;
        });
        jdbc.update("INSERT INTO sys_dept (dept_id, dept_name, is_delete) VALUES (?, 'Review department', 0)", DEPT);
        seedUser(ADMIN, "0");
        seedUser(USER, "1");
        seedUser(LEADER, "2");
        seedUser(AUDITOR, "1");
        jdbc.update("UPDATE sys_dept SET leader_id=? WHERE dept_id=?", LEADER, DEPT);
    }

    private void seedUser(long id, String role) {
        jdbc.update("INSERT INTO sys_user (user_id, dept_id, user_name, nick_name, email, password, role, status, is_delete) "
                        + "VALUES (?, ?, ?, ?, 'review@example.invalid', ?, ?, '1', 0)",
                id, DEPT, "review" + id, "Review " + id, PASSWORD, role);
    }

    private ResponseEntity<String> request(HttpMethod method, String path, String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) headers.set("Authorization", token);
        return http.exchange("http://127.0.0.1:" + port + "/api" + path, method, new HttpEntity<>(body, headers), String.class);
    }

    private JsonNode body(ResponseEntity<String> response) throws Exception {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return json.readTree(response.getBody());
    }

    private String login(long userId) throws Exception {
        JsonNode result = body(request(HttpMethod.POST, "/system/login", null,
                Map.of("user_id", userId, "password", PASSWORD)));
        assertTrue(result.hasNonNull("token"), "Fixture login must succeed");
        return result.get("token").asText();
    }

    @Test
    void userDirectoryNeverReturnsPasswords() throws Exception {
        assertEquals(HttpStatus.UNAUTHORIZED, request(HttpMethod.GET, "/system/allUsers", null, null).getStatusCode());
        for (long userId : new long[]{USER, LEADER, ADMIN}) {
            JsonNode users = body(request(HttpMethod.GET, "/system/allUsers", login(userId), null));
            assertTrue(users.isArray());
            assertEquals(4, users.size());
            for (JsonNode user : users) {
                assertFalse(user.has("password"), "Password field must not be serialized");
                assertTrue(user.hasNonNull("userId"));
                assertTrue(user.hasNonNull("nickName"));
                assertEquals(DEPT, user.path("deptId").asLong());
                assertTrue(user.hasNonNull("role"));
            }
        }
        assertEquals(4, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user WHERE password=?", Integer.class, PASSWORD));
        login(USER);
    }

    @Test
    void deletedUsersCannotLoginOrReuseTokens() throws Exception {
        String token = login(USER);
        assertEquals(HttpStatus.OK, request(HttpMethod.GET, "/biz/tasks", token, null).getStatusCode());
        ResponseEntity<String> deleted = request(HttpMethod.POST, "/system/users/delete/" + USER, login(ADMIN), null);
        assertEquals(HttpStatus.OK, deleted.getStatusCode());
        assertTrue(deleted.getBody().contains("删除成功"));
        assertEquals(1, jdbc.queryForObject("SELECT is_delete FROM sys_user WHERE user_id=?", Integer.class, USER));
        for (String path : new String[]{"/system/allUsers", "/biz/tasks", "/performance"}) {
            assertEquals(HttpStatus.UNAUTHORIZED, request(HttpMethod.GET, path, token, null).getStatusCode());
        }
        JsonNode rejected = body(request(HttpMethod.POST, "/system/login", null,
                Map.of("user_id", USER, "password", PASSWORD)));
        assertEquals(500, rejected.path("code").asInt());
        assertEquals("用户不存在", rejected.path("message").asText());
        assertFalse(rejected.has("token"));
        login(ADMIN);
        login(LEADER); // 保持历史创建账号 status=1 的登录兼容。
        jdbc.update("UPDATE sys_user SET password=NULL WHERE user_id=?", AUDITOR);
        JsonNode missingPassword = body(request(HttpMethod.POST, "/system/login", null, Map.of("user_id", AUDITOR)));
        assertEquals("密码错误", missingPassword.path("message").asText());
        assertFalse(missingPassword.has("token"));
    }

    private void seedTasks() {
        jdbc.update("INSERT INTO biz_project (project_id, project_name, leader_id) VALUES (1, 'Review project', ?)", ADMIN);
        seedTask(930001L, 0L, 2);
        seedTask(930002L, 930001L, 3);
    }

    private void seedTask(long taskId, long parentId, int level) {
        jdbc.update("INSERT INTO biz_task (task_id, project_id, parent_id, phase, task_name, level, leader_id, "
                        + "auditor_id, principal_id, dept_id, data_type, target_value, current_value, progress, status, is_delete) "
                        + "VALUES (?, 1, ?, 2026, ?, ?, ?, ?, ?, ?, '1', 10, 0, 0, '1', 0)",
                taskId, parentId, "Review task " + taskId, level, USER, AUDITOR, LEADER, DEPT);
    }

    private void assertSuccess(ResponseEntity<String> response, String text) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains(text), response.getBody());
    }

    private void assertDenied(ResponseEntity<String> response) throws Exception {
        JsonNode error = body(response);
        assertEquals(500, error.path("code").asInt());
        assertTrue(error.path("message").asText().contains("仅限管理员访问"));
    }

    @Test
    void taskManagementRequiresCurrentAdminRole() throws Exception {
        seedTasks();
        String admin = login(ADMIN);
        Map<String, Object> task = json.convertValue(body(request(HttpMethod.GET, "/biz/tasks/930002", admin, null)), Map.class);
        task = new LinkedHashMap<>(task);
        task.keySet().removeAll(List.of("isDelete", "createTime", "updateTime"));
        task.put("taskName", "Review added task");
        int count = jdbc.queryForObject("SELECT COUNT(*) FROM biz_task", Integer.class);
        Map<String, Object> original = jdbc.queryForMap("SELECT * FROM biz_task WHERE task_id=930002");
        for (String path : new String[]{"/biz/tasks/manage/add", "/biz/tasks/manage/update"}) {
            assertEquals(HttpStatus.UNAUTHORIZED, request(HttpMethod.POST, path, null, task).getStatusCode());
            for (long userId : new long[]{USER, LEADER}) {
                assertDenied(request(HttpMethod.POST, path, login(userId), task));
                assertEquals(count, jdbc.queryForObject("SELECT COUNT(*) FROM biz_task", Integer.class));
                assertEquals(original, jdbc.queryForMap("SELECT * FROM biz_task WHERE task_id=930002"));
            }
        }
        assertSuccess(request(HttpMethod.POST, "/biz/tasks/manage/add", admin, task), "添加成功");
        long added = jdbc.queryForObject("SELECT task_id FROM biz_task WHERE task_name='Review added task'", Long.class);
        Map<String, Object> created = jdbc.queryForMap("SELECT * FROM biz_task WHERE task_id=?", added);
        assertEquals(DEPT, ((Number) created.get("dept_id")).longValue());
        task.put("taskId", added);
        task.put("taskName", "Review updated task");
        assertSuccess(request(HttpMethod.POST, "/biz/tasks/manage/update", admin, task), "更新成功");
        Map<String, Object> updated = jdbc.queryForMap("SELECT * FROM biz_task WHERE task_id=?", added);
        assertEquals("Review updated task", updated.get("task_name"));
        assertEquals(created.get("is_delete"), updated.get("is_delete"));
        assertEquals(created.get("create_time"), updated.get("create_time"));
        assertEquals("Review updated task", body(request(HttpMethod.GET, "/biz/tasks/" + added, admin, null)).path("taskName").asText());
        jdbc.update("UPDATE sys_user SET role='1' WHERE user_id=?", ADMIN);
        Map<String, Object> beforeDenied = jdbc.queryForMap("SELECT * FROM biz_task WHERE task_id=?", added);
        task.put("taskName", "Must not save");
        assertDenied(request(HttpMethod.POST, "/biz/tasks/manage/update", admin, task));
        assertDenied(request(HttpMethod.POST, "/biz/tasks/manage/add", admin, task));
        assertEquals(count + 1, jdbc.queryForObject("SELECT COUNT(*) FROM biz_task", Integer.class));
        assertEquals(beforeDenied, jdbc.queryForMap("SELECT * FROM biz_task WHERE task_id=?", added));
    }

    private void seedSubmissionFlow() {
        seedTasks();
        jdbc.update("INSERT INTO sys_file (file_id, file_name, file_path, file_url, file_suffix, upload_by) "
                + "VALUES (940001, 'review.pdf', 'review.pdf', '/uploads/review.pdf', 'pdf', ?)", USER);
        jdbc.update("INSERT INTO biz_performance (perf_id, project_id, perf_code, perf_name, target_value, data_type, "
                + "dept_id, principal_id, auditor_id, leader_id) VALUES (950001, 1, '1.1.review', 'Review performance', 10, '1', ?, ?, ?, ?)",
                DEPT, LEADER, AUDITOR, USER);
        jdbc.update("INSERT INTO biz_performance_year (year_id, perf_id, year, target_value) VALUES (950002, 950001, 2026, 10)");
        jdbc.update("INSERT INTO rel_task_performance (task_id, perf_id, year_id) VALUES (930002, 950001, 950002)");
    }

    private Map<String, Object> submission(String value) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("task_id", 930002L);
        payload.put("file_id", 940001L);
        payload.put("reported_value", value == null ? null : new BigDecimal(value));
        payload.put("data_type", "1");
        payload.put("comment", "Review submission");
        return payload;
    }

    private void assertDecimal(String expected, String sql, Object... args) {
        BigDecimal actual = jdbc.queryForObject(sql, BigDecimal.class, args);
        assertNotNull(actual);
        assertEquals(0, new BigDecimal(expected).compareTo(actual), sql + ": " + actual);
    }

    private void assertTaskAndPerformance(String actual, int progress, String status) {
        assertDecimal(actual, "SELECT current_value FROM biz_task WHERE task_id=930002");
        assertEquals(progress, jdbc.queryForObject("SELECT progress FROM biz_task WHERE task_id=930002", Integer.class));
        assertEquals(status, jdbc.queryForObject("SELECT status FROM biz_task WHERE task_id=930002", String.class));
        assertDecimal(actual, "SELECT current_value FROM biz_performance WHERE perf_id=950001");
        assertDecimal(actual, "SELECT actual_value FROM biz_performance_year WHERE year_id=950002");
    }

    private long newestSubmission() {
        return jdbc.queryForObject("SELECT MAX(sub_id) FROM biz_material_submission WHERE task_id=930002", Long.class);
    }

    private void review(long subId, boolean pass, String token) throws Exception {
        ResponseEntity<String> response = request(HttpMethod.POST, "/biz/audit", token,
                Map.of("sub_id", subId, "is_pass", pass, "title", "Review decision", "content", "Review decision"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().contains("\"code\""), response.getBody());
        if (!pass) assertTrue(response.getBody().contains("已退回"));
    }

    @Test
    void directSubmissionAndResubmissionStoreActualValuesThroughApproval() throws Exception {
        seedSubmissionFlow();
        String user = login(USER);
        String auditor = login(AUDITOR);
        String leader = login(LEADER);
        String admin = login(ADMIN);
        assertSuccess(request(HttpMethod.POST, "/biz/sub", user, submission("3")), "提交成功");
        long first = newestSubmission();
        assertDecimal("3", "SELECT reported_value FROM biz_material_submission WHERE sub_id=?", first);
        assertTaskAndPerformance("3", 30, "2");
        JsonNode task = body(request(HttpMethod.GET, "/biz/tasks/930002", user, null));
        assertEquals(0, new BigDecimal("3").compareTo(task.path("currentValue").decimalValue()));
        assertEquals(30, task.path("progress").asInt());
        assertEquals(500, body(request(HttpMethod.POST, "/biz/sub", user, submission("9"))).path("code").asInt());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM biz_material_submission", Integer.class));
        assertTaskAndPerformance("3", 30, "2");

        review(first, false, auditor);
        assertTaskAndPerformance("0", 0, "1");
        assertEquals(-10, jdbc.queryForObject("SELECT flow_status FROM biz_material_submission WHERE sub_id=?", Integer.class, first));
        assertEquals(1, jdbc.queryForObject("SELECT is_delete FROM biz_material_submission WHERE sub_id=?", Integer.class, first));
        Map<String, Object> resub = submission("4");
        resub.remove("task_id");
        resub.put("sub_id", first);
        assertSuccess(request(HttpMethod.POST, "/biz/resub", user, resub), "已重新提交");
        long second = newestSubmission();
        assertNotEquals(first, second);
        assertDecimal("3", "SELECT reported_value FROM biz_material_submission WHERE sub_id=?", first);
        assertDecimal("4", "SELECT reported_value FROM biz_material_submission WHERE sub_id=?", second);
        assertTaskAndPerformance("4", 40, "2");
        review(second, true, auditor);
        assertEquals(20, jdbc.queryForObject("SELECT flow_status FROM biz_material_submission WHERE sub_id=?", Integer.class, second));
        review(second, true, leader);
        assertEquals(30, jdbc.queryForObject("SELECT flow_status FROM biz_material_submission WHERE sub_id=?", Integer.class, second));
        review(second, true, admin);
        assertEquals(40, jdbc.queryForObject("SELECT flow_status FROM biz_material_submission WHERE sub_id=?", Integer.class, second));
        assertTaskAndPerformance("4", 40, "1");
        assertEquals(6, jdbc.queryForObject("SELECT COUNT(*) FROM biz_audit_log", Integer.class));
        assertEquals(6, jdbc.queryForObject("SELECT COUNT(*) FROM sys_notice", Integer.class));
    }

    @Test
    void invalidDirectValuesAndResubmissionsLeaveDataUnchanged() throws Exception {
        seedSubmissionFlow();
        String user = login(USER);
        for (String value : new String[]{null, "-1"}) {
            JsonNode error = body(request(HttpMethod.POST, "/biz/sub", user, submission(value)));
            assertEquals(500, error.path("code").asInt());
            assertTrue(error.path("message").asText().contains(value == null ? "填报值不能为空" : "填报值不能小于0"));
        }
        jdbc.update("UPDATE biz_task SET data_type='2', target_value=80 WHERE task_id=930002");
        JsonNode tooLarge = body(request(HttpMethod.POST, "/biz/sub", user, submission("100.01")));
        assertEquals(500, tooLarge.path("code").asInt());
        assertTrue(tooLarge.path("message").asText().contains("百分比任务填报值不能超过100"));
        for (String table : List.of("biz_material_submission", "biz_audit_snapshot", "biz_audit_log", "sys_notice")) {
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class));
        }
        assertTaskAndPerformance("0", 0, "1");
        assertSuccess(request(HttpMethod.POST, "/biz/sub", user, submission("80")), "提交成功");
        assertTaskAndPerformance("80", 100, "2");
        long subId = newestSubmission();
        review(subId, false, login(AUDITOR));
        Map<String, Object> old = jdbc.queryForMap("SELECT * FROM biz_material_submission WHERE sub_id=?", subId);
        int snapshots = jdbc.queryForObject("SELECT COUNT(*) FROM biz_audit_snapshot", Integer.class);
        for (String value : new String[]{null, "-1", "100.01"}) {
            Map<String, Object> resub = submission(value);
            resub.remove("task_id");
            resub.put("sub_id", subId);
            assertEquals(500, body(request(HttpMethod.POST, "/biz/resub", user, resub)).path("code").asInt());
            assertEquals(old, jdbc.queryForMap("SELECT * FROM biz_material_submission WHERE sub_id=?", subId));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM biz_material_submission", Integer.class));
            assertEquals(snapshots, jdbc.queryForObject("SELECT COUNT(*) FROM biz_audit_snapshot", Integer.class));
            assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM biz_audit_log", Integer.class));
            assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM sys_notice", Integer.class));
            assertTaskAndPerformance("0", 0, "1");
        }
    }

    @Test
    void directValuesKeepDecimalZeroAndProgressCapSemantics() throws Exception {
        String[][] cases = {{"10", "0", "0"}, {"10", "3.5", "35"}, {"10", "12.5", "100"},
                {"0", "3", "0"}, {null, "3", "0"}};
        for (String[] example : cases) {
            seed();
            seedSubmissionFlow();
            jdbc.update("UPDATE biz_task SET target_value=? WHERE task_id=930002", example[0]);
            assertSuccess(request(HttpMethod.POST, "/biz/sub", login(USER), submission(example[1])), "提交成功");
            assertDecimal(example[1], "SELECT reported_value FROM biz_material_submission WHERE sub_id=?", newestSubmission());
            assertTaskAndPerformance(example[1], Integer.parseInt(example[2]), "2");
        }
    }

    @Test
    void level4SubmissionAggregationRemainsCompatible() throws Exception {
        seedSubmissionFlow();
        for (long id : new long[]{960001L, 960002L}) {
            jdbc.update("INSERT INTO biz_level4_task (task_id, parent_id, phase, task_name, leader_id, dept_id, "
                    + "data_type, target_value, current_value, progress, status) VALUES (?, 930002, 2026, ?, ?, ?, '1', 5, 0, 0, '1')",
                    id, "Review level4 " + id, USER, DEPT);
        }
        String user = login(USER);
        Map<String, Object> payload = Map.of("third_task_id", 930002L, "file_id", 940001L, "comment", "Review level4",
                "sub_list", List.of(Map.of("task_id", 960001L, "reported_value", 2, "data_type", "1"),
                        Map.of("task_id", 960002L, "reported_value", 3, "data_type", "1")));
        assertSuccess(request(HttpMethod.POST, "/biz/sub", user, payload), "提交成功");
        assertTaskAndPerformance("5", 50, "2");
        assertDecimal("2", "SELECT current_value FROM biz_level4_task WHERE task_id=960001");
        assertDecimal("3", "SELECT current_value FROM biz_level4_task WHERE task_id=960002");
        long first = newestSubmission();
        review(first, false, login(AUDITOR));
        assertTaskAndPerformance("0", 0, "1");
        assertDecimal("0", "SELECT current_value FROM biz_level4_task WHERE task_id=960001");
        assertDecimal("0", "SELECT current_value FROM biz_level4_task WHERE task_id=960002");
        Map<String, Object> resub = new LinkedHashMap<>(payload);
        resub.remove("third_task_id");
        resub.put("sub_id", first);
        assertSuccess(request(HttpMethod.POST, "/biz/resub", user, resub), "已重新提交");
        assertNotEquals(first, newestSubmission());
        assertTaskAndPerformance("5", 50, "2");
    }

    private void seedManualPerformance(String dataType) {
        seedSubmissionFlow();
        jdbc.update("UPDATE biz_performance SET perf_code='2.review', data_type=? WHERE perf_id=950001", dataType);
        jdbc.update("UPDATE biz_performance_year SET data_type=? WHERE year_id=950002", dataType);
        jdbc.update("INSERT INTO biz_performance_year (year_id, perf_id, year, target_value, data_type) "
                + "VALUES (950003, 950001, 2027, 10, ?)", dataType);
    }

    private long submitPerformance(int year, String value, String token) {
        assertSuccess(request(HttpMethod.POST, "/performance/submit?pref_id=950001&actual_value=" + value
                + "&year=" + year + "&comment=Review", token, null), "提交");
        return jdbc.queryForObject("SELECT MAX(sub_id) FROM biz_performance_submission WHERE perf_id=950001 AND year=?", Long.class, year);
    }

    private ResponseEntity<String> reviewPerformance(long subId, boolean pass, String token) {
        return request(HttpMethod.POST, "/performance/audit", token,
                Map.of("sub_id", subId, "is_pass", pass, "title", "Review decision"));
    }

    @Test
    void performanceRollbackPreservesOtherYearsAndCurrentTargets() throws Exception {
        for (String dataType : List.of("1", "2")) {
            for (boolean withdraw : new boolean[]{true, false}) {
                seed();
                seedManualPerformance(dataType);
                String user = login(USER);
                long first = submitPerformance(2026, "3", user);
                long second = submitPerformance(2027, "5", user);
                assertDecimal("1".equals(dataType) ? "8" : "5", "SELECT current_value FROM biz_performance WHERE perf_id=950001");
                Map<String, Object> otherYear = jdbc.queryForMap("SELECT * FROM biz_performance_year WHERE year_id=950003");
                Map<String, Object> otherSubmission = jdbc.queryForMap("SELECT * FROM biz_performance_submission WHERE sub_id=?", second);
                // 填报后另行调整的目标，不应被撤回实际值的动作覆盖。
                jdbc.update("UPDATE biz_performance_year SET target_value=20 WHERE year_id=950002");
                if (withdraw) {
                    assertSuccess(request(HttpMethod.POST, "/performance/audit/withdraw/" + first, user, null), "已撤回");
                } else {
                    assertSuccess(reviewPerformance(first, false, login(AUDITOR)), "已退回");
                }
                assertDecimal("0", "SELECT actual_value FROM biz_performance_year WHERE year_id=950002");
                assertDecimal("5", "SELECT current_value FROM biz_performance WHERE perf_id=950001");
                assertDecimal("20", "SELECT target_value FROM biz_performance_year WHERE year_id=950002");
                assertEquals(otherYear, jdbc.queryForMap("SELECT * FROM biz_performance_year WHERE year_id=950003"));
                assertEquals(otherSubmission, jdbc.queryForMap("SELECT * FROM biz_performance_submission WHERE sub_id=?", second));
                assertEquals(withdraw ? 0 : -10, jdbc.queryForObject("SELECT flow_status FROM biz_performance_submission WHERE sub_id=?", Integer.class, first));
                assertEquals(3, jdbc.queryForObject("SELECT COUNT(*) FROM biz_performance_audit_log", Integer.class));
            }
        }
    }
}
