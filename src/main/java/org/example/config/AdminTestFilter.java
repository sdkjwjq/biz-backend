package org.example.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.AdminOperationLog;
import org.example.entity.SysUser;
import org.example.entity.vo.ErrorVO;
import org.example.mapper.AdminManagementMapper;
import org.example.mapper.SysMapper;
import org.example.mapper.TokenBlacklistMapper;
import org.example.utils.AdminTestContext;
import org.example.utils.JWTUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;

/** Test operations use the real account; successful business writes and their trace commit together. */
@Component
public class AdminTestFilter extends OncePerRequestFilter {
    private final SysMapper users;
    private final TokenBlacklistMapper blacklist;
    private final AdminManagementMapper operations;
    private final ObjectMapper json;
    private final TransactionTemplate transaction;
    @org.springframework.beans.factory.annotation.Autowired private org.example.service.PermissionService permissions;

    public AdminTestFilter(SysMapper users, TokenBlacklistMapper blacklist, AdminManagementMapper operations,
                           ObjectMapper json, org.springframework.transaction.PlatformTransactionManager manager) {
        this.users = users; this.blacklist = blacklist; this.operations = operations;
        this.json = json; this.transaction = new TransactionTemplate(manager);
        this.transaction.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!"1".equals(request.getHeader("X-Admin-Test")) || "OPTIONS".equals(request.getMethod())
                || !path.matches("/(system|biz|achievement|dashboard|performance|budget|scheduled|work-records|manage|admin|core-indicators|expert-reviews)(/.*)?")
                || path.matches("/system/(login|password.*|logout)/?") || path.startsWith("/admin/permissions/")) {
            chain.doFilter(request, response); return;
        }
        Long actor;
        try {
            String token = request.getHeader("Authorization");
            if (token == null || blacklist.findByToken(token) != null) throw new IllegalArgumentException();
            actor = JWTUtil.verifyJwtToken(token).getClaim("id").asLong();
            SysUser user = actor == null ? null : users.getUserById(actor);
            if (user == null || !"0".equals(user.getRole()) || !"1".equals(user.getStatus())
                    || Integer.valueOf(1).equals(user.getIsDelete())) {
                error(response, 403, "管理员测试权限已失效，请退出测试并重新加载"); return;
            }
        } catch (Exception ignored) { error(response, 401, "登录已失效，请重新登录"); return; }
        String reason;
        try {
            reason = URLDecoder.decode(request.getHeader("X-Admin-Test-Reason"), StandardCharsets.UTF_8).strip();
            if (reason.isBlank() || reason.codePointCount(0, reason.length()) > 200) throw new IllegalArgumentException();
        } catch (Exception ignored) { error(response, 400, "请填写管理员测试原因（1至200字）"); return; }
        request.setAttribute(AdminTestContext.ACTOR, actor);
        try {
            if (Set.of("GET", "HEAD").contains(request.getMethod())) {
                chain.doFilter(request, response); return;
            }
            ContentCachingResponseWrapper buffered = new ContentCachingResponseWrapper(response);
            ContentCachingRequestWrapper traced = new ContentCachingRequestWrapper(request, 65536);
            try {
                TransactionTemplate currentTransaction=new TransactionTemplate(transaction.getTransactionManager(),transaction);
                if(path.startsWith("/work-records"))currentTransaction.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_REPEATABLE_READ);
                currentTransaction.executeWithoutResult(status -> {
                    try {
                        if(path.matches("/(admin/(users|departments)|system/users)(/.*)?"))permissions.lockChange();else permissions.lockOperation();
                        chain.doFilter(traced, buffered);
                        if (failed(buffered)) { status.setRollbackOnly(); return; }
                        AdminOperationLog log = new AdminOperationLog();
                        log.setModule("ADMIN_TEST"); log.setAction(request.getMethod());
                        log.setTargetType(path.split("/")[1].toUpperCase(java.util.Locale.ROOT));
                        // Only whitelisted numeric references: never persist forms, passwords, tokens or file contents.
                        Object variables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                        Map<String, Object> targets = targets(traced, buffered);
                        log.setTargetId(truncate(variables == null || "{}".equals(variables.toString()) ? targets.toString() : variables.toString(), 100));
                        log.setTargetName(truncate(path, 200)); log.setOperatorId(actor);
                        if (request.getAttribute(AdminTestContext.TARGET) instanceof AdminTestContext.Target target) {
                            log.setTargetType(target.type()); log.setTargetId(target.id());
                            log.setTargetName(truncate(target.name(), 200)); log.setAction(target.action());
                        }
                        log.setReason(reason); log.setCreateTime(new Date());
                        log.setAfterJson(json.writeValueAsString(Map.of("method", request.getMethod(), "path", path,
                                "operatorId", actor, "adminTest", true, "targets", targets)));
                        operations.insertOperation(log);
                    } catch (Exception failure) { throw new IllegalStateException("Admin test transaction failed", failure); }
                });
                buffered.copyBodyToResponse();
            } catch (Exception failure) {
                logger.error("Admin test operation rolled back: " + request.getMethod() + " " + path);
                buffered.reset(); error(response, 500, "测试操作未完成或记录失败，数据库修改已回滚，请刷新后重试");
            }
        } finally { request.removeAttribute(AdminTestContext.ACTOR); request.removeAttribute(AdminTestContext.TARGET); }
    }

    private Map<String, Object> targets(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String field : Set.of("id", "taskId", "task_id", "third_task_id", "sub_id", "subId", "perfId", "pref_id",
                "reportId", "recordId", "achId", "user_id", "userId", "deptId", "sheetId", "versionId", "indicatorId")) {
            String parameter = request.getParameter(field);
            if (parameter != null && parameter.matches("[0-9]{1,18}")) result.put(field, parameter);
        }
        if (request.getContentType() != null && request.getContentType().contains("application/json")) {
            collectIds(result, request.getContentAsByteArray());
        }
        if (response.getContentType() != null && response.getContentType().contains("application/json")) {
            collectIds(result, response.getContentAsByteArray());
        }
        return result;
    }

    private void collectIds(Map<String, Object> targets, byte[] body) {
        if (body.length == 0 || body.length > 65536) return;
        try {
            JsonNode node = json.readTree(body);
            for (String field : Set.of("id", "taskId", "task_id", "third_task_id", "sub_id", "subId", "perfId", "pref_id",
                    "reportId", "recordId", "achId", "user_id", "userId", "deptId", "sheetId", "versionId", "indicatorId", "fileId")) {
                if (node.path(field).isIntegralNumber()) targets.put(field, node.get(field).asText());
                if (node.path("record").path(field).isIntegralNumber()) targets.put(field, node.path("record").get(field).asText());
            }
            for (String field : Set.of("ids", "taskIds", "perfIds", "recordIds")) {
                JsonNode ids = node.path(field);
                if (ids.isArray()) {
                    java.util.List<String> values = new java.util.ArrayList<>();
                    for (JsonNode id : ids) if (id.isIntegralNumber() && values.size() < 200) values.add(id.asText());
                    targets.put(field, values); targets.put(field + "Count", ids.size());
                }
            }
        } catch (IOException ignored) { /* Non-object responses contribute no references. */ }
    }

    private boolean failed(ContentCachingResponseWrapper response) throws IOException {
        if (response.getStatus() >= 400) return true;
        if (response.getContentType() != null && response.getContentType().contains("application/json")) {
            byte[] body = response.getContentAsByteArray();
            if (body.length > 0) {
                JsonNode value = json.readTree(body);
                return value.has("code") && value.path("code").asInt() >= 400;
            }
        }
        return false;
    }

    private static String truncate(String value, int length) {
        return value.length() <= length ? value : value.substring(0, length);
    }

    private void error(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status); response.setCharacterEncoding("UTF-8"); response.setContentType("application/json");
        response.getWriter().write(json.writeValueAsString(new ErrorVO(message, status)));
    }
}
