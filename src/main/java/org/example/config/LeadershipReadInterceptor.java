package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.vo.ErrorVO;
import org.example.service.LeadershipAccessService;
import org.example.utils.JWTUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

/** 在读取对象之前校验领导范围，避免通过详情、日志和文件链接绕过列表。 */
@Component
public class LeadershipReadInterceptor implements HandlerInterceptor {
    private final LeadershipAccessService access;
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    @org.springframework.beans.factory.annotation.Autowired private org.example.service.PermissionService permissions;
    public LeadershipReadInterceptor(LeadershipAccessService access, JdbcTemplate jdbc, ObjectMapper json) {
        this.access = access; this.jdbc = jdbc; this.json = json;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) return true;
        Long userId = JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
        if (userId == null) return true;
        if (permissions.enforced(userId)) return true;
        var scope = access.scope(userId);
        if (!scope.leader() && scope.version() == 0) return true;
        if (scope.leader() && !access.isLeader(userId)) return deny(response, "账号已停用或不存在");
        if (access.isAdmin(userId)) return true;
        String path = request.getRequestURI().substring(request.getContextPath().length()).replaceAll("/+$", "");
        if (!"GET".equals(request.getMethod())) {
            if (path.equals("/performance/refresh") || path.startsWith("/scheduled/") || path.equals("/dashboard/trend/record"))
                return deny(response, "校级领导查阅权限不包含管理写操作");
            return true; // 填报和审核仍由原业务职责校验，不能由领导身份直接放行。
        }
        Map<String, String> variables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (variables == null) variables = Map.of();
        Long taskId = number(variables.get("taskId"));
        if (path.startsWith("/biz/tasks/")) {
            if (request.getParameter("task_id") != null) taskId = number(request.getParameter("task_id"));
            if (request.getParameter("parent_id") != null) taskId = number(request.getParameter("parent_id"));
        }
        if (path.startsWith("/biz/audit/logs/")) taskId = linked("SELECT task_id FROM biz_material_submission WHERE sub_id=? AND COALESCE(is_delete,0)=0", variables.get("subId"));
        if (taskId != null && !access.canAccessTask(userId, taskId)) return deny(response, "任务不存在或不在查阅范围内");
        if (path.startsWith("/biz/audit/logs/") && taskId == null) return deny(response, "记录不存在或不在查阅范围内");
        Long perfId = number(variables.get("perfId"));
        if (perfId == null) perfId = number(variables.get("prefId"));
        if (path.startsWith("/performance/audit/logs/")) perfId = linked("SELECT perf_id FROM biz_performance_submission WHERE sub_id=? AND COALESCE(is_delete,0)=0", variables.get("subId"));
        if (perfId != null && !access.canAccessPerformance(userId, perfId)) return deny(response, "绩效不存在或不在查阅范围内");
        if (path.startsWith("/performance/audit/logs/") && perfId == null) return deny(response, "记录不存在或不在查阅范围内");
        if (path.startsWith("/system/download/")) {
            Long fileId = number(variables.get("file_id"));
            if (fileId == null || !canDownload(userId, fileId)) return deny(response, "材料不存在或不在查阅范围内");
        }
        if (path.startsWith("/dashboard/dept/") && variables.containsKey("deptId")
                && !access.canReadDepartment(userId, number(variables.get("deptId")))) return deny(response, "部门不在查阅范围内");
        return true;
    }

    private boolean canDownload(Long userId, Long fileId) {
        for (Long taskId : jdbc.queryForList("SELECT DISTINCT task_id FROM biz_material_submission WHERE file_id=? AND COALESCE(is_delete,0)=0", Long.class, fileId))
            if (access.canAccessTask(userId, taskId)) return true;
        if (!access.isLeader(userId)) return jdbc.queryForObject("SELECT COUNT(*) FROM sys_file WHERE file_id=? AND upload_by=? AND COALESCE(is_delete,0)=0", Integer.class, fileId, userId) > 0;
        return jdbc.queryForObject("SELECT (EXISTS(SELECT 1 FROM biz_achievement WHERE file_id=? AND COALESCE(is_delete,0)=0) OR EXISTS(SELECT 1 FROM biz_budget_sheet WHERE file_id=? AND COALESCE(is_delete,0)=0) OR EXISTS(SELECT 1 FROM biz_budget_import_version WHERE file_id=? AND COALESCE(is_delete,0)=0))", Integer.class,
                fileId, fileId, fileId) == 1;
    }

    private Long linked(String sql, String id) {
        Long value = number(id);
        if (value == null) return null;
        return jdbc.queryForList(sql, Long.class, value).stream().findFirst().orElse(null);
    }
    private Long number(String value) {
        try { return value == null ? null : Long.valueOf(value); } catch (NumberFormatException exception) { return null; }
    }
    private boolean deny(HttpServletResponse response, String message) throws Exception {
        response.setStatus(403); response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json.writeValueAsString(new ErrorVO(message, 403)));
        return false;
    }
}
