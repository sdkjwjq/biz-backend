package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.SysNotice;
import org.example.mapper.SysMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Offline deployment maintenance. Never exposed as HTTP or recorded as an approval. */
@Service
public class AuditWorkflowMaintenance {
    private final JdbcTemplate db;
    private final ObjectMapper json;
    private final PermissionManagementService permissions;
    private final SysMapper notices;
    public AuditWorkflowMaintenance(JdbcTemplate db,ObjectMapper json,PermissionManagementService permissions,SysMapper notices) {
        this.db=db;this.json=json;this.permissions=permissions;this.notices=notices;
    }
    @Transactional(rollbackFor=Exception.class)
    public Map<String,Object> run(String mode,Path output) throws IOException {
        if(!Set.of("check","apply").contains(mode))throw new IllegalArgumentException("Expected check or apply");
        if(Files.exists(output))throw new IllegalArgumentException("Maintenance report already exists");
        JsonNode manifest;
        try(var stream=getClass().getResourceAsStream("/maintenance/audit-workflow-20261103.json")){manifest=json.readTree(Objects.requireNonNull(stream));}
        List<Map<String,Object>> changes=new ArrayList<>(),skipped=new ArrayList<>();
        // Lock and validate every candidate before changing any business row.
        for(JsonNode candidate:manifest.path("tasks")) {
            long id=candidate.path("subId").asLong();
            var rows=db.queryForList("SELECT * FROM biz_material_submission WHERE sub_id=? FOR UPDATE",id);
            if(rows.isEmpty()){skipped.add(Map.of("subId",id,"reason","missing"));continue;}
            var before=rows.get(0);long state=number(before.get("flow_status"));
            if(number(before.get("is_delete"))!=0||state!=20){skipped.add(Map.of("subId",id,"reason","no longer at principal review"));continue;}
            var taskRows=db.queryForList("SELECT * FROM biz_task WHERE task_id=? FOR UPDATE",candidate.path("taskId").asLong());
            if(taskRows.isEmpty()||number(taskRows.get(0).get("is_delete"))!=0){skipped.add(Map.of("subId",id,"reason","task unavailable"));continue;}
            var task=taskRows.get(0);long target=candidate.path("newHandler").asLong();
            boolean sameTask=Objects.equals(String.valueOf(task.get("task_code")),candidate.path("taskCode").asText())
                    &&number(task.get("phase"))==candidate.path("year").asLong()
                    &&number(before.get("task_id"))==candidate.path("taskId").asLong()
                    &&java.time.LocalDateTime.parse(String.valueOf(before.get("submit_time")).replace(' ','T')).equals(java.time.LocalDateTime.parse(candidate.path("submitTime").asText().replace(' ','T')));
            if(!sameTask||state!=20||number(task.get("principal_id"))!=target)throw new IllegalStateException("Pending configuration changed: "+id);
            if(number(before.get("current_handler_id"))==target){skipped.add(Map.of("subId",id,"reason","already aligned"));continue;}
            if(number(before.get("current_handler_id"))!=candidate.path("oldHandler").asLong())throw new IllegalStateException("Pending handler changed: "+id);
            db.queryForList("SELECT user_id FROM sys_user WHERE user_id=? FOR UPDATE",target);
            permissions.requireHandler(target,"tasks",candidate.path("taskId").asLong(),candidate.path("year").asInt(),20);
            var change=new LinkedHashMap<String,Object>();change.put("kind","task");change.put("before",before);change.put("task",task);change.put("target",target);changes.add(change);
        }
        for(JsonNode candidate:manifest.path("deletedAchievements")) {
            long id=candidate.path("subId").asLong(),ach=candidate.path("achId").asLong();
            var rows=db.queryForList("SELECT * FROM biz_achievement_submission WHERE sub_id=? FOR UPDATE",id);
            if(rows.isEmpty()||number(rows.get(0).get("is_delete"))!=0||number(rows.get(0).get("flow_status"))!=10){skipped.add(Map.of("achievementSubId",id,"reason","no longer pending"));continue;}
            var parent=db.queryForList("SELECT * FROM biz_achievement WHERE ach_id=? FOR UPDATE",ach);
            if(number(rows.get(0).get("ach_id"))!=ach||parent.isEmpty()||number(parent.get(0).get("is_delete"))!=1)throw new IllegalStateException("Achievement configuration changed: "+id);
            var change=new LinkedHashMap<String,Object>();change.put("kind","achievement");change.put("before",rows.get(0));change.put("task",parent.get(0));changes.add(change);
        }
        if(mode.equals("apply"))for(var change:changes) {
            var before=(Map<?,?>)change.get("before");long id=number(before.get("sub_id"));
            if(change.get("kind").equals("task")) {
                long target=number(change.get("target")),task=number(before.get("task_id"));
                if(db.update("UPDATE biz_material_submission SET current_handler_id=? WHERE sub_id=? AND flow_status=20 AND current_handler_id=? AND COALESCE(is_delete,0)=0",target,id,before.get("current_handler_id"))!=1)throw new IllegalStateException("Pending update conflict: "+id);
                List<Long> stale=notices.staleTaskNotices(id);change.put("retiredNotices",noticeRows(stale));notices.retireIfPresent(stale);
                var existing=db.queryForList("SELECT notice_id FROM sys_notice WHERE source_type='0' AND source_id=? AND to_user_id=? AND trigger_event='任务审核' AND COALESCE(is_delete,0)=0",task,target);
                if(existing.isEmpty()) {
                    SysNotice notice=new SysNotice();notice.setFromUserId(number(before.get("submit_by")));notice.setToUserId(target);notice.setTriggerEvent("任务审核");notice.setTitle("任务审核");notice.setContent("您有新的任务需要审核");notice.setSourceType("0");notice.setSourceId(task);notice.setIsRead("0");notice.setIsDelete(0);notice.setCreateTime(new Date());notices.sendNotice(notice);
                    change.put("createdNotices",db.queryForList("SELECT * FROM sys_notice WHERE notice_id=LAST_INSERT_ID()"));
                }
                change.put("after",db.queryForMap("SELECT * FROM biz_material_submission WHERE sub_id=?",id));
                change.put("logs",db.queryForList("SELECT * FROM biz_audit_log WHERE sub_id=? ORDER BY log_id",id));
            } else {
                if(db.update("UPDATE biz_achievement_submission SET is_delete=1 WHERE sub_id=? AND flow_status=10 AND COALESCE(is_delete,0)=0",id)!=1)throw new IllegalStateException("Achievement update conflict: "+id);
                List<Long> stale=notices.staleAchievementNotices(id);change.put("retiredNotices",noticeRows(stale));notices.retireIfPresent(stale);
                change.put("after",db.queryForMap("SELECT * FROM biz_achievement_submission WHERE sub_id=?",id));
                change.put("logs",db.queryForList("SELECT * FROM biz_achievement_audit_log WHERE sub_id=? ORDER BY log_id",id));
            }
        }
        var report=new LinkedHashMap<String,Object>();report.put("batch",manifest.path("batch").asText());report.put("mode",mode);report.put("changes",changes);report.put("skipped",skipped);
        // Write inside the transaction: failure to preserve rollback evidence cancels all changes.
        Files.writeString(output,json.writerWithDefaultPrettyPrinter().writeValueAsString(report),StandardOpenOption.CREATE_NEW);
        return Map.of("mode",mode,"changes",changes.size(),"skipped",skipped.size());
    }
    private List<Map<String,Object>> noticeRows(List<Long> ids){List<Map<String,Object>> rows=new ArrayList<>();for(Long id:ids)rows.add(db.queryForMap("SELECT * FROM sys_notice WHERE notice_id=? FOR UPDATE",id));return rows;}
    private static long number(Object value){return value==null?0:value instanceof Boolean b?(b?1:0):value instanceof Number n?n.longValue():Long.parseLong(value.toString());}
    private JsonNode tree(Object value) throws IOException {return json.readTree(json.writeValueAsBytes(value));}

    @Transactional(rollbackFor=Exception.class)
    public void rollback(Path report) throws IOException {
        JsonNode saved=json.readTree(Files.readString(report));
        if(!saved.path("mode").asText().equals("apply")||!saved.path("batch").asText().equals("audit-workflow-20261103-v1"))throw new IllegalArgumentException("Not an applied maintenance report");
        for(JsonNode change:saved.path("changes")) {
            boolean task=change.path("kind").asText().equals("task");String table=task?"biz_material_submission":"biz_achievement_submission",log=task?"biz_audit_log":"biz_achievement_audit_log";
            long id=change.path("before").path("sub_id").asLong();var current=db.queryForMap("SELECT * FROM "+table+" WHERE sub_id=? FOR UPDATE",id);
            if(!tree(current).equals(change.path("after"))||!tree(db.queryForList("SELECT * FROM "+log+" WHERE sub_id=? ORDER BY log_id",id)).equals(change.path("logs")))throw new IllegalStateException("Subsequent business changes prevent rollback: "+id);
            String parent=task?"biz_task":"biz_achievement",key=task?"task_id":"ach_id";
            if(!tree(db.queryForMap("SELECT * FROM "+parent+" WHERE "+key+"=? FOR UPDATE",change.path("task").path(key).asLong())).equals(change.path("task")))throw new IllegalStateException("Configuration changed after maintenance: "+id);
            for(JsonNode n:change.path("createdNotices")){var row=db.queryForMap("SELECT * FROM sys_notice WHERE notice_id=? FOR UPDATE",n.path("notice_id").asLong());if(!tree(row).equals(n))throw new IllegalStateException("Notice changed after maintenance");}
            for(JsonNode n:change.path("retiredNotices")){var row=db.queryForMap("SELECT * FROM sys_notice WHERE notice_id=? FOR UPDATE",n.path("notice_id").asLong());if(number(row.get("is_delete"))!=1)throw new IllegalStateException("Notice changed after maintenance");row.put("is_delete",json.convertValue(n.get("is_delete"),Object.class));if(!tree(row).equals(n))throw new IllegalStateException("Notice changed after maintenance");}
        }
        for(JsonNode change:saved.path("changes")) {
            long id=change.path("before").path("sub_id").asLong();
            if(change.path("kind").asText().equals("task"))db.update("UPDATE biz_material_submission SET current_handler_id=? WHERE sub_id=?",change.path("before").path("current_handler_id").asLong(),id);
            else db.update("UPDATE biz_achievement_submission SET is_delete=0 WHERE sub_id=?",id);
            for(JsonNode n:change.path("createdNotices"))db.update("DELETE FROM sys_notice WHERE notice_id=?",n.path("notice_id").asLong());
            for(JsonNode n:change.path("retiredNotices"))db.update("UPDATE sys_notice SET is_delete=? WHERE notice_id=?",n.path("is_delete").isNull()?null:n.path("is_delete").asInt(),n.path("notice_id").asLong());
        }
    }
}
