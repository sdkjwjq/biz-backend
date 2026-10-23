package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.AdminOperationLog;
import org.example.entity.SysUser;
import org.example.entity.BizTask;
import org.example.entity.BizPerformance;
import org.example.mapper.BizMapper;
import org.example.mapper.PerformanceMapper;
import org.example.utils.JWTUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.example.mapper.AdminManagementMapper;
import org.example.mapper.SysMapper;
import org.example.utils.AchievementPermissionUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** 校级领导仅增加查阅范围，不能替代管理员或业务审核授权。 */
@Service
public class LeadershipAccessService {
    private final JdbcTemplate jdbc;
    private final SysMapper users;
    private final AdminManagementMapper logs;
    private final ObjectMapper json;
    private final BizMapper tasks;
    private final PerformanceMapper performances;

    public LeadershipAccessService(JdbcTemplate jdbc, SysMapper users, AdminManagementMapper logs, ObjectMapper json,
                                   BizMapper tasks, PerformanceMapper performances) {
        this.jdbc = jdbc;
        this.users = users;
        this.logs = logs;
        this.json = json;
        this.tasks = tasks;
        this.performances = performances;
    }

    public record Scope(String scope, long version, List<Long> departmentIds) {
        public boolean leader() { return "ALL".equals(scope) || "DEPARTMENTS".equals(scope); }
        public boolean contains(Long deptId) { return "ALL".equals(scope) || departmentIds.contains(deptId); }
    }

    public Scope scope(Long userId) {
        if (userId == null) return new Scope("NONE", 0, List.of());
        return requestValue("scope:" + userId, () -> loadScope(userId));
    }

    private Scope loadScope(Long userId) {
        return loadScope(userId, false);
    }

    private Scope loadScope(Long userId, boolean lock) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT scope,version FROM sys_leadership_access WHERE user_id=?" + (lock ? " FOR UPDATE" : ""), userId);
        if (rows.isEmpty()) return new Scope("NONE", 0, List.of());
        Map<String, Object> row = rows.get(0);
        return new Scope((String) row.get("scope"), ((Number) row.get("version")).longValue(),
                jdbc.queryForList("SELECT dept_id FROM sys_leadership_department WHERE user_id=? ORDER BY dept_id" + (lock ? " FOR UPDATE" : ""), Long.class, userId));
    }

    public boolean isLeader(Long userId) {
        SysUser user = currentUser(userId);
        return active(user) && scope(userId).leader();
    }

    public boolean canReadDepartment(Long userId, Long deptId) {
        return isLeader(userId) && scope(userId).contains(deptId);
    }

    public Long currentUserId() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) return null;
        String token = attributes.getRequest().getHeader("Authorization");
        return token == null || token.isBlank() ? null : JWTUtil.getUserIdFromToken(token);
    }

    public boolean restricted(Long userId) {
        return !isAdmin(userId) && isLeader(userId) && "DEPARTMENTS".equals(scope(userId).scope());
    }

    /** 保留目录以便浏览，但目录派生值仅由范围内的三级任务计算。 */
    public List<BizTask> visibleTasks(Long userId) {
        return requestValue("tasks:" + userId, () -> loadVisibleTasks(userId, true));
    }

    public List<BizTask> scopedTasks(Long userId) {
        return requestValue("scopedTasks:" + userId, () -> loadVisibleTasks(userId, false));
    }

    private boolean explicitDuty(Long userId, Long leaderId, Long auditorId, Long principalId) {
        return Objects.equals(userId, leaderId) || Objects.equals(userId, auditorId) || Objects.equals(userId, principalId);
    }

    private List<BizTask> loadVisibleTasks(Long userId, boolean includeDuties) {
        List<BizTask> all = tasks.getAllTasks().stream().filter(t -> !Integer.valueOf(1).equals(t.getIsDelete())).toList();
        if (!restricted(userId)) return all;
        Scope scope = scope(userId);
        Map<Long, BizTask> byId = new HashMap<>();
        all.forEach(task -> byId.put(task.getTaskId(), task));
        Set<Long> ids = new HashSet<>();
        for (BizTask task : all) {
            if (Integer.valueOf(3).equals(task.getLevel()) && (scope.contains(task.getDeptId())
                    || includeDuties && explicitDuty(userId, task.getLeaderId(), task.getAuditorId(), task.getPrincipalId()))) {
                BizTask cursor = task;
                Set<Long> visited = new HashSet<>();
                while (cursor != null && visited.add(cursor.getTaskId())) {
                    ids.add(cursor.getTaskId());
                    cursor = byId.get(cursor.getParentId());
                }
            }
        }
        List<BizTask> result = new ArrayList<>();
        for (BizTask task : all) {
            if (!ids.contains(task.getTaskId())) continue;
            if (!Integer.valueOf(3).equals(task.getLevel())) {
                List<BizTask> children = all.stream().filter(t -> Integer.valueOf(3).equals(t.getLevel()) && ids.contains(t.getTaskId())
                        && descendsFrom(t, task.getTaskId(), byId)).toList();
                int progress = children.isEmpty() ? 0 : (int) children.stream().mapToInt(t -> t.getProgress() == null ? 0 : t.getProgress()).average().orElse(0);
                task.setProgress(progress);
                task.setTargetValue(java.math.BigDecimal.valueOf(100));
                task.setCurrentValue(java.math.BigDecimal.valueOf(progress));
                task.setStatus(progress == 100 ? "3" : "1");
                // 目录不承载分管范围外的描述或人员配置。
                task.setComment(null); task.setExpTarget(null); task.setExpEffect(null); task.setExpMaterialDesc(null);
            }
            result.add(task);
        }
        return result;
    }

    private boolean descendsFrom(BizTask task, Long parentId, Map<Long, BizTask> byId) {
        Set<Long> visited = new HashSet<>();
        for (BizTask cursor = task; cursor != null && visited.add(cursor.getTaskId()); cursor = byId.get(cursor.getParentId()))
            if (Objects.equals(cursor.getParentId(), parentId)) return true;
        return false;
    }

    public boolean canReadTask(Long userId, Long taskId) {
        return isLeader(userId) && visibleTasks(userId).stream().anyMatch(t -> Objects.equals(t.getTaskId(), taskId));
    }

    public boolean canAccessTask(Long userId, Long taskId) {
        if (isAdmin(userId)) return true;
        if (isLeader(userId)) return canReadTask(userId, taskId);
        if (tasks.getVisibleTasks(userId).stream().anyMatch(t -> !Integer.valueOf(1).equals(t.getIsDelete()) && Objects.equals(t.getTaskId(), taskId))) return true;
        return jdbc.queryForObject("SELECT COUNT(*) FROM biz_material_submission WHERE task_id=? AND submit_by=? AND COALESCE(is_delete,0)=0", Integer.class, taskId, userId) > 0;
    }

    public List<BizTask> filterTasks(Long userId, List<BizTask> list) {
        if (!restricted(userId)) return list;
        Map<Long, BizTask> allowed = new HashMap<>();
        visibleTasks(userId).forEach(t -> allowed.put(t.getTaskId(), t));
        return list.stream().filter(t -> allowed.containsKey(t.getTaskId())).map(t -> allowed.get(t.getTaskId())).toList();
    }

    public List<BizPerformance> visiblePerformances(Long userId) {
        return requestValue("performances:" + userId, () -> loadVisiblePerformances(userId));
    }

    private List<BizPerformance> loadVisiblePerformances(Long userId) {
        List<BizPerformance> all = performances.getAllPerformance().stream().filter(p -> !Integer.valueOf(1).equals(p.getIsDelete())).toList();
        if (!restricted(userId)) return all;
        Scope scope = scope(userId);
        Set<Long> parentIds = new HashSet<>();
        all.forEach(p -> parentIds.add(p.getParentId()));
        Map<Long, BizPerformance> byId = new HashMap<>();
        all.forEach(p -> byId.put(p.getPerfId(), p));
        Set<Long> ids = new HashSet<>();
        for (BizPerformance p : all) {
            if (!parentIds.contains(p.getPerfId()) && (scope.contains(p.getDeptId())
                    || explicitDuty(userId, p.getLeaderId(), p.getAuditorId(), p.getPrincipalId()))) {
                Set<Long> visited = new HashSet<>();
                for (BizPerformance cursor = p; cursor != null && visited.add(cursor.getPerfId()); cursor = byId.get(cursor.getParentId())) ids.add(cursor.getPerfId());
            }
        }
        return all.stream().filter(p -> ids.contains(p.getPerfId())).peek(p -> {
            if (parentIds.contains(p.getPerfId())) { p.setCurrentValue(null); p.setTargetValue(null); }
        }).toList();
    }

    public boolean canReadPerformance(Long userId, Long perfId) {
        return isLeader(userId) && visiblePerformances(userId).stream().anyMatch(p -> Objects.equals(p.getPerfId(), perfId));
    }

    public boolean canAccessPerformance(Long userId, Long perfId) {
        return isAdmin(userId) || (isLeader(userId) ? canReadPerformance(userId, perfId)
                : performances.getVisiblePerformance(userId).stream().anyMatch(p -> Objects.equals(p.getPerfId(), perfId)));
    }

    public boolean isAdmin(Long userId) {
        SysUser user = currentUser(userId);
        return active(user) && "0".equals(user.getRole());
    }

    /** 双高建设办公室沿用现有组织编号100，仅有效部门的正常账号具有填报资格。 */
    public boolean isDoubleHighOffice(Long userId) {
        return isActiveDepartmentMember(userId, 100L);
    }

    /** 组织编号已与现有部门数据核对：100双高建设办公室，104财务处。 */
    public boolean canFillBudget(Long userId) {
        return isAdmin(userId) || isDoubleHighOffice(userId) || isActiveDepartmentMember(userId, 104L);
    }

    private boolean isActiveDepartmentMember(Long userId, Long departmentId) {
        SysUser user = currentUser(userId);
        if (!active(user) || !departmentId.equals(user.getDeptId())) return false;
        var dept = users.getDeptById(user.getDeptId());
        return dept != null && !Integer.valueOf(1).equals(dept.getIsDelete()) && !"1".equals(dept.getStatus());
    }

    private boolean active(SysUser user) {
        return user != null && "1".equals(user.getStatus()) && !Integer.valueOf(1).equals(user.getIsDelete());
    }

    private SysUser currentUser(Long userId) {
        return userId == null ? null : requestValue("user:" + userId, () -> users.getUserById(userId));
    }

    /** 只在一次GET中复用，下一次请求必须重新读取数据库；修改请求不缓存。 */
    @SuppressWarnings("unchecked")
    private <T> T requestValue(String key, java.util.function.Supplier<T> load) {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)
                || !"GET".equals(attributes.getRequest().getMethod())) return load.get();
        String name = LeadershipAccessService.class.getName() + ":" + key;
        Object value = attributes.getRequest().getAttribute(name);
        if (value != null) return (T) value;
        T result = load.get();
        if (result != null) attributes.getRequest().setAttribute(name, result);
        return result;
    }

    public Map<String, Object> capabilities(Long userId) {
        SysUser user = users.getUserById(userId);
        if (!active(user)) throw new AdminManagementException(403, "账号已停用或不存在");
        Scope scope = scope(userId);
        boolean admin = "0".equals(user.getRole());
        boolean duties = admin || jdbc.queryForObject("SELECT (EXISTS(SELECT 1 FROM biz_task WHERE COALESCE(is_delete,0)=0 AND (auditor_id=? OR principal_id=?)) OR EXISTS(SELECT 1 FROM biz_performance WHERE COALESCE(is_delete,0)=0 AND (auditor_id=? OR principal_id=?)))", Integer.class,
                userId, userId, userId, userId) == 1;
        Map<String, Object> result = new LinkedHashMap<>();
        boolean coreDuties = jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM biz_core_report r JOIN biz_core_indicator c ON c.indicator_id=r.indicator_id AND BINARY c.indicator_code=BINARY r.performance_code WHERE r.current_handler_id=? AND r.flow_status IN(10,20))", Integer.class, userId) == 1;
        boolean coreFiller = jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM biz_task t WHERE t.level=3 AND t.phase BETWEEN 2025 AND 2029 AND COALESCE(t.is_delete,0)=0 AND (t.leader_id=? OR EXISTS(SELECT 1 FROM biz_level4_task c WHERE c.parent_id=t.task_id AND c.leader_id=? AND COALESCE(c.is_delete,0)=0)))", Integer.class, userId,userId) == 1;
        boolean coreReviewer = jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM biz_performance p JOIN biz_core_indicator c ON BINARY c.indicator_code=BINARY p.perf_code WHERE p.principal_id=? AND COALESCE(p.is_delete,0)=0)",Integer.class,userId)==1;
        result.put("canViewCoreIndicators", admin || isDoubleHighOffice(userId) || scope.leader() || coreFiller || coreReviewer || coreDuties);
        result.put("userId", userId);
        result.put("role", user.getRole());
        result.put("isAdmin", admin);
        result.put("isLeader", scope.leader());
        result.put("leadershipScope", scope.scope());
        result.put("departmentIds", scope.departmentIds());
        result.put("canViewOverview", admin || scope.leader());
        result.put("canViewBudget", canFillBudget(userId) || scope.leader() || "2".equals(user.getRole()));
        result.put("canFillBudget", canFillBudget(userId));
        result.put("canViewAchievement", scope.leader() || AchievementPermissionUtil.canViewAchievement(user,
                user.getDeptId() == null ? null : users.getDeptById(user.getDeptId())));
        result.put("canViewAuditCenter", !scope.leader() || duties || coreDuties || AchievementPermissionUtil.isAchievementAuditor(user));
        return result;
    }

    public Scope configuration(Long targetId, Long operatorId) {
        requireAdmin(operatorId);
        requireUser(targetId);
        return scope(targetId);
    }

    public record Change(String scope, List<Long> departmentIds, Long version, String reason) {}

    @Transactional(rollbackFor = Exception.class)
    public Scope save(Long targetId, Change body, Long operatorId) throws Exception {
        requireAdmin(operatorId);
        // 与账号启停共用账号行锁，避免为已被删除的账号保存配置。
        if (logs.userForUpdate(targetId) == null) throw new AdminManagementException(404, "账号不存在");
        if (body == null || !Set.of("NONE", "ALL", "DEPARTMENTS").contains(Objects.toString(body.scope(), ""))
                || body.version() == null || body.reason() == null || body.reason().trim().isEmpty()
                || body.reason().length() > 500) throw new AdminManagementException(400, "请填写有效范围、版本及变更原因（最多500字）");
        // 使用当前读，而非REPEATABLE READ中在等待账号锁前建立的旧快照。
        Scope before = loadScope(targetId, true);
        if (before.version() != body.version()) throw new AdminManagementException(409, "查阅范围已被修改，请重新加载");
        List<Long> departments = body.departmentIds() == null ? List.of() : body.departmentIds();
        if ("DEPARTMENTS".equals(body.scope())) {
            if (departments.isEmpty() || departments.contains(null) || new HashSet<>(departments).size() != departments.size())
                throw new AdminManagementException(400, "请选择不重复的有效归口部门");
            for (Long id : departments) {
                if (jdbc.queryForObject("SELECT COUNT(*) FROM sys_dept WHERE dept_id=? AND status='0' AND COALESCE(is_delete,0)=0", Integer.class, id) != 1)
                    throw new AdminManagementException(400, "归口部门不存在或已停用");
            }
        } else if (!departments.isEmpty()) throw new AdminManagementException(400, "全校或取消配置时不能指定部门");
        jdbc.update("INSERT INTO sys_leadership_access(user_id,scope,version,update_by) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE scope=VALUES(scope),version=VALUES(version),update_by=VALUES(update_by),update_time=NOW()",
                targetId, body.scope(), before.version() + 1, operatorId);
        jdbc.update("DELETE FROM sys_leadership_department WHERE user_id=?", targetId);
        for (Long id : departments) jdbc.update("INSERT INTO sys_leadership_department(user_id,dept_id) VALUES(?,?)", targetId, id);
        Scope after = loadScope(targetId, true);
        AdminOperationLog log = new AdminOperationLog();
        log.setModule("USER"); log.setAction("LEADERSHIP_ACCESS"); log.setTargetType("USER");
        log.setTargetId(String.valueOf(targetId)); log.setTargetName(requireUser(targetId).getNickName());
        log.setOperatorId(operatorId); log.setReason(body.reason().trim()); log.setCreateTime(new Date());
        log.setBeforeJson(json.writeValueAsString(before)); log.setAfterJson(json.writeValueAsString(after));
        logs.insertOperation(log);
        return after;
    }

    private SysUser requireUser(Long id) {
        SysUser user = id == null ? null : users.getUserById(id);
        if (user == null || Integer.valueOf(1).equals(user.getIsDelete())) throw new AdminManagementException(404, "账号不存在");
        return user;
    }

    private void requireAdmin(Long id) {
        if (!isAdmin(id)) throw new AdminManagementException(403, "仅正常状态的管理员可修改查阅范围");
    }
}
