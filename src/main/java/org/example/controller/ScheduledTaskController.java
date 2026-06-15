package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.vo.ErrorVO;
import org.example.service.ScheduledTaskService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scheduled")
public class ScheduledTaskController {

    @Autowired
    private ScheduledTaskService scheduledTaskService;

    @PostMapping("/month_leader_trigger")
    public Object triggerManualReminder(HttpServletRequest request) {
        try {
            ensureAdmin(request);
            return scheduledTaskService.triggerMonthDeptLeaderReminder();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @PostMapping("/month_auditor_trigger")
    public Object triggerAuditorMonthReminder(HttpServletRequest request) {
        try {
            ensureAdmin(request);
            return scheduledTaskService.triggerMonthAuditorReminder();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @PostMapping("/year_trigger")
    public Object triggerYearReminder(HttpServletRequest request) {
        try {
            ensureAdmin(request);
            return scheduledTaskService.triggerYearlyReminder();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @PostMapping("/update_task_status")
    public Object updateTaskStatus(HttpServletRequest request) {
        try {
            ensureAdmin(request);
            scheduledTaskService.updateTaskStatus();
            return "任务状态更新完成";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    private void ensureAdmin(HttpServletRequest request) {
        String role = JWTUtil.getRoleFromToken(request.getHeader("Authorization"));
        if (!"0".equals(role)) {
            throw new RuntimeException("仅管理员可以操作定时任务");
        }
    }
}
