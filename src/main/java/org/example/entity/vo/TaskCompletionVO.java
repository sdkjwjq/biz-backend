package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 任务完成率统计VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskCompletionVO {
    private Integer totalTasks;         // 总任务数
    private Integer completedTasks;     // 已完成任务数
    private BigDecimal completionRate;  // 完成率（百分比）
    private String period;             // 统计周期：all, year, midterm
    private String description;        // 描述

    // 新增：任务状态统计
    private Integer notStartedCount;   // 未开始任务数
    private Integer inProgressCount;   // 进行中任务数
    private Integer inReviewCount;     // 审核中任务数
    private Integer finishedCount;     // 已完成任务数

    private BigDecimal notStartedRate;  // 未开始占比
    private BigDecimal inProgressRate;  // 进行中占比
    private BigDecimal inReviewRate;    // 审核中占比
    private BigDecimal finishedRate;    // 已完成占比

    // 方便构造的静态方法
    public TaskCompletionVO(Integer totalTasks, Integer completedTasks, String period, String description,
                            Integer notStartedCount, Integer inProgressCount, Integer inReviewCount, Integer finishedCount) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.period = period;
        this.description = description;

        // 设置状态统计
        this.notStartedCount = notStartedCount;
        this.inProgressCount = inProgressCount;
        this.inReviewCount = inReviewCount;
        this.finishedCount = finishedCount;

        // 计算完成率
        if (totalTasks != null && totalTasks > 0) {
            this.completionRate = BigDecimal.valueOf(completedTasks * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP);

            // 计算各个状态占比
            this.notStartedRate = BigDecimal.valueOf(notStartedCount * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP);
            this.inProgressRate = BigDecimal.valueOf(inProgressCount * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP);
            this.inReviewRate = BigDecimal.valueOf(inReviewCount * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP);
            this.finishedRate = BigDecimal.valueOf(finishedCount * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.completionRate = BigDecimal.ZERO;
            this.notStartedRate = BigDecimal.ZERO;
            this.inProgressRate = BigDecimal.ZERO;
            this.inReviewRate = BigDecimal.ZERO;
            this.finishedRate = BigDecimal.ZERO;
        }
    }

    // 原有构造方法的重载，保持向后兼容
    public TaskCompletionVO(Integer totalTasks, Integer completedTasks, String period, String description) {
        this(totalTasks, completedTasks, period, description, 0, 0, 0, completedTasks);
    }
}