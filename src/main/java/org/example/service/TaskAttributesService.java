package org.example.service;

import org.example.entity.BizTask;
import org.example.mapper.BizMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** 仅更新任务管理属性，避免覆盖填报、归属及审批数据。 */
@Service
public class TaskAttributesService {
    private final BizMapper tasks;
    @org.springframework.beans.factory.annotation.Autowired private PermissionService permissions;
    private final JdbcTemplate jdbc;
    private final LeadershipAccessService access;
    private final AdminManagementService logs;
    public TaskAttributesService(BizMapper tasks, JdbcTemplate jdbc, LeadershipAccessService access, AdminManagementService logs) {
        this.tasks=tasks; this.jdbc=jdbc; this.access=access; this.logs=logs;
    }
    public record Change(Boolean priority, Boolean difficult, LocalDate plannedCompletionDate, Long version, String reason) {}
    private Map<String,Object> values(BizTask task) {
        Map<String,Object> value=new LinkedHashMap<>();
        value.put("priority", Boolean.TRUE.equals(task.getIsPriority()));
        value.put("difficult", Boolean.TRUE.equals(task.getIsDifficult()));
        value.put("plannedCompletionDate",task.getPlannedCompletionDate());
        return value;
    }
    @Transactional(rollbackFor=Exception.class)
    public BizTask save(Long id, Change input, Long operator) {
        if (!access.isAdmin(operator)&&!(permissions.enforced(operator)&&permissions.allowed(operator,"tasks.attributes"))) throw new TaskManagementException(403,"仅管理员可修改任务管理属性");
        if (input==null || input.priority()==null || input.difficult()==null || input.version()==null || input.version()<0
                || input.plannedCompletionDate()==null || input.plannedCompletionDate().getYear()<1000 || input.plannedCompletionDate().getYear()>9999
                || input.reason()==null || input.reason().isBlank() || input.reason().length()>200)
            throw new TaskManagementException(400,"请填写标记、有效计划日期及变更原因（最多200字）");
        BizTask task=tasks.getTaskByIdForUpdate(id);
        if (task==null || Integer.valueOf(1).equals(task.getIsDelete())) throw new TaskManagementException(404,"任务不存在或已删除");
        if (!Integer.valueOf(3).equals(task.getLevel())) throw new TaskManagementException(400,"仅三级任务可以修改管理属性");
        if (!input.version().equals(task.getManagementVersion())) throw new TaskManagementException(409,"任务管理属性已变更，请重新加载");
        Map<String,Object> before=values(task);
        jdbc.update("UPDATE biz_task SET is_priority=?,is_difficult=?,planned_completion_date=?,management_version=management_version+1,update_time=NOW() WHERE task_id=?",
                input.priority(),input.difficult(),input.plannedCompletionDate(),id);
        BizTask after=tasks.getTaskById(id);
        logs.recordOperation("TASK","ATTRIBUTES","TASK",String.valueOf(id),task.getTaskName(),operator,input.reason().trim(),before,values(after));
        return after;
    }
}
