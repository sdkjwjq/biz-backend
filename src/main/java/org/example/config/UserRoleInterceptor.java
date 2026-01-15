package org.example.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.utils.JWTUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

@Component
public class UserRoleInterceptor implements HandlerInterceptor {

    // 仅允许角色0访问（管理员）
    private static final String ALLOWED_ROLE = "0";
    //    角色验证,待调试, 待完善
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头解析Token获取角色（你的真实逻辑）
        String token = request.getHeader("Authorization");
        String userRole = JWTUtil.getRoleFromToken(token);

        // 2. 角色校验：无角色/角色非0则拒绝访问
        if (userRole == null || !ALLOWED_ROLE.equals(userRole)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpStatus.FORBIDDEN.value());
            PrintWriter writer = response.getWriter();
            writer.write("{\"code\":403,\"msg\":\"无权限访问该接口，仅管理员可访问\"}");
            writer.flush();
            writer.close();
            return false;
        }

        // 角色校验通过，放行请求
        return true;
    }
}