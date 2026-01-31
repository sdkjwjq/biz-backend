package org.example.controller;

import org.example.service.ScheduledTaskService;
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
    public String triggerManualReminder() {
        return scheduledTaskService.triggerMonthDeptLeaderReminder();
    }

    @PostMapping("/month_auditor_trigger")
    public String triggerAuditorMonthReminder() {
        return scheduledTaskService.triggerMonthAuditorReminder();

    }

    @PostMapping("/year_trigger")
    public String triggerYearReminder() {
        return scheduledTaskService.triggerYearlyReminder();
    }

    @PostMapping("/update_task_status")
    public void updateTaskStatus() {
        scheduledTaskService.updateTaskStatus();
    }
}
