package org.example.service;

import org.example.entity.BizTask;
import org.example.entity.SysNotice;
import org.example.entity.SysUser;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/** Task reminders use task reviewer duties, independently of the generic notice permission. */
@Service
public class TaskSupervisionService {
    public record Reminder(Long taskId, Long toUserId, String title, String content) { }

    private final PermissionService access;
    private final BizMapper tasks;
    private final SysMapper users;

    public TaskSupervisionService(PermissionService access, BizMapper tasks, SysMapper users) {
        this.access = access;
        this.tasks = tasks;
        this.users = users;
    }

    @Transactional
    public void send(Long actor, Reminder input) {
        access.require(actor, "tasks.supervise");
        if (input == null || input.taskId() == null || input.toUserId() == null)
            throw new AdminManagementException(400, "请选择任务和督办对象");
        BizTask task = tasks.getTaskById(input.taskId());
        if (task == null || !access.canRead(actor, "tasks", task.getTaskId(), task.getPhase()) || !access.canSuperviseTask(actor, task))
            throw new AdminManagementException(403, "任务不在督办范围内");
        if (!Objects.equals(input.toUserId(), task.getLeaderId()) && !Objects.equals(input.toUserId(), task.getPrincipalId()))
            throw new AdminManagementException(400, "督办对象必须是该任务的负责人或归口审核人");
        if (Objects.equals(actor, input.toUserId()))
            throw new AdminManagementException(400, "不能督办自己");
        SysUser recipient = users.getUserById(input.toUserId());
        if (recipient == null || !"1".equals(recipient.getStatus()) || Integer.valueOf(1).equals(recipient.getIsDelete()))
            throw new AdminManagementException(400, "督办对象不存在或已停用");
        String title = input.title() == null ? "" : input.title().trim();
        String content = input.content() == null ? "" : input.content().trim();
        if (title.isEmpty() || title.length() > 200 || content.isEmpty() || content.length() > 2000)
            throw new AdminManagementException(400, "请填写主题摘要和详细说明（主题不超过200字，说明不超过2000字）");

        SysNotice notice = new SysNotice();
        notice.setFromUserId(actor);
        notice.setToUserId(input.toUserId());
        notice.setType("3");
        notice.setTriggerEvent("TASK");
        notice.setTitle(title);
        notice.setContent(content);
        notice.setSourceType("0");
        notice.setSourceId(task.getTaskId());
        notice.setIsRead("0");
        notice.setIsDelete(0);
        notice.setCreateTime(new Date());
        users.sendNotice(notice);
    }

    public Map<String, Object> sent(Long actor, int requestedPage) {
        access.require(actor, "tasks.supervise");
        int page = Math.max(1, requestedPage);
        int pageSize = 10;
        long total = users.countSentTaskReminders(actor);
        long offset = Math.min((long) (page - 1) * pageSize, total);
        return Map.of("items", users.sentTaskReminders(actor, pageSize, offset),
                "total", total, "page", page, "pageSize", pageSize);
    }
}
