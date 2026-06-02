package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.password=981119"
)
class BudgetFlowApiTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void adminCanImportQueryLockAndExportBudget() {
        Integer year = 2029;
        Integer month = 12;
        String adminToken = login(110228L);
        String normalToken = login(112212L);
        cleanup(year, month);

        try {
            String normalImport = uploadBudget(normalToken, year, month);
            assertError(normalImport);

            String importResponse = uploadBudget(adminToken, year, month);
            assertSuccess(importResponse);

            Map<?, ?> budget = getBudget(adminToken, year, month);
            Map<?, ?> sheet = (Map<?, ?>) budget.get("sheet");
            List<?> sources = (List<?>) budget.get("sources");
            List<?> tasks = (List<?>) budget.get("tasks");
            assertEquals(year, ((Number) sheet.get("year")).intValue());
            assertEquals(month, ((Number) sheet.get("month")).intValue());
            assertEquals(1, ((Number) sheet.get("locked")).intValue());
            assertEquals(5, sources.size());
            assertEquals(10, tasks.size());
            assertEquals("中央财政投入资金", ((Map<?, ?>) sources.get(0)).get("sourceName"));
            assertEquals("落实立德树人根本任务", ((Map<?, ?>) tasks.get(0)).get("taskName"));

            String unlockResponse = postForm(adminToken, "/api/budget/lock", Map.of(
                    "year", String.valueOf(year),
                    "month", String.valueOf(month),
                    "locked", "false"
            ));
            assertSuccess(unlockResponse);
            Map<?, ?> afterUnlock = getBudget(adminToken, year, month);
            Map<?, ?> afterUnlockSheet = (Map<?, ?>) afterUnlock.get("sheet");
            assertEquals(0, ((Number) afterUnlockSheet.get("locked")).intValue());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", adminToken);
            ResponseEntity<byte[]> exportResponse = restTemplate.exchange(
                    url("/api/budget/export?year=" + year + "&month=" + month),
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(headers),
                    byte[].class
            );
            assertEquals(200, exportResponse.getStatusCode().value());
            byte[] body = exportResponse.getBody();
            assertNotNull(body);
            assertTrue(body.length > 1000);
            assertEquals('P', body[0]);
            assertEquals('K', body[1]);
        } finally {
            cleanup(year, month);
        }
    }

    private String uploadBudget(String token, Integer year, Integer month) {
        File file = new File("../biz/public/templates/budget-template.xlsx");
        assertTrue(file.exists(), "template file should exist: " + file.getAbsolutePath());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("year", String.valueOf(year));
        body.add("month", String.valueOf(month));
        body.add("file", new FileSystemResource(file));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", token);
        return restTemplate.postForObject(url("/api/budget/import"), new HttpEntity<>(body, headers), String.class);
    }

    private Map<?, ?> getBudget(String token, Integer year, Integer month) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        return restTemplate.exchange(
                url("/api/budget?year=" + year + "&month=" + month),
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        ).getBody();
    }

    private String postForm(String token, String path, Map<String, String> params) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        params.forEach(body::add);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", token);
        return restTemplate.postForObject(url(path), new HttpEntity<>(body, headers), String.class);
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

    private void cleanup(Integer year, Integer month) {
        List<Long> sheetIds = jdbcTemplate.queryForList(
                "select sheet_id from biz_budget_sheet where year = ? and month = ?",
                Long.class,
                year,
                month
        );
        for (Long sheetId : sheetIds) {
            jdbcTemplate.update("delete from biz_budget_task_item where sheet_id = ?", sheetId);
            jdbcTemplate.update("delete from biz_budget_source_item where sheet_id = ?", sheetId);
            jdbcTemplate.update("delete from biz_budget_sheet where sheet_id = ?", sheetId);
        }
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
