package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelTaskPerformance {
    private Long id; // 关联ID
    private Long taskId; // 任务ID
    private Long perfId; // 指标ID
    private Long yearId; // 绩效年度ID
    private BigDecimal weight; // 权重
    private BigDecimal contributionValue; // 该任务为KPI贡献的数值
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大
}