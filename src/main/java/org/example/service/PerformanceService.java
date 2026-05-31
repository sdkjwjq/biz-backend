package org.example.service;

import org.example.entity.BizPerformance;
import org.example.entity.BizPerformanceAuditLog;
import org.example.entity.BizPerformanceAuditSnapshot;
import org.example.entity.BizPerformanceSubmission;
import org.example.entity.BizPerformanceYear;
import org.example.entity.BizTask;
import org.example.entity.RelTaskPerformance;
import org.example.entity.SysNotice;
import org.example.entity.SysUser;
import org.example.entity.dto.BizAuditDTO;
import org.example.entity.vo.PerformanceAuditVO;
import org.example.entity.vo.PerformanceYearVO;
import org.example.mapper.BizMapper;
import org.example.mapper.PerformanceMapper;
import org.example.mapper.SysMapper;
import org.example.utils.BusinessLogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PerformanceService {
    private static final Long ADMIN_ID = 110228L;

    @Autowired
    private PerformanceMapper performanceMapper;

    @Autowired
    private BizMapper taskMapper;

    @Autowired
    private SysMapper sysMapper;

    /**
     * 判断绩效是否需要手动填报。
     * 目前 1.3、2、3 开头的绩效暂不参与任务自动汇总。
     */
    private boolean isManualPerformance(BizPerformance pref) {
        if (pref == null || pref.getPerfCode() == null) {
            return true;
        }
        String perfCode = pref.getPerfCode();
        return perfCode.startsWith("1.3") || perfCode.startsWith("2") || perfCode.startsWith("3");
    }

    private boolean shouldSkipPerformance(BizPerformance pref) {
        return isManualPerformance(pref);
    }

    private boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        SysUser user = sysMapper.getUserById(userId);
        return user != null && "0".equals(user.getRole());
    }

    private boolean canViewPerformance(BizPerformance performance, Long userId) {
        if (performance == null || userId == null) {
            return false;
        }
        return isAdmin(userId)
                || Objects.equals(performance.getLeaderId(), userId)
                || Objects.equals(performance.getAuditorId(), userId)
                || Objects.equals(performance.getPrincipalId(), userId);
    }

    private boolean canModifyPerformance(BizPerformance performance, Long userId) {
        if (performance == null || userId == null) {
            return false;
        }
        return isAdmin(userId) || Objects.equals(performance.getLeaderId(), userId);
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal aggregate(String dataType, BigDecimal current, BigDecimal value) {
        BigDecimal safeCurrent = zeroIfNull(current);
        BigDecimal safeValue = zeroIfNull(value);
        if ("2".equals(dataType)) {
            BigDecimal maxValue = safeValue.compareTo(safeCurrent) > 0 ? safeValue : safeCurrent;
            return maxValue.compareTo(BigDecimal.valueOf(100)) > 0 ? BigDecimal.valueOf(100) : maxValue;
        }
        return safeCurrent.add(safeValue);
    }

    private void addRelationSample(List<Map<String, Object>> samples, String reason,
                                   RelTaskPerformance rel, BizPerformance performance,
                                   BizPerformanceYear year, BizTask task) {
        if (samples.size() >= 20) {
            return;
        }
        Map<String, Object> sample = new LinkedHashMap<>();
        sample.put("reason", reason);
        sample.put("relationId", rel == null ? null : rel.getId());
        sample.put("perfId", performance == null ? (rel == null ? null : rel.getPerfId()) : performance.getPerfId());
        sample.put("perfCode", performance == null ? null : performance.getPerfCode());
        sample.put("perfName", performance == null ? null : performance.getPerfName());
        sample.put("relationYear", year == null ? null : year.getYear());
        sample.put("taskId", task == null ? (rel == null ? null : rel.getTaskId()) : task.getTaskId());
        sample.put("taskCode", task == null ? null : task.getTaskCode());
        sample.put("taskName", task == null ? null : task.getTaskName());
        sample.put("taskYear", task == null ? null : task.getPhase());
        sample.put("taskDataType", task == null ? null : task.getDataType());
        samples.add(sample);
    }

    private Object calculateAllPerformanceV2() {
        long startTime = System.currentTimeMillis();
        List<BizTask> allTasks = taskMapper.getAllTasks();
        List<BizPerformance> allPerformances = performanceMapper.getAllPerformance();
        List<RelTaskPerformance> allRels = performanceMapper.getAllRelTaskPerformance();
        List<BizPerformanceYear> allYears = performanceMapper.getAllPerformanceYear();

        Map<Long, BizTask> taskMap = new HashMap<>();
        Map<Long, BizTask> allTaskMap = new HashMap<>();
        for (BizTask task : allTasks) {
            if (task == null || task.getTaskId() == null) {
                continue;
            }
            allTaskMap.put(task.getTaskId(), task);
            if (task.getIsDelete() != null && task.getIsDelete() == 1) {
                continue;
            }
            if (task.getLevel() != null && (task.getLevel() == 1 || task.getLevel() == 2)) {
                continue;
            }
            if ("0".equals(task.getDataType())) {
                continue;
            }
            taskMap.put(task.getTaskId(), task);
        }

        Map<Long, BizPerformance> performanceMap = new HashMap<>();
        Set<Long> manualPerfIds = new HashSet<>();
        for (BizPerformance performance : allPerformances) {
            if (performance == null || performance.getPerfId() == null) {
                continue;
            }
            if (performance.getIsDelete() != null && performance.getIsDelete() == 1) {
                continue;
            }
            if (isManualPerformance(performance)) {
                manualPerfIds.add(performance.getPerfId());
                continue;
            }
            performanceMap.put(performance.getPerfId(), performance);
        }

        Map<Long, BizPerformanceYear> yearMap = new HashMap<>();
        Map<Long, List<BizPerformanceYear>> yearsByPerf = new HashMap<>();
        for (BizPerformanceYear year : allYears) {
            if (year == null || year.getYearId() == null || manualPerfIds.contains(year.getPerfId())) {
                continue;
            }
            if (year.getIsDelete() != null && year.getIsDelete() == 1) {
                continue;
            }
            year.setActualValue(BigDecimal.ZERO);
            yearMap.put(year.getYearId(), year);
            yearsByPerf.computeIfAbsent(year.getPerfId(), k -> new ArrayList<>()).add(year);
        }

        int relationCount = 0;
        int skippedMissingRelationCount = 0;
        int skippedTaskDataType0Count = 0;
        int skippedYearMismatchCount = 0;
        List<Map<String, Object>> skippedSamples = new ArrayList<>();
        for (RelTaskPerformance rel : allRels) {
            if (rel == null || manualPerfIds.contains(rel.getPerfId())) {
                continue;
            }
            BizTask rawTask = allTaskMap.get(rel.getTaskId());
            BizTask task = taskMap.get(rel.getTaskId());
            BizPerformance performance = performanceMap.get(rel.getPerfId());
            BizPerformanceYear year = yearMap.get(rel.getYearId());
            if (task == null || performance == null || year == null) {
                skippedMissingRelationCount++;
                if (rawTask != null && "0".equals(rawTask.getDataType())) {
                    skippedTaskDataType0Count++;
                    addRelationSample(skippedSamples, "task_data_type_0", rel, performance, year, rawTask);
                } else {
                    addRelationSample(skippedSamples, "missing_or_filtered_target", rel, performance, year, rawTask);
                }
                continue;
            }
            if (task.getPhase() == null || year.getYear() == null || !task.getPhase().equals(year.getYear())) {
                skippedYearMismatchCount++;
                addRelationSample(skippedSamples, "year_mismatch", rel, performance, year, task);
                continue;
            }
            BigDecimal contribution = zeroIfNull(task.getCurrentValue());
            year.setActualValue(aggregate(performance.getDataType(), year.getActualValue(), contribution));
            relationCount++;
        }

        int yearUpdateCount = 0;
        for (BizPerformanceYear year : yearMap.values()) {
            performanceMapper.updatePerformanceYear(year);
            yearUpdateCount++;
        }

        int performanceUpdateCount = 0;
        for (BizPerformance performance : performanceMap.values()) {
            BigDecimal current = BigDecimal.ZERO;
            List<BizPerformanceYear> years = yearsByPerf.get(performance.getPerfId());
            if (years != null) {
                for (BizPerformanceYear year : years) {
                    current = aggregate(performance.getDataType(), current, year.getActualValue());
                }
            }
            performance.setCurrentValue(current);
            performance.setUpdateTime(new Date());
            performanceMapper.updatePerformance(performance);
            performanceUpdateCount++;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "绩效数据已更新");
        result.put("performanceUpdateCount", performanceUpdateCount);
        result.put("yearUpdateCount", yearUpdateCount);
        result.put("validRelationCount", relationCount);
        result.put("skippedManualPerformanceCount", manualPerfIds.size());
        result.put("skippedMissingRelationCount", skippedMissingRelationCount);
        result.put("skippedTaskDataType0Count", skippedTaskDataType0Count);
        result.put("skippedYearMismatchCount", skippedYearMismatchCount);
        result.put("elapsedMs", System.currentTimeMillis() - startTime);
        result.put("samples", skippedSamples);
        BusinessLogUtil.info("绩效刷新",
                "result", "完成",
                "performanceUpdateCount", performanceUpdateCount,
                "yearUpdateCount", yearUpdateCount,
                "validRelationCount", relationCount,
                "skippedManualPerformanceCount", manualPerfIds.size(),
                "skippedMissingRelationCount", skippedMissingRelationCount,
                "skippedTaskDataType0Count", skippedTaskDataType0Count,
                "skippedYearMismatchCount", skippedYearMismatchCount,
                "elapsedMs", result.get("elapsedMs"));
        return result;
    }

    @Transactional
    public Object calcuateAllPerformance() {
        return calculateAllPerformanceV2();
    }

    public Object updatePerformanceByTaskId(Long taskId) {
        if (taskId == null) {
            throw new RuntimeException("任务ID不能为空");
        }

        Object result = calcuateAllPerformance();
        BizTask task = taskMapper.getTaskById(taskId);
        BusinessLogUtil.info("任务影响绩效",
                "result", "已刷新",
                "taskId", taskId,
                "taskName", task == null ? null : task.getTaskName(),
                "taskYear", task == null ? null : task.getPhase(),
                "taskCurrentValue", task == null ? null : task.getCurrentValue());
        return result;
    }

    public Object getAllPerformance() {
        return performanceMapper.getAllPerformance();
    }

    public Object getAllPerformance(Long userId) {
        if (isAdmin(userId)) {
            return performanceMapper.getAllPerformance();
        }
        return performanceMapper.getVisiblePerformance(userId);
    }

    public Object getPerformanceById(Long perfId) {
        if (perfId == null) {
            throw new RuntimeException("绩效ID不能为空");
        }
        BizPerformance performance = performanceMapper.getPerformanceById(perfId);
        if (performance == null) {
            throw new RuntimeException("没有找到该绩效");
        }
//        if (shouldSkipPerformance(performance)) {
//            throw new RuntimeException("该绩效暂不参与自动计算");
//        }
        return performance;
    }

    public Object getPerformanceById(Long perfId, Long userId) {
        BizPerformance performance = (BizPerformance) getPerformanceById(perfId);
        if (!canViewPerformance(performance, userId)) {
            throw new RuntimeException("无权查看该绩效");
        }
        return performance;
    }

    public Object getPerfByTaskId(Long taskId) {
        if (taskId == null) {
            throw new RuntimeException("任务ID不能为空");
        }
        BizTask task = taskMapper.getTaskById(taskId);
        if (task == null) {
            throw new RuntimeException("没有找到该任务");
        }

        List<Long> prefIds = performanceMapper.getPerfIdByTaskId(taskId);
        if (prefIds == null || prefIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<BizPerformance> prefList = new ArrayList<>();
        for (Long prefId : prefIds) {
            BizPerformance performance = performanceMapper.getPerformanceById(prefId);
            if (performance != null) {
                prefList.add(performance);
            }
        }
        return prefList;
    }

    public Object getTaskByPerfId(Long perfId) {
        if (perfId == null) {
            throw new RuntimeException("绩效ID不能为空");
        }
        BizPerformance performance = performanceMapper.getPerformanceById(perfId);
        if (performance == null) {
            throw new RuntimeException("没有找到该绩效");
        }


        List<Long> taskIds = performanceMapper.getTaskIdByPerfId(perfId);
        if (taskIds == null || taskIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<BizTask> taskList = new ArrayList<>();
        for (Long taskId : taskIds) {
            BizTask task = taskMapper.getTaskById(taskId);
            if (task != null) {
                taskList.add(task);
            }
        }
        return taskList;
    }

    public Object getTaskByPerfId(Long perfId, Integer year) {
        if (perfId == null) {
            throw new RuntimeException("绩效ID不能为空");
        }
        BizPerformance performance = performanceMapper.getPerformanceById(perfId);
        if (performance == null) {
            throw new RuntimeException("没有找到该绩效");
        }

        List<Long> taskIds = year == null
                ? performanceMapper.getTaskIdByPerfId(perfId)
                : performanceMapper.getTaskIdByPerfIdAndYear(perfId, year);
        if (taskIds == null || taskIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<BizTask> taskList = new ArrayList<>();
        for (Long taskId : taskIds) {
            BizTask task = taskMapper.getTaskById(taskId);
            if (task != null && (task.getIsDelete() == null || task.getIsDelete() == 0)) {
                taskList.add(task);
            }
        }
        return taskList;
    }

    public Object getTaskByPerfId(Long perfId, Long userId) {
        BizPerformance performance = performanceMapper.getPerformanceById(perfId);
        if (!canViewPerformance(performance, userId)) {
            throw new RuntimeException("无权查看该绩效关联任务");
        }
        return getTaskByPerfId(perfId);
    }

    /**
     * 按年度查看某个绩效关联的任务。
     */
    public Object getTaskByPerfIdForYear(Long perfId, Integer year, Long userId) {
        BizPerformance performance = performanceMapper.getPerformanceById(perfId);
        if (!canViewPerformance(performance, userId)) {
            throw new RuntimeException("无权查看该绩效关联任务");
        }
        return getTaskByPerfId(perfId, year);
    }

    public Object getPerformanceByYear(Integer year) {
        if (year == null) {
            throw new RuntimeException("请输入有效年份");
        }

        List<BizPerformanceYear> prefYears = performanceMapper.getPerformanceYearByYear(year);
        if (prefYears == null || prefYears.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> perfIds = new ArrayList<>();
        for (BizPerformanceYear prefYear : prefYears) {
            if (prefYear != null && prefYear.getPerfId() != null) {
                perfIds.add(prefYear.getPerfId());
            }
        }

        List<BizPerformance> allPerformances = performanceMapper.getAllPerformance();

        Map<Long, BizPerformance> perfMap = new HashMap<>();
        for (BizPerformance perf : allPerformances) {
            if (perf != null) {
                perfMap.put(perf.getPerfId(), perf);
            }
        }

        List<PerformanceYearVO> prefYearVOS = new ArrayList<>();
        for (BizPerformanceYear prefYear : prefYears) {
            if (prefYear == null) {
                continue;
            }
            BizPerformance performance = perfMap.get(prefYear.getPerfId());
            if (performance != null) {
                prefYearVOS.add(new PerformanceYearVO(performance, prefYear));
            }
        }

        return prefYearVOS;
    }

    @SuppressWarnings("unchecked")
    public Object getPerformanceByYear(Integer year, Long userId) {
        List<PerformanceYearVO> list = (List<PerformanceYearVO>) getPerformanceByYear(year);
        if (isAdmin(userId)) {
            return list;
        }
        List<PerformanceYearVO> visible = new ArrayList<>();
        for (PerformanceYearVO vo : list) {
            if (Objects.equals(vo.getLeaderId(), userId)
                    || Objects.equals(vo.getAuditorId(), userId)
                    || Objects.equals(vo.getPrincipalId(), userId)) {
                visible.add(vo);
            }
        }
        return visible;
    }

    public Object submitPref(Long perfId,BigDecimal actualValue,Integer year){
        if(actualValue.intValue()<0 || actualValue.intValue()>100 ){
            throw new RuntimeException("请输入有效的实际完成度");
        }
        if(year == null){
            throw new RuntimeException("请输入有效年份");
        }

        BizPerformance pref = performanceMapper.getPerformanceById(perfId);
        if(pref == null){
            throw new RuntimeException("没有找到该绩效");
        }
        if(pref.getPerfCode().startsWith("1.3") || pref.getPerfCode().startsWith("2") || pref.getPerfCode().startsWith("3")){
            pref.setCurrentValue(actualValue.multiply(BigDecimal.valueOf(0.2)));
            pref.setUpdateTime(new Date());
            performanceMapper.updatePerformance(pref);
            BizPerformanceYear prefYear = performanceMapper.getPerformanceYearByPerfIdAndYear(perfId,year);
            prefYear.setActualValue(actualValue);
            performanceMapper.updatePerformanceYear(prefYear);
            return "填报成功";
        }else {
            throw new RuntimeException("该绩效由任务自动计算，不可手动填报");
        }
    }
    @Transactional
    public Object submitPref(Long perfId, BigDecimal actualValue, Integer year, Long userId, String comment) {
        if (perfId == null) {
            throw new RuntimeException("绩效ID不能为空");
        }
        if (actualValue == null || actualValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("请输入有效的实际值");
        }
        if (year == null) {
            throw new RuntimeException("请输入有效年份");
        }

        BizPerformance performance = performanceMapper.getPerformanceById(perfId);
        if (performance == null) {
            throw new RuntimeException("没有找到该绩效");
        }
        if (!isManualPerformance(performance)) {
            throw new RuntimeException("该绩效由任务自动计算，不可手动填报");
        }
        if (!canModifyPerformance(performance, userId)) {
            throw new RuntimeException("无权填报该绩效");
        }

        if ("2".equals(performance.getDataType()) && actualValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new RuntimeException("百分比类型绩效不能超过100");
        }
        if (performance.getAuditorId() == null) {
            throw new RuntimeException("该绩效未设置专业群审核人，无法提交");
        }

        BizPerformanceYear performanceYear = performanceMapper.getPerformanceYearByPerfIdAndYear(perfId, year);
        if (performanceYear == null) {
            throw new RuntimeException("没有找到该绩效对应年度");
        }
        BizPerformanceSubmission active = performanceMapper.getActivePerformanceSubmission(perfId, year);
        if (active != null) {
            throw new RuntimeException("已有正在审核中的绩效填报");
        }

        BizPerformanceSubmission submission = new BizPerformanceSubmission();
        submission.setPerfId(perfId);
        submission.setYearId(performanceYear.getYearId());
        submission.setYear(year);
        submission.setActualValue(actualValue);
        submission.setSubmitBy(userId);
        submission.setSubmitTime(new Date());
        submission.setFlowStatus(10);
        submission.setCurrentHandlerId(performance.getAuditorId());
        submission.setComment(comment);
        submission.setIsDelete(0);
        performanceMapper.createPerformanceSubmission(submission);

        BizPerformanceAuditSnapshot snapshot = new BizPerformanceAuditSnapshot();
        snapshot.setSubId(submission.getSubId());
        snapshot.setPerfId(perfId);
        snapshot.setYearId(performanceYear.getYearId());
        snapshot.setPreviousPerformanceValue(zeroIfNull(performance.getCurrentValue()));
        snapshot.setPreviousPerformanceUpdateTime(performance.getUpdateTime());
        snapshot.setPreviousYearActualValue(zeroIfNull(performanceYear.getActualValue()));
        snapshot.setPreviousYearTargetValue(zeroIfNull(performanceYear.getTargetValue()));
        snapshot.setCreateTime(new Date());
        performanceMapper.createPerformanceAuditSnapshot(snapshot);

        performanceYear.setActualValue(actualValue);
        performanceMapper.updatePerformanceYear(performanceYear);
        recalculateSinglePerformanceFromYears(performance);

        createPerformanceAuditLog(submission.getSubId(), userId, "submit", 0, 10, comment);
        sendNotice(userId, performance.getAuditorId(), "绩效审核", "绩效待审核",
                "绩效「" + performance.getPerfName() + "」已提交，请进行专业群审核", "2", submission.getSubId());
        BusinessLogUtil.info("绩效填报",
                "result", "成功",
                "userId", userId,
                "perfId", performance.getPerfId(),
                "perfName", performance.getPerfName(),
                "year", year,
                "subId", submission.getSubId(),
                "actualValue", actualValue,
                "previousYearActualValue", snapshot.getPreviousYearActualValue(),
                "currentPerformanceValue", performance.getCurrentValue(),
                "nextHandlerId", performance.getAuditorId());
        return "绩效已提交";
    }

    @Transactional
    public Object auditPerformance(BizAuditDTO auditDTO, Long userId) {
        if (auditDTO == null || auditDTO.getSub_id() == null) {
            throw new RuntimeException("绩效审核单ID不能为空");
        }
        BizPerformanceSubmission submission = performanceMapper.getPerformanceSubmissionById(auditDTO.getSub_id());
        if (submission == null || (submission.getIsDelete() != null && submission.getIsDelete() == 1)) {
            throw new RuntimeException("绩效审核单不存在");
        }
        if (!Objects.equals(submission.getCurrentHandlerId(), userId) && !isAdmin(userId)) {
            throw new RuntimeException("当前用户不是该绩效审核单的处理人");
        }
        BizPerformance performance = performanceMapper.getPerformanceById(submission.getPerfId());
        if (performance == null) {
            throw new RuntimeException("绩效指标不存在");
        }

        Integer preStatus = submission.getFlowStatus();
        boolean pass = Boolean.TRUE.equals(auditDTO.getIs_pass());
        String auditComment = auditDTO.getTitle() != null ? auditDTO.getTitle() : auditDTO.getContent();

        if (!pass) {
            Integer rejectStatus = preStatus != null && preStatus == 20 ? -20 : -10;
            restorePerformanceSnapshot(submission);
            submission.setFlowStatus(rejectStatus);
            submission.setCurrentHandlerId(submission.getSubmitBy());
            submission.setComment(auditComment);
            performanceMapper.updatePerformanceSubmission(submission);
            createPerformanceAuditLog(submission.getSubId(), userId, "reject", preStatus, rejectStatus, auditComment);
            BusinessLogUtil.info("绩效审核",
                    "result", "退回",
                    "operatorId", userId,
                    "perfId", performance.getPerfId(),
                    "perfName", performance.getPerfName(),
                    "year", submission.getYear(),
                    "subId", submission.getSubId(),
                    "preStatus", preStatus,
                    "postStatus", rejectStatus,
                    "nextHandlerId", submission.getSubmitBy());
            sendNotice(userId, submission.getSubmitBy(), "绩效退回", "绩效已退回",
                    "绩效「" + performance.getPerfName() + "」已被退回，请查看处理意见", "2", submission.getSubId());
            return "绩效已退回";
        }

        if (preStatus != null && preStatus == 10) {
            submission.setFlowStatus(20);
            submission.setCurrentHandlerId(ADMIN_ID);
            submission.setComment(auditComment);
            performanceMapper.updatePerformanceSubmission(submission);
            createPerformanceAuditLog(submission.getSubId(), userId, "pass", 10, 20, auditComment);
            BusinessLogUtil.info("绩效审核",
                    "result", "专业群通过",
                    "operatorId", userId,
                    "perfId", performance.getPerfId(),
                    "perfName", performance.getPerfName(),
                    "year", submission.getYear(),
                    "subId", submission.getSubId(),
                    "preStatus", 10,
                    "postStatus", 20,
                    "nextHandlerId", ADMIN_ID);
            sendNotice(userId, ADMIN_ID, "绩效归档", "绩效待完结归档",
                    "绩效「" + performance.getPerfName() + "」专业群审核已通过，请进行完结归档", "2", submission.getSubId());
            return "绩效已通过专业群审核，待完结归档";
        }
        if (preStatus != null && preStatus == 20) {
            submission.setFlowStatus(30);
            submission.setCurrentHandlerId(ADMIN_ID);
            submission.setComment(auditComment);
            performanceMapper.updatePerformanceSubmission(submission);
            createPerformanceAuditLog(submission.getSubId(), userId, "pass", 20, 30, auditComment);
            BusinessLogUtil.info("绩效审核",
                    "result", "归档完成",
                    "operatorId", userId,
                    "perfId", performance.getPerfId(),
                    "perfName", performance.getPerfName(),
                    "year", submission.getYear(),
                    "subId", submission.getSubId(),
                    "preStatus", 20,
                    "postStatus", 30,
                    "actualValue", submission.getActualValue());
            sendNotice(userId, submission.getSubmitBy(), "绩效归档完成", "绩效已完结归档",
                    "绩效「" + performance.getPerfName() + "」已完结归档", "2", submission.getSubId());
            return "绩效已完结归档";
        }
        throw new RuntimeException("当前状态不可审核");
    }

    @Transactional
    public Object withdrawPerformanceSubmission(Long subId, Long userId) {
        BizPerformanceSubmission submission = performanceMapper.getPerformanceSubmissionById(subId);
        if (submission == null || (submission.getIsDelete() != null && submission.getIsDelete() == 1)) {
            throw new RuntimeException("绩效审核单不存在");
        }
        if (!Objects.equals(submission.getSubmitBy(), userId) && !isAdmin(userId)) {
            throw new RuntimeException("无权撤回该绩效填报");
        }
        if (submission.getFlowStatus() == null || submission.getFlowStatus() >= 30 || submission.getFlowStatus() <= 0) {
            throw new RuntimeException("当前状态不可撤回");
        }
        Integer preStatus = submission.getFlowStatus();
        restorePerformanceSnapshot(submission);
        submission.setFlowStatus(0);
        submission.setIsDelete(1);
        submission.setCurrentHandlerId(submission.getSubmitBy());
        performanceMapper.updatePerformanceSubmission(submission);
        createPerformanceAuditLog(subId, userId, "withdraw", preStatus, 0, "撤回绩效填报");
        BizPerformance performance = performanceMapper.getPerformanceById(submission.getPerfId());
        BusinessLogUtil.info("绩效撤回",
                "result", "成功",
                "userId", userId,
                "perfId", submission.getPerfId(),
                "perfName", performance == null ? null : performance.getPerfName(),
                "year", submission.getYear(),
                "subId", subId,
                "preStatus", preStatus,
                "postStatus", 0);
        return "绩效填报已撤回";
    }

    public Object getTodoPerformanceAudits(Long userId) {
        return toPerformanceAuditVOs(performanceMapper.getTodoPerformanceSubmissions(userId));
    }

    public Object getPerformanceAuditRecords(Long userId) {
        return toPerformanceAuditVOs(performanceMapper.getPerformanceSubmissionRecords(userId));
    }

    public Object getPerformanceAuditsByPerfAndYear(Long perfId, Integer year, Long userId) {
        BizPerformance performance = performanceMapper.getPerformanceById(perfId);
        if (!canViewPerformance(performance, userId)) {
            throw new RuntimeException("无权查看该绩效");
        }
        return toPerformanceAuditVOs(performanceMapper.getPerformanceSubmissionsByPerfAndYear(perfId, year));
    }

    public Object getPerformanceAuditLogs(Long subId, Long userId) {
        BizPerformanceSubmission submission = performanceMapper.getPerformanceSubmissionById(subId);
        if (submission == null) {
            throw new RuntimeException("没有找到该绩效审核单");
        }
        BizPerformance performance = performanceMapper.getPerformanceById(submission.getPerfId());
        if (!canViewPerformance(performance, userId) && !Objects.equals(submission.getSubmitBy(), userId)) {
            throw new RuntimeException("无权查看该绩效审核日志");
        }
        return performanceMapper.getPerformanceAuditLogs(subId);
    }

    private List<PerformanceAuditVO> toPerformanceAuditVOs(List<BizPerformanceSubmission> submissions) {
        List<PerformanceAuditVO> result = new ArrayList<>();
        if (submissions == null) {
            return result;
        }
        for (BizPerformanceSubmission submission : submissions) {
            PerformanceAuditVO vo = performanceMapper.getPerformanceAuditVO(submission.getSubId());
            if (vo != null) {
                result.add(vo);
            }
        }
        return result;
    }

    private void restorePerformanceSnapshot(BizPerformanceSubmission submission) {
        BizPerformanceAuditSnapshot snapshot = performanceMapper.getPerformanceAuditSnapshot(submission.getSubId());
        if (snapshot == null) {
            throw new RuntimeException("没有找到绩效快照，无法恢复");
        }
        BizPerformance performance = performanceMapper.getPerformanceById(snapshot.getPerfId());
        BizPerformanceYear year = performanceMapper.getPerformanceYearById(snapshot.getYearId());
        if (performance == null || year == null) {
            throw new RuntimeException("绩效或年度绩效不存在，无法恢复");
        }
        year.setActualValue(zeroIfNull(snapshot.getPreviousYearActualValue()));
        year.setTargetValue(zeroIfNull(snapshot.getPreviousYearTargetValue()));
        performanceMapper.updatePerformanceYear(year);
        performance.setCurrentValue(zeroIfNull(snapshot.getPreviousPerformanceValue()));
        performance.setUpdateTime(snapshot.getPreviousPerformanceUpdateTime());
        performanceMapper.updatePerformance(performance);
    }

    private void recalculateSinglePerformanceFromYears(BizPerformance performance) {
        List<BizPerformanceYear> years = performanceMapper.getPerformanceYearByPerfId(performance.getPerfId());
        BigDecimal total = BigDecimal.ZERO;
        if (years != null) {
            for (BizPerformanceYear year : years) {
                total = aggregate(performance.getDataType(), total, year.getActualValue());
            }
        }
        performance.setCurrentValue(total);
        performance.setUpdateTime(new Date());
        performanceMapper.updatePerformance(performance);
    }

    private void createPerformanceAuditLog(Long subId, Long operatorId, String actionType,
                                           Integer preStatus, Integer postStatus, String comment) {
        BizPerformanceAuditLog log = new BizPerformanceAuditLog();
        log.setSubId(subId);
        log.setOperatorId(operatorId);
        log.setActionType(actionType);
        log.setPreStatus(preStatus);
        log.setPostStatus(postStatus);
        log.setComment(comment);
        log.setCreateTime(new Date());
        performanceMapper.createPerformanceAuditLog(log);
    }

    private void sendNotice(Long fromUserId, Long toUserId, String triggerEvent,
                            String title, String content, String sourceType, Long sourceId) {
        if (toUserId == null) {
            return;
        }
        SysNotice notice = new SysNotice();
        notice.setFromUserId(fromUserId);
        notice.setToUserId(toUserId);
        notice.setTriggerEvent(triggerEvent);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setSourceType(sourceType);
        notice.setSourceId(sourceId);
        notice.setIsRead("0");
        notice.setIsDelete(0);
        notice.setCreateTime(new Date());
        sysMapper.sendNotice(notice);
    }
}
