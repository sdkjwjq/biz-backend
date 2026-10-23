package org.example.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.utils.JWTUtil;
import org.example.entity.SysUser;
import org.example.mapper.SysMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * 用户角色拦截器：验证用户角色权限
 * 仅允许管理员角色（role=0）访问受保护接口
 */
@Component
public class UserRoleInterceptor implements HandlerInterceptor {

    /** 允许访问的角色：0-管理员 */
    private static final String ALLOWED_ROLE = "0";

    private final SysMapper sysMapper;
    @org.springframework.beans.factory.annotation.Autowired private org.example.service.PermissionService permissions;
    private final org.example.service.LeadershipAccessService access;

    public UserRoleInterceptor(SysMapper sysMapper, org.example.service.LeadershipAccessService access) {
        this.sysMapper = sysMapper;
        this.access = access;
    }

    /**
     * 角色验证，待调试，待完善
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        Long userId = JWTUtil.getUserIdFromToken(token);
        SysUser current = userId == null ? null : sysMapper.getUserById(userId);
        String path = request.getRequestURI().substring(request.getContextPath().length()).replaceAll("/+$", "");
        if ("GET".equals(request.getMethod()) && java.util.Set.of("/admin/overview", "/admin/overview/summary",
                "/admin/overview/analysis").contains(path) && (access.isLeader(userId) || permissions.allowed(userId,"overview.read"))) return true;

        // 管理接口每次读取数据库中的当前状态，停用或降级立即生效。
        if (current == null || !ALLOWED_ROLE.equals(current.getRole()) || !"1".equals(current.getStatus())
                || Integer.valueOf(1).equals(current.getIsDelete())) {
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
