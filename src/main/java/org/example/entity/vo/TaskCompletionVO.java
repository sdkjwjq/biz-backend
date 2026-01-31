package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

    // 方便构造的静态方法
    public TaskCompletionVO(Integer totalTasks, Integer completedTasks, String period, String description) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.period = period;
        this.description = description;

        // 计算完成率
        if (totalTasks != null && totalTasks > 0) {
            this.completionRate = BigDecimal.valueOf(completedTasks * 100.0 / totalTasks)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.completionRate = BigDecimal.ZERO;
        }
    }
}



