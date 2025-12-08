package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizMaterialSubmission {
    private Long subId;
    private Long taskId;
    private Long fileId;
    private BigDecimal reportedValue;
    private String dataType;
    private Long submitBy;
    private Long submitDeptId;
    private Long manageDeptId;
    private Date submitTime;
    private String fileSuffix;
    private Integer flowStatus;
    private Long currentHandlerId;
    private Integer isDelete;
}