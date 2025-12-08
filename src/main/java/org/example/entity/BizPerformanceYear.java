package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizPerformanceYear {
    private Long yearId;
    private Long perfId;
    private Integer year;
    private BigDecimal targetValue;
    private BigDecimal actualValue;
    private String dataType;
    private Integer isDelete;
}