package org.example.service;

import org.example.entity.BizTask;
import org.example.entity.vo.WorkRecordVO.*;
import org.example.mapper.WorkRecordMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkRecordStatisticsService {
    public static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private final WorkRecordMapper mapper;
    private final Clock clock;

    public WorkRecordStatisticsService(WorkRecordMapper mapper, @Qualifier("workRecordClock") Clock clock) {
        this.mapper = mapper;
        this.clock = clock;
    }

    public void validatePeriod(int year, int month) {
        if (year < 2025 || year > 2029 || month < 1 || month > 12) {
            throw new WorkRecordException(400, "纪实年月必须在2025—2029年及1—12月范围内");
        }
        if (YearMonth.of(year, month).isAfter(YearMonth.now(clock.withZone(ZONE)))) {
            throw new WorkRecordException(400, "不能创建未来月份的工作纪实");
        }
    }

    /** schoolWide=true 时统计全校该年度全部三级任务（双高办/管理员填报口径）。 */
    public Statistics calculate(Long ownerId, int year, int month, boolean schoolWide) {
        validatePeriod(year, month);
        Instant now = clock.instant();
        Instant end = YearMonth.of(year, month).plusMonths(1).atDay(1).atStartOfDay(ZONE).toInstant().minusMillis(1);
        Instant cutoff = now.isBefore(end) ? now : end;
        List<Task> tasks = schoolWide ? mapper.allLevel3Tasks(year) : mapper.ownedTasks(ownerId, year);
        List<Evidence> evidence = tasks.isEmpty() ? List.of() : mapper.evidence(
                tasks.stream().map(BizTask::getTaskId).toList(), Date.from(cutoff));
        Map<Long, List<Evidence>> byTask = evidence.stream().collect(Collectors.groupingBy(Evidence::getParentTaskId));
        Map<Long, BizTask> structure = mapper.structure().stream().collect(Collectors.toMap(BizTask::getTaskId, t -> t));
        Map<Long, ReformTask> reforms = new LinkedHashMap<>();
        List<TaskSummary> details = new ArrayList<>();
        for (Task task : tasks) {
            BizTask root = reform(task, structure);
            if (root != null) reforms.putIfAbsent(root.getTaskId(), new ReformTask(root.getTaskId(), root.getTaskCode(), root.getTaskName()));
            details.add(summarize(task, root, byTask.getOrDefault(task.getTaskId(), List.of()), year, month, cutoff));
        }
        int completed = (int) details.stream().filter(TaskSummary::completed).count();
        int added = (int) details.stream().filter(TaskSummary::completedThisMonth).count();
        int unknown = (int) details.stream().filter(TaskSummary::needsVerification).count();
        BigDecimal rate = tasks.isEmpty() ? null : BigDecimal.valueOf(completed).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(tasks.size()), 2, RoundingMode.HALF_UP);
        String notice = "采用生成时的年度任务配置；统计按三级任务去重，勾选文字填报项目不改变统计范围。";
        if (unknown > 0) notice += "可核实统计：" + unknown + "条任务历史完成时间待核实，未计入完成数量。";
        if (details.stream().anyMatch(row -> row.reformTaskId() == null)) notice += "部分任务缺少有效一级任务目录，请先核实任务配置。";
        return new Statistics(1, year, month, format(now), format(cutoff), tasks.size(), added, completed, rate,
                unknown, notice, details, List.copyOf(reforms.values()));
    }

    private TaskSummary summarize(Task task, BizTask root, List<Evidence> rows, int year, int month, Instant cutoff) {
        boolean validTarget = task.getTargetValue() != null && task.getTargetValue().signum() > 0;
        List<Evidence> direct = rows.stream().filter(row -> task.getTaskId().equals(row.getSourceTaskId())).toList();
        Instant first = !validTarget ? null : direct.stream()
                .filter(row -> row.getArchiveTime() != null && row.getSubmitTime() != null && row.getReportedValue() != null)
                .filter(row -> !row.getArchiveTime().before(row.getSubmitTime()) && row.getReportedValue().compareTo(task.getTargetValue()) >= 0)
                .map(row -> row.getArchiveTime().toInstant()).min(Comparator.naturalOrder()).orElse(null);
        Instant candidate = first;
        // 旧快照已显示完成，但更早的归档证据丢失时，不能把后续重提的月份当成首次完成月份。
        boolean previousGap = direct.stream().anyMatch(row -> "3".equals(row.getPreviousStatus()) && row.getSubmitTime() != null
                && !row.getSubmitTime().toInstant().isAfter(cutoff)
                && (candidate == null || row.getSubmitTime().toInstant().isBefore(candidate)));
        boolean childArchive = rows.stream().anyMatch(row -> !task.getTaskId().equals(row.getSourceTaskId())
                && row.getArchiveTime() != null && !row.getArchiveTime().toInstant().isAfter(cutoff));
        boolean missingArchive = direct.stream().anyMatch(row -> Integer.valueOf(40).equals(row.getFlowStatus())
                && row.getArchiveTime() == null && row.getSubmitTime() != null && !row.getSubmitTime().toInstant().isAfter(cutoff)
                && (candidate == null || row.getSubmitTime().toInstant().isBefore(candidate))
                && (row.getReportedValue() == null || (validTarget && row.getReportedValue().compareTo(task.getTargetValue()) >= 0)));
        boolean unexplainedComplete = first == null && "3".equals(task.getStatus());
        String reason = "";
        if (!validTarget) reason = "任务目标缺失或非正数，无法核实达标时间";
        else if (previousGap) reason = "历史快照显示此前已完成，但首次完成归档证据缺失";
        else if (missingArchive) reason = "存在已归档的历史达标填报，但归档时间证据缺失";
        else if (first != null && first.atZone(ZONE).getYear() != year) reason = "首次达标归档时间与任务所属年度不一致";
        else if (first == null && childArchive) reason = "仅有四级归档记录，缺少可核实的三级任务归档汇总值";
        else if (first == null && unexplainedComplete) reason = "历史完成时间待核实：缺少有效归档时间或填报值";
        boolean unknown = !reason.isEmpty();
        boolean completed = !unknown && first != null && !first.isAfter(cutoff);
        boolean added = completed && first.atZone(ZONE).getMonthValue() == month;
        String state = auditState(rows, cutoff);
        if (unknown) state += "；历史完成时间待核实";
        return new TaskSummary(task.getTaskId(), task.getTaskCode(), task.getTaskName(), task.getLeaderName(),
                root == null ? null : root.getTaskId(), state, completed ? format(first) : null,
                completed, added, unknown, reason, "");
    }

    private String auditState(List<Evidence> rows, Instant cutoff) {
        Map<Long, Evidence> latest = new LinkedHashMap<>();
        for (Evidence row : rows) {
            if (row.getSubmitTime() != null && !row.getSubmitTime().toInstant().isAfter(cutoff)) latest.put(row.getSourceTaskId(), row);
        }
        if (latest.isEmpty()) return "未填报";
        return latest.values().stream().map(row -> {
            Integer state = row.getCutoffStatus();
            if (state == null) return "历史审核情况待核实";
            return switch (state) {
                case 0 -> "草稿";
                case 10 -> "待专业群审核";
                case 20 -> "待归口部门审核";
                case 30 -> "待双高办归档";
                case 40 -> "已归档";
                case -10, -20, -30 -> "已退回";
                default -> "历史审核情况待核实";
            };
        }).distinct().sorted().collect(Collectors.joining(" / "));
    }

    private BizTask reform(BizTask task, Map<Long, BizTask> structure) {
        Set<Long> visited = new HashSet<>();
        BizTask current = task;
        while (current != null && visited.add(current.getTaskId())) {
            if (Integer.valueOf(1).equals(current.getLevel())) return current;
            current = structure.get(current.getParentId());
        }
        return null;
    }

    private String format(Instant value) { return value.atZone(ZONE).toOffsetDateTime().toString(); }
}
