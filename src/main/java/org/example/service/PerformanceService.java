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
     * 閸掋倖鏌囩紒鈺傛櫏閹稿洦鐖ｉ弰顖氭儊闂団偓鐟曚浇鐑︽潻鍥吀缁?
     * 鐠哄疇绻冮弶鈥叉閿涙erf_code 娴?"1.3"閵?"2"閵?"3" 瀵偓婢?
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
        if (System.currentTimeMillis() >= 0) {
            return calculateAllPerformanceV2();
        }
        try {
            System.out.println("========== 瀵偓婵顓哥粻妤佸閺堝鍝楅弫鍫熸殶閹?==========");
            long startTime = System.currentTimeMillis();

            // 1. 閸旂姾娴囬幍鈧張澶夋崲閸?
            List<BizTask> allTasks = taskMapper.getAllTasks();
            System.out.println("閸旂姾娴囨禒璇插閺佷即鍣? " + allTasks.size());

            // 2. 閸旂姾娴囬幍鈧張澶屽摋閺佸牊瀵氶弽?
            List<BizPerformance> allPerformance = performanceMapper.getAllPerformance();
            System.out.println("閸旂姾娴囩紒鈺傛櫏閹稿洦鐖ｉ弫浼村櫤: " + allPerformance.size());

            // 3. 鏉╁洦鎶ら幒澶愭付鐟曚浇鐑︽潻鍥╂畱缂佲晜鏅ラ幐鍥ㄧ垼
            List<BizPerformance> validPerformances = new ArrayList<>();
            List<Long> skipPerfIds = new ArrayList<>();
            for (BizPerformance pref : allPerformance) {
                if (pref == null) {
                    continue;
                }
                if (shouldSkipPerformance(pref)) {
                    skipPerfIds.add(pref.getPerfId());
                    System.out.println("鐠哄疇绻冪紒鈺傛櫏: perf_id=" + pref.getPerfId() + ", perf_code=" + pref.getPerfCode());
                } else {
                    validPerformances.add(pref);
                }
            }
            System.out.println("閺堝鏅ョ紒鈺傛櫏閹稿洦鐖ｉ弫浼村櫤: " + validPerformances.size());
            System.out.println("鐠哄疇绻冮惃鍕摋閺佸牊瀵氶弽鍥ㄦ殶闁? " + skipPerfIds.size());

            // 4. 閸旂姾娴囬幍鈧張澶婂彠閼辨柨鍙х化?
            List<RelTaskPerformance> allRelList = performanceMapper.getAllRelTaskPerformance();
            System.out.println("閸旂姾娴囬崗瀹犱粓閸忓磭閮撮弫浼村櫤: " + allRelList.size());

            // 5. 鏉╁洦鎶ら幒澶庮潶鐠哄疇绻冪紒鈺傛櫏閻ㄥ嫬鍙ч懕鏂垮彠缁?
            Set<Long> skipPerfIdSet = new HashSet<>(skipPerfIds);
            List<RelTaskPerformance> validRelList = new ArrayList<>();
            for (RelTaskPerformance rel : allRelList) {
                if (!skipPerfIdSet.contains(rel.getPerfId())) {
                    validRelList.add(rel);
                }
            }
            System.out.println("閺堝鏅ラ崗瀹犱粓閸忓磭閮撮弫浼村櫤: " + validRelList.size());
            System.out.println("鐠哄疇绻冮惃鍕彠閼辨柨鍙х化缁樻殶闁? " + (allRelList.size() - validRelList.size()));

            // 6. 閸旂姾娴囬幍鈧張澶婂嬀鎼达妇鍝楅弫?
            List<BizPerformanceYear> allPerfYearList = performanceMapper.getAllPerformanceYear();
            System.out.println("閸旂姾娴囬獮鏉戝缂佲晜鏅ラ弫浼村櫤: " + allPerfYearList.size());

            // 7. 鏉╁洦鎶ら幒澶庮潶鐠哄疇绻冪紒鈺傛櫏閻ㄥ嫬鍕炬惔锔惧摋閺?
            List<BizPerformanceYear> validPerfYearList = new ArrayList<>();
            for (BizPerformanceYear perfYear : allPerfYearList) {
                if (!skipPerfIdSet.contains(perfYear.getPerfId())) {
                    validPerfYearList.add(perfYear);
                }
            }
            System.out.println("閺堝鏅ラ獮鏉戝缂佲晜鏅ラ弫浼村櫤: " + validPerfYearList.size());
            System.out.println("鐠哄疇绻冮惃鍕嬀鎼达妇鍝楅弫鍫熸殶闁? " + (allPerfYearList.size() - validPerfYearList.size()));

            // 8. 閸掓繂顫愰崠鏍ㄦ箒閺佸牏鍝楅弫鍫㈡畱瑜版挸澧犻崐闂磋礋0
            for (BizPerformance pref : validPerformances) {
                if (pref != null) {
                    pref.setCurrentValue(BigDecimal.ZERO);
                }
            }

            // 9. 閺嬪嫬缂撴禒璇插Map (閹稿〖askId缁便垹绱?
            Map<Long, BizTask> taskMap = new HashMap<>();
            for (BizTask task : allTasks) {
                if (task != null && task.getLevel() != null && task.getLevel() != 1 && task.getLevel() != 2) {
                    taskMap.put(task.getTaskId(), task);
                }
            }
            System.out.println("閺堝鏅ユ禒璇插閺佷即鍣? " + taskMap.size());

            // 10. 閺嬪嫬缂撶紒鈺傛櫏Map (閹稿「erfId缁便垹绱?
            Map<Long, BizPerformance> perfMap = new HashMap<>();
            for (BizPerformance pref : validPerformances) {
                if (pref != null) {
                    perfMap.put(pref.getPerfId(), pref);
                }
            }

            // 11. 閺嬪嫬缂撻崗瀹犱粓閸忓磭閮碝ap (閹稿〖askId閸掑棛绮?
            Map<Long, List<RelTaskPerformance>> relByTaskMap = new HashMap<>();
            // 閺嬪嫬缂撻崗瀹犱粓閸忓磭閮碝ap (閹稿「erfId閸掑棛绮嶉敍宀€鏁ゆ禍搴″嬀鎼达妇鍝楅弫鍫ｎ吀缁?
            Map<Long, List<RelTaskPerformance>> relByPerfMap = new HashMap<>();
            for (RelTaskPerformance rel : validRelList) {
                relByTaskMap.computeIfAbsent(rel.getTaskId(), k -> new ArrayList<>()).add(rel);
                relByPerfMap.computeIfAbsent(rel.getPerfId(), k -> new ArrayList<>()).add(rel);
            }

            // 12. 閺嬪嫬缂撻獮鏉戝缂佲晜鏅ap (閹稿「erfId_year缁便垹绱?
            Map<String, BizPerformanceYear> perfYearMap = new HashMap<>();
            // 閺嬪嫬缂撻獮鏉戝缂佲晜鏅ap (閹稿「erfId閸掑棛绮?
            Map<Long, List<BizPerformanceYear>> perfYearByPerfIdMap = new HashMap<>();
            for (BizPerformanceYear perfYear : validPerfYearList) {
                String key = perfYear.getPerfId() + "_" + perfYear.getYear();
                perfYearMap.put(key, perfYear);
                perfYearByPerfIdMap.computeIfAbsent(perfYear.getPerfId(), k -> new ArrayList<>()).add(perfYear);
            }

            // ========== 缁楊兛绔村銉窗鐠侊紕鐣荤紒鈺傛櫏閻?current_value ==========
            System.out.println("---------- 瀵偓婵顓哥粻妤冨摋閺?current_value ----------");
            int perfUpdateCount = 0;
            int perfFailCount = 0;

            for (Map.Entry<Long, BizTask> taskEntry : taskMap.entrySet()) {
                Long taskId = taskEntry.getKey();
                BizTask task = taskEntry.getValue();

                // 閼惧嘲褰囨禒璇插閸忓疇浠堥惃鍕摋閺佸牆鍙х化?
                List<RelTaskPerformance> relList = relByTaskMap.get(taskId);
                if (relList == null || relList.isEmpty()) {
                    continue;
                }

                BigDecimal taskCurrentValue = task.getCurrentValue();
                if (taskCurrentValue == null) {
                    taskCurrentValue = BigDecimal.ZERO;
                }

                for (RelTaskPerformance rel : relList) {
                    try {
                        Long perfId = rel.getPerfId();
                        BizPerformance pref = perfMap.get(perfId);

                        if (pref == null) {
                            continue;
                        }

                        String dataType = pref.getDataType();
                        if (dataType == null || dataType.equals("0")) {
                            continue;
                        }

                        if (pref.getCurrentValue() == null) {
                            pref.setCurrentValue(BigDecimal.ZERO);
                        }

                        if (dataType.equals("1")) {
                            // 閺佹澘鈧偐鐤崝?
                            BigDecimal newValue = pref.getCurrentValue().add(taskCurrentValue);
                            pref.setCurrentValue(newValue);
                            pref.setUpdateTime(new Date());
                            perfUpdateCount++;
                        } else if (dataType.equals("2")) {
                            // 閻ф儳鍨庡В鏂垮絿婢?
                            if (taskCurrentValue.compareTo(pref.getCurrentValue()) > 0) {
                                pref.setCurrentValue(taskCurrentValue);
                                pref.setUpdateTime(new Date());
                                perfUpdateCount++;
                            }
                        }
                    } catch (Exception e) {
                        perfFailCount++;
                        System.err.println("婢跺嫮鎮婃禒璇插 " + taskId + " 閸滃瞼鍝楅弫?" + rel.getPerfId() + " 閺冭泛鍤柨? " + e.getMessage());
                    }
                }
            }

            // 閹靛綊鍣洪弴瀛樻煀缂佲晜鏅ラ惃?current_value
            System.out.println("闂団偓鐟曚焦娲块弬鎵畱缂佲晜鏅ラ弫浼村櫤: " + perfUpdateCount);
            int actualUpdateCount = 0;
            for (BizPerformance pref : perfMap.values()) {
                if (pref.getUpdateTime() != null) {
                    performanceMapper.updatePerformance(pref);
                    actualUpdateCount++;
                }
            }
            System.out.println("缂佲晜鏅?current_value 閺囧瓨鏌婄€瑰本鍨氶敍灞界杽闂勫懏娲块弬? " + actualUpdateCount + "閿涘本鍨氶崝? " + perfUpdateCount + "閿涘苯銇戠拹? " + perfFailCount);

            // ========== 缁楊兛绨╁銉窗缂佺喍绔撮柌宥嗘煀鐠侊紕鐣婚獮鏉戝缂佲晜鏅ラ惃?actual_value ==========
            System.out.println("---------- 瀵偓婵鍣搁弬鎷岊吀缁犳鍕炬惔锔惧摋閺?actual_value ----------");
            int yearUpdateCount = 0;
            int yearFailCount = 0;

            for (Map.Entry<Long, List<BizPerformanceYear>> entry : perfYearByPerfIdMap.entrySet()) {
                Long perfId = entry.getKey();
                List<BizPerformanceYear> perfYears = entry.getValue();

                // 閼惧嘲褰囩拠銉у摋閺佸牏娈戦幍鈧張澶婂彠閼辨柧鎹㈤崝?
                List<RelTaskPerformance> relList = relByPerfMap.get(perfId);

                if (relList == null || relList.isEmpty()) {
                    // 濞屸剝婀侀崗瀹犱粓娴犺濮熼敍灞惧閺堝鍕炬惔锔惧摋閺佸牐顔曟稉?
                    for (BizPerformanceYear perfYear : perfYears) {
                        try {
                            perfYear.setActualValue(BigDecimal.ZERO);
                            performanceMapper.updatePerformanceYear(perfYear);
                            yearUpdateCount++;
                        } catch (Exception e) {
                            yearFailCount++;
                            System.err.println("閺囧瓨鏌婇獮鏉戝缂佲晜鏅?" + perfYear.getYearId() + " 婢惰精瑙? " + e.getMessage());
                        }
                    }
                    continue;
                }

                // 閹稿鍕炬惔锕€鍨庣紒鍕彠閼辨柧鎹㈤崝?(yearId -> List<taskId>)
                Map<Long, List<Long>> tasksByYearMap = new HashMap<>();
                for (RelTaskPerformance rel : relList) {
                    tasksByYearMap.computeIfAbsent(rel.getYearId(), k -> new ArrayList<>()).add(rel.getTaskId());
                }

                // 鐠侊紕鐣诲В蹇庨嚋楠炴潙瀹抽惃鍕杽闂勫懎鈧?
                for (BizPerformanceYear perfYear : perfYears) {
                    try {
                        List<Long> taskIdsForYear = tasksByYearMap.get(perfYear.getYearId());
                        BigDecimal totalActualValue = BigDecimal.ZERO;

                        if (taskIdsForYear != null && !taskIdsForYear.isEmpty()) {
                            // 閹靛綊鍣洪懢宄板絿鏉╂瑤绨烘禒璇插
                            List<BizTask> tasks = taskMapper.getTasksByIds(taskIdsForYear);
                            for (BizTask task : tasks) {
                                if (task != null && task.getCurrentValue() != null) {
                                    totalActualValue = totalActualValue.add(task.getCurrentValue());
                                }
                            }
                        }

                        perfYear.setActualValue(totalActualValue);
                        performanceMapper.updatePerformanceYear(perfYear);
                        yearUpdateCount++;
                    } catch (Exception e) {
                        yearFailCount++;
                        System.err.println("閺囧瓨鏌婇獮鏉戝缂佲晜鏅?" + perfYear.getYearId() + " 婢惰精瑙? " + e.getMessage());
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            System.out.println("========== 缂佲晜鏅ョ拋锛勭暬鐎瑰本鍨?==========");
            System.out.println("跳过绩效指标 " + skipPerfIds.size() + " 条");
            System.out.println("绩效更新: 成功 " + perfUpdateCount + " 条, 失败 " + perfFailCount + " 条");
            System.out.println("年度绩效更新: 成功 " + yearUpdateCount + " 条, 失败 " + yearFailCount + " 条");
            System.out.println("閹槒鈧妞? " + (endTime - startTime) + " ms");

            return "缂佲晜鏅ラ弫鐗堝祦瀹稿弶娲块弬鐧╪" +
                    "鐠哄疇绻冮惃鍕摋閺佸牊瀵氶弽? " + skipPerfIds.size() + " 閺?(perf_code娴?.3閵?閵?瀵偓婢?\n" +
                    "缂佲晜鏅ラ弴瀛樻煀: 閹存劕濮?" + perfUpdateCount + " 閺夆槄绱濇径杈Е " + perfFailCount + " 閺夘摙n" +
                    "楠炴潙瀹崇紒鈺傛櫏閺囧瓨鏌? 閹存劕濮?" + yearUpdateCount + " 閺夆槄绱濇径杈Е " + yearFailCount + " 閺夘摙n" +
                    "閹槒鈧妞? " + (endTime - startTime) + " ms";

        } catch (Exception e) {
            System.err.println("缂佲晜鏅ョ拋锛勭暬婢惰精瑙? " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("缂佲晜鏅ョ拋锛勭暬婢惰精瑙? " + e.getMessage(), e);
        }
    }

    public Object updatePerformanceByTaskId(Long taskId) {
        if (taskId == null) {
            throw new RuntimeException("鐠囩柉绶崗銉︽箒閺佸牏娈戞禒璇插ID");
        }

        // 闁插秵鏌婄拋锛勭暬閹碘偓閺堝鍝楅弫鍫礉閸ョ姳璐熼崡鏇氶嚋娴犺濮熼弴瀛樻煀閸欘垵鍏樿ぐ鍗炴惙婢舵矮閲滅紒鈺傛櫏
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
            throw new RuntimeException("鐠囩柉绶崗銉︽箒閺佸牏娈戠紒鈺傛櫏ID");
        }
        BizPerformance performance = performanceMapper.getPerformanceById(perfId);
        if (performance == null) {
            throw new RuntimeException("没有找到该绩效");
        }
//        // 濡偓閺屻儲妲搁崥锕傛付鐟曚浇鐑︽潻?
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
            throw new RuntimeException("鐠囩柉绶崗銉︽箒閺佸牏娈戞禒璇插ID");
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
            throw new RuntimeException("鐠囩柉绶崗銉︽箒閺佸牏娈戠紒鈺傛櫏ID");
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
            throw new RuntimeException("閻犲洨鏌夌欢顓㈠礂閵夛附绠掗柡浣哥墢濞堟垹绱掗埡鍌涙珡ID");
        }
        BizPerformance performance = performanceMapper.getPerformanceById(perfId);
        if (performance == null) {
            throw new RuntimeException("婵炲备鍓濆﹢渚€骞嶉幆褍鐓傞悹鍥ュ劤閸濇寮?");
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
     * 娴兼ê瀵查崥搴ｆ畱閺嶈宓侀獮缈犲敜閼惧嘲褰囩紒鈺傛櫏閺傝纭?
     * 娴ｈ法鏁ら幍褰掑櫤閺屻儴顕楁禒锝嗘禌瀵邦亞骞嗛弻銉嚄閿涘矁袙閸愮爟+1闂傤噣顣?
     */
    public Object getTaskByPerfIdForYear(Long perfId, Integer year, Long userId) {
        BizPerformance performance = performanceMapper.getPerformanceById(perfId);
        if (!canViewPerformance(performance, userId)) {
            throw new RuntimeException("闁哄啰濮靛鍫ュ蓟閵壯勭畽閻犲洢鍎抽崫妤呭极閸繂褰犻柤杈ㄦ煣閹广垽宕?");
        }
        return getTaskByPerfId(perfId, year);
    }

    public Object getPerformanceByYear(Integer year) {
        if (year == null) {
            throw new RuntimeException("璇疯緭鍏ユ湁鏁堢殑骞翠唤");
        }

        long startTime = System.currentTimeMillis();

        // 1. 閺屻儴顕楅幐鍥х暰楠炵繝鍞ら惃鍕閺堝鍕炬惔锔惧摋閺佸牐顔囪ぐ?
        List<BizPerformanceYear> prefYears = performanceMapper.getPerformanceYearByYear(year);
        if (prefYears == null || prefYears.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 閺€鍫曟肠閹碘偓閺堝娓剁憰浣圭叀鐠囥垻娈戠紒鈺傛櫏ID
        List<Long> perfIds = new ArrayList<>();
        for (BizPerformanceYear prefYear : prefYears) {
            if (prefYear != null && prefYear.getPerfId() != null) {
                perfIds.add(prefYear.getPerfId());
            }
        }

        // 3. 閹靛綊鍣洪弻銉嚄閹碘偓閺堝娴夐崗宕囨畱缂佲晜鏅ラ幐鍥ㄧ垼閿涘牅绔村▎鈩冣偓褎鐓＄拠顫礉闁灝鍘+1閿?
        List<BizPerformance> allPerformances = performanceMapper.getAllPerformance();

        // 4. 閺嬪嫬缂撶紒鈺傛櫏Map閿涘奔绌舵禍搴℃彥闁喐鐓￠幍?
        Map<Long, BizPerformance> perfMap = new HashMap<>();
        for (BizPerformance perf : allPerformances) {
            if (perf != null) {
                perfMap.put(perf.getPerfId(), perf);
            }
        }

        // 5. 缂佸嫯顥婃潻鏂挎礀缂佹挻鐏?
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

        long endTime = System.currentTimeMillis();
        System.out.println("getPerformanceByYear 閺屻儴顕楅獮缈犲敜 " + year + "閿涘矁绻戦崶?" + prefYearVOS.size() + " 閺壜ゎ唶瑜版洩绱濋懓妤佹: " + (endTime - startTime) + " ms");

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
            return "閹绘劒姘﹂幋鎰";
        }else {
            throw new RuntimeException("鐠囥儳鍝楅弫鍫熷瘹閺嶅洤鍙ч懕鏂炬崲閸斺槄绱濇稉宥呭讲閹靛濮╅幓鎰唉");
        }
    }
    @Transactional
    public Object submitPref(Long perfId, BigDecimal actualValue, Integer year, Long userId, String comment) {
        if (perfId == null) {
            throw new RuntimeException("鐠囩柉绶崗銉︽箒閺佸牏娈戠紒鈺傛櫏ID");
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
            throw new RuntimeException("閻ф儳鍨庡В鏃傚摋閺佸牆锝為幎銉モ偓闂寸瑝閼冲€熺Т鏉?00");
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
