package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizPerformanceYear {
    private Long yearId; // 年度ID
    private Long perfId; // 指标ID
    private Integer year; // 年份
    private BigDecimal targetValue; // 年度目标值
    private BigDecimal actualValue; // 年度实际完成
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大
    private Integer isDelete; // 0:存在 1:删除
}