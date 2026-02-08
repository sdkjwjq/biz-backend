package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 部门任务统计VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeptTaskStatsVO {
    private Long deptId;
    private String deptName;
    private Integer totalTasks;
    private Integer completedTasks;
    private BigDecimal completionRate;

    // 新增：任务状态统计
    private Integer notStartedCount;   // 未开始任务数
    private Integer inProgressCount;   // 进行中任务数
    private Integer inReviewCount;     // 审核中任务数
    private Integer finishedCount;     // 已完成任务数

    private BigDecimal notStartedRate;  // 未开始占比
    private BigDecimal inProgressRate;  // 进行中占比
    private BigDecimal inReviewRate;    // 审核中占比
    private BigDecimal finishedRate;    // 已完成占比

    // 计算完成率和状态占比
    public void calculateCompletionRate() {
        if (totalTasks != null && totalTasks > 0) {
            this.completionRate = BigDecimal.valueOf(completedTasks * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP);

            // 计算各个状态占比
            if (notStartedCount != null) {
                this.notStartedRate = BigDecimal.valueOf(notStartedCount * 100.0 / totalTasks)
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                this.notStartedRate = BigDecimal.ZERO;
            }

            if (inProgressCount != null) {
                this.inProgressRate = BigDecimal.valueOf(inProgressCount * 100.0 / totalTasks)
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                this.inProgressRate = BigDecimal.ZERO;
            }

            if (inReviewCount != null) {
                this.inReviewRate = BigDecimal.valueOf(inReviewCount * 100.0 / totalTasks)
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                this.inReviewRate = BigDecimal.ZERO;
            }

            if (finishedCount != null) {
                this.finishedRate = BigDecimal.valueOf(finishedCount * 100.0 / totalTasks)
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                this.finishedRate = BigDecimal.ZERO;
            }
        } else {
            this.completionRate = BigDecimal.ZERO;
            this.notStartedRate = BigDecimal.ZERO;
            this.inProgressRate = BigDecimal.ZERO;
            this.inReviewRate = BigDecimal.ZERO;
            this.finishedRate = BigDecimal.ZERO;
        }
    }
}