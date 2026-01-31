package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 一级任务详情VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FirstLevelTaskDetailVO {
    private Long taskId;
    private String taskName;
    private Long deptId;
    private String deptName;
    private String status;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private Integer progress;
    private String createTime;
    private String updateTime;

    // 判断是否完成
    public boolean isCompleted() {
        return "3".equals(status);
    }

    // 获取完成率
    public BigDecimal getCompletionRate() {
        if (targetValue != null && targetValue.compareTo(BigDecimal.ZERO) > 0) {
            return currentValue.multiply(BigDecimal.valueOf(100))
                    .divide(targetValue, 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
