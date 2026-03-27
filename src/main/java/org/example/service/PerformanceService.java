package org.example.service;

import org.example.entity.BizPerformance;
import org.example.entity.BizPerformanceYear;
import org.example.entity.BizTask;
import org.example.entity.RelTaskPerformance;
import org.example.entity.vo.PerformanceYearVO;
import org.example.mapper.BizMapper;
import org.example.mapper.PerformanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PerformanceService {

    @Autowired
    private PerformanceMapper performanceMapper;

    @Autowired
    private BizMapper taskMapper;

    /**
     * 判断绩效指标是否需要跳过计算
     * 跳过条件：perf_code 以 "1.3"、 "2"、 "3" 开头
     */
    private boolean shouldSkipPerformance(BizPerformance pref) {
        if (pref == null || pref.getPerfCode() == null) {
            return true;
        }
        String perfCode = pref.getPerfCode();
        return perfCode.startsWith("1.3") || perfCode.startsWith("2") || perfCode.startsWith("3");
    }

    @Transactional
    public Object calcuateAllPerformance() {
        try {
            System.out.println("========== 开始计算所有绩效数据 ==========");
            long startTime = System.currentTimeMillis();

            // 1. 加载所有任务
            List<BizTask> allTasks = taskMapper.getAllTasks();
            System.out.println("加载任务数量: " + allTasks.size());

            // 2. 加载所有绩效指标
            List<BizPerformance> allPerformance = performanceMapper.getAllPerformance();
            System.out.println("加载绩效指标数量: " + allPerformance.size());

            // 3. 过滤掉需要跳过的绩效指标
            List<BizPerformance> validPerformances = new ArrayList<>();
            List<Long> skipPerfIds = new ArrayList<>();
            for (BizPerformance pref : allPerformance) {
                if (pref == null) {
                    continue;
                }
                if (shouldSkipPerformance(pref)) {
                    skipPerfIds.add(pref.getPerfId());
                    System.out.println("跳过绩效: perf_id=" + pref.getPerfId() + ", perf_code=" + pref.getPerfCode());
                } else {
                    validPerformances.add(pref);
                }
            }
            System.out.println("有效绩效指标数量: " + validPerformances.size());
            System.out.println("跳过的绩效指标数量: " + skipPerfIds.size());

            // 4. 加载所有关联关系
            List<RelTaskPerformance> allRelList = performanceMapper.getAllRelTaskPerformance();
            System.out.println("加载关联关系数量: " + allRelList.size());

            // 5. 过滤掉被跳过绩效的关联关系
            Set<Long> skipPerfIdSet = new HashSet<>(skipPerfIds);
            List<RelTaskPerformance> validRelList = new ArrayList<>();
            for (RelTaskPerformance rel : allRelList) {
                if (!skipPerfIdSet.contains(rel.getPerfId())) {
                    validRelList.add(rel);
                }
            }
            System.out.println("有效关联关系数量: " + validRelList.size());
            System.out.println("跳过的关联关系数量: " + (allRelList.size() - validRelList.size()));

            // 6. 加载所有年度绩效
            List<BizPerformanceYear> allPerfYearList = performanceMapper.getAllPerformanceYear();
            System.out.println("加载年度绩效数量: " + allPerfYearList.size());

            // 7. 过滤掉被跳过绩效的年度绩效
            List<BizPerformanceYear> validPerfYearList = new ArrayList<>();
            for (BizPerformanceYear perfYear : allPerfYearList) {
                if (!skipPerfIdSet.contains(perfYear.getPerfId())) {
                    validPerfYearList.add(perfYear);
                }
            }
            System.out.println("有效年度绩效数量: " + validPerfYearList.size());
            System.out.println("跳过的年度绩效数量: " + (allPerfYearList.size() - validPerfYearList.size()));

            // 8. 初始化有效绩效的当前值为0
            for (BizPerformance pref : validPerformances) {
                if (pref != null) {
                    pref.setCurrentValue(BigDecimal.ZERO);
                }
            }

            // 9. 构建任务Map (按taskId索引)
            Map<Long, BizTask> taskMap = new HashMap<>();
            for (BizTask task : allTasks) {
                if (task != null && task.getLevel() != null && task.getLevel() != 1 && task.getLevel() != 2) {
                    taskMap.put(task.getTaskId(), task);
                }
            }
            System.out.println("有效任务数量: " + taskMap.size());

            // 10. 构建绩效Map (按perfId索引)
            Map<Long, BizPerformance> perfMap = new HashMap<>();
            for (BizPerformance pref : validPerformances) {
                if (pref != null) {
                    perfMap.put(pref.getPerfId(), pref);
                }
            }

            // 11. 构建关联关系Map (按taskId分组)
            Map<Long, List<RelTaskPerformance>> relByTaskMap = new HashMap<>();
            // 构建关联关系Map (按perfId分组，用于年度绩效计算)
            Map<Long, List<RelTaskPerformance>> relByPerfMap = new HashMap<>();
            for (RelTaskPerformance rel : validRelList) {
                relByTaskMap.computeIfAbsent(rel.getTaskId(), k -> new ArrayList<>()).add(rel);
                relByPerfMap.computeIfAbsent(rel.getPerfId(), k -> new ArrayList<>()).add(rel);
            }

            // 12. 构建年度绩效Map (按perfId_year索引)
            Map<String, BizPerformanceYear> perfYearMap = new HashMap<>();
            // 构建年度绩效Map (按perfId分组)
            Map<Long, List<BizPerformanceYear>> perfYearByPerfIdMap = new HashMap<>();
            for (BizPerformanceYear perfYear : validPerfYearList) {
                String key = perfYear.getPerfId() + "_" + perfYear.getYear();
                perfYearMap.put(key, perfYear);
                perfYearByPerfIdMap.computeIfAbsent(perfYear.getPerfId(), k -> new ArrayList<>()).add(perfYear);
            }

            // ========== 第一步：计算绩效的 current_value ==========
            System.out.println("---------- 开始计算绩效 current_value ----------");
            int perfUpdateCount = 0;
            int perfFailCount = 0;

            for (Map.Entry<Long, BizTask> taskEntry : taskMap.entrySet()) {
                Long taskId = taskEntry.getKey();
                BizTask task = taskEntry.getValue();

                // 获取任务关联的绩效关系
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
                            // 数值累加
                            BigDecimal newValue = pref.getCurrentValue().add(taskCurrentValue);
                            pref.setCurrentValue(newValue);
                            pref.setUpdateTime(new Date());
                            perfUpdateCount++;
                        } else if (dataType.equals("2")) {
                            // 百分比取大
                            if (taskCurrentValue.compareTo(pref.getCurrentValue()) > 0) {
                                pref.setCurrentValue(taskCurrentValue);
                                pref.setUpdateTime(new Date());
                                perfUpdateCount++;
                            }
                        }
                    } catch (Exception e) {
                        perfFailCount++;
                        System.err.println("处理任务 " + taskId + " 和绩效 " + rel.getPerfId() + " 时出错: " + e.getMessage());
                    }
                }
            }

            // 批量更新绩效的 current_value
            System.out.println("需要更新的绩效数量: " + perfUpdateCount);
            int actualUpdateCount = 0;
            for (BizPerformance pref : perfMap.values()) {
                if (pref.getUpdateTime() != null) {
                    performanceMapper.updatePerformance(pref);
                    actualUpdateCount++;
                }
            }
            System.out.println("绩效 current_value 更新完成，实际更新: " + actualUpdateCount + "，成功: " + perfUpdateCount + "，失败: " + perfFailCount);

            // ========== 第二步：统一重新计算年度绩效的 actual_value ==========
            System.out.println("---------- 开始重新计算年度绩效 actual_value ----------");
            int yearUpdateCount = 0;
            int yearFailCount = 0;

            for (Map.Entry<Long, List<BizPerformanceYear>> entry : perfYearByPerfIdMap.entrySet()) {
                Long perfId = entry.getKey();
                List<BizPerformanceYear> perfYears = entry.getValue();

                // 获取该绩效的所有关联任务
                List<RelTaskPerformance> relList = relByPerfMap.get(perfId);

                if (relList == null || relList.isEmpty()) {
                    // 没有关联任务，所有年度绩效设为0
                    for (BizPerformanceYear perfYear : perfYears) {
                        try {
                            perfYear.setActualValue(BigDecimal.ZERO);
                            performanceMapper.updatePerformanceYear(perfYear);
                            yearUpdateCount++;
                        } catch (Exception e) {
                            yearFailCount++;
                            System.err.println("更新年度绩效 " + perfYear.getYearId() + " 失败: " + e.getMessage());
                        }
                    }
                    continue;
                }

                // 按年度分组关联任务 (yearId -> List<taskId>)
                Map<Long, List<Long>> tasksByYearMap = new HashMap<>();
                for (RelTaskPerformance rel : relList) {
                    tasksByYearMap.computeIfAbsent(rel.getYearId(), k -> new ArrayList<>()).add(rel.getTaskId());
                }

                // 计算每个年度的实际值
                for (BizPerformanceYear perfYear : perfYears) {
                    try {
                        List<Long> taskIdsForYear = tasksByYearMap.get(perfYear.getYearId());
                        BigDecimal totalActualValue = BigDecimal.ZERO;

                        if (taskIdsForYear != null && !taskIdsForYear.isEmpty()) {
                            // 批量获取这些任务
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
                        System.err.println("更新年度绩效 " + perfYear.getYearId() + " 失败: " + e.getMessage());
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            System.out.println("========== 绩效计算完成 ==========");
            System.out.println("跳过的绩效指标: " + skipPerfIds.size() + " 条");
            System.out.println("绩效更新: 成功 " + perfUpdateCount + " 条，失败 " + perfFailCount + " 条");
            System.out.println("年度绩效更新: 成功 " + yearUpdateCount + " 条，失败 " + yearFailCount + " 条");
            System.out.println("总耗时: " + (endTime - startTime) + " ms");

            return "绩效数据已更新\n" +
                    "跳过的绩效指标: " + skipPerfIds.size() + " 条 (perf_code以1.3、2、3开头)\n" +
                    "绩效更新: 成功 " + perfUpdateCount + " 条，失败 " + perfFailCount + " 条\n" +
                    "年度绩效更新: 成功 " + yearUpdateCount + " 条，失败 " + yearFailCount + " 条\n" +
                    "总耗时: " + (endTime - startTime) + " ms";

        } catch (Exception e) {
            System.err.println("绩效计算失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("绩效计算失败: " + e.getMessage(), e);
        }
    }

    public Object updatePerformanceByTaskId(Long taskId) {
        if (taskId == null) {
            throw new RuntimeException("请输入有效的任务ID");
        }

        // 重新计算所有绩效，因为单个任务更新可能影响多个绩效
        return calcuateAllPerformance();
    }

    public Object getAllPerformance() {
        return performanceMapper.getAllPerformance();
    }

    public Object getPerformanceById(Long perfId) {
        if (perfId == null) {
            throw new RuntimeException("请输入有效的绩效ID");
        }
        BizPerformance performance = performanceMapper.getPerformanceById(perfId);
        if (performance == null) {
            throw new RuntimeException("没有找到该绩效");
        }
        // 检查是否需要跳过
        if (shouldSkipPerformance(performance)) {
            throw new RuntimeException("该绩效指标被标记为跳过计算（perf_code以1.3、2、3开头）");
        }
        return performance;
    }

    public Object getPerfByTaskId(Long taskId) {
        if (taskId == null) {
            throw new RuntimeException("请输入有效的任务ID");
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
            if (performance != null && !shouldSkipPerformance(performance)) {
                prefList.add(performance);
            }
        }
        return prefList;
    }

    public Object getTaskByPerfId(Long perfId) {
        if (perfId == null) {
            throw new RuntimeException("请输入有效的绩效ID");
        }
        BizPerformance performance = performanceMapper.getPerformanceById(perfId);
        if (performance == null) {
            throw new RuntimeException("没有找到该绩效");
        }
        if (shouldSkipPerformance(performance)) {
            throw new RuntimeException("该绩效指标被标记为跳过计算，无法获取关联任务");
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

    /**
     * 优化后的根据年份获取绩效方法
     * 使用批量查询代替循环查询，解决N+1问题
     */
    public Object getPerformanceByYear(Integer year) {
        if (year == null) {
            throw new RuntimeException("请输入有效的年");
        }

        long startTime = System.currentTimeMillis();

        // 1. 查询指定年份的所有年度绩效记录
        List<BizPerformanceYear> prefYears = performanceMapper.getPerformanceYearByYear(year);
        if (prefYears == null || prefYears.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 收集所有需要查询的绩效ID
        List<Long> perfIds = new ArrayList<>();
        for (BizPerformanceYear prefYear : prefYears) {
            if (prefYear != null && prefYear.getPerfId() != null) {
                perfIds.add(prefYear.getPerfId());
            }
        }

        // 3. 批量查询所有相关的绩效指标（一次性查询，避免N+1）
        List<BizPerformance> allPerformances = performanceMapper.getAllPerformance();

        // 4. 构建绩效Map，便于快速查找
        Map<Long, BizPerformance> perfMap = new HashMap<>();
        for (BizPerformance perf : allPerformances) {
            if (perf != null) {
                perfMap.put(perf.getPerfId(), perf);
            }
        }

        // 5. 组装返回结果
        List<PerformanceYearVO> prefYearVOS = new ArrayList<>();
        for (BizPerformanceYear prefYear : prefYears) {
            if (prefYear == null) {
                continue;
            }
            BizPerformance performance = perfMap.get(prefYear.getPerfId());
            // 过滤掉需要跳过的绩效
            if (performance != null && !shouldSkipPerformance(performance)) {
                prefYearVOS.add(new PerformanceYearVO(performance, prefYear));
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("getPerformanceByYear 查询年份 " + year + "，返回 " + prefYearVOS.size() + " 条记录，耗时: " + (endTime - startTime) + " ms");

        return prefYearVOS;
    }
}