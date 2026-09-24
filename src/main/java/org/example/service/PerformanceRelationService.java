package org.example.service;

import org.example.entity.BizPerformance;
import org.example.entity.BizPerformanceRelationLog;
import org.example.entity.BizPerformanceYear;
import org.example.entity.BizTask;
import org.example.entity.SysUser;
import org.example.entity.dto.PerformanceRelationDTO;
import org.example.entity.vo.PerformanceRelationCandidateVO;
import org.example.entity.vo.PerformanceRelationLogVO;
import org.example.mapper.BizMapper;
import org.example.mapper.PerformanceMapper;
import org.example.mapper.PerformanceRelationMapper;
import org.example.mapper.SysMapper;
import org.example.utils.BusinessLogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 管理员编辑绩效关联任务：按指标 + 年度新增或解除关联，解除为软删除留痕。
 * 整批校验，任一记录不满足条件即整批拒绝；变更与日志在同一事务提交，提交后重算绩效。
 */
@Service
public class PerformanceRelationService {

    private static final String ACTION_ADD = "ADD";
    private static final String ACTION_REMOVE = "REMOVE";
    private static final int MAX_BATCH = 500;
    private static final int MAX_REASON = 200;
    private static final String EVENT = "绩效关联变更";

    @Autowired
    private SysMapper sysMapper;

    @Autowired
    private BizMapper bizMapper;

    @Autowired
    private PerformanceMapper performanceMapper;

    @Autowired
    private PerformanceRelationMapper relationMapper;

    @Autowired
    private PerformanceService performanceService;

    private record AddTarget(BizTask task, Long relationId) { }

    private record RemoveTarget(Long relationId, BizTask task) { }

    public boolean canManageRelation(Long userId) {
        SysUser user = userId == null ? null : sysMapper.getUserById(userId);
        return user != null && !Integer.valueOf(1).equals(user.getIsDelete()) && "0".equals(user.getRole());
    }

    /** 可关联任务候选：已关联的历史数据即使不再满足汇总条件也返回，便于解除。 */
    public Map<String, Object> candidates(Long perfId, Integer year, Long userId) {
        requireAdmin(userId);
        BizPerformance performance = performance(perfId);
        requireAutoPerformance(performance);
        BizPerformanceYear yearRow = yearRow(perfId, year);
        List<PerformanceRelationCandidateVO> rows = relationMapper.getCandidates(perfId, yearRow.getYearId(), year);
        for (PerformanceRelationCandidateVO row : rows) {
            row.setSkipReason(skipReason(row, year));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("perfId", perfId);
        result.put("year", year);
        result.put("yearId", yearRow.getYearId());
        result.put("candidates", rows);
        return result;
    }

    public List<PerformanceRelationLogVO> logs(Long perfId, Integer year, Long userId) {
        requireAdmin(userId);
        performance(perfId);
        if (year == null || year <= 1900) {
            throw new TaskManagementException(400, "请选择年度");
        }
        return relationMapper.getLogs(perfId, year);
    }

    /** 当前年度各指标的关联任务数，供列表页展示。 */
    public Map<String, Object> counts(Integer year, Long userId) {
        requireAdmin(userId);
        if (year == null || year <= 1900) {
            throw new TaskManagementException(400, "请选择年度");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map<String, Object> row : relationMapper.countRelationByYear(year)) {
            Object id = row.get("perfId");
            Object count = row.get("relationCount");
            if (id != null) {
                result.put(String.valueOf(id), count == null ? 0 : count);
            }
        }
        return result;
    }

    @Transactional
    public Map<String, Object> save(PerformanceRelationDTO input, Long userId) {
        requireAdmin(userId);
        if (input == null) {
            throw new TaskManagementException(400, "请填写关联信息");
        }
        Long perfId = input.getPerfId();
        Integer year = input.getYear();
        BizPerformance performance = performance(perfId);
        requireAutoPerformance(performance);
        BizPerformanceYear yearRow = yearRow(perfId, year);

        String reason = input.getReason() == null ? "" : input.getReason().trim();
        if (reason.isEmpty()) {
            throw new TaskManagementException(400, "请填写变更原因");
        }
        if (reason.codePointCount(0, reason.length()) > MAX_REASON) {
            throw new TaskManagementException(400, "变更原因不能超过 " + MAX_REASON + " 字");
        }

        List<Long> addIds = distinct(input.getAddTaskIds());
        List<Long> removeIds = distinct(input.getRemoveTaskIds());
        if (addIds.isEmpty() && removeIds.isEmpty()) {
            throw new TaskManagementException(400, "请选择需要新增或解除关联的任务");
        }
        if (addIds.size() + removeIds.size() > MAX_BATCH) {
            throw new TaskManagementException(400, "一次最多变更 " + MAX_BATCH + " 条关联");
        }
        List<Long> both = new ArrayList<>(addIds);
        both.retainAll(removeIds);
        if (!both.isEmpty()) {
            throw new TaskManagementException(400, "同一任务不能同时新增和解除关联");
        }
        if (relationMapper.countActiveAudits(perfId, year) > 0) {
            throw new TaskManagementException(409,
                    "该指标 " + year + " 年度存在审核中的单据，请先处理完成后再编辑关联任务");
        }

        Long yearId = yearRow.getYearId();
        List<AddTarget> pendingAdds = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        List<String> wrongLevel = new ArrayList<>();
        List<String> noEffect = new ArrayList<>();
        List<String> yearMismatch = new ArrayList<>();
        List<String> notLinked = new ArrayList<>();
        int skipped = 0;
        for (Long taskId : addIds) {
            BizTask task = bizMapper.getTaskByIdForUpdate(taskId);
            if (task == null || Integer.valueOf(1).equals(task.getIsDelete())) {
                missing.add("#" + taskId);
                continue;
            }
            if (!Integer.valueOf(3).equals(task.getLevel())) {
                wrongLevel.add(label(task.getTaskCode(), task.getTaskName()));
                continue;
            }
            if ("0".equals(task.getDataType())) {
                noEffect.add(label(task.getTaskCode(), task.getTaskName()));
                continue;
            }
            if (!Objects.equals(task.getPhase(), year)) {
                yearMismatch.add(label(task.getTaskCode(), task.getTaskName()));
                continue;
            }
            Long relationId = relationMapper.findRelationId(taskId, perfId, yearId);
            if (relationId != null && Integer.valueOf(0).equals(relationMapper.getRelationDeleted(relationId))) {
                skipped++;
                continue;
            }
            pendingAdds.add(new AddTarget(task, relationId));
        }
        List<RemoveTarget> pendingRemoves = new ArrayList<>();
        for (Long taskId : removeIds) {
            Long relationId = relationMapper.findRelationId(taskId, perfId, yearId);
            if (relationId == null || !Integer.valueOf(0).equals(relationMapper.getRelationDeleted(relationId))) {
                notLinked.add("#" + taskId);
                continue;
            }
            pendingRemoves.add(new RemoveTarget(relationId, bizMapper.getTaskByIdForUpdate(taskId)));
        }

        if (!missing.isEmpty()) {
            throw new TaskManagementException(400, "以下任务不存在或已删除：" + join(missing));
        }
        if (!wrongLevel.isEmpty()) {
            throw new TaskManagementException(400, "仅三级任务可以关联绩效：" + join(wrongLevel));
        }
        if (!noEffect.isEmpty()) {
            throw new TaskManagementException(400,
                    "以下任务数据类型为 0，关联后不会计入汇总，请先修正任务数据：" + join(noEffect));
        }
        if (!yearMismatch.isEmpty()) {
            throw new TaskManagementException(400,
                    "以下任务年度与指标 " + year + " 年度不一致，关联后不会计入汇总：" + join(yearMismatch));
        }
        if (!notLinked.isEmpty()) {
            throw new TaskManagementException(409, "以下任务当前未关联该指标，请刷新后重试：" + join(notLinked));
        }
        if (pendingAdds.isEmpty() && pendingRemoves.isEmpty()) {
            throw new TaskManagementException(400, "没有需要变更的内容");
        }

        String batchId = UUID.randomUUID().toString();
        Date now = new Date();
        for (AddTarget target : pendingAdds) {
            BizTask task = target.task();
            if (target.relationId() == null) {
                relationMapper.insertRelation(task.getTaskId(), perfId, yearId, task.getDataType());
            } else {
                // 之前解除过同一关联，重新挂回时复用原行，避免同一任务出现多条关联
                relationMapper.updateRelationDeleted(target.relationId(), 0);
            }
            writeLog(performance, year, ACTION_ADD, task, batchId, reason, userId, now);
        }
        for (RemoveTarget target : pendingRemoves) {
            relationMapper.updateRelationDeleted(target.relationId(), 1);
            if (target.task() != null) {
                writeLog(performance, year, ACTION_REMOVE, target.task(), batchId, reason, userId, now);
            }
        }

        // 关联变化后立即重算，保证列表与详情的完成值同步
        performanceService.calcuateAllPerformance();

        BusinessLogUtil.info("绩效关联变更",
                "result", "成功",
                "operatorId", userId,
                "perfId", perfId,
                "year", year,
                "batchId", batchId,
                "added", pendingAdds.size(),
                "removed", pendingRemoves.size(),
                "skipped", skipped,
                "reason", reason);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("batchId", batchId);
        result.put("added", pendingAdds.size());
        result.put("removed", pendingRemoves.size());
        result.put("skipped", skipped);
        return result;
    }

    private void writeLog(BizPerformance performance, Integer year, String action, BizTask task, String batchId,
                          String reason, Long userId, Date now) {
        BizPerformanceRelationLog log = new BizPerformanceRelationLog();
        log.setPerfId(performance.getPerfId());
        log.setPerfCode(performance.getPerfCode());
        log.setPerfName(performance.getPerfName());
        log.setYear(year);
        log.setAction(action);
        log.setTaskId(task.getTaskId());
        log.setTaskCode(task.getTaskCode());
        log.setTaskName(task.getTaskName());
        log.setBatchId(batchId);
        log.setOperatorId(userId);
        log.setReason(reason);
        log.setCreateTime(now);
        relationMapper.insertLog(log);
    }

    private void requireAdmin(Long userId) {
        if (!canManageRelation(userId)) {
            throw new TaskManagementException(403, "仅限管理员操作");
        }
    }

    private BizPerformance performance(Long perfId) {
        if (perfId == null || perfId <= 0) {
            throw new TaskManagementException(400, "指标ID不正确");
        }
        BizPerformance performance = performanceMapper.getPerformanceByIdForUpdate(perfId);
        if (performance == null || Integer.valueOf(1).equals(performance.getIsDelete())) {
            throw new TaskManagementException(400, "绩效指标不存在或已删除");
        }
        return performance;
    }

    /** 与绩效汇总口径一致：编码为空或以 1.3 / 2 / 3 开头的手动填报指标不参与任务汇总。 */
    private void requireAutoPerformance(BizPerformance performance) {
        String code = performance.getPerfCode();
        if (code == null || code.startsWith("1.3") || code.startsWith("2") || code.startsWith("3")) {
            throw new TaskManagementException(400, "该指标为手动填报指标，关联任务不会计入汇总");
        }
    }

    private BizPerformanceYear yearRow(Long perfId, Integer year) {
        if (year == null || year <= 1900) {
            throw new TaskManagementException(400, "请选择年度");
        }
        BizPerformanceYear yearRow = performanceMapper.getPerformanceYearByPerfIdAndYear(perfId, year);
        if (yearRow == null || Integer.valueOf(1).equals(yearRow.getIsDelete())) {
            throw new TaskManagementException(400, "该指标没有 " + year + " 年度的指标记录，无法维护关联");
        }
        return yearRow;
    }

    private String skipReason(PerformanceRelationCandidateVO row, Integer year) {
        if (Integer.valueOf(1).equals(row.getTaskDeleted())) {
            return "任务已删除，不计入汇总";
        }
        if (!Integer.valueOf(3).equals(row.getLevel())) {
            return "非三级任务，不计入汇总";
        }
        if ("0".equals(row.getDataType())) {
            return "任务数据类型为 0，不计入汇总";
        }
        if (!Objects.equals(row.getPhase(), year)) {
            return "任务年度与指标年度不一致，不计入汇总";
        }
        return null;
    }

    private List<Long> distinct(List<Long> ids) {
        if (ids == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(new LinkedHashSet<>(ids.stream().filter(Objects::nonNull).toList()));
    }

    private String label(String code, String name) {
        if (code == null || code.isBlank()) {
            return name == null ? "—" : name;
        }
        return name == null ? code : code + " " + name;
    }

    private String join(List<String> values) {
        List<String> trimmed = new ArrayList<>();
        for (String value : values) {
            if (trimmed.size() >= 10) {
                trimmed.add("…");
                break;
            }
            trimmed.add(value);
        }
        return String.join("、", trimmed);
    }
}
