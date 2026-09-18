package org.example.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.SysUser;
import org.example.entity.vo.ErrorVO;
import org.example.mapper.SysMapper;
import org.example.mapper.TokenBlacklistMapper;
import org.example.utils.JWTUtil;
import org.example.utils.PasswordPolicy;
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

    @Autowired
    private SysMapper sysMapper;

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
            SysUser user = sysMapper.getUserById(decodedJWT.getClaim("id").asLong());
            if (user == null || Integer.valueOf(1).equals(user.getIsDelete())) {
                sendError(response, 401, "Invalid Token");
                return false;
            }
            String path = request.getRequestURI().substring(request.getContextPath().length());
            while (path.endsWith("/") && path.length() > 1) path = path.substring(0, path.length() - 1);
            boolean passwordEndpoint = ("GET".equals(request.getMethod()) && "/system/password/status".equals(path))
                    || ("POST".equals(request.getMethod())
                    && ("/system/password".equals(path) || "/system/logout".equals(path)));
            if (PasswordPolicy.requiresChange(user.getPassword()) && !passwordEndpoint) {
                sendError(response, 428, "请先修改密码，新密码至少8位，并同时包含大写字母、小写字母和数字");
                return false;
            }
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
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(new ErrorVO(message, code)));
    }
}
