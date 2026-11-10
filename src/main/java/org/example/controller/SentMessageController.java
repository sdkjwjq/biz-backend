package org.example.controller;

import org.example.entity.vo.ErrorVO;
import org.example.service.AdminManagementException;
import org.example.service.PermissionService;
import org.example.service.SentMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/messages")
public class SentMessageController {
    private final SentMessageService messages;
    private final PermissionService access;

    public SentMessageController(SentMessageService messages, PermissionService access) {
        this.messages = messages;
        this.access = access;
    }

    @GetMapping("/sent")
    public Map<String, Object> sent(@RequestParam(defaultValue = "1") int page) {
        return messages.sent(access.actor(), page);
    }

    @PostMapping("/{noticeId}/recall")
    public Map<String, String> recall(@PathVariable Long noticeId) {
        messages.recall(access.actor(), noticeId);
        return Map.of("message", "消息已撤回");
    }

    @ExceptionHandler(AdminManagementException.class)
    public ResponseEntity<ErrorVO> error(AdminManagementException e) {
        return ResponseEntity.status(e.getCode()).body(new ErrorVO(e.getMessage(), e.getCode()));
    }
}
