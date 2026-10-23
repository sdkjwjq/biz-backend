package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.entity.AdminOperationLog;
import org.example.entity.SysDept;
import org.example.entity.SysUser;
import org.example.entity.dto.AdminActionDTO;
import org.example.entity.dto.AdminDepartmentDTO;
import org.example.entity.dto.AdminUserDTO;
import org.example.mapper.AdminManagementMapper;
import org.example.mapper.SysMapper;
import org.example.utils.AchievementPermissionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class AdminManagementService {
    @org.springframework.beans.factory.annotation.Autowired private PermissionService permissions;
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private final AdminManagementMapper mapper;
    private final SysMapper users;
    private final ObjectMapper json;
    private final LeadershipAccessService leadershipAccess;
    private final WorkRecordScopeService workRecordScopes;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    public AdminManagementService(AdminManagementMapper mapper, SysMapper users, ObjectMapper json,
                                   LeadershipAccessService leadershipAccess, org.springframework.jdbc.core.JdbcTemplate jdbc,
                                   WorkRecordScopeService workRecordScopes) {
        this.mapper = mapper;
        this.users = users;
        this.json = json;
        this.leadershipAccess = leadershipAccess;
        this.jdbc = jdbc;
        this.workRecordScopes = workRecordScopes;
    }

    public void requireAdmin(Long userId) {
        SysUser user = userId == null ? null : users.getUserById(userId);
        if (user == null || !"0".equals(user.getRole()) || !"1".equals(user.getStatus())
                || Integer.valueOf(1).equals(user.getIsDelete())) {
            throw new AdminManagementException(403, "仅正常状态的管理员可以访问管理中心");
        }
    }

    public Map<String, Object> overview(Integer year, Integer month, Long operatorId) {
        if (!leadershipAccess.isLeader(operatorId)&&!permissions.allowed(operatorId,"overview.read")) requireAdmin(operatorId);
        validateYearMonth(year, month);
        List<Map<String, Object>> taskIssues = mapper.taskIssues(year);
        List<Map<String, Object>> performanceIssues = mapper.performanceIssues(year);
        if (permissions.configured(operatorId)) {
            taskIssues=taskIssues.stream().filter(r->permissions.canRead(operatorId,"tasks",number(r.get("taskId")),year)).toList();
            performanceIssues=performanceIssues.stream().filter(r->permissions.canRead(operatorId,"performance",number(r.get("perfId")),year)).toList();
        } else if (leadershipAccess.restricted(operatorId)) {
            taskIssues = taskIssues.stream().filter(row -> leadershipAccess.canReadDepartment(operatorId, number(row.get("deptId")))).toList();
            performanceIssues = performanceIssues.stream().filter(row -> leadershipAccess.canReadDepartment(operatorId, number(row.get("deptId")))).toList();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("activeUsers", mapper.userCountByStatus("1"));
        result.put("disabledUsers", mapper.userCountByStatus("0"));
        result.put("taskIssueCount", taskIssues.size());
        result.put("performanceIssueCount", performanceIssues.size());
        result.put("invalidPendingCount", leadershipAccess.restricted(operatorId) ? scopedPendingCount(operatorId) : mapper.invalidPendingCount());
        result.put("budget", !permissions.configured(operatorId)||permissions.budgetScope(operatorId,year)?mapper.budgetSummary(year, month):null);
        result.put("expectedWorkRecordAuthors", !permissions.configured(operatorId)?workRecordScopes.expectedAuthors(year):workRecordScopes.eligibleAuthors(year).stream().filter(u->permissions.canRecordOwner(operatorId,u.getUserId(),u.getDeptId(),year)).count());
        result.put("submittedWorkRecordAuthors", !permissions.configured(operatorId)?mapper.submittedWorkRecordAuthors(year, month):jdbc.queryForList("SELECT record_id FROM biz_work_record WHERE status=1 AND delete_marker=0 AND record_year=? AND record_month=?",Long.class,year,month).stream().filter(id->permissions.canRead(operatorId,"records",id,year)).count());
        result.put("taskIssues", taskIssues.stream().limit(100).toList());
        result.put("performanceIssues", performanceIssues.stream().limit(100).toList());
        result.put("readOnly", !leadershipAccess.isAdmin(operatorId));
        result.put("scope", leadershipAccess.scope(operatorId));
        return result;
    }

    private int scopedPendingCount(Long userId) {
        // 返回异常所属部门后再限定范围，避免把全校待办数量透露给部门范围账号。
        String sql = "SELECT 'tasks' domain,t.task_id objectId,t.phase year,t.dept_id deptId FROM biz_material_submission s JOIN biz_task t ON t.task_id=s.task_id LEFT JOIN sys_user u ON u.user_id=s.current_handler_id AND COALESCE(u.is_delete,0)=0 WHERE COALESCE(s.is_delete,0)=0 AND s.flow_status IN (10,20,30) AND (u.user_id IS NULL OR u.status<>'1' OR (s.flow_status=10 AND NOT(s.current_handler_id <=> t.auditor_id)) OR (s.flow_status=20 AND NOT(s.current_handler_id <=> t.principal_id)) OR (s.flow_status=30 AND u.role<>'0')) UNION ALL SELECT 'performance',p.perf_id,s.year,p.dept_id FROM biz_performance_submission s JOIN biz_performance p ON p.perf_id=s.perf_id LEFT JOIN sys_user u ON u.user_id=s.current_handler_id AND COALESCE(u.is_delete,0)=0 WHERE COALESCE(s.is_delete,0)=0 AND s.flow_status IN (10,20) AND (u.user_id IS NULL OR u.status<>'1' OR (s.flow_status=10 AND NOT(s.current_handler_id <=> p.auditor_id)) OR (s.flow_status=20 AND u.role<>'0'))";
        int scoped = (int) jdbc.queryForList(sql).stream().filter(row -> permissions.configured(userId)?permissions.canRead(userId,String.valueOf(row.get("domain")),number(row.get("objectId")),row.get("year")==null?null:((Number)row.get("year")).intValue()):leadershipAccess.canReadDepartment(userId, number(row.get("deptId")))).count();
        int achievements = (int)jdbc.queryForList("SELECT a.ach_id FROM biz_achievement a LEFT JOIN sys_user u ON u.user_id=a.current_handler_id AND COALESCE(u.is_delete,0)=0 WHERE COALESCE(a.is_delete,0)=0 AND a.audit_status=10 AND (u.user_id IS NULL OR u.status<>'1' OR u.role<>'0')", Long.class).stream().filter(id->!permissions.configured(userId)||permissions.canRead(userId,"achievements",id,null)).count();
        return scoped + achievements;
    }

    public Map<String, Object> users(String keyword, Long deptId, String role, String status,
                                     Integer page, Integer pageSize, Long operatorId) {
        requireAdmin(operatorId);
        String term = trim(keyword).toLowerCase(Locale.ROOT);
        List<Map<String, Object>> filtered = mapper.users().stream().filter(row -> {
            if (deptId != null && !sameNumber(row.get("deptId"), deptId)) return false;
            if (!isBlank(role) && !role.equals(string(row.get("role")))) return false;
            if (!isBlank(status) && !status.equals(string(row.get("status")))) return false;
            if (!term.isEmpty()) {
                String haystack = (string(row.get("userId")) + " " + string(row.get("userName")) + " "
                        + string(row.get("nickName")) + " " + string(row.get("deptName"))).toLowerCase(Locale.ROOT);
                if (!haystack.contains(term)) return false;
            }
            SysUser user = users.getUserById(number(row.get("userId")));
            SysDept dept = user == null || user.getDeptId() == null ? null : users.getDeptById(user.getDeptId());
            row.put("canViewAchievement", AchievementPermissionUtil.canViewAchievement(user, dept));
            row.put("canUploadAchievement", AchievementPermissionUtil.canUploadAchievement(user, dept));
            return true;
        }).toList();
        return page(filtered, page, pageSize);
    }

    public List<Map<String, Object>> departments(Long operatorId) {
        requireAdmin(operatorId);
        return mapper.departments();
    }

    public Map<String, Object> userImpact(Long userId, Long operatorId) {
        requireAdmin(operatorId);
        SysUser user = users.getUserById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getIsDelete())) notFound("用户不存在");
        Map<String, Object> impact = new LinkedHashMap<>(mapper.userImpact(userId));
        impact.put("blockingCount", blockingUserImpact(impact));
        return impact;
    }

    public Map<String, Object> departmentImpact(Long deptId, Long operatorId) {
        requireAdmin(operatorId);
        SysDept dept = users.getDeptById(deptId);
        if (dept == null || Integer.valueOf(1).equals(dept.getIsDelete())) notFound("部门不存在");
        Map<String, Object> impact = new LinkedHashMap<>(mapper.departmentImpact(deptId));
        impact.put("blockingCount", numericSum(impact));
        return impact;
    }

    @Transactional
    public Map<String, Object> createUser(AdminUserDTO body, Long operatorId) {
        requireAdmin(operatorId);
        validateUser(body, true);
        if (users.getUserById(body.getUserId()) != null) bad("工号已存在");
        if (users.getUserByName(body.getUserName()) != null) bad("账号名称已存在");
        requireActiveDept(body.getDeptId());
        Date now = new Date();
        SysUser user = new SysUser();
        user.setUserId(body.getUserId());
        user.setDeptId(body.getDeptId());
        user.setUserName(trim(body.getUserName()));
        user.setNickName(trim(body.getNickName()));
        user.setEmail(trim(body.getEmail()));
        user.setPassword(String.valueOf(body.getUserId()));
        user.setForcePasswordChange(1);
        user.setRole(body.getRole());
        user.setStatus("1");
        user.setIsDelete(0);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        users.addUser(user);
        log("USER", "CREATE", "USER", String.valueOf(user.getUserId()), user.getNickName(), operatorId,
                "新增账号", null, safeUser(user));
        return userRow(user.getUserId());
    }

    @Transactional
    public Map<String, Object> updateUser(Long userId, AdminUserDTO body, Long operatorId) {
        requireAdmin(operatorId);
        if (body == null) bad("请求内容不能为空");
        body.setUserId(userId);
        validateUser(body, false);
        requireReason(body.getReason());
        SysUser before = mapper.userForUpdate(userId);
        if (before == null) notFound("用户不存在");
        if(body.getDeptId()!=null)requireActiveDept(body.getDeptId());
        else if(before.getDeptId()!=null||!permissions.policy(userId).roles().contains("EXPERT"))bad("请选择正常状态的部门");
        checkVersion(before.getUpdateTime(), body.getExpectedUpdateTime());
        if (mapper.duplicateUserName(trim(body.getUserName()), userId) > 0) bad("账号名称已存在");
        if ("0".equals(before.getRole()) && "1".equals(before.getStatus()) && !"0".equals(body.getRole())
                && mapper.activeAdminCount() <= 1) {
            conflict("不能降级最后一个正常管理员");
        }
        int changed = mapper.updateUserProfile(userId, body.getDeptId(), trim(body.getUserName()),
                trim(body.getNickName()), trim(body.getEmail()), body.getRole(), body.getExpectedUpdateTime());
        if (changed != 1) conflict("账号已被其他管理员修改，请刷新后重试");
        SysUser after = users.getUserById(userId);
        log("USER", "UPDATE", "USER", String.valueOf(userId), after.getNickName(), operatorId,
                trim(body.getReason()), safeUser(before), safeUser(after));
        return userRow(userId);
    }

    @Transactional
    public Map<String, Object> updateUserStatus(Long userId, AdminActionDTO body, Long operatorId) {
        requireAdmin(operatorId);
        if (body == null || !("0".equals(body.getStatus()) || "1".equals(body.getStatus()))) bad("账号状态必须为0或1");
        requireReason(body.getReason());
        SysUser before = mapper.userForUpdate(userId);
        if (before == null) notFound("用户不存在");
        checkVersion(before.getUpdateTime(), body.getExpectedUpdateTime());
        if (Objects.equals(before.getStatus(), body.getStatus())) return userRow(userId);
        if ("0".equals(body.getStatus())) {
            if ("0".equals(before.getRole()) && "1".equals(before.getStatus()) && mapper.activeAdminCount() <= 1) {
                conflict("不能停用最后一个正常管理员");
            }
            Map<String, Object> impact = mapper.userImpact(userId);
            long blocking = blockingUserImpact(impact);
            if (blocking > 0) {
                throw new AdminManagementException(409, "该账号仍承担部门、任务、绩效或待办职责，请先完成移交");
            }
        }
        if (mapper.updateUserStatus(userId, body.getStatus(), body.getExpectedUpdateTime()) != 1) {
            conflict("账号已被其他管理员修改，请刷新后重试");
        }
        SysUser after = users.getUserById(userId);
        log("USER", "1".equals(body.getStatus()) ? "ENABLE" : "DISABLE", "USER", String.valueOf(userId),
                after.getNickName(), operatorId, trim(body.getReason()), safeUser(before), safeUser(after));
        return userRow(userId);
    }

    @Transactional
    public void resetPassword(Long userId, AdminActionDTO body, Long operatorId) {
        requireAdmin(operatorId);
        if (body == null) bad("请求内容不能为空");
        requireReason(body.getReason());
        SysUser before = mapper.userForUpdate(userId);
        if (before == null) notFound("用户不存在");
        checkVersion(before.getUpdateTime(), body.getExpectedUpdateTime());
        if (mapper.resetUserPassword(userId, body.getExpectedUpdateTime()) != 1) {
            conflict("账号已被其他管理员修改，请刷新后重试");
        }
        log("USER", "RESET_PASSWORD", "USER", String.valueOf(userId), before.getNickName(), operatorId,
                trim(body.getReason()), Map.of("forcePasswordChange", before.getForcePasswordChange()),
                Map.of("forcePasswordChange", 1));
    }

    @Transactional
    public Map<String, Object> updateDepartment(Long deptId, AdminDepartmentDTO body, Long operatorId) {
        requireAdmin(operatorId);
        if (body == null || !("0".equals(body.getStatus()) || "1".equals(body.getStatus()))) bad("部门状态必须为0或1");
        requireReason(body.getReason());
        SysDept before = mapper.deptForUpdate(deptId);
        if (before == null) notFound("部门不存在");
        checkVersion(before.getUpdateTime(), body.getExpectedUpdateTime());
        if (body.getLeaderId() != null && mapper.activeUserInDept(body.getLeaderId(), deptId) != 1) {
            bad("部门负责人必须是本部门正常账号");
        }
        if ("1".equals(body.getStatus()) && !"1".equals(before.getStatus())) {
            Map<String, Object> impact = mapper.departmentImpact(deptId);
            if (numericSum(impact) > 0) conflict("该部门仍有关联账号、任务、绩效或待办，不能停用");
        }
        if (mapper.updateDepartment(deptId, body.getLeaderId(), body.getStatus(), body.getExpectedUpdateTime()) != 1) {
            conflict("部门已被其他管理员修改，请刷新后重试");
        }
        SysDept after = users.getDeptById(deptId);
        log("DEPARTMENT", "UPDATE", "DEPARTMENT", String.valueOf(deptId), after.getDeptName(), operatorId,
                trim(body.getReason()), safeDept(before), safeDept(after));
        return departmentRow(deptId);
    }

    public Map<String, Object> operations(String module, Long operatorIdFilter, String keyword, String action,
                                          String startDate, String endDate, Integer page, Integer pageSize,
                                          Long operatorId) {
        requireAdmin(operatorId);
        return page(filteredOperations(module, operatorIdFilter, keyword, action, startDate, endDate), page, pageSize);
    }

    public Map<String, Object> operation(String type, Long id, Long operatorId) {
        requireAdmin(operatorId);
        return allOperations().stream()
                .filter(row -> type.equalsIgnoreCase(string(row.get("recordType"))) && sameNumber(row.get("recordId"), id))
                .findFirst().orElseThrow(() -> new AdminManagementException(404, "操作记录不存在"));
    }

    public void exportOperations(String module, Long operatorIdFilter, String keyword, String action,
                                 String startDate, String endDate, HttpServletResponse response, Long operatorId) {
        requireAdmin(operatorId);
        List<Map<String, Object>> rows = filteredOperations(module, operatorIdFilter, keyword, action, startDate, endDate);
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("管理员操作记录");
            String[] headers = {"模块", "动作", "对象类型", "对象ID", "对象名称", "操作人ID", "操作人", "原因", "时间"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            for (int i = 0; i < rows.size(); i++) {
                Map<String, Object> item = rows.get(i);
                Row row = sheet.createRow(i + 1);
                Object[] values = {item.get("module"), item.get("action"), item.get("targetType"), item.get("targetId"),
                        item.get("targetName"), item.get("operatorId"), item.get("operatorName"), item.get("reason"), item.get("createTime")};
                for (int c = 0; c < values.length; c++) row.createCell(c).setCellValue(string(values[c]));
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            String name = URLEncoder.encode("管理员操作记录.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + name);
            workbook.write(response.getOutputStream());
        } catch (IOException error) {
            throw new AdminManagementException(500, "操作记录导出失败");
        }
    }

    public Map<String, Object> deletedWorkRecords(Integer year, Integer month, String keyword,
                                                   Integer page, Integer pageSize, Long operatorId) {
        requireAdmin(operatorId);
        String term = trim(keyword).toLowerCase(Locale.ROOT);
        List<Map<String, Object>> rows = mapper.deletedWorkRecords().stream().filter(row -> {
            if (year != null && !sameNumber(row.get("recordYear"), year.longValue())) return false;
            if (month != null && !sameNumber(row.get("recordMonth"), month.longValue())) return false;
            return term.isEmpty() || (string(row.get("ownerId")) + " " + string(row.get("ownerName")))
                    .toLowerCase(Locale.ROOT).contains(term);
        }).toList();
        return page(rows, page, pageSize);
    }

    private List<Map<String, Object>> filteredOperations(String module, Long operatorId, String keyword, String action,
                                                         String startDate, String endDate) {
        String term = trim(keyword).toLowerCase(Locale.ROOT);
        Date start = parseDate(startDate, false);
        Date end = parseDate(endDate, true);
        return allOperations().stream().filter(row -> {
            if (!isBlank(module) && !module.equalsIgnoreCase(string(row.get("module")))) return false;
            if (!isBlank(action) && !action.equalsIgnoreCase(string(row.get("action")))) return false;
            if (operatorId != null && !sameNumber(row.get("operatorId"), operatorId)) return false;
            Instant time = operationTime(row.get("createTime"));
            if (start != null && (time == null || time.isBefore(start.toInstant()))) return false;
            if (end != null && (time == null || time.isAfter(end.toInstant()))) return false;
            String haystack = (string(row.get("targetId")) + " " + string(row.get("targetName")) + " "
                    + string(row.get("operatorName")) + " " + string(row.get("reason"))).toLowerCase(Locale.ROOT);
            return term.isEmpty() || haystack.contains(term);
        }).toList();
    }

    private List<Map<String, Object>> allOperations() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.addAll(mapper.adminOperations());
        rows.addAll(mapper.ownershipOperations());
        rows.addAll(mapper.relationOperations());
        rows.addAll(mapper.budgetOperations());
        rows.addAll(mapper.deletedWorkRecordOperations());
        rows.sort((left, right) -> {
            Instant leftTime = operationTime(left.get("createTime"));
            Instant rightTime = operationTime(right.get("createTime"));
            if (leftTime == null) return rightTime == null ? 0 : 1;
            if (rightTime == null) return -1;
            return rightTime.compareTo(leftTime);
        });
        return rows;
    }

    private Map<String, Object> userRow(Long userId) {
        return mapper.users().stream().filter(row -> sameNumber(row.get("userId"), userId)).findFirst()
                .orElseThrow(() -> new AdminManagementException(404, "用户不存在"));
    }

    private Map<String, Object> departmentRow(Long deptId) {
        return mapper.departments().stream().filter(row -> sameNumber(row.get("deptId"), deptId)).findFirst()
                .orElseThrow(() -> new AdminManagementException(404, "部门不存在"));
    }

    private void validateUser(AdminUserDTO body, boolean creating) {
        if (body == null) bad("请求内容不能为空");
        if (creating && (body.getUserId() == null || body.getUserId() <= 0)) bad("请输入正确的工号");
        String userName = trim(body.getUserName());
        String nickName = trim(body.getNickName());
        if (userName.isEmpty() || userName.length() > 100) bad("账号名称不能为空且不能超过100字");
        if (nickName.isEmpty() || nickName.length() > 100) bad("姓名不能为空且不能超过100字");
        if (creating && body.getDeptId() == null) bad("请选择部门");
        if (!("0".equals(body.getRole()) || "1".equals(body.getRole()) || "2".equals(body.getRole()))) bad("账号角色不正确");
        String email = trim(body.getEmail());
        if (email.length() > 50 || (!email.isEmpty() && (!email.contains("@") || email.startsWith("@") || email.endsWith("@")))) {
            bad("邮箱格式不正确");
        }
    }

    private void requireActiveDept(Long deptId) {
        if (deptId == null || mapper.activeDept(deptId) != 1) bad("请选择正常状态的部门");
    }

    private void checkVersion(Date actual, Date expected) {
        if (!Objects.equals(actual, expected)) conflict("数据已被其他管理员修改，请刷新后重试");
    }

    private long blockingUserImpact(Map<String, Object> impact) {
        return impact.entrySet().stream().filter(entry -> !"draftWorkRecordCount".equals(entry.getKey()))
                .mapToLong(entry -> longValue(entry.getValue())).sum();
    }

    private long numericSum(Map<String, Object> values) {
        return values.values().stream().mapToLong(this::longValue).sum();
    }

    private Map<String, Object> safeUser(SysUser user) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("userId", user.getUserId());
        value.put("deptId", user.getDeptId());
        value.put("userName", user.getUserName());
        value.put("nickName", user.getNickName());
        value.put("email", user.getEmail());
        value.put("role", user.getRole());
        value.put("status", user.getStatus());
        value.put("forcePasswordChange", user.getForcePasswordChange());
        return value;
    }

    private Map<String, Object> safeDept(SysDept dept) {
        return Map.of("deptId", dept.getDeptId(), "deptName", dept.getDeptName(),
                "leaderId", dept.getLeaderId() == null ? "" : dept.getLeaderId(), "status", dept.getStatus());
    }

    private void log(String module, String action, String type, String id, String name, Long operatorId,
                     String reason, Object before, Object after) {
        AdminOperationLog log = new AdminOperationLog();
        log.setModule(module);
        log.setAction(action);
        log.setTargetType(type);
        log.setTargetId(id);
        log.setTargetName(name);
        log.setOperatorId(operatorId);
        log.setReason(reason);
        log.setBeforeJson(toJson(before));
        log.setAfterJson(toJson(after));
        log.setCreateTime(new Date());
        mapper.insertOperation(log);
    }

    /** 供既有管理流程在同一事务中补充统一审计记录。 */
    public void recordOperation(String module, String action, String type, String id, String name, Long operatorId,
                                String reason, Object before, Object after) {
        if(!permissions.admin(operatorId)&&!permissions.configured(operatorId))requireAdmin(operatorId);
        log(module, action, type, id, name, operatorId, reason, before, after);
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try { return json.writeValueAsString(value); }
        catch (JsonProcessingException error) { throw new AdminManagementException(500, "管理操作日志保存失败"); }
    }

    private Map<String, Object> page(List<Map<String, Object>> rows, Integer page, Integer size) {
        int current = page == null ? 1 : Math.max(1, page);
        int pageSize = size == null ? 10 : Math.min(100, Math.max(1, size));
        int from = Math.min(rows.size(), (current - 1) * pageSize);
        int to = Math.min(rows.size(), from + pageSize);
        return Map.of("items", rows.subList(from, to), "total", rows.size(), "page", current, "pageSize", pageSize);
    }

    private Date parseDate(String value, boolean end) {
        if (isBlank(value)) return null;
        try {
            LocalDate date = LocalDate.parse(value);
            return Date.from((end ? date.plusDays(1).atStartOfDay(ZONE).minusNanos(1) : date.atStartOfDay(ZONE)).toInstant());
        } catch (RuntimeException error) {
            bad("日期格式必须为yyyy-MM-dd");
            return null;
        }
    }

    private Instant operationTime(Object value) {
        if (value instanceof Date date) return date.toInstant();
        if (value instanceof LocalDateTime dateTime) return dateTime.atZone(ZONE).toInstant();
        if (value instanceof OffsetDateTime dateTime) return dateTime.toInstant();
        return null;
    }

    private void validateYearMonth(Integer year, Integer month) {
        if (year == null || year < 2025 || year > 2029) bad("年度必须在2025—2029年之间");
        if (month == null || month < 1 || month > 12) bad("月份必须在1—12之间");
    }

    private String trim(String value) { return value == null ? "" : value.trim(); }
    private boolean isBlank(String value) { return trim(value).isEmpty(); }
    private String string(Object value) { return value == null ? "" : String.valueOf(value); }
    private long longValue(Object value) { return value instanceof Number n ? n.longValue() : Long.parseLong(string(value).isEmpty() ? "0" : string(value)); }
    private Long number(Object value) { return value == null ? null : longValue(value); }
    private boolean sameNumber(Object value, Long expected) { return value != null && expected != null && longValue(value) == expected; }
    private void requireReason(String reason) { if (trim(reason).isEmpty() || trim(reason).length() > 500) bad("请填写1—500字的操作原因"); }
    private void bad(String message) { throw new AdminManagementException(400, message); }
    private void conflict(String message) { throw new AdminManagementException(409, message); }
    private void notFound(String message) { throw new AdminManagementException(404, message); }
}
