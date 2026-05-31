package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerformanceAuditVO {
    private Long subId;
    private Long perfId;
    private Long yearId;
    private Integer year;
    private String perfCode;
    private String perfName;
    private BigDecimal actualValue;
    private Long submitBy;
    private Date submitTime;
    private Integer flowStatus;
    private Long currentHandlerId;
    private String comment;
    private Integer isDelete;
    private Long leaderId;
    private Long auditorId;
    private Long principalId;
}
