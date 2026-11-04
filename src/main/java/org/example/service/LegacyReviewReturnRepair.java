package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.SysNotice;
import org.example.mapper.SysMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/** Offline transition repair; never an HTTP operation or an invented approval. */
@Service
public class LegacyReviewReturnRepair {
    static final LocalDateTime CUTOFF = LocalDateTime.of(2026, 11, 3, 0, 0);
    static final String BATCH = "legacy-review-return-20261104-v1";
    private final JdbcTemplate db;
    private final ObjectMapper json;
    private final PermissionManagementService permissions;
    private final SysMapper notices;

    public LegacyReviewReturnRepair(JdbcTemplate db, ObjectMapper json, PermissionManagementService permissions, SysMapper notices) {
        this.db = db; this.json = json; this.permissions = permissions; this.notices = notices;
    }

    @Transactional(rollbackFor = Exception.class, isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public Map<String, Object> run(String mode, Path output) throws IOException {
        if (!Set.of("check", "apply").contains(mode) || Files.exists(output)) throw new IllegalArgumentException("New check/apply report required");
        var changes = new ArrayList<Map<String, Object>>();
        var skipped = new ArrayList<Map<String, Object>>();
        var blocked = new ArrayList<Map<String, Object>>();
        var ids = db.queryForList("SELECT s.sub_id FROM biz_material_submission s JOIN biz_task t ON t.task_id=s.task_id "
                + "WHERE s.flow_status<0 AND s.is_delete=1 AND COALESCE(t.is_delete,0)=0 AND t.level=3 "
                + "AND NOT EXISTS(SELECT 1 FROM biz_material_submission n WHERE n.task_id=s.task_id AND n.sub_id>s.sub_id) "
                + "AND EXISTS(SELECT 1 FROM biz_audit_log l WHERE l.sub_id=s.sub_id AND l.pre_status IN(20,30) AND l.post_status<0 AND l.create_time>=?) ORDER BY s.task_id,s.sub_id", Long.class, Timestamp.valueOf(CUTOFF));
        for (Long id : ids) {
            var sub = db.queryForMap("SELECT * FROM biz_material_submission WHERE sub_id=? FOR UPDATE", id);
            var task = db.queryForMap("SELECT * FROM biz_task WHERE task_id=? FOR UPDATE", n(sub.get("task_id")));
            var logs = db.queryForList("SELECT * FROM biz_audit_log WHERE sub_id=? ORDER BY create_time DESC,log_id DESC", id);
            if (logs.isEmpty()) continue;
            var last = logs.get(0);
            long from = n(last.get("pre_status"));
            if (n(sub.get("flow_status")) >= 0 || n(sub.get("is_delete")) != 1 || n(last.get("post_status")) >= 0
                    || !Set.of(20L, 30L).contains(from) || !Set.of("退回", "驳回").contains(String.valueOf(last.get("action_type")))
                    || time(last.get("create_time")).isBefore(CUTOFF) || time(last.get("create_time")).isBefore(time(sub.get("submit_time")))
                    || n(task.get("is_delete")) != 0 || n(task.get("level")) != 3
                    || db.queryForObject("SELECT COUNT(*) FROM biz_material_submission WHERE task_id=? AND sub_id>?", Integer.class, sub.get("task_id"), id) != 0) {
                skipped.add(Map.of("subId", id, "reason", "Not an unchanged latest transition-period reviewer return")); continue;
            }
            long next = from == 30 ? 20 : 10;
            long target = n(task.get(from == 30 ? "principal_id" : "auditor_id"));
            String problem = null;
            if (db.queryForObject("SELECT COUNT(*) FROM biz_material_submission WHERE task_id=? AND sub_id<>? AND COALESCE(is_delete,0)=0 AND flow_status IN(10,20,30)", Integer.class, sub.get("task_id"), id) != 0) problem = "Another active submission exists";
            if (target == 0) problem = "Previous reviewer is not configured";
            if (sub.get("reported_value") == null || task.get("current_value") == null
                    || new BigDecimal(sub.get("reported_value").toString()).compareTo(new BigDecimal(task.get("current_value").toString())) != 0) problem = "Reported value differs from restored task value; original material must be checked, no figures will be guessed";
            if (problem == null) {
                try { permissions.requireHandler(target, "tasks", n(task.get("task_id")), (int)n(task.get("phase")), (int)next); }
                catch (RuntimeException error) { problem = "Configured recipient cannot process this task: " + error.getMessage(); }
            }
            if (problem != null) { blocked.add(Map.of("subId", id, "taskId", n(task.get("task_id")), "reason", problem)); continue; }
            var ancestors = new ArrayList<Map<String, Object>>();
            var seen = new HashSet<Long>(); long parent = n(task.get("parent_id"));
            while (parent > 0) {
                if (!seen.add(parent) || seen.size() > 4) throw new IllegalStateException("Invalid task ancestry: " + id);
                var rows = db.queryForList("SELECT * FROM biz_task WHERE task_id=? FOR UPDATE", parent);
                if (rows.isEmpty() || n(rows.get(0).get("is_delete")) != 0) throw new IllegalStateException("Missing task ancestor: " + id);
                ancestors.add(rows.get(0)); parent = n(rows.get(0).get("parent_id"));
            }
            var change = new LinkedHashMap<String, Object>();
            change.put("before", sub); change.put("taskBefore", task); change.put("ancestorsBefore", ancestors);
            change.put("logs", logs); change.put("target", target); change.put("nextStatus", next); changes.add(change);
        }
        if (mode.equals("apply") && !blocked.isEmpty()) {
            write(output, mode, changes, skipped, blocked, false);
            throw new IllegalStateException("Legacy return repair requires review of " + blocked.size() + " blocked records; no business changes made");
        }
        if (mode.equals("apply")) for (var change : changes) {
            var before = (Map<?, ?>)change.get("before"); long id = n(before.get("sub_id")), taskId = n(before.get("task_id"));
            long target = n(change.get("target")), next = n(change.get("nextStatus"));
            if (db.update("UPDATE biz_material_submission SET flow_status=?,current_handler_id=?,is_delete=0 WHERE sub_id=? AND flow_status=? AND is_delete=1", next, target, id, before.get("flow_status")) != 1) throw new IllegalStateException("Submission changed: " + id);
            db.update("UPDATE biz_task SET status='2' WHERE task_id=?", taskId);
            for (var ancestor : (List<Map<String,Object>>)change.get("ancestorsBefore")) db.update("UPDATE biz_task SET status='2' WHERE task_id=?", ancestor.get("task_id"));
            // Only obsolete action requests are retired; original result history and audit logs remain.
            var stale = new LinkedHashSet<>(notices.staleTaskNotices(id));
            var last = ((List<Map<String,Object>>)change.get("logs")).get(0);
            stale.addAll(db.queryForList("SELECT notice_id FROM sys_notice WHERE source_type='0' AND source_id=? AND to_user_id=? AND from_user_id=? AND trigger_event='任务退回' AND is_read='0' AND COALESCE(is_delete,0)=0 AND create_time>=?", Long.class, taskId, before.get("submit_by"), last.get("operator_id"), last.get("create_time")));
            var retired = new ArrayList<Map<String,Object>>(); for (Long noticeId : stale) retired.add(db.queryForMap("SELECT * FROM sys_notice WHERE notice_id=? FOR UPDATE", noticeId));
            change.put("retiredNotices", retired); notices.retireIfPresent(new ArrayList<>(stale));
            var notice = new SysNotice(); notice.setFromUserId(n(last.get("operator_id"))); notice.setToUserId(target);
            notice.setTriggerEvent("任务退回"); notice.setTitle("任务退回"); notice.setContent("任务材料已退回，请重新审核");
            notice.setSourceType("0"); notice.setSourceId(taskId); notice.setIsRead("0"); notice.setIsDelete(0); notice.setCreateTime(new Date()); notices.sendNotice(notice);
            change.put("createdNotices", db.queryForList("SELECT * FROM sys_notice WHERE notice_id=LAST_INSERT_ID()"));
            change.put("after", db.queryForMap("SELECT * FROM biz_material_submission WHERE sub_id=?", id));
            change.put("taskAfter", db.queryForMap("SELECT * FROM biz_task WHERE task_id=?", taskId));
        }
        write(output, mode, changes, skipped, blocked, mode.equals("apply"));
        return Map.of("mode", mode, "changes", changes.size(), "blocked", blocked.size(), "skipped", skipped.size());
    }

    private void write(Path output, String mode, Object changes, Object skipped, Object blocked, boolean applied) throws IOException {
        var report = new LinkedHashMap<String,Object>(); report.put("batch", BATCH); report.put("mode", mode); report.put("cutoff", CUTOFF.toString()); report.put("applied", applied); report.put("changes", changes); report.put("skipped", skipped); report.put("blocked", blocked);
        Files.writeString(output, json.writerWithDefaultPrettyPrinter().writeValueAsString(report), StandardOpenOption.CREATE_NEW);
    }
    private static long n(Object value) { return value == null ? 0 : value instanceof Boolean b ? (b ? 1 : 0) : ((Number)value).longValue(); }
    private static LocalDateTime time(Object value) { return value instanceof Timestamp t ? t.toLocalDateTime() : LocalDateTime.parse(value.toString().replace(' ', 'T')); }
}
