package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 看板数据汇总VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryVO {
    // 整体数据
    private TaskCompletionVO overallCompletion;      // 所有任务完成率
    private TaskCompletionVO yearCompletion;         // 本年度任务完成率
    private TaskCompletionVO midTermCompletion;      // 中期任务完成率

    // 一级任务
    private TaskCompletionVO firstLevelCompletion;   // 一级任务完成率

    // 部门数据
    private List<DeptTaskStatsVO> deptOverallStats;  // 各部门整体完成率
    private List<DeptTaskStatsVO> deptYearStats;     // 各部门本年度完成率
    private List<DeptTaskStatsVO> deptMidTermStats;  // 各部门中期完成率

    // 一级任务详情
    private List<FirstLevelTaskDetailVO> firstLevelTasks;

    // 统计时间
    private String currentYear;
    private String midTermEndYear = "2028";
    private String updateTime;
}
