package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizPerformanceSubmission {
    private Long subId;
    private Long perfId;
    private Long yearId;
    private Integer year;
    private BigDecimal actualValue;
    private Long submitBy;
    private Date submitTime;
    private Integer flowStatus;
    private Long currentHandlerId;
    private String comment;
    private Integer isDelete;
}
