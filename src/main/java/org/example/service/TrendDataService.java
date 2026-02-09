package org.example.service;

import org.example.entity.BizTask;
import org.example.entity.BizTrendData;
import org.example.mapper.BizMapper;
import org.example.mapper.TrendDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class TrendDataService {

    @Autowired
    private TrendDataMapper trendDataMapper;

    @Autowired
    private BizMapper bizMapper;

    /**
     * 每日记录趋势数据（定时任务）
     */
    @Transactional
    public void recordDailyTrendData() {
        try {
            // 检查今天是否已记录
            Integer count = trendDataMapper.checkTodayRecorded();
            if (count > 0) {
                System.out.println("今天已记录趋势数据，跳过");
                return;
            }

            LocalDate today = LocalDate.now();
            int year = today.getYear();
            int month = today.getMonthValue();
            int day = today.getDayOfMonth();

            // 获取今年的所有三级任务
            List<BizTask> tasks = bizMapper.getTasksByPhase(year);

            // 筛选三级任务
            List<BizTask> thirdLevelTasks = tasks.stream()
                    .filter(task -> task.getLevel() == 3 && task.getIsDelete() == 0)
                    .toList();

            if (thirdLevelTasks.isEmpty()) {
                return; // 没有三级任务，不记录
            }

            // 计算统计数据
            int totalTasks = thirdLevelTasks.size(); // 总任务数
            int completionCount = (int) thirdLevelTasks.stream()
                    .filter(task -> "3".equals(task.getStatus()))
                    .count(); // 完成数量

            double completionRate = calculateCompletionRate(thirdLevelTasks); // 完成率

            // 保存到数据库
            BizTrendData trendData = new BizTrendData();
            trendData.setYear(year);
            trendData.setMonth(month);
            trendData.setDay(day);
            trendData.setTotalTasks(totalTasks);        // 设置总任务数
            trendData.setCompletionCount(completionCount);
            trendData.setCompletionRate(completionRate);

            trendDataMapper.insertTrendData(trendData);

            System.out.println("趋势数据记录成功：" + year + "-" + month + "-" + day +
                    "，总任务数：" + totalTasks +
                    "，完成数量：" + completionCount +
                    "，完成率：" + completionRate + "%");

        } catch (Exception e) {
            System.err.println("记录趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 计算完成率（所有三级任务的平均完成率）
     */
    private double calculateCompletionRate(List<BizTask> tasks) {
        if (tasks.isEmpty()) {
            return 0.0;
        }

        BigDecimal totalRate = BigDecimal.ZERO;
        int validTasks = 0;

        for (BizTask task : tasks) {
            BigDecimal target = task.getTargetValue();
            BigDecimal current = task.getCurrentValue();

            if (target != null && target.compareTo(BigDecimal.ZERO) > 0) {
                // 计算单个任务的完成率
                BigDecimal taskRate = current
                        .multiply(BigDecimal.valueOf(100))
                        .divide(target, 2, RoundingMode.HALF_UP);

                // 限制在0-100之间
                if (taskRate.compareTo(BigDecimal.ZERO) < 0) {
                    taskRate = BigDecimal.ZERO;
                } else if (taskRate.compareTo(BigDecimal.valueOf(100)) > 0) {
                    taskRate = BigDecimal.valueOf(100);
                }

                totalRate = totalRate.add(taskRate);
                validTasks++;
            }
        }

        if (validTasks > 0) {
            return totalRate.divide(BigDecimal.valueOf(validTasks), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        return 0.0;
    }
    /**
     * 根据年份获取趋势数据（直接返回数据库记录）
     */
    public List<BizTrendData> getTrendDataByYear(Integer year) {
        try {
            if (year == null) {
                // 默认当前年份
                year = LocalDate.now().getYear();
            }
            return trendDataMapper.getTrendDataByYear(year);
        } catch (Exception e) {
            throw new RuntimeException("获取趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发记录（测试用）
     */
    public String triggerRecord() {
        try {
            recordDailyTrendData();
            return "手动记录成功";
        } catch (Exception e) {
            return "手动记录失败: " + e.getMessage();
        }
    }
}