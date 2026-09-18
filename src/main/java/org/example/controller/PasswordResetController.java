package org.example.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.entity.dto.SysPasswordResetDTO;
import org.example.entity.vo.ErrorVO;
import org.example.service.SysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 登录前凭原密码修改密码，不创建登录会话。 */
@RestController
@RequestMapping("/system/password")
public class PasswordResetController {
    @Autowired
    private SysService sysService;

    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody SysPasswordResetDTO body) {
        try {
            String id = body.getUser_id() == null ? "" : body.getUser_id().asText("");
            if (!id.matches("[0-9]+")) throw new IllegalArgumentException("请输入有效的数字账号");
            long userId;
            try {
                userId = Long.parseLong(id);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("请输入有效的数字账号");
            }
            if (userId <= 0) throw new IllegalArgumentException("请输入有效的数字账号");
            JsonNode oldPassword = body.getOld_password();
            JsonNode newPassword = body.getNew_password();
            if (oldPassword == null || !oldPassword.isTextual() || oldPassword.asText().isEmpty()) {
                throw new IllegalArgumentException("请输入原密码");
            }
            if (newPassword == null || !newPassword.isTextual()) throw new IllegalArgumentException("请输入新密码");
            sysService.resetPassword(userId, oldPassword.asText(), newPassword.asText());
            return ResponseEntity.ok(java.util.Map.of("message", "密码重置成功，请使用新密码登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorVO(e.getMessage(), 400));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(new ErrorVO("账号或原密码错误", 401));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorVO("密码重置失败，请稍后重试", 500));
        }
    }
}
