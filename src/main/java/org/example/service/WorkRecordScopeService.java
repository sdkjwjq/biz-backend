package org.example.service;

import org.example.entity.BizLevel4Task;
import org.example.entity.BizTask;
import org.example.entity.SysDept;
import org.example.entity.SysUser;
import org.example.entity.vo.WorkRecordVO.Task;
import org.example.mapper.SysMapper;
import org.example.mapper.WorkRecordMapper;
import org.example.utils.AdminTestContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** 仅决定工作纪实的一级任务范围，不授予建设任务或核心指标填报权限。 */
@Service
public class WorkRecordScopeService {
    private final WorkRecordMapper records;
    private final SysMapper users;

    public WorkRecordScopeService(WorkRecordMapper records, SysMapper users) {
        this.records = records;
        this.users = users;
    }

    public record Scope(List<Task> tasks, Map<Long, BizTask> roots, String notice) { }
    private record Catalog(List<Task> tasks, Map<Long, BizTask> roots, Map<Long, Long> directoryDepartments, List<BizLevel4Task> goals) { }

    public List<Integer> fillableYears(SysUser owner) {
        Catalog catalog = catalog();
        Map<Long, SysDept> departments = requestDepartments();
        List<Integer> result = new ArrayList<>();
        for (int year = 2025; year <= 2029; year++) {
            if (AdminTestContext.enabled(owner.getUserId()) || !resolve(owner, year, catalog, departments).tasks().isEmpty()) result.add(year);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<Long,SysDept> requestDepartments(){
        if(org.springframework.web.context.request.RequestContextHolder.getRequestAttributes() instanceof org.springframework.web.context.request.ServletRequestAttributes a && "GET".equals(a.getRequest().getMethod())){
            String key=WorkRecordScopeService.class.getName()+".departments";
            Object old=a.getRequest().getAttribute(key);if(old instanceof Map<?,?>)return (Map<Long,SysDept>)old;
            Map<Long,SysDept> value=new HashMap<>();a.getRequest().setAttribute(key,value);return value;
        }
        return new HashMap<>();
    }

    public Scope resolve(Long ownerId, int year) {
        return resolve(users.getUserById(ownerId), year, catalog(), new HashMap<>());
    }

    /** 概览统计复用相同资格；管理员测试开关不改变全校应填人数。 */
    public int expectedAuthors(int year) {
        return eligibleAuthors(year).size();
    }

    /** 返回同一资格集合供概览计算分子、分母；调用者只可输出必要的人员信息。 */
    public List<SysUser> eligibleAuthors(int year) {
        Catalog catalog = catalog();
        List<Task> annual=catalog.tasks().stream().filter(task->Objects.equals(task.getPhase(),year)).toList();
        if(annual.isEmpty())return List.of();
        Set<Long> taskIds=annual.stream().map(Task::getTaskId).collect(Collectors.toSet());
        Set<Long> professionals=new LinkedHashSet<>(),participatingDepartments=new LinkedHashSet<>();
        for(var task:annual){
            professionals.add(task.getLeaderId());participatingDepartments.add(task.getDeptId());
            participatingDepartments.add(catalog.directoryDepartments().get(task.getTaskId()));
        }
        for(var goal:catalog.goals())if(taskIds.contains(goal.getParentId())&&(goal.getPhase()==null||Objects.equals(goal.getPhase(),year))){
            professionals.add(goal.getLeaderId());participatingDepartments.add(goal.getDeptId());
        }
        Map<Long, SysDept> departments = requestDepartments();
        return users.getAllUsers().stream().filter(this::active)
                .filter(owner->professionals.contains(owner.getUserId())||eligibleDepartment(owner.getDeptId(),participatingDepartments,departments)).toList();
    }
    /** Assignment previews reuse qualification membership without constructing every person's full task scope. */
    public Set<Long> fillableAccountIds(java.util.Collection<SysUser> accounts){
        Catalog catalog=catalog();Map<Long,SysDept> departments=requestDepartments();Set<Long> result=new HashSet<>();
        for(int year=2025;year<=2029;year++){
            int selected=year;var annual=catalog.tasks().stream().filter(t->Objects.equals(t.getPhase(),selected)).toList();if(annual.isEmpty())continue;
            Set<Long> ids=annual.stream().map(Task::getTaskId).collect(Collectors.toSet());Set<Long> professionals=new HashSet<>(),participating=new HashSet<>();
            for(var task:annual){professionals.add(task.getLeaderId());participating.add(task.getDeptId());participating.add(catalog.directoryDepartments().get(task.getTaskId()));}
            for(var goal:catalog.goals())if(ids.contains(goal.getParentId())&&(goal.getPhase()==null||Objects.equals(goal.getPhase(),selected))){professionals.add(goal.getLeaderId());participating.add(goal.getDeptId());}
            for(var account:accounts)if(active(account)&&(professionals.contains(account.getUserId())||eligibleDepartment(account.getDeptId(),participating,departments)))result.add(account.getUserId());
        }
        return result;
    }

    public Set<Long> fillableAccountIdsSql(java.util.Collection<SysUser> accounts){
        var ids=accounts.stream().map(SysUser::getUserId).filter(Objects::nonNull).distinct().toList();Set<Long> result=new HashSet<>();
        for(int start=0;start<ids.size();start+=500)result.addAll(records.fillableAccountIds(ids.subList(start,Math.min(start+500,ids.size()))));return result;
    }

    private boolean eligibleDepartment(Long id,Set<Long> participating,Map<Long,SysDept> departments){
        if(id==null||!participating.contains(id))return false;
        if(!departments.containsKey(id))departments.put(id,users.getDeptById(id));
        var department=departments.get(id);
        return department!=null&&!Integer.valueOf(1).equals(department.getIsDelete())&&!"1".equals(department.getStatus());
    }

    private Scope resolve(SysUser owner, int year, Catalog catalog, Map<Long, SysDept> departments) {
        return resolve(owner, year, catalog, departments, owner != null && AdminTestContext.enabled(owner.getUserId()));
    }

    private Scope resolve(SysUser owner, int year, Catalog catalog, Map<Long, SysDept> departments, boolean testing) {
        if (!active(owner)) return new Scope(List.of(), Map.of(), "当前账号没有有效的工作纪实填报范围。");
        List<Task> annual = catalog.tasks().stream().filter(task -> Objects.equals(task.getPhase(), year)).toList();
        Set<Long> taskIds = annual.stream().map(Task::getTaskId).collect(Collectors.toSet());
        List<BizLevel4Task> goals = catalog.goals().stream().filter(goal -> taskIds.contains(goal.getParentId())
                && (goal.getPhase() == null || Objects.equals(goal.getPhase(), year))).toList();
        boolean professional = annual.stream().anyMatch(task -> Objects.equals(task.getLeaderId(), owner.getUserId()))
                || goals.stream().anyMatch(goal -> Objects.equals(goal.getLeaderId(), owner.getUserId()));
        if (testing || professional) {
            return new Scope(annual, roots(annual, catalog), testing
                    ? "管理员统计：按一级任务归集全校该年度有效三级任务，四级目标归并到所属三级任务。"
                    : "专业群统计：按一级任务归集全校该年度有效三级任务，四级目标归并到所属三级任务。");
        }
        Long deptId = owner.getDeptId();
        if (deptId != null && !departments.containsKey(deptId)) departments.put(deptId, users.getDeptById(deptId));
        SysDept dept = departments.get(deptId);
        if (dept == null || Integer.valueOf(1).equals(dept.getIsDelete()) || "1".equals(dept.getStatus())) {
            return new Scope(List.of(), Map.of(), "所属部门不存在或已停用，没有有效的工作纪实填报范围。");
        }
        Set<Long> matchedTasks = goals.stream().filter(goal -> Objects.equals(deptId, goal.getDeptId()))
                .map(BizLevel4Task::getParentId).collect(Collectors.toCollection(LinkedHashSet::new));
        annual.stream().filter(task -> Objects.equals(deptId, task.getDeptId())
                || Objects.equals(deptId, catalog.directoryDepartments().get(task.getTaskId())))
                .map(Task::getTaskId).forEach(matchedTasks::add);
        Set<Long> roots = matchedTasks.stream().map(catalog.roots()::get).filter(Objects::nonNull)
                .map(BizTask::getTaskId).collect(Collectors.toSet());
        List<Task> tasks = annual.stream().filter(task -> roots.contains(catalog.roots().get(task.getTaskId()).getTaskId())).toList();
        return new Scope(tasks, roots(tasks, catalog), "归口部门统计：一级任务下的有效二级目录、三级任务或四级目标归口本部门时，纳入该一级任务下所选年度全部有效三级任务；四级目标归并去重。");
    }

    private Map<Long, BizTask> roots(List<Task> tasks, Catalog catalog) {
        Map<Long, BizTask> result = new LinkedHashMap<>();
        tasks.forEach(task -> result.put(task.getTaskId(), catalog.roots().get(task.getTaskId())));
        return result;
    }

    private Catalog catalog() {
        if(org.springframework.web.context.request.RequestContextHolder.getRequestAttributes() instanceof org.springframework.web.context.request.ServletRequestAttributes attributes && "GET".equals(attributes.getRequest().getMethod())) {
            String key=WorkRecordScopeService.class.getName()+".catalog";Object found=attributes.getRequest().getAttribute(key);
            if(found instanceof Catalog cached)return cached;Catalog result=loadCatalog();attributes.getRequest().setAttribute(key,result);return result;
        }
        return loadCatalog();
    }
    private Catalog loadCatalog() {
        Map<Long, BizTask> structure = records.structure().stream().collect(Collectors.toMap(BizTask::getTaskId, task -> task));
        Map<Long, BizTask> roots = new LinkedHashMap<>();
        Map<Long, Long> directoryDepartments = new HashMap<>();
        List<Task> valid = new ArrayList<>();
        for (Task task : records.scopeTasks()) {
            BizTask middle = structure.get(task.getParentId());
            BizTask root = middle == null ? null : structure.get(middle.getParentId());
            if (middle == null || root == null || !Integer.valueOf(2).equals(middle.getLevel()) || !Integer.valueOf(1).equals(root.getLevel())
                    || task.getProjectId() == null || !task.getProjectId().equals(middle.getProjectId()) || !task.getProjectId().equals(root.getProjectId())
                    || (root.getParentId() != null && root.getParentId() != 0L)) continue;
            valid.add(task);
            roots.put(task.getTaskId(), root);
            directoryDepartments.put(task.getTaskId(), middle.getDeptId());
        }
        return new Catalog(List.copyOf(valid), roots, directoryDepartments, records.scopeGoals());
    }

    private boolean active(SysUser owner) {
        return owner != null && "1".equals(owner.getStatus()) && !Integer.valueOf(1).equals(owner.getIsDelete());
    }
}
