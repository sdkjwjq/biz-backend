package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAuditSnapshot {
    private Long snapshotId;
    private Long subId;
    private String targetType;
    private Long targetId;
    private BigDecimal previousValue;
    private String previousStatus;
    private String previousComment;
    // Immutable content submitted with this approval, never restored into the live task.
    private BigDecimal submittedValue;
    private String submittedComment;
    private String submittedName;
    private BigDecimal submittedTargetValue;
    private String submittedDataType;
    private Date createTime;
}
