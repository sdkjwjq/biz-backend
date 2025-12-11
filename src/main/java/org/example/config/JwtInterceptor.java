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

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenBlacklistMapper tokenBlacklistMapper;

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

    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(new ErrorVO(message, code)));
    }
}
