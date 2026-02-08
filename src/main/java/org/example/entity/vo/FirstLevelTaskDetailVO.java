package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    // 获取状态中文描述
    public String getStatusText() {
        switch (status) {
            case "0": return "未开始";
            case "1": return "进行中";
            case "2": return "审核中";
            case "3": return "已完成";
            default: return "未知";
        }
    }

    // 获取完成率
    public BigDecimal getCompletionRate() {
        if (targetValue != null && targetValue.compareTo(BigDecimal.ZERO) > 0) {
            return currentValue.multiply(BigDecimal.valueOf(100))
                    .divide(targetValue, 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}