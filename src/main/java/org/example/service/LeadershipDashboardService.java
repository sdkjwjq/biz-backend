package org.example.service;

import org.example.entity.BizTask;
import org.example.entity.BizTrendData;
import org.example.entity.vo.*;
import org.example.mapper.SysMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;

/** 部门范围看板从可见三级任务计算，禁止沿用全校聚合值。 */
@Service
public class LeadershipDashboardService {
    private final LeadershipAccessService access;
    private final SysMapper users;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;
    public LeadershipDashboardService(LeadershipAccessService access, SysMapper users, org.springframework.jdbc.core.JdbcTemplate jdbc) { this.access = access; this.users = users; this.jdbc = jdbc; }
    public boolean restricted() { return access.restricted(access.currentUserId()); }
    public void recordDepartmentTrend() {
        // 同一天只记一次，不覆盖已经留存的历史快照；不从全校历史值推算。
        jdbc.update("INSERT IGNORE INTO biz_department_trend(dept_id,record_date,total_tasks,completed_tasks,progress_sum) SELECT dept_id,CURDATE(),COUNT(*),SUM(status='3'),SUM(LEAST(100,GREATEST(0,COALESCE(progress,0)))) FROM biz_task WHERE level=3 AND phase=YEAR(CURDATE()) AND COALESCE(is_delete,0)=0 AND dept_id IS NOT NULL GROUP BY dept_id");
    }
    public List<BizTrendData> trends(Integer year) {
        int selectedYear = year == null ? LocalDate.now().getYear() : year;
        List<Long> ids = access.scope(access.currentUserId()).departmentIds();
        if (ids.isEmpty()) return List.of();
        List<Object> params = new ArrayList<>(); params.add(selectedYear); params.addAll(ids);
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        return jdbc.query("SELECT record_date,SUM(total_tasks) total,SUM(completed_tasks) completed,SUM(progress_sum)/NULLIF(SUM(total_tasks),0) rate FROM biz_department_trend WHERE YEAR(record_date)=? AND dept_id IN (" + placeholders + ") GROUP BY record_date ORDER BY record_date", (rs, row) -> {
            LocalDate date = rs.getDate("record_date").toLocalDate();
            BizTrendData data = new BizTrendData(); data.setYear(date.getYear()); data.setMonth(date.getMonthValue()); data.setDay(date.getDayOfMonth());
            data.setTotalTasks(rs.getInt("total")); data.setCompletionCount(rs.getInt("completed")); data.setCompletionRate(rs.getDouble("rate")); data.setIsDelete(0); return data;
        }, params.toArray());
    }
    public List<BizTask> tasks() { return access.scopedTasks(access.currentUserId()); }
    private List<BizTask> leaves(Integer year, Integer endYear) {
        return tasks().stream().filter(t -> Integer.valueOf(3).equals(t.getLevel())
                && (year == null || Objects.equals(year, t.getPhase()))
                && (endYear == null || t.getPhase() != null && t.getPhase() < endYear)).toList();
    }
    private int count(List<BizTask> tasks, String status) { return (int) tasks.stream().filter(t -> status.equals(t.getStatus())).count(); }
    public TaskCompletionVO completion(Integer year, Integer endYear, boolean firstLevel) {
        List<BizTask> rows = firstLevel ? tasks().stream().filter(t -> Integer.valueOf(1).equals(t.getLevel())).toList() : leaves(year, endYear);
        return new TaskCompletionVO(rows.size(), count(rows, "3"), firstLevel ? "firstLevel" : year != null ? "year" : endYear != null ? "midterm" : "all",
                "分管归口部门范围内任务", count(rows, "0"), count(rows, "1"), count(rows, "2"), count(rows, "3"));
    }
    public List<DeptTaskStatsVO> departments(Integer year, Integer endYear) {
        Map<Long, List<BizTask>> grouped = new TreeMap<>();
        leaves(year, endYear).forEach(t -> { if (t.getDeptId() != null) grouped.computeIfAbsent(t.getDeptId(), id -> new ArrayList<>()).add(t); });
        List<DeptTaskStatsVO> result = new ArrayList<>();
        grouped.forEach((id, rows) -> {
            DeptTaskStatsVO stats = new DeptTaskStatsVO(); stats.setDeptId(id);
            var department = users.getDeptById(id); stats.setDeptName(department == null ? "" : department.getDeptName());
            stats.setTotalTasks(rows.size()); stats.setCompletedTasks(count(rows, "3"));
            stats.setNotStartedCount(count(rows, "0")); stats.setInProgressCount(count(rows, "1"));
            stats.setInReviewCount(count(rows, "2")); stats.setFinishedCount(count(rows, "3")); stats.calculateCompletionRate(); result.add(stats);
        });
        return result;
    }
    public List<FirstLevelTaskDetailVO> details(boolean firstLevel) {
        List<FirstLevelTaskDetailVO> result = new ArrayList<>();
        for (BizTask task : tasks()) {
            if (firstLevel && !Integer.valueOf(1).equals(task.getLevel())) continue;
            FirstLevelTaskDetailVO row = new FirstLevelTaskDetailVO();
            row.setTaskId(task.getTaskId()); row.setTaskName(task.getTaskName()); row.setDeptId(task.getDeptId());
            var dept = task.getDeptId() == null ? null : users.getDeptById(task.getDeptId()); row.setDeptName(dept == null ? "" : dept.getDeptName());
            row.setStatus(task.getStatus()); row.setTargetValue(task.getTargetValue()); row.setCurrentValue(task.getCurrentValue() == null ? java.math.BigDecimal.ZERO : task.getCurrentValue());
            row.setProgress(task.getProgress()); row.setCreateTime(Objects.toString(task.getCreateTime(), "")); row.setUpdateTime(Objects.toString(task.getUpdateTime(), "")); result.add(row);
        }
        return result;
    }
    public DashboardSummaryVO summary() {
        int year = LocalDate.now().getYear();
        DashboardSummaryVO summary = new DashboardSummaryVO(); summary.setCurrentYear(String.valueOf(year)); summary.setMidTermEndYear("2028");
        summary.setUpdateTime(java.time.LocalDateTime.now().toString());
        summary.setOverallCompletion(completion(null, null, false)); summary.setYearCompletion(completion(year, null, false));
        summary.setMidTermCompletion(completion(null, 2028, false)); summary.setFirstLevelCompletion(completion(null, null, true));
        summary.setDeptOverallStats(departments(null, null)); summary.setDeptYearStats(departments(year, null)); summary.setDeptMidTermStats(departments(null, 2028));
        summary.setFirstLevelTasks(details(true)); return summary;
    }
    public Map<String, Object> departmentDetail(Long deptId) {
        if (!access.canReadDepartment(access.currentUserId(), deptId)) throw new AdminManagementException(403, "部门不在查阅范围内");
        var dept = users.getDeptById(deptId);
        Map<String, Object> result = new LinkedHashMap<>(); result.put("deptId", deptId); result.put("deptName", dept == null ? "" : dept.getDeptName());
        int year = LocalDate.now().getYear();
        result.put("overall", departmentStats(deptId, null, null)); result.put("year", departmentStats(deptId, year, null)); result.put("midterm", departmentStats(deptId, null, 2028));
        if (dept != null && dept.getLeaderId() != null) { var leader = users.getUserById(dept.getLeaderId()); if (leader != null) { result.put("leaderId", leader.getUserId()); result.put("leaderName", leader.getNickName()); } }
        return result;
    }
    private DeptTaskStatsVO departmentStats(Long id, Integer year, Integer endYear) {
        return departments(year, endYear).stream().filter(d -> Objects.equals(d.getDeptId(), id)).findFirst().orElseGet(() -> {
            DeptTaskStatsVO empty = new DeptTaskStatsVO(); empty.setDeptId(id); empty.setTotalTasks(0); empty.setCompletedTasks(0); empty.setNotStartedCount(0); empty.setInProgressCount(0); empty.setInReviewCount(0); empty.setFinishedCount(0); empty.calculateCompletionRate(); return empty;
        });
    }
    public List<Map<String, Object>> comparison(boolean byYear) {
        Map<Integer, List<BizTask>> grouped = new TreeMap<>();
        for (BizTask task : tasks()) {
            Integer category = byYear ? task.getPhase() : task.getLevel();
            if (category != null) grouped.computeIfAbsent(category, key -> new ArrayList<>()).add(task);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        grouped.forEach((category, rows) -> {
            int done = count(rows, "3");
            result.add(Map.of(byYear ? "year" : "level", byYear ? category : new String[]{"一级任务", "二级任务", "三级任务"}[category - 1],
                    "totalTasks", rows.size(), "completedTasks", done, "completionRate", java.math.BigDecimal.valueOf(done * 100.0 / rows.size()).setScale(2, java.math.RoundingMode.HALF_UP)));
        }); return result;
    }
}
