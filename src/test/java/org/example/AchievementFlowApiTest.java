package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.password=981119"
)
class AchievementFlowApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void achievementSubmitArchiveAndVisibilityFlow() {
        String achievementName = "成果审核接口测试-" + System.currentTimeMillis();
        Long uploaderId = 800001L;
        Long adminId = 110228L;
        Long normalId = 112212L;

        String uploaderToken = login(uploaderId);
        String adminToken = login(adminId);
        String normalToken = login(normalId);

        Long achId = null;
        Long subId = null;
        try {
            Map<String, Object> payload = achievementPayload(achievementName);

            String normalAdd = postJson("/api/achievement/add", normalToken, payload);
            assertError(normalAdd);

            String addResponse = postJson("/api/achievement/add", uploaderToken, payload);
            assertSuccess(addResponse);

            achId = jdbcTemplate.queryForObject(
                    "select ach_id from biz_achievement where ach_name = ? order by ach_id desc limit 1",
                    Long.class,
                    achievementName
            );
            subId = jdbcTemplate.queryForObject(
                    "select sub_id from biz_achievement_submission where ach_id = ? order by sub_id desc limit 1",
                    Long.class,
                    achId
            );
            assertNotNull(achId);
            assertNotNull(subId);
            assertAchievementState(achId, 10, adminId);

            Map<?, ?>[] todo = getArray("/api/achievement/audit/todo", adminToken);
            assertTrue(containsSubId(todo, subId), "admin todo list should include achievement submission");

            Map<?, ?>[] normalBeforeArchive = getArray("/api/achievement/", normalToken);
            assertFalse(containsAchievementName(normalBeforeArchive, achievementName),
                    "normal users should not see unarchived achievements");

            String auditResponse = postJson("/api/achievement/audit", adminToken, Map.of(
                    "sub_id", subId,
                    "is_pass", true,
                    "title", "同意",
                    "content", "同意"
            ));
            assertSuccess(auditResponse);
            assertAchievementState(achId, 30, null);

            Map<?, ?>[] normalAfterArchive = getArray("/api/achievement/", normalToken);
            assertTrue(containsAchievementName(normalAfterArchive, achievementName),
                    "normal users should see archived achievements");

            Map<?, ?>[] logs = getArray("/api/achievement/audit/logs/" + subId, adminToken);
            assertTrue(logs.length >= 2, "submit and archive actions should be logged");
        } finally {
            if (subId != null) {
                jdbcTemplate.update("delete from biz_achievement_audit_log where sub_id = ?", subId);
                jdbcTemplate.update("delete from biz_achievement_submission where sub_id = ?", subId);
            }
            if (achId != null) {
                jdbcTemplate.update("delete from biz_achievement where ach_id = ?", achId);
            }
        }
    }

    private Map<String, Object> achievementPayload(String achievementName) {
        return Map.ofEntries(
                Map.entry("category", 1),
                Map.entry("level", "省级"),
                Map.entry("achName", achievementName),
                Map.entry("department", "测试部门"),
                Map.entry("gotTime", "2026-05-31 12:00:00"),
                Map.entry("deptId", 118),
                Map.entry("comment", "测试成果归档审核"),
                Map.entry("isCompetition", 0),
                Map.entry("teDengJiang", 0),
                Map.entry("yiDengJiang", 0),
                Map.entry("erDengJiang", 0),
                Map.entry("sanDengJiang", 0),
                Map.entry("jinJiang", 0),
                Map.entry("yinJiang", 0),
                Map.entry("tongJiang", 0),
                Map.entry("youShengJiang", 0),
                Map.entry("budDengDengCi", 0)
        );
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

    private String postJson(String path, String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);
        return restTemplate.postForObject(url(path), new HttpEntity<>(body, headers), String.class);
    }

    private Map<?, ?>[] getArray(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        return restTemplate.exchange(url(path), org.springframework.http.HttpMethod.GET, new HttpEntity<>(headers), Map[].class).getBody();
    }

    private void assertAchievementState(Long achId, Integer status, Long handlerId) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "select audit_status, current_handler_id from biz_achievement where ach_id = ?",
                achId
        );
        assertEquals(status, ((Number) row.get("audit_status")).intValue());
        Object currentHandler = row.get("current_handler_id");
        if (handlerId == null) {
            assertNull(currentHandler);
        } else {
            assertEquals(handlerId.longValue(), ((Number) currentHandler).longValue());
        }
    }

    private boolean containsSubId(Map<?, ?>[] rows, Long subId) {
        if (rows == null) return false;
        for (Map<?, ?> row : rows) {
            Object value = row.get("subId");
            if (value instanceof Number && ((Number) value).longValue() == subId) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAchievementName(Map<?, ?>[] rows, String name) {
        if (rows == null) return false;
        for (Map<?, ?> row : rows) {
            Object value = row.get("achName");
            if (name.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private void assertError(String body) {
        assertNotNull(body);
        assertTrue(body.contains("\"code\":500") || body.contains("\"code\": 500"), "expected ErrorVO but got: " + body);
    }

    private void assertSuccess(String body) {
        assertNotNull(body);
        assertFalse(body.contains("\"code\":500") || body.contains("\"code\": 500"), "unexpected ErrorVO: " + body);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
