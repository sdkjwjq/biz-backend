package org.example.controller;

import org.example.service.AdminManagementException;
import org.example.service.PermissionService;
import org.example.service.TaskSupervisionService;
import org.example.entity.vo.ErrorVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/supervision")
public class TaskSupervisionController {
    private final TaskSupervisionService reminders;
    private final PermissionService access;

    public TaskSupervisionController(TaskSupervisionService reminders, PermissionService access) {
        this.reminders = reminders;
        this.access = access;
    }

    @PostMapping("/reminders")
    public Map<String, String> send(@RequestBody TaskSupervisionService.Reminder input) {
        reminders.send(access.actor(), input);
        return Map.of("message", "督办信息已发送");
    }

    @ExceptionHandler(AdminManagementException.class)
    public ResponseEntity<ErrorVO> error(AdminManagementException e) {
        return ResponseEntity.status(e.getCode()).body(new ErrorVO(e.getMessage(), e.getCode()));
    }
}
