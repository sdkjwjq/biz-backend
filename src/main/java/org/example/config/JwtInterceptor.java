package org.example.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.vo.ErrorVO;
import org.example.mapper.TokenBlacklistMapper;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * JWT拦截器：验证请求中的Token有效性
 * - 检查Token是否存在
 * - 验证Token是否在黑名单中
 * - 解析Token并设置用户角色信息
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenBlacklistMapper tokenBlacklistMapper;

    /**
     * 前置处理：验证Token
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @return 验证通过返回true，否则返回false
     * @throws Exception 异常信息
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            sendError(response, 401, "No Token");
            return false;
        }

        // 检查黑名单
        if (tokenBlacklistMapper.findByToken(token) != null) {
            sendError(response, 401, "Invalid Token");
            return false;
        }

        try {
            DecodedJWT decodedJWT = JWTUtil.verifyJwtToken(token);
            request.setAttribute("userRole", decodedJWT.getClaim("role").asString());
            return true;
        } catch (Exception e) {
            sendError(response, 401, "No Token: " + e.getMessage());
            return false;
        }
    }

    /**
     * 发送错误响应
     * @param response HTTP响应
     * @param code 错误码
     * @param message 错误信息
     * @throws IOException IO异常
     */
    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(new ErrorVO(message, code)));
    }
}