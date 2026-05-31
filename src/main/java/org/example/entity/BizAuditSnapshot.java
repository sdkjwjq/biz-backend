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
    private Date createTime;
}
