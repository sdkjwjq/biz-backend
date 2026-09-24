package org.example.entity;

import lombok.Data;

import java.util.Date;

/** 归属变更日志：一次移交写入一条，记录三个字段的变更前后值与原因。 */
@Data
public class BizOwnershipChangeLog {
    private Long logId;
    private String targetType;
    private Long targetId;
    private String targetCode;
    private String targetName;
    private String batchId;
    private Long operatorId;
    private String reason;
    private Long deptBeforeId;
    private Long deptAfterId;
    private Long leaderBeforeId;
    private Long leaderAfterId;
    private Long principalBeforeId;
    private Long principalAfterId;
    private Date createTime;
}
