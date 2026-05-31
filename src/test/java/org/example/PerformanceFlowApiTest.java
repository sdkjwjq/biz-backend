package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.example.service.PerformanceService;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.password=981119"
)
class PerformanceFlowApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PerformanceService performanceService;

    @Test
    void relatedTasksCanBeFilteredBySelectedYear() {
        String adminToken = login(110228L);

        Map<?, ?>[] allTasks = getArray("/api/performance/task/14", adminToken);
        Map<?, ?>[] yearTasks = getArray("/api/performance/task/14?year=2025", adminToken);

        assertTrue(allTasks.length > yearTasks.length, "unfiltered tasks should include multiple years");
        assertTrue(yearTasks.length > 0, "filtered tasks should not be empty");
        for (Map<?, ?> task : yearTasks) {
            assertEquals(2025, ((Number) task.get("phase")).intValue());
        }
    }

    @Test
    @Transactional
    void refreshCalculatesYearlyAndTotalPerformanceByDataType() {
        Long sumPerfId = 14L;
        Long maxPerfId = 21L;

        zeroLinkedTaskValues(sumPerfId);
        zeroLinkedTaskValues(maxPerfId);

        jdbcTemplate.update("update biz_task set current_value = ? where task_id = ?", new BigDecimal("2"), 43L);
        jdbcTemplate.update("update biz_task set current_value = ? where task_id = ?", new BigDecimal("5"), 47L);
        jdbcTemplate.update("update biz_task set current_value = ? where task_id = ?", new BigDecimal("11"), 185L);
        jdbcTemplate.update("update biz_task set current_value = ? where task_id = ?", new BigDecimal("22"), 191L);

        performanceService.calcuateAllPerformance();

        assertYearValue(sumPerfId, 2025, new BigDecimal("2.0000"));
        assertYearValue(sumPerfId, 2026, new BigDecimal("5.0000"));
        assertPerformanceValue(sumPerfId, new BigDecimal("7.0000"));

        assertYearValue(maxPerfId, 2026, new BigDecimal("11.0000"));
        assertYearValue(maxPerfId, 2027, new BigDecimal("22.0000"));
        assertPerformanceValue(maxPerfId, new BigDecimal("22.0000"));
    }

    @Test
    void manualPerformanceSubmitAuditAndVisibilityFlow() {
        Long perfId = 148L;
        Integer year = 2026;
        Long submitterId = 112327L;
        Long auditorId = 110279L;
        Long adminId = 110228L;
        Long strangerId = 110009L;

        BigDecimal previousYearValue = jdbcTemplate.queryForObject(
                "select actual_value from biz_performance_year where perf_id = ? and year = ?",
                BigDecimal.class,
                perfId,
                year
        );

        String submitterToken = login(submitterId);
        String auditorToken = login(auditorId);
        String adminToken = login(adminId);
        String strangerToken = login(strangerId);

        jdbcTemplate.update(
                "update biz_performance_submission set is_delete = 1 where perf_id = ? and year = ? and flow_status in (10, 20)",
                perfId,
                year
        );

        String auditorSubmitResponse = postForm("/api/performance/submit", auditorToken, Map.of(
                "pref_id", String.valueOf(perfId),
                "actual_value", "11",
                "year", String.valueOf(year)
        ));
        assertError(auditorSubmitResponse);

        String submitResponse = postForm("/api/performance/submit", submitterToken, Map.of(
                "pref_id", String.valueOf(perfId),
                "actual_value", "12",
                "year", String.valueOf(year),
                "comment", "performance api test"
        ));
        assertSuccess(submitResponse);

        Long subId = jdbcTemplate.queryForObject(
                "select sub_id from biz_performance_submission where perf_id = ? and year = ? order by sub_id desc limit 1",
                Long.class,
                perfId,
                year
        );
        assertNotNull(subId);
        assertSubmission(subId, 10, auditorId, false);
        assertSnapshot(subId, previousYearValue);
        assertYearValue(perfId, year, new BigDecimal("12.0000"));

        Map<?, ?>[] drawerAudits = getArray("/api/performance/audit/perf/" + perfId + "?year=" + year, submitterToken);
        assertTrue(drawerAudits.length > 0, "drawer audit flow should include current submission");
        assertEquals(subId, ((Number) drawerAudits[0].get("subId")).longValue());
        assertError(getString("/api/performance/audit/perf/" + perfId + "?year=" + year, strangerToken));

        Map<?, ?>[] submitLogs = getArray("/api/performance/audit/logs/" + subId, submitterToken);
        assertEquals(1, submitLogs.length, "submit action should be logged");

        String duplicateResponse = postForm("/api/performance/submit", submitterToken, Map.of(
                "pref_id", String.valueOf(perfId),
                "actual_value", "13",
                "year", String.valueOf(year)
        ));
        assertError(duplicateResponse);

        String submitterPass = postJson("/api/performance/audit", submitterToken, Map.of(
                "sub_id", subId,
                "is_pass", true,
                "title", "submitter should not audit",
                "content", "submitter should not audit"
        ));
        assertError(submitterPass);

        String auditorPass = postJson("/api/performance/audit", auditorToken, Map.of(
                "sub_id", subId,
                "is_pass", true,
                "title", "auditor pass",
                "content", "auditor pass"
        ));
        assertSuccess(auditorPass);
        assertSubmission(subId, 20, adminId, false);

        String auditorSecondPass = postJson("/api/performance/audit", auditorToken, Map.of(
                "sub_id", subId,
                "is_pass", true,
                "title", "auditor should not archive",
                "content", "auditor should not archive"
        ));
        assertError(auditorSecondPass);

        String adminPass = postJson("/api/performance/audit", adminToken, Map.of(
                "sub_id", subId,
                "is_pass", true,
                "title", "admin pass",
                "content", "admin pass"
        ));
        assertSuccess(adminPass);
        assertSubmission(subId, 30, adminId, false);
        assertYearValue(perfId, year, new BigDecimal("12.0000"));

        Map<?, ?>[] finishedLogs = getArray("/api/performance/audit/logs/" + subId, adminToken);
        assertTrue(finishedLogs.length >= 3, "submit, auditor pass and archive actions should be logged");
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

    private Map<?, ?>[] getArray(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        ResponseEntity<Map[]> response = restTemplate.exchange(
                url(path),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map[].class
        );
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    private String getString(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        ResponseEntity<String> response = restTemplate.exchange(
                url(path),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    private void zeroLinkedTaskValues(Long perfId) {
        jdbcTemplate.update(
                "update biz_task set current_value = 0 where task_id in " +
                        "(select task_id from rel_task_performance where perf_id = ?)",
                perfId
        );
    }

    private void assertSubmission(Long subId, int flowStatus, Long handlerId, boolean deleted) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "select flow_status, current_handler_id, is_delete from biz_performance_submission where sub_id = ?",
                subId
        );
        assertEquals(flowStatus, ((Number) row.get("flow_status")).intValue());
        assertEquals(handlerId, ((Number) row.get("current_handler_id")).longValue());
        assertEquals(deleted, row.get("is_delete"));
    }

    private void assertSnapshot(Long subId, BigDecimal previousYearValue) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "select previous_year_actual_value from biz_performance_audit_snapshot where sub_id = ?",
                subId
        );
        BigDecimal actual = (BigDecimal) row.get("previous_year_actual_value");
        assertEquals(0, actual.compareTo(previousYearValue));
    }

    private void assertYearValue(Long perfId, Integer year, BigDecimal expected) {
        BigDecimal actual = jdbcTemplate.queryForObject(
                "select actual_value from biz_performance_year where perf_id = ? and year = ?",
                BigDecimal.class,
                perfId,
                year
        );
        assertNotNull(actual);
        assertEquals(0, actual.compareTo(expected));
    }

    private void assertPerformanceValue(Long perfId, BigDecimal expected) {
        BigDecimal actual = jdbcTemplate.queryForObject(
                "select current_value from biz_performance where perf_id = ?",
                BigDecimal.class,
                perfId
        );
        assertNotNull(actual);
        assertEquals(0, actual.compareTo(expected));
    }

    private void assertSuccess(String response) {
        assertFalse(response.contains("\"code\""), "expected success but got: " + response);
    }

    private void assertError(String response) {
        assertTrue(response.contains("\"code\""), "expected ErrorVO but got: " + response);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
