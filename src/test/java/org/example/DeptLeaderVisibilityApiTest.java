package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.password=981119"
)
class DeptLeaderVisibilityApiTest {
    private static final Long DEPT_LEADER_ID = 110379L;
    private static final Long NORMAL_USER_ID = 112212L;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deptLeaderCanViewManagedDeptTasksAndPerformances() {
        String leaderToken = login(DEPT_LEADER_ID);
        String normalToken = login(NORMAL_USER_ID);

        Long managedDeptId = jdbcTemplate.queryForObject(
                "select dept_id from sys_dept where leader_id = ? and is_delete = 0 limit 1",
                Long.class,
                DEPT_LEADER_ID
        );
        assertNotNull(managedDeptId);

        Set<Long> expectedTaskIds = new HashSet<>(jdbcTemplate.queryForList(
                "select task_id from biz_task where dept_id = ? and level <= 3",
                Long.class,
                managedDeptId
        ));
        assertFalse(expectedTaskIds.isEmpty(), "managed department should have tasks");

        Set<Long> leaderTaskIds = extractIds(getList(leaderToken, "/api/biz/tasks"), "taskId", "task_id");
        assertTrue(leaderTaskIds.containsAll(expectedTaskIds),
                "dept leader should see all tasks in managed department");

        Long unrelatedDeptTaskId = jdbcTemplate.queryForObject(
                "select task_id from biz_task where dept_id = ? and level <= 3 " +
                        "and coalesce(leader_id, -1) <> ? and coalesce(auditor_id, -1) <> ? and coalesce(principal_id, -1) <> ? " +
                        "limit 1",
                Long.class,
                managedDeptId,
                NORMAL_USER_ID,
                NORMAL_USER_ID,
                NORMAL_USER_ID
        );
        Set<Long> normalTaskIds = extractIds(getList(normalToken, "/api/biz/tasks"), "taskId", "task_id");
        assertFalse(normalTaskIds.contains(unrelatedDeptTaskId),
                "ordinary users should not gain visibility into another managed department");

        Set<Long> expectedPerfIds = new HashSet<>(jdbcTemplate.queryForList(
                "select perf_id from biz_performance where dept_id = ? and is_delete = 0",
                Long.class,
                managedDeptId
        ));
        assertFalse(expectedPerfIds.isEmpty(), "managed department should have performances");

        Set<Long> leaderPerfIds = extractIds(getList(leaderToken, "/api/performance"), "perfId", "perf_id");
        assertTrue(leaderPerfIds.containsAll(expectedPerfIds),
                "dept leader should see all performances in managed department");

        Set<Long> expectedYearPerfIds = new HashSet<>(jdbcTemplate.queryForList(
                "select distinct p.perf_id from biz_performance p " +
                        "join biz_performance_year y on y.perf_id = p.perf_id and y.year = 2025 " +
                        "where p.dept_id = ? and p.is_delete = 0",
                Long.class,
                managedDeptId
        ));
        assertFalse(expectedYearPerfIds.isEmpty(), "managed department should have annual performances");

        Set<Long> leaderYearPerfIds = extractIds(getList(leaderToken, "/api/performance/year/2025"), "perfId", "perf_id");
        assertTrue(leaderYearPerfIds.containsAll(expectedYearPerfIds),
                "dept leader should see all annual performances in managed department");
    }

    private String login(Long userId) {
        Map<?, ?> response = restTemplate.postForObject(
                url("/api/system/login"),
                Map.of("user_id", userId, "password", String.valueOf(userId)),
                Map.class
        );
        assertNotNull(response);
        assertTrue(response.containsKey("token"), "login failed: " + response);
        return String.valueOf(response.get("token"));
    }

    private List<?> getList(String token, String path) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        Object body = restTemplate.exchange(
                url(path),
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class
        ).getBody();
        assertTrue(body instanceof List<?>, "expected list response from " + path + " but got: " + body);
        return (List<?>) body;
    }

    private Set<Long> extractIds(List<?> rows, String camelKey, String snakeKey) {
        Set<Long> result = new HashSet<>();
        for (Object row : rows) {
            assertTrue(row instanceof Map<?, ?>, "expected row map but got: " + row);
            Object value = ((Map<?, ?>) row).get(camelKey);
            if (value == null) {
                value = ((Map<?, ?>) row).get(snakeKey);
            }
            if (value instanceof Number number) {
                result.add(number.longValue());
            }
        }
        return result;
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
