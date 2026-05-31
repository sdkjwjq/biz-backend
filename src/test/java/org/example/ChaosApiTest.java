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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.password=981119"
)
class ChaosApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void taskModuleRejectsChaoticSubmissionsWithoutCreatingAuditRows() {
        String submitterToken = login(112212L);
        int before = materialSubmissionCount();

        assertError(postJson("/api/biz/sub", submitterToken, Map.of(
                "third_task_id", 53L,
                "file_id", 1L,
                "comment", "wrong child",
                "sub_list", List.of(Map.of(
                        "task_id", 1L,
                        "reported_value", 1,
                        "data_type", "1"
                ))
        )));

        assertError(postJson("/api/biz/sub", submitterToken, Map.of(
                "third_task_id", 53L,
                "file_id", 1L,
                "comment", "negative value",
                "sub_list", List.of(Map.of(
                        "task_id", 19L,
                        "reported_value", -1,
                        "data_type", "1"
                ))
        )));

        assertError(postJson("/api/biz/sub", submitterToken, Map.of(
                "third_task_id", 53L,
                "file_id", 1L,
                "comment", "duplicate level4",
                "sub_list", List.of(
                        Map.of("task_id", 19L, "reported_value", 1, "data_type", "1"),
                        Map.of("task_id", 19L, "reported_value", 2, "data_type", "1")
                )
        )));

        assertError(postJson("/api/biz/sub", submitterToken, Map.of(
                "third_task_id", 53L,
                "file_id", 999999L,
                "comment", "missing file",
                "sub_list", List.of(Map.of(
                        "task_id", 19L,
                        "reported_value", 1,
                        "data_type", "1"
                ))
        )));

        assertEquals(before, materialSubmissionCount());
    }

    @Test
    void taskModuleRejectsChaoticAuditOperations() {
        String randomUserToken = login(110009L);
        String submitterToken = login(112212L);

        assertError(postJson("/api/biz/audit", randomUserToken, Map.of(
                "sub_id", 999999999L,
                "is_pass", true,
                "title", "audit ghost",
                "content", "audit ghost"
        )));

        assertError(postJson("/api/biz/drawback/999999999", submitterToken, null));
    }

    @Test
    void performanceModuleRejectsChaoticSubmissionsAndAuditsWithoutCreatingRows() {
        String submitterToken = login(112327L);
        String unrelatedToken = login(110009L);
        String adminToken = login(110228L);
        int before = performanceSubmissionCount();

        assertError(postForm("/api/performance/submit", unrelatedToken, Map.of(
                "pref_id", "148",
                "actual_value", "1",
                "year", "2026"
        )));

        assertError(postForm("/api/performance/submit", submitterToken, Map.of(
                "pref_id", "148",
                "actual_value", "-1",
                "year", "2026"
        )));

        assertError(postForm("/api/performance/submit", submitterToken, Map.of(
                "pref_id", "148",
                "actual_value", "101",
                "year", "2026"
        )));

        assertError(postForm("/api/performance/submit", adminToken, Map.of(
                "pref_id", "14",
                "actual_value", "1",
                "year", "2025"
        )));

        assertError(postJson("/api/performance/audit", adminToken, Map.of(
                "sub_id", 999999999L,
                "is_pass", true,
                "title", "audit ghost",
                "content", "audit ghost"
        )));

        assertEquals(before, performanceSubmissionCount());
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

    private String postForm(String path, String token, Map<String, String> fields) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", token);
        StringBuilder body = new StringBuilder();
        fields.forEach((key, value) -> {
            if (body.length() > 0) {
                body.append("&");
            }
            body.append(key).append("=").append(value);
        });
        return restTemplate.postForObject(url(path), new HttpEntity<>(body.toString(), headers), String.class);
    }

    private int materialSubmissionCount() {
        return jdbcTemplate.queryForObject("select count(*) from biz_material_submission", Integer.class);
    }

    private int performanceSubmissionCount() {
        return jdbcTemplate.queryForObject("select count(*) from biz_performance_submission", Integer.class);
    }

    private void assertError(String response) {
        assertNotNull(response);
        assertTrue(response.contains("\"code\""), "expected ErrorVO but got: " + response);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
