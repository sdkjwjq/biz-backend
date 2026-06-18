package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.example.entity.dto.BizNewSubDTO;
import org.example.entity.dto.BizSubForthDTO;
import org.example.service.BizService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.password=Lty981119"
)
class AuditFlowApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BizService bizService;

    @BeforeEach
    void resetAuditFlowTestData() {
        deleteAuditArtifactsForTaskIds(List.of(41L, 53L, 54L));
        jdbcTemplate.update(
                "update biz_task set status = '0', current_value = 0, progress = 0 where task_id in (41, 53, 54)"
        );
        jdbcTemplate.update(
                "update biz_level4_task set status = '0', current_value = 0, progress = 0 where task_id in (1, 19, 20)"
        );
    }

    private void deleteAuditArtifactsForTaskIds(List<Long> taskIds) {
        String taskPlaceholders = String.join(",", Collections.nCopies(taskIds.size(), "?"));
        List<Long> subIds = jdbcTemplate.queryForList(
                "select sub_id from biz_material_submission where task_id in (" + taskPlaceholders + ")",
                Long.class,
                taskIds.toArray()
        );
        if (subIds.isEmpty()) {
            return;
        }
        String placeholders = String.join(",", Collections.nCopies(subIds.size(), "?"));
        Object[] args = subIds.toArray();
        jdbcTemplate.update("delete from biz_audit_log where sub_id in (" + placeholders + ")", args);
        jdbcTemplate.update("delete from biz_audit_snapshot where sub_id in (" + placeholders + ")", args);
        jdbcTemplate.update("delete from biz_material_submission where sub_id in (" + placeholders + ")", args);
    }

    @Test
    @Transactional
    void parentTaskStatusFollowsThirdLevelSubmission() {
        Long submitterId = 112212L;
        Long thirdTaskId = 53L;
        Long level4TaskId = 19L;
        Long parentTaskId = jdbcTemplate.queryForObject(
                "select parent_id from biz_task where task_id = ?",
                Long.class,
                thirdTaskId
        );

        jdbcTemplate.update(
                "update biz_material_submission set is_delete = 1 where task_id = ? and is_delete = 0 and flow_status < 40",
                thirdTaskId
        );
        jdbcTemplate.update(
                "update biz_task set status = '0', current_value = 0, progress = 0 where task_id = ?",
                parentTaskId
        );
        jdbcTemplate.update(
                "update biz_task set status = '0', current_value = 0, progress = 0 where task_id = ?",
                thirdTaskId
        );
        jdbcTemplate.update(
                "update biz_level4_task set status = '0', current_value = 0, progress = 0 where task_id = ?",
                level4TaskId
        );

        BizNewSubDTO dto = new BizNewSubDTO();
        dto.setThird_task_id(thirdTaskId);
        dto.setFile_id(1L);
        dto.setComment("parent status sync test");
        dto.setSub_list(List.of(new BizSubForthDTO(level4TaskId, BigDecimal.ONE, "1")));

        String response = bizService.subFourLevelTasks(dto, submitterId);

        assertTrue(response.contains("提交成功"));
        assertEquals("2", taskStatus(thirdTaskId));
        assertEquals("2", taskStatus(parentTaskId));
    }

    @Test
    @Transactional
    void adminFinishTaskSimulatesNormalAuditFlow() {
        Long taskId = 53L;
        Long adminId = 110228L;

        Map<String, Object> task = jdbcTemplate.queryForMap(
                "select task_id, task_name, leader_id, auditor_id, principal_id, dept_id, target_value " +
                        "from biz_task where task_id = ?",
                taskId
        );
        Long auditorId = ((Number) task.get("auditor_id")).longValue();
        Long principalId = ((Number) task.get("principal_id")).longValue();
        Long leaderId = ((Number) task.get("leader_id")).longValue();
        Long deptId = ((Number) task.get("dept_id")).longValue();
        BigDecimal targetValue = (BigDecimal) task.get("target_value");

        jdbcTemplate.update(
                "update biz_task set status = '2', current_value = 0, progress = 0 where task_id = ?",
                taskId
        );
        jdbcTemplate.update(
                "update biz_material_submission set is_delete = 1 where task_id = ? and is_delete = 0 and flow_status < 40",
                taskId
        );
        jdbcTemplate.update(
                "insert into biz_material_submission(" +
                        "task_id, file_id, reported_value, data_type, submit_by, submit_dept_id, manage_dept_id, " +
                        "submit_time, file_suffix, flow_status, current_handler_id, is_delete" +
                        ") values(?, 1, 1, '1', ?, ?, ?, now(), 'pdf', 10, ?, 0)",
                taskId,
                leaderId,
                deptId,
                deptId,
                auditorId
        );
        Long subId = latestSubId(taskId);
        Long startLogId = maxAuditLogId(subId);

        String response = bizService.finishTask(taskId, adminId);

        assertTrue(response.contains("已完成"));
        assertAudit(subId, 40, adminId, false);
        assertTask(taskId, "3", targetValue);
        assertAuditLogStep(subId, auditorId, 10, 20, startLogId);
        assertAuditLogStep(subId, principalId, 20, 30, startLogId);
        assertAuditLogStep(subId, adminId, 30, 40, startLogId);
    }

    @Test
    void completedTaskCanBeSubmittedAgainAndCreatesSnapshots() {
        Long submitterId = 112212L;
        Long thirdTaskId = 41L;
        Long level4TaskId = 1L;

        String token = login(submitterId);
        Long auditorId = jdbcTemplate.queryForObject(
                "select auditor_id from biz_task where task_id = ?",
                Long.class,
                thirdTaskId
        );

        Map<String, Object> request = Map.of(
                "third_task_id", thirdTaskId,
                "file_id", 1L,
                "comment", "completed submit api test",
                "sub_list", List.of(Map.of(
                        "task_id", level4TaskId,
                        "reported_value", BigDecimal.ONE,
                        "data_type", "1"
                ))
        );

        String response = post("/api/biz/sub", token, request, String.class);

        assertFalse(response.contains("\"code\""),
                "completed task submission should not return ErrorVO: " + response);

        Map<String, Object> audit = jdbcTemplate.queryForMap(
                "select sub_id, flow_status, current_handler_id, is_delete " +
                        "from biz_material_submission where task_id = ? order by sub_id desc limit 1",
                thirdTaskId
        );
        Long subId = ((Number) audit.get("sub_id")).longValue();

        assertEquals(10, ((Number) audit.get("flow_status")).intValue());
        assertEquals(auditorId, ((Number) audit.get("current_handler_id")).longValue());
        assertEquals(Boolean.FALSE, audit.get("is_delete"));

        Integer snapshotCount = jdbcTemplate.queryForObject(
                "select count(*) from biz_audit_snapshot where sub_id = ?",
                Integer.class,
                subId
        );
        assertEquals(2, snapshotCount);
    }

    @Test
    void submitRejectResubmitApproveAndWithdrawFlow() {
        Long submitterId = 112212L;
        Long auditorId = 120221L;
        Long principalId = 110009L;
        Long adminId = 110228L;
        Long rejectTaskId = 53L;
        Long rejectLevel4TaskId = 19L;
        Long withdrawTaskId = 54L;
        Long withdrawLevel4TaskId = 20L;
        String rejectTaskOriginalComment = taskComment(rejectTaskId);
        String withdrawTaskOriginalComment = taskComment(withdrawTaskId);

        String submitterToken = login(submitterId);
        String auditorToken = login(auditorId);
        String principalToken = login(principalId);
        String adminToken = login(adminId);

        String nonLeaderResponse = submit(auditorToken, rejectTaskId, rejectLevel4TaskId, BigDecimal.ONE);
        assertError(nonLeaderResponse);

        String submitResponse = submit(submitterToken, rejectTaskId, rejectLevel4TaskId, BigDecimal.ONE);
        assertSuccess(submitResponse);
        Long firstSubId = latestSubId(rejectTaskId);

        assertAudit(firstSubId, 10, auditorId, false);
        assertSnapshotCount(firstSubId, 2);
        assertTask(rejectTaskId, "2", BigDecimal.ONE);
        assertEquals("audit flow api test", taskComment(rejectTaskId));
        assertLevel4Task(rejectLevel4TaskId, "1", BigDecimal.ONE);

        String duplicateResponse = submit(submitterToken, rejectTaskId, rejectLevel4TaskId, BigDecimal.ONE);
        assertError(duplicateResponse);

        String wrongHandlerResponse = audit(principalToken, firstSubId, true, "wrong handler");
        assertError(wrongHandlerResponse);

        String rejectResponse = audit(auditorToken, firstSubId, false, "reject to submitter");
        assertSuccess(rejectResponse);
        assertAudit(firstSubId, -10, submitterId, true);
        assertTask(rejectTaskId, "0", BigDecimal.ZERO);
        assertEquals(rejectTaskOriginalComment, taskComment(rejectTaskId));
        assertLevel4Task(rejectLevel4TaskId, "0", BigDecimal.ZERO);

        String resubmitResponse = resubmit(submitterToken, firstSubId, rejectLevel4TaskId, BigDecimal.ONE);
        assertSuccess(resubmitResponse);
        Long secondSubId = latestSubId(rejectTaskId);
        assertTrue(secondSubId > firstSubId);
        assertAudit(firstSubId, -10, submitterId, true);
        assertAudit(secondSubId, 10, auditorId, false);
        assertSnapshotCount(secondSubId, 2);

        assertSuccess(audit(auditorToken, secondSubId, true, "auditor pass"));
        assertAudit(secondSubId, 20, principalId, false);

        assertSuccess(audit(principalToken, secondSubId, true, "principal pass"));
        assertAudit(secondSubId, 30, adminId, false);

        assertSuccess(audit(adminToken, secondSubId, true, "admin pass"));
        assertAudit(secondSubId, 40, adminId, false);
        assertTask(rejectTaskId, "3", BigDecimal.ONE);
        assertAuditLogCount(secondSubId, 4);

        String withdrawSubmitResponse = submit(submitterToken, withdrawTaskId, withdrawLevel4TaskId, BigDecimal.ONE);
        assertSuccess(withdrawSubmitResponse);
        Long withdrawSubId = latestSubId(withdrawTaskId);
        assertAudit(withdrawSubId, 10, auditorId, false);
        assertEquals("audit flow api test", taskComment(withdrawTaskId));

        String withdrawResponse = post("/api/biz/drawback/" + withdrawTaskId, submitterToken, null, String.class);
        assertSuccess(withdrawResponse);
        assertAudit(withdrawSubId, 10, auditorId, true);
        assertTask(withdrawTaskId, "0", BigDecimal.ZERO);
        assertEquals(withdrawTaskOriginalComment, taskComment(withdrawTaskId));
        assertLevel4Task(withdrawLevel4TaskId, "0", BigDecimal.ZERO);
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

    private String submit(String token, Long thirdTaskId, Long level4TaskId, BigDecimal reportedValue) {
        Map<String, Object> request = Map.of(
                "third_task_id", thirdTaskId,
                "file_id", 1L,
                "comment", "audit flow api test",
                "sub_list", List.of(Map.of(
                        "task_id", level4TaskId,
                        "reported_value", reportedValue,
                        "data_type", "1"
                ))
        );
        return post("/api/biz/sub", token, request, String.class);
    }

    private String resubmit(String token, Long subId, Long level4TaskId, BigDecimal reportedValue) {
        Map<String, Object> request = Map.of(
                "sub_id", subId,
                "file_id", 1L,
                "comment", "audit flow api resubmit test",
                "sub_list", List.of(Map.of(
                        "task_id", level4TaskId,
                        "reported_value", reportedValue,
                        "data_type", "1"
                ))
        );
        return post("/api/biz/resub", token, request, String.class);
    }

    private String audit(String token, Long subId, boolean pass, String title) {
        Map<String, Object> request = Map.of(
                "sub_id", subId,
                "is_pass", pass,
                "title", title,
                "content", title
        );
        return post("/api/biz/audit", token, request, String.class);
    }

    private <T> T post(String path, String token, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);
        return restTemplate.postForObject(url(path), new HttpEntity<>(body, headers), responseType);
    }

    private Long latestSubId(Long taskId) {
        return jdbcTemplate.queryForObject(
                "select sub_id from biz_material_submission where task_id = ? order by sub_id desc limit 1",
                Long.class,
                taskId
        );
    }

    private void assertAudit(Long subId, int flowStatus, Long currentHandlerId, boolean deleted) {
        Map<String, Object> audit = jdbcTemplate.queryForMap(
                "select flow_status, current_handler_id, is_delete from biz_material_submission where sub_id = ?",
                subId
        );
        assertEquals(flowStatus, ((Number) audit.get("flow_status")).intValue());
        assertEquals(currentHandlerId, ((Number) audit.get("current_handler_id")).longValue());
        assertEquals(deleted, audit.get("is_delete"));
    }

    private void assertSnapshotCount(Long subId, int expectedCount) {
        Integer snapshotCount = jdbcTemplate.queryForObject(
                "select count(*) from biz_audit_snapshot where sub_id = ?",
                Integer.class,
                subId
        );
        assertEquals(expectedCount, snapshotCount);
    }

    private void assertAuditLogCount(Long subId, int expectedCount) {
        Integer logCount = jdbcTemplate.queryForObject(
                "select count(*) from biz_audit_log where sub_id = ?",
                Integer.class,
                subId
        );
        assertEquals(expectedCount, logCount);
    }

    private Long maxAuditLogId(Long subId) {
        return jdbcTemplate.queryForObject(
                "select coalesce(max(log_id), 0) from biz_audit_log where sub_id = ?",
                Long.class,
                subId
        );
    }

    private void assertAuditLogStep(Long subId, Long operatorId, int preStatus, int postStatus, Long minLogId) {
        Integer logCount = jdbcTemplate.queryForObject(
                "select count(*) from biz_audit_log " +
                        "where sub_id = ? and operator_id = ? and action_type = '通过' " +
                        "and pre_status = ? and post_status = ? and log_id > ?",
                Integer.class,
                subId,
                operatorId,
                preStatus,
                postStatus,
                minLogId
        );
        assertEquals(1, logCount);
    }

    private void assertTask(Long taskId, String status, BigDecimal currentValue) {
        Map<String, Object> task = jdbcTemplate.queryForMap(
                "select status, current_value from biz_task where task_id = ?",
                taskId
        );
        assertEquals(status, String.valueOf(task.get("status")));
        assertBigDecimalEquals(currentValue, (BigDecimal) task.get("current_value"));
    }

    private void assertLevel4Task(Long taskId, String status, BigDecimal currentValue) {
        Map<String, Object> task = jdbcTemplate.queryForMap(
                "select status, current_value from biz_level4_task where task_id = ?",
                taskId
        );
        assertEquals(status, String.valueOf(task.get("status")));
        assertBigDecimalEquals(currentValue, (BigDecimal) task.get("current_value"));
    }

    private String taskComment(Long taskId) {
        return jdbcTemplate.queryForObject(
                "select comment from biz_task where task_id = ?",
                String.class,
                taskId
        );
    }

    private String taskStatus(Long taskId) {
        return jdbcTemplate.queryForObject(
                "select status from biz_task where task_id = ?",
                String.class,
                taskId
        );
    }

    private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
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
