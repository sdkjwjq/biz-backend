package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.service.SchoolSsoService;
import org.example.entity.vo.ErrorVO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/system/sso")
public class SchoolSsoController {
    private final SchoolSsoService sso;

    public SchoolSsoController(SchoolSsoService sso) { this.sso = sso; }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("enabled", sso.available(), "logoutUrl", sso.logoutUrl());
    }

    @PostMapping("/start")
    public ResponseEntity<?> start(HttpServletRequest request) { return sso.start(request); }

    @PostMapping("/exchange")
    public ResponseEntity<?> exchange(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        try {
            return sso.exchange(payload.get("code"), payload.get("state"), request);
        } catch (IllegalArgumentException | SecurityException error) {
            return ResponseEntity.badRequest().header(HttpHeaders.SET_COOKIE, sso.clearStateCookie(request))
                    .body(new ErrorVO(error.getMessage(), 400));
        } catch (RuntimeException error) {
            return ResponseEntity.status(502).header(HttpHeaders.SET_COOKIE, sso.clearStateCookie(request))
                    .body(new ErrorVO("学校统一认证暂时无法完成登录，请稍后重试", 502));
        }
    }
}
