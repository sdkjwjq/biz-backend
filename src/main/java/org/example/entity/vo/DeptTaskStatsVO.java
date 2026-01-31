package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

    // 计算完成率
    public void calculateCompletionRate() {
        if (totalTasks != null && totalTasks > 0) {
            this.completionRate = BigDecimal.valueOf(completedTasks * 100.0 / totalTasks)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.completionRate = BigDecimal.ZERO;
        }
    }
}