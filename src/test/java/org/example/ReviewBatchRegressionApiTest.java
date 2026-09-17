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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.config.TaskManagementConfigUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.sql.ResultSet;
import java.sql.Statement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** 本批修复的真实 HTTP / MySQL 回归；仅由隔离库脚本显式开启。 */
@EnabledIfEnvironmentVariable(named = "SHUANGGAO_REVIEW_TEST", matches = "true")
@Import(ReviewBatchRegressionApiTest.NoAutomaticSchedules.class)
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
    @Autowired private ReviewSubmissionRaceGate submissionRaceGate;

    /** 保留真实调度服务供 HTTP 测试调用，只关闭测试进程的自动定时触发。 */
    @TestConfiguration(proxyBeanMethods = false)
    static class NoAutomaticSchedules {
        @Bean
        ReviewSubmissionRaceGate submissionRaceGate() {
            return new ReviewSubmissionRaceGate();
        }

        @Bean
        static BeanFactoryPostProcessor disableAutomaticSchedules() {
            return beanFactory -> {
                BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
                String name = TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME;
                if (registry.containsBeanDefinition(name)) registry.removeBeanDefinition(name);
            };
        }
    }

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
    void passwordValidationPreservesExistingCredentials() throws Exception {
        String token = login(USER);
        List<Map<String, Object>> invalidBodies = new ArrayList<>();
        invalidBodies.add(new LinkedHashMap<>());
        Map<String, Object> nullBody = new LinkedHashMap<>();
        nullBody.put("new_password", null);
        invalidBodies.add(nullBody);
        for (String invalid : List.of("", "      ", "\t\n    ", "12345")) {
            invalidBodies.add(Map.of("new_password", invalid));
        }
        for (Map<String, Object> invalid : invalidBodies) {
            ResponseEntity<String> response = request(HttpMethod.POST, "/system/password", token, invalid);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals(400, json.readTree(response.getBody()).get("code").asInt());
            assertEquals(PASSWORD, jdbc.queryForObject("SELECT password FROM sys_user WHERE user_id=?", String.class, USER));
            login(USER);
        }
        for (String valid : List.of("abc123", " a123 ")) {
            ResponseEntity<String> response = request(HttpMethod.POST, "/system/password", token,
                    Map.of("new_password", valid));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(valid, jdbc.queryForObject("SELECT password FROM sys_user WHERE user_id=?", String.class, USER));
            assertTrue(body(request(HttpMethod.POST, "/system/login", null,
                    Map.of("user_id", USER, "password", valid))).hasNonNull("token"));
        }
        assertFalse(body(request(HttpMethod.POST, "/system/login", null,
                Map.of("user_id", USER, "password", PASSWORD))).hasNonNull("token"));
    }

    @Test
    void accountStatusPreservesExplicitAndOmittedValues() throws Exception {
        String admin = login(ADMIN);
        long id = 915100L;
        Map<String, Object> user = new LinkedHashMap<>(Map.of(
                "userId", id, "deptId", DEPT, "userName", "status_fixture",
                "nickName", "Status fixture", "password", PASSWORD, "role", "1"));
        assertEquals(HttpStatus.OK, request(HttpMethod.POST, "/system/users/add", admin, user).getStatusCode());
        assertEquals("1", jdbc.queryForObject("SELECT status FROM sys_user WHERE user_id=?", String.class, id));
        for (String status : List.of("0", "1", "0")) {
            user.put("status", status);
            assertEquals(HttpStatus.OK, request(HttpMethod.POST, "/system/users/update", admin, user).getStatusCode());
            assertEquals(status, jdbc.queryForObject("SELECT status FROM sys_user WHERE user_id=?", String.class, id));
            user.remove("status");
            request(HttpMethod.POST, "/system/users/update", admin, user);
            assertEquals(status, jdbc.queryForObject("SELECT status FROM sys_user WHERE user_id=?", String.class, id));
            user.put("status", null);
            request(HttpMethod.POST, "/system/users/update", admin, user);
            assertEquals(status, jdbc.queryForObject("SELECT status FROM sys_user WHERE user_id=?", String.class, id));
        }
        for (String invalid : List.of("", " ", "2", "normal")) {
            user.put("status", invalid);
            user.put("nickName", "Must not be saved");
            ResponseEntity<String> response = request(HttpMethod.POST, "/system/users/update", admin, user);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("0", jdbc.queryForObject("SELECT status FROM sys_user WHERE user_id=?", String.class, id));
            assertEquals("Status fixture", jdbc.queryForObject("SELECT nick_name FROM sys_user WHERE user_id=?", String.class, id));
            user.put("userId", id + 1);
            user.put("userName", "invalid_status_fixture");
            assertEquals(HttpStatus.BAD_REQUEST, request(HttpMethod.POST, "/system/users/add", admin, user).getStatusCode());
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user WHERE user_id=?", Integer.class, id + 1));
            user.put("userId", id);
            user.put("userName", "status_fixture");
        }
        for (String status : List.of("0", "1")) {
            long addedId = id + 2 + Integer.parseInt(status);
            user.put("userId", addedId);
            user.put("userName", "explicit_status_" + status);
            user.put("status", status);
            assertEquals(HttpStatus.OK, request(HttpMethod.POST, "/system/users/add", admin, user).getStatusCode());
            assertEquals(status, jdbc.queryForObject("SELECT status FROM sys_user WHERE user_id=?", String.class, addedId));
            login(addedId); // 已确认兼容范围：本次不改变状态与登录的关系。
        }
    }

    @Test
    void achievementQuantitiesRejectInvalidWithoutWrites() throws Exception {
        seedUser(990000L + DEPT, "1");
        String token = login(990000L + DEPT);
        Map<String, Object> payload = achievementQuantityPayload();
        assertSuccess(request(HttpMethod.POST, "/achievement/add", token, payload), "添加成功");
        long id = jdbc.queryForObject("SELECT MAX(ach_id) FROM biz_achievement", Long.class);
        Map<String, List<Map<String, Object>>> before = achievementFlowState();
        for (String field : achievementQuantityFields()) {
            for (Object invalid : List.of(new BigDecimal("1.5"), "1.5", -1, "-1", 2147483648L,
                    "2147483648", "abc", "", true, List.of(2), Map.of("count", 2))) {
                for (String path : List.of("/achievement/add", "/achievement/update/" + id)) {
                    Map<String, Object> changed = new LinkedHashMap<>(payload);
                    changed.put(field, invalid);
                    ResponseEntity<String> response = request(HttpMethod.POST, path, token, changed);
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), field + "=" + invalid);
                    JsonNode error = json.readTree(response.getBody());
                    assertEquals(400, error.path("code").asInt());
                    assertTrue(error.path("message").asText().contains(field));
                    assertTrue(error.path("message").asText().contains("整数"));
                    assertEquals(before, achievementFlowState(), "Rejected quantity must not change business data");
                }
            }
        }
    }

    @Test
    void achievementQuantitiesPreserveCompatibleInputs() throws Exception {
        seedUser(990000L + DEPT, "1");
        String token = login(990000L + DEPT);
        Object[] values = {2, "2", 0, "0", null, new BigDecimal("1.0"), "1.0", 1998, Integer.MAX_VALUE};
        for (Object value : values) {
            Map<String, Object> payload = achievementQuantityPayload();
            for (String field : achievementQuantityFields()) payload.put(field, value);
            assertSuccess(request(HttpMethod.POST, "/achievement/add", token, payload), "添加成功");
            long id = jdbc.queryForObject("SELECT MAX(ach_id) FROM biz_achievement", Long.class);
            JsonNode added = body(request(HttpMethod.GET, "/achievement/" + id, token, null));
            assertSuccess(request(HttpMethod.POST, "/achievement/update/" + id, token, payload), "修改成功");
            JsonNode updated = body(request(HttpMethod.GET, "/achievement/" + id, token, null));
            for (String field : achievementQuantityFields()) {
                if (value == null) {
                    assertTrue(added.path(field).isNull());
                    assertTrue(updated.path(field).isNull());
                } else {
                    int expected = new BigDecimal(value.toString()).intValueExact();
                    assertEquals(expected, added.path(field).asInt());
                    assertEquals(expected, updated.path(field).asInt());
                }
            }
        }
        Map<String, Object> missing = achievementQuantityPayload();
        assertSuccess(request(HttpMethod.POST, "/achievement/add", token, missing), "添加成功");
        long id = jdbc.queryForObject("SELECT MAX(ach_id) FROM biz_achievement", Long.class);
        assertSuccess(request(HttpMethod.POST, "/achievement/update/" + id, token, missing), "修改成功");
        JsonNode saved = body(request(HttpMethod.GET, "/achievement/" + id, token, null));
        for (String field : achievementQuantityFields()) assertTrue(saved.path(field).isNull());
    }

    private List<String> achievementQuantityFields() {
        return List.of("teDengJiang", "yiDengJiang", "erDengJiang", "sanDengJiang", "jinJiang",
                "yinJiang", "tongJiang", "youShengJiang", "budDengDengCi");
    }

    private Map<String, Object> achievementQuantityPayload() {
        return new LinkedHashMap<>(Map.of("category", 1, "level", "省级", "achName", "Quantity regression",
                "gotTime", 1767225600000L, "department", "Synthetic department", "isCompetition", 1,
                "comment", "Synthetic quantity regression"));
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

    private Map<String, Object> taskUpdatePayload(long taskId, String token) throws Exception {
        Map<String, Object> task = new LinkedHashMap<>(json.convertValue(
                body(request(HttpMethod.GET, "/biz/tasks/" + taskId, token, null)), Map.class));
        task.keySet().removeAll(List.of("isDelete", "createTime", "updateTime"));
        return task;
    }

    @Test
    void taskHierarchyRejectsCyclesAndKeepsValidEdits() throws Exception {
        seedTasks();
        seedTask(930000L, 0L, 1);
        seedTask(930010L, 0L, 2);
        seedTask(930011L, 930002L, 4);
        seedTask(930012L, 0L, 2);
        seedTask(930013L, 930014L, 2);
        seedTask(930014L, 930013L, 1);
        jdbc.update("UPDATE biz_task SET is_delete=1 WHERE task_id=930012");
        String admin = login(ADMIN);
        Map<String, Object> original = taskUpdatePayload(930002L, admin);
        for (Long parentId : new Long[]{930002L, 930011L, 939999L, 930012L, 930013L, 930000L, 0L, null}) {
            Map<String, Object> changed = new LinkedHashMap<>(original);
            changed.put("parentId", parentId);
            changed.put("taskName", "Must not save");
            Map<String, List<Map<String, Object>>> before = taskFlowState();
            JsonNode error = body(request(HttpMethod.POST, "/biz/tasks/manage/update", admin, changed));
            assertEquals(500, error.path("code").asInt(), "parent=" + parentId);
            assertEquals(before, taskFlowState());
        }
        Map<String, Object> changed = new LinkedHashMap<>(original);
        changed.put("parentId", 930000L);
        changed.put("level", 2);
        assertEquals(500, body(request(HttpMethod.POST, "/biz/tasks/manage/update", admin, changed)).path("code").asInt());
        assertEquals(3, jdbc.queryForObject("SELECT level FROM biz_task WHERE task_id=930002", Integer.class));

        changed = new LinkedHashMap<>(original);
        changed.put("parentId", 930010L);
        changed.put("taskName", "Valid moved task");
        assertSuccess(request(HttpMethod.POST, "/biz/tasks/manage/update", admin, changed), "更新成功");
        assertEquals(930010L, jdbc.queryForObject("SELECT parent_id FROM biz_task WHERE task_id=930002", Long.class));
        assertEquals("Valid moved task", body(request(HttpMethod.GET, "/biz/tasks/930002", admin, null)).path("taskName").asText());
        changed.put("taskName", "Valid renamed task");
        assertSuccess(request(HttpMethod.POST, "/biz/tasks/manage/update", admin, changed), "更新成功");

        Map<String, Object> root = taskUpdatePayload(930000L, admin);
        root.put("parentId", null);
        assertSuccess(request(HttpMethod.POST, "/biz/tasks/manage/update", admin, root), "更新成功");
        Map<String, Object> legacyRoot = taskUpdatePayload(930001L, admin);
        legacyRoot.put("taskName", "Legacy root renamed");
        assertSuccess(request(HttpMethod.POST, "/biz/tasks/manage/update", admin, legacyRoot), "更新成功");
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

    private Map<String, List<Map<String, Object>>> taskFlowState() {
        Map<String, List<Map<String, Object>>> state = new LinkedHashMap<>();
        for (String table : List.of("biz_task", "biz_level4_task", "biz_material_submission", "biz_audit_snapshot",
                "biz_audit_log", "sys_notice", "biz_performance", "biz_performance_year")) {
            state.put(table, jdbc.queryForList("SELECT * FROM " + table + " ORDER BY 1"));
        }
        return state;
    }

    @Test
    void taskSubmissionRequiresExistingReviewerBeforeAnyWrite() throws Exception {
        seedSubmissionFlow();
        for (long id : new long[]{960001L, 960002L}) {
            jdbc.update("INSERT INTO biz_level4_task (task_id,parent_id,phase,task_name,leader_id,dept_id,data_type,target_value,current_value,progress,status) "
                    + "VALUES (?,930002,2026,'Reviewer fixture',?,?,'1',5,0,0,'1')", id, USER, DEPT);
        }
        seedUser(919999L, "1");
        jdbc.update("UPDATE sys_user SET is_delete=1 WHERE user_id=919999");
        String user = login(USER);
        for (Long reviewer : new Long[]{null, 919998L, 919999L}) {
            jdbc.update("UPDATE biz_task SET auditor_id=? WHERE task_id=930002", reviewer);
            Map<String, List<Map<String, Object>>> before = taskFlowState();
            for (Map<String, Object> payload : List.of(submission("3"), level4Submission(2, 3))) {
                JsonNode error = body(request(HttpMethod.POST, "/biz/sub", user, payload));
                assertEquals(500, error.path("code").asInt());
                assertTrue(error.path("message").asText().contains("审核人"));
                assertEquals(before, taskFlowState());
            }
        }
        jdbc.update("UPDATE biz_task SET auditor_id=? WHERE task_id=930002", AUDITOR);
        assertSuccess(request(HttpMethod.POST, "/biz/sub", user, level4Submission(2, 3)), "提交成功");
        long subId = newestSubmission();
        assertEquals(AUDITOR, jdbc.queryForObject("SELECT current_handler_id FROM biz_material_submission WHERE sub_id=?", Long.class, subId));
        review(subId, false, login(AUDITOR));
        jdbc.update("UPDATE biz_task SET auditor_id=NULL WHERE task_id=930002");
        Map<String, List<Map<String, Object>>> beforeResub = taskFlowState();
        for (Map<String, Object> payload : List.of(submission("3"), level4Submission(2, 3))) {
            payload = new LinkedHashMap<>(payload);
            payload.remove("task_id");
            payload.remove("third_task_id");
            payload.put("sub_id", subId);
            assertEquals(500, body(request(HttpMethod.POST, "/biz/resub", user, payload)).path("code").asInt());
            assertEquals(beforeResub, taskFlowState());
        }
        jdbc.update("UPDATE biz_task SET auditor_id=? WHERE task_id=930002", AUDITOR);
        assertSuccess(request(HttpMethod.POST, "/biz/sub", user, submission("3")), "提交成功");
        assertTaskAndPerformance("3", 30, "2");
    }

    @Test
    void archivedTasksCannotBeWithdrawn() throws Exception {
        seedSubmissionFlow();
        String user = login(USER);
        assertSuccess(request(HttpMethod.POST, "/biz/sub", user, submission("10")), "提交成功");
        long subId = newestSubmission();
        review(subId, true, login(AUDITOR));
        review(subId, true, login(LEADER));
        review(subId, true, login(ADMIN));
        assertTaskAndPerformance("10", 100, "3");
        Map<String, List<Map<String, Object>>> before = taskFlowState();
        JsonNode error = body(request(HttpMethod.POST, "/biz/drawback/930002", user, null));
        assertEquals(500, error.path("code").asInt());
        assertTrue(error.path("message").asText().contains("当前状态不可撤回"));
        assertEquals(before, taskFlowState());
    }

    @Test
    void activeTaskWithdrawalRestoresValuesAtEveryReviewStage() throws Exception {
        for (int status : new int[]{10, 20, 30}) {
            seed();
            seedSubmissionFlow();
            String user = login(USER);
            assertSuccess(request(HttpMethod.POST, "/biz/sub", user, submission("3")), "提交成功");
            long subId = newestSubmission();
            if (status >= 20) review(subId, true, login(AUDITOR));
            if (status >= 30) review(subId, true, login(LEADER));
            Map<String, List<Map<String, Object>>> before = taskFlowState();
            assertEquals(500, body(request(HttpMethod.POST, "/biz/drawback/930002", login(LEADER), null)).path("code").asInt());
            assertEquals(before, taskFlowState());
            assertSuccess(request(HttpMethod.POST, "/biz/drawback/930002", user, null), "已撤回提交");
            assertTaskAndPerformance("0", 0, "1");
            assertEquals(1, jdbc.queryForObject("SELECT is_delete FROM biz_material_submission WHERE sub_id=?", Integer.class, subId));
            assertEquals(-status, jdbc.queryForObject("SELECT post_status FROM biz_audit_log WHERE sub_id=? ORDER BY log_id DESC LIMIT 1", Integer.class, subId));
            before = taskFlowState();
            assertEquals(500, body(request(HttpMethod.POST, "/biz/drawback/930002", user, null)).path("code").asInt());
            assertEquals(before, taskFlowState());
            assertSuccess(request(HttpMethod.POST, "/biz/sub", user, submission("4")), "提交成功");
            assertNotEquals(subId, newestSubmission());
            assertTaskAndPerformance("4", 40, "2");
        }
    }

    @Test
    void taskWithdrawalRollsBackOnDatabaseFailure() throws Exception {
        seedSubmissionFlow();
        String user = login(USER);
        assertSuccess(request(HttpMethod.POST, "/biz/sub", user, submission("3")), "提交成功");
        Map<String, List<Map<String, Object>>> before = taskFlowState();
        // 在隔离库内让绩效写入失败，验证此前审核单、日志和任务更新也被回滚。
        jdbc.execute("CREATE TRIGGER review_fail_performance BEFORE UPDATE ON biz_performance FOR EACH ROW "
                + "SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='review rollback failure'");
        try {
            JsonNode error = body(request(HttpMethod.POST, "/biz/drawback/930002", user, null));
            assertEquals(500, error.path("code").asInt());
            assertTrue(error.path("message").asText().contains("review rollback failure"));
        } finally {
            jdbc.execute("DROP TRIGGER review_fail_performance");
        }
        assertEquals(before, taskFlowState());
        assertSuccess(request(HttpMethod.POST, "/biz/drawback/930002", user, null), "已撤回提交");
        assertTaskAndPerformance("0", 0, "1");
    }

    private Map<String, List<Map<String, Object>>> performanceFlowState() {
        Map<String, List<Map<String, Object>>> state = new LinkedHashMap<>();
        for (String table : List.of("biz_performance", "biz_performance_year", "biz_performance_submission",
                "biz_performance_audit_snapshot", "biz_performance_audit_log", "sys_notice")) {
            state.put(table, jdbc.queryForList("SELECT * FROM " + table + " ORDER BY 1"));
        }
        return state;
    }

    private void assertPerformanceAuditDenied(long subId, String token) throws Exception {
        Map<String, List<Map<String, Object>>> before = performanceFlowState();
        for (boolean pass : new boolean[]{false, true}) {
            ResponseEntity<String> response = reviewPerformance(subId, pass, token);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().contains("当前状态不可审核"), response.getBody());
            assertEquals(500, body(response).path("code").asInt());
            assertEquals(before, performanceFlowState());
        }
    }

    @Test
    void performanceAuditRejectsArchivedAndReturnedSubmissions() throws Exception {
        seedManualPerformance("1");
        String user = login(USER);
        String auditor = login(AUDITOR);
        String admin = login(ADMIN);
        long archived = submitPerformance(2026, "3", user);
        assertSuccess(reviewPerformance(archived, true, auditor), "已通过专业群审核");
        assertSuccess(reviewPerformance(archived, true, admin), "已完结归档");
        assertEquals(30, jdbc.queryForObject("SELECT flow_status FROM biz_performance_submission WHERE sub_id=?", Integer.class, archived));
        assertPerformanceAuditDenied(archived, admin);
        assertDecimal("3", "SELECT current_value FROM biz_performance WHERE perf_id=950001");

        long returned = submitPerformance(2026, "4", user);
        assertSuccess(reviewPerformance(returned, false, auditor), "已退回");
        long current = submitPerformance(2026, "5", user);
        assertPerformanceAuditDenied(returned, user);
        assertPerformanceAuditDenied(returned, admin);
        assertDecimal("5", "SELECT current_value FROM biz_performance WHERE perf_id=950001");
        assertEquals(10, jdbc.queryForObject("SELECT flow_status FROM biz_performance_submission WHERE sub_id=?", Integer.class, current));

        assertSuccess(request(HttpMethod.POST, "/performance/audit/withdraw/" + current, user, null), "已撤回");
        Map<String, List<Map<String, Object>>> before = performanceFlowState();
        assertEquals(500, body(reviewPerformance(current, false, admin)).path("code").asInt());
        assertEquals(before, performanceFlowState());
    }

    @Test
    void performanceActiveReviewStagesStillAllowRejection() throws Exception {
        for (int status : new int[]{10, 20}) {
            seed();
            seedManualPerformance("1");
            String user = login(USER);
            String auditor = login(AUDITOR);
            long subId = submitPerformance(2026, "3", user);
            if (status == 20) assertSuccess(reviewPerformance(subId, true, auditor), "已通过专业群审核");
            Map<String, List<Map<String, Object>>> before = performanceFlowState();
            assertEquals(500, body(reviewPerformance(subId, false, login(LEADER))).path("code").asInt());
            assertEquals(before, performanceFlowState());
            assertSuccess(reviewPerformance(subId, false, status == 10 ? auditor : login(ADMIN)), "已退回");
            assertEquals(-status, jdbc.queryForObject("SELECT flow_status FROM biz_performance_submission WHERE sub_id=?", Integer.class, subId));
            assertDecimal("0", "SELECT actual_value FROM biz_performance_year WHERE year_id=950002");
            assertDecimal("0", "SELECT current_value FROM biz_performance WHERE perf_id=950001");
        }
    }

    @Test
    void performanceReviewRequiresExplicitDecision() throws Exception {
        for (int status : new int[]{10, 20}) {
            seed();
            seedManualPerformance("1");
            long subId = submitPerformance(2026, "3", login(USER));
            String auditor = login(AUDITOR);
            if (status == 20) assertSuccess(reviewPerformance(subId, true, auditor), "已通过专业群审核");
            String handler = status == 10 ? auditor : login(ADMIN);
            Map<String, List<Map<String, Object>>> before = performanceFlowState();
            Map<String, Object> payload = new LinkedHashMap<>(Map.of("sub_id", subId, "title", "Review decision"));
            for (boolean explicitNull : new boolean[]{false, true}) {
                if (explicitNull) payload.put("is_pass", null);
                ResponseEntity<String> response = request(HttpMethod.POST, "/performance/audit", handler, payload);
                assertEquals(before, performanceFlowState(), "Missing decision must not change performance data");
                assertTrue(response.getBody().contains("审核结果不能为空"), response.getBody());
                assertEquals(500, body(response).path("code").asInt());
            }
            assertSuccess(reviewPerformance(subId, false, handler), "已退回");
            assertDecimal("0", "SELECT actual_value FROM biz_performance_year WHERE year_id=950002");
            assertEquals(-status, jdbc.queryForObject("SELECT flow_status FROM biz_performance_submission WHERE sub_id=?", Integer.class, subId));
        }
    }

    private Map<String, List<Map<String, Object>>> achievementFlowState() {
        Map<String, List<Map<String, Object>>> state = new LinkedHashMap<>();
        for (String table : List.of("biz_achievement", "biz_achievement_submission", "biz_achievement_audit_log", "sys_notice")) {
            state.put(table, jdbc.queryForList("SELECT * FROM " + table + " ORDER BY 1"));
        }
        return state;
    }

    @Test
    void achievementReviewRequiresExplicitDecision() throws Exception {
        seedUser(990000L + DEPT, "1");
        String uploader = login(990000L + DEPT);
        String admin = login(ADMIN);
        for (boolean pass : new boolean[]{true, false}) {
            assertSuccess(request(HttpMethod.POST, "/achievement/add", uploader,
                    Map.of("category", 1, "level", "省级", "achName", "Review achievement", "gotTime", 1767225600000L,
                            "department", "Review organization", "comment", "Review")), "添加成功");
            long subId = jdbc.queryForObject("SELECT MAX(sub_id) FROM biz_achievement_submission", Long.class);
            Map<String, List<Map<String, Object>>> before = achievementFlowState();
            Map<String, Object> payload = new LinkedHashMap<>(Map.of("sub_id", subId, "title", "Review decision"));
            for (boolean explicitNull : new boolean[]{false, true}) {
                if (explicitNull) payload.put("is_pass", null);
                ResponseEntity<String> response = request(HttpMethod.POST, "/achievement/audit", admin, payload);
                assertEquals(before, achievementFlowState(), "Missing decision must not change achievement data");
                assertTrue(response.getBody().contains("审核结果不能为空"), response.getBody());
                assertEquals(500, body(response).path("code").asInt());
            }
            payload.put("is_pass", pass);
            assertSuccess(request(HttpMethod.POST, "/achievement/audit", admin, payload), pass ? "已归档" : "已退回");
            assertEquals(pass ? 30 : -10, jdbc.queryForObject("SELECT flow_status FROM biz_achievement_submission WHERE sub_id=?", Integer.class, subId));
            assertEquals(pass ? 30 : -10, jdbc.queryForObject("SELECT audit_status FROM biz_achievement WHERE ach_id="
                    + "(SELECT ach_id FROM biz_achievement_submission WHERE sub_id=?)", Integer.class, subId));
        }
    }

    private List<ResponseEntity<String>> concurrentRequests(String queryName,
            Callable<ResponseEntity<String>> first, Callable<ResponseEntity<String>> second) throws Exception {
        ReviewSubmissionRaceGate.Race race = new ReviewSubmissionRaceGate.Race(queryName);
        submissionRaceGate.current = race;
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<ResponseEntity<String>> firstResponse = executor.submit(() -> {
                try {
                    return first.call();
                } finally {
                    race.firstFinished.countDown();
                }
            });
            assertTrue(race.firstChecked.await(10, TimeUnit.SECONDS), "First request must reach the active submission check");
            Future<ResponseEntity<String>> secondResponse = executor.submit(second);
            return List.of(firstResponse.get(30, TimeUnit.SECONDS), secondResponse.get(30, TimeUnit.SECONDS));
        } finally {
            race.firstFinished.countDown();
            race.secondChecked.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Concurrent HTTP requests must finish");
            submissionRaceGate.current = null;
        }
    }

    private Map<String, Object> level4Submission(int first, int second) {
        return new LinkedHashMap<>(Map.of("third_task_id", 930002L, "file_id", 940001L, "comment", "Review level4",
                "sub_list", List.of(Map.of("task_id", 960001L, "reported_value", first, "data_type", "1"),
                        Map.of("task_id", 960002L, "reported_value", second, "data_type", "1"))));
    }

    @Test
    void concurrentTaskSubmissionsKeepOneActiveSubmission() throws Exception {
        for (String mode : List.of("direct", "level4", "resubDirect", "resubLevel4", "mixed", "resubMixed")) {
            seed();
            seedSubmissionFlow();
            boolean level4 = List.of("level4", "resubLevel4", "mixed").contains(mode);
            if (level4) {
                for (long id : new long[]{960001L, 960002L}) {
                    jdbc.update("INSERT INTO biz_level4_task (task_id, parent_id, phase, task_name, leader_id, dept_id, "
                                    + "data_type, target_value, current_value, progress, status) VALUES (?, 930002, 2026, ?, ?, ?, '1', 5, 0, 0, '1')",
                            id, "Review level4 " + id, USER, DEPT);
                }
            }
            String user = login(USER);
            String auditor = login(AUDITOR);
            Map<String, Object> first = level4 ? level4Submission(2, 1) : submission("3");
            Map<String, Object> second = level4 && !"mixed".equals(mode) ? level4Submission(4, 5) : submission("9");
            boolean resub = mode.startsWith("resub");
            boolean mixedResub = "resubMixed".equals(mode);
            if (resub) {
                assertSuccess(request(HttpMethod.POST, "/biz/sub", user, first), "提交成功");
                long oldSubId = newestSubmission();
                review(oldSubId, false, auditor);
                for (Map<String, Object> payload : mixedResub ? List.of(second) : List.of(first, second)) {
                    payload.remove("task_id");
                    payload.remove("third_task_id");
                    payload.put("sub_id", oldSubId);
                }
            }
            Map<String, List<Map<String, Object>>> before = taskFlowState();
            String firstPath = resub && !mixedResub ? "/biz/resub" : "/biz/sub";
            String secondPath = resub ? "/biz/resub" : "/biz/sub";
            List<ResponseEntity<String>> responses = concurrentRequests("getActiveAuditByTaskId",
                    () -> request(HttpMethod.POST, firstPath, user, first),
                    () -> request(HttpMethod.POST, secondPath, user, second));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM biz_material_submission WHERE task_id=930002 "
                    + "AND is_delete=0 AND flow_status IN (10,20,30)", Integer.class), mode);
            assertSuccess(responses.get(0), resub && !mixedResub ? "已重新提交" : "提交成功");
            assertTrue(responses.get(1).getBody().contains("正在审核"), responses.get(1).getBody());
            assertEquals(500, body(responses.get(1)).path("code").asInt());
            Map<String, List<Map<String, Object>>> after = taskFlowState();
            for (String table : List.of("biz_material_submission", "biz_audit_log", "sys_notice")) {
                assertEquals(before.get(table).size() + 1, after.get(table).size(), mode + ": " + table);
            }
            assertEquals(before.get("biz_audit_snapshot").size() + (level4 ? 3 : 1), after.get("biz_audit_snapshot").size(), mode);
            assertTaskAndPerformance("3", 30, "2");
            if (level4) {
                assertDecimal("2", "SELECT current_value FROM biz_level4_task WHERE task_id=960001");
                assertDecimal("1", "SELECT current_value FROM biz_level4_task WHERE task_id=960002");
            }
            review(newestSubmission(), false, auditor);
            assertTaskAndPerformance("0", 0, "1");
            if (level4) {
                assertDecimal("0", "SELECT current_value FROM biz_level4_task WHERE task_id=960001");
                assertDecimal("0", "SELECT current_value FROM biz_level4_task WHERE task_id=960002");
            }
        }
    }

    @Test
    void concurrentPerformanceSubmissionsKeepOneActiveSubmission() throws Exception {
        for (String dataType : List.of("1", "2")) {
            seed();
            seedManualPerformance(dataType);
            String user = login(USER);
            List<ResponseEntity<String>> responses = concurrentRequests("getActivePerformanceSubmission",
                    () -> request(HttpMethod.POST, "/performance/submit?pref_id=950001&year=2026&actual_value=3", user, null),
                    () -> request(HttpMethod.POST, "/performance/submit?pref_id=950001&year=2026&actual_value=9", user, null));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM biz_performance_submission "
                    + "WHERE perf_id=950001 AND year=2026 AND is_delete=0 AND flow_status IN (10,20)", Integer.class));
            assertSuccess(responses.get(0), "绩效已提交");
            assertTrue(responses.get(1).getBody().contains("已有正在审核中的绩效填报"), responses.get(1).getBody());
            assertEquals(500, body(responses.get(1)).path("code").asInt());
            for (String table : List.of("biz_performance_submission", "biz_performance_audit_snapshot", "biz_performance_audit_log", "sys_notice")) {
                assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class), table);
            }
            assertDecimal("3", "SELECT actual_value FROM biz_performance_year WHERE year_id=950002");
            assertDecimal("3", "SELECT current_value FROM biz_performance WHERE perf_id=950001");
            long subId = jdbc.queryForObject("SELECT MAX(sub_id) FROM biz_performance_submission", Long.class);
            assertSuccess(reviewPerformance(subId, false, login(AUDITOR)), "已退回");
            assertDecimal("0", "SELECT actual_value FROM biz_performance_year WHERE year_id=950002");
            assertDecimal("0", "SELECT current_value FROM biz_performance WHERE perf_id=950001");
            submitPerformance(2026, "4", user);
        }
    }

    @Test
    void concurrentPerformanceYearsPreserveCombinedValue() throws Exception {
        for (String dataType : List.of("1", "2")) {
            seed();
            seedManualPerformance(dataType);
            String user = login(USER);
            List<ResponseEntity<String>> responses = concurrentRequests("getActivePerformanceSubmission",
                    () -> request(HttpMethod.POST, "/performance/submit?pref_id=950001&year=2026&actual_value=7", user, null),
                    () -> request(HttpMethod.POST, "/performance/submit?pref_id=950001&year=2027&actual_value=5", user, null));
            for (ResponseEntity<String> response : responses) assertSuccess(response, "绩效已提交");
            assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM biz_performance_submission", Integer.class));
            assertDecimal("7", "SELECT actual_value FROM biz_performance_year WHERE year_id=950002");
            assertDecimal("5", "SELECT actual_value FROM biz_performance_year WHERE year_id=950003");
            assertDecimal("1".equals(dataType) ? "12" : "7", "SELECT current_value FROM biz_performance WHERE perf_id=950001");
        }
    }

    @Test
    void failedConcurrentSubmissionRollsBackAndReleasesLock() throws Exception {
        for (boolean performance : new boolean[]{false, true}) {
            seed();
            if (performance) seedManualPerformance("1");
            else seedSubmissionFlow();
            String user = login(USER);
            String table = performance ? "biz_performance_year" : "biz_task";
            String condition = performance ? "NEW.year_id=950002 AND NEW.actual_value=3"
                    : "NEW.task_id=930002 AND NEW.current_value=3";
            jdbc.execute("CREATE TRIGGER review_fail_submission BEFORE UPDATE ON " + table + " FOR EACH ROW BEGIN IF "
                    + condition + " THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='review submission failure'; END IF; END");
            List<ResponseEntity<String>> responses;
            try {
                responses = concurrentRequests(performance ? "getActivePerformanceSubmission" : "getActiveAuditByTaskId",
                        () -> request(HttpMethod.POST, performance
                                ? "/performance/submit?pref_id=950001&year=2026&actual_value=3" : "/biz/sub",
                                user, performance ? null : submission("3")),
                        () -> request(HttpMethod.POST, performance
                                ? "/performance/submit?pref_id=950001&year=2026&actual_value=9" : "/biz/sub",
                                user, performance ? null : submission("9")));
            } finally {
                jdbc.execute("DROP TRIGGER review_fail_submission");
            }
            assertEquals(500, body(responses.get(0)).path("code").asInt());
            assertTrue(responses.get(0).getBody().contains("review submission failure"), responses.get(0).getBody());
            assertSuccess(responses.get(1), performance ? "绩效已提交" : "提交成功");
            List<String> writtenTables = performance
                    ? List.of("biz_performance_submission", "biz_performance_audit_snapshot", "biz_performance_audit_log", "sys_notice")
                    : List.of("biz_material_submission", "biz_audit_snapshot", "biz_audit_log", "sys_notice");
            for (String writtenTable : writtenTables) {
                assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM " + writtenTable, Integer.class), writtenTable);
            }
            assertDecimal("9", "SELECT current_value FROM biz_performance WHERE perf_id=950001");
            assertDecimal("9", "SELECT actual_value FROM biz_performance_year WHERE year_id=950002");
            if (performance) {
                long subId = jdbc.queryForObject("SELECT MAX(sub_id) FROM biz_performance_submission", Long.class);
                assertSuccess(reviewPerformance(subId, false, login(AUDITOR)), "已退回");
            } else {
                assertTaskAndPerformance("9", 90, "2");
                review(newestSubmission(), false, login(AUDITOR));
                assertTaskAndPerformance("0", 0, "1");
            }
            assertDecimal("0", "SELECT current_value FROM biz_performance WHERE perf_id=950001");
            assertDecimal("0", "SELECT actual_value FROM biz_performance_year WHERE year_id=950002");
        }
    }

    private void seedAnnualTasks() {
        seedTasks();
        for (long id = 970001L; id <= 970008L; id++) {
            seedTask(id, 930001L, 3);
        }
        int year = LocalDate.now().getYear();
        jdbc.update("UPDATE biz_task SET phase=?, status='0', comment='Keep task content', current_value=2, progress=20 "
                + "WHERE task_id BETWEEN 970001 AND 970008", year);
        jdbc.update("UPDATE biz_task SET status='2' WHERE task_id=970002");
        jdbc.update("UPDATE biz_task SET status='3' WHERE task_id=970003");
        jdbc.update("UPDATE biz_task SET is_delete=1 WHERE task_id=970004");
        jdbc.update("UPDATE biz_task SET phase=? WHERE task_id=970005", year - 1);
        jdbc.update("UPDATE biz_task SET phase=? WHERE task_id=970006", year + 1);
        jdbc.update("UPDATE biz_task SET status=NULL WHERE task_id=970007");
        jdbc.update("UPDATE biz_task SET is_delete=NULL WHERE task_id=970008");
    }

    @Test
    void annualActivationOnlyStartsPendingTasksInCurrentYear() throws Exception {
        seedAnnualTasks();
        List<Map<String, Object>> before = jdbc.queryForList("SELECT * FROM biz_task ORDER BY task_id");
        String path = "/scheduled/update_task_status";
        assertEquals(HttpStatus.UNAUTHORIZED, request(HttpMethod.POST, path, null, null).getStatusCode());
        for (long id : new long[]{USER, LEADER}) {
            assertEquals(500, body(request(HttpMethod.POST, path, login(id), null)).path("code").asInt());
            assertEquals(before, jdbc.queryForList("SELECT * FROM biz_task ORDER BY task_id"));
        }
        String admin = login(ADMIN);
        assertSuccess(request(HttpMethod.POST, path, admin, null), "任务状态更新完成");
        for (Map<String, Object> old : before) {
            long id = ((Number) old.get("task_id")).longValue();
            Map<String, Object> updated = jdbc.queryForMap("SELECT * FROM biz_task WHERE task_id=?", id);
            if (id == 970001L || id == 970008L) {
                assertEquals("1", updated.get("status"));
                Map<String, Object> oldContent = new LinkedHashMap<>(old);
                oldContent.keySet().removeAll(List.of("status", "update_time"));
                updated.keySet().removeAll(List.of("status", "update_time"));
                assertEquals(oldContent, updated);
            } else {
                assertEquals(old, updated, "Task " + id + " must remain unchanged");
            }
        }
        List<Map<String, Object>> firstResult = jdbc.queryForList("SELECT * FROM biz_task ORDER BY task_id");
        assertSuccess(request(HttpMethod.POST, path, admin, null), "任务状态更新完成");
        assertEquals(firstResult, jdbc.queryForList("SELECT * FROM biz_task ORDER BY task_id"));
    }

    @Test
    void annualActivationReportsDatabaseFailureWithoutPartialUpdates() throws Exception {
        seedAnnualTasks();
        List<Map<String, Object>> before = jdbc.queryForList("SELECT * FROM biz_task ORDER BY task_id");
        jdbc.execute("CREATE TRIGGER review_fail_activation BEFORE UPDATE ON biz_task FOR EACH ROW "
                + "SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='review activation failure'");
        try {
            ResponseEntity<String> response = request(HttpMethod.POST, "/scheduled/update_task_status", login(ADMIN), null);
            assertTrue(response.getBody().contains("更新任务状态失败"), response.getBody());
            assertEquals(500, body(response).path("code").asInt());
        } finally {
            jdbc.execute("DROP TRIGGER review_fail_activation");
        }
        assertEquals(before, jdbc.queryForList("SELECT * FROM biz_task ORDER BY task_id"));
        assertSuccess(request(HttpMethod.POST, "/scheduled/update_task_status", login(ADMIN), null), "任务状态更新完成");
        assertEquals("1", jdbc.queryForObject("SELECT status FROM biz_task WHERE task_id=970001", String.class));
    }
}
