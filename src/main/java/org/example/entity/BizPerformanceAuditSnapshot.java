package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizPerformanceAuditSnapshot {
    private Long snapshotId;
    private Long subId;
    private Long perfId;
    private Long yearId;
    private BigDecimal previousPerformanceValue;
    private Date previousPerformanceUpdateTime;
    private BigDecimal previousYearActualValue;
    private BigDecimal previousYearTargetValue;
    private Date createTime;
}
