package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.vo.ErrorVO;
import org.example.service.AdminManagementException;
import org.example.service.LeadershipAccessService;
import org.example.utils.JWTUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class LeadershipAccessController {
    private final LeadershipAccessService access;
    public LeadershipAccessController(LeadershipAccessService access) { this.access = access; }

    @GetMapping("/system/access")
    public Object current(HttpServletRequest request) { return access.capabilities(user(request)); }

    @GetMapping("/admin/users/{userId}/leadership")
    public Object configuration(@PathVariable Long userId, HttpServletRequest request) {
        return access.configuration(userId, user(request));
    }

    @PutMapping("/admin/users/{userId}/leadership")
    public Object save(@PathVariable Long userId, @RequestBody LeadershipAccessService.Change body,
                       HttpServletRequest request) throws Exception {
        return access.save(userId, body, user(request));
    }

    @ExceptionHandler(AdminManagementException.class)
    public ResponseEntity<ErrorVO> error(AdminManagementException exception) {
        return ResponseEntity.status(exception.getCode()).body(new ErrorVO(exception.getMessage(), exception.getCode()));
    }

    @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorVO> invalidParameter(Exception exception) {
        return ResponseEntity.badRequest().body(new ErrorVO("查阅范围参数格式不正确", 400));
    }

    private Long user(HttpServletRequest request) { return JWTUtil.getUserIdFromToken(request.getHeader("Authorization")); }
}
