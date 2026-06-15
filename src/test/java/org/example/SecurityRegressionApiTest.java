package org.example;

import org.example.entity.SysUser;
import org.example.entity.dto.SysUserDTO;
import org.example.service.SysService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.password=981119"
)
class SecurityRegressionApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SysService sysService;

    @Test
    void budgetAndScheduledEndpointsRequireJwtAndScheduledRequiresAdmin() {
        ResponseEntity<String> noTokenBudget = restTemplate.exchange(
                url("/api/budget?year=2026&month=5"),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class
        );
        assertEquals(401, noTokenBudget.getStatusCode().value());

        String normalToken = login(112212L);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", normalToken);
        ResponseEntity<String> normalScheduled = restTemplate.postForEntity(
                url("/api/scheduled/update_task_status"),
                new HttpEntity<>(null, headers),
                String.class
        );
        assertEquals(200, normalScheduled.getStatusCode().value());
        assertNotNull(normalScheduled.getBody());
        assertTrue(normalScheduled.getBody().contains("\"code\":500") || normalScheduled.getBody().contains("\"code\": 500"));
        assertTrue(normalScheduled.getBody().contains("仅管理员可以操作定时任务"));
    }

    @Test
    @Transactional
    void updateUserAllowsKeepingOwnUserName() {
        SysUser user = sysService.getUserById(112212L);
        assertNotNull(user);

        SysUserDTO dto = new SysUserDTO();
        dto.setUserId(user.getUserId());
        dto.setDeptId(user.getDeptId());
        dto.setUserName(user.getUserName());
        dto.setNickName(user.getNickName());
        dto.setEmail(user.getEmail());
        dto.setPassword(user.getPassword());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setIsDelete(user.getIsDelete());

        assertDoesNotThrow(() -> sysService.updateUser(dto));
    }

    @Test
    @Transactional
    void noticeReadCanOnlyUpdateCurrentUsersNotice() {
        Long ownerId = 112212L;
        Long otherId = 110009L;
        jdbcTemplate.update(
                "insert into sys_notice(from_user_id, to_user_id, type, trigger_event, title, content, source_type, source_id, is_read, is_delete, create_time) " +
                        "values(?, ?, '0', 'TEST', '测试通知', '测试通知内容', '0', 0, '0', 0, now())",
                110228L,
                ownerId
        );
        Long noticeId = jdbcTemplate.queryForObject("select last_insert_id()", Long.class);
        assertNotNull(noticeId);

        assertThrows(RuntimeException.class, () -> sysService.setRead(noticeId, otherId));
        String stillUnread = jdbcTemplate.queryForObject(
                "select is_read from sys_notice where notice_id = ?",
                String.class,
                noticeId
        );
        assertEquals("0", stillUnread);

        assertDoesNotThrow(() -> sysService.setRead(noticeId, ownerId));
        String read = jdbcTemplate.queryForObject(
                "select is_read from sys_notice where notice_id = ?",
                String.class,
                noticeId
        );
        assertEquals("1", read);
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

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
