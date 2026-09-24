package org.example.entity.vo;

import lombok.Data;

import java.util.Date;

/** 归属变更日志视图：人名与部门名在读取时联表取得，便于直接展示。 */
@Data
public class OwnershipChangeLogVO {
    private Long logId;
    private String targetType;
    private Long targetId;
    private String targetCode;
    private String targetName;
    private String batchId;
    private Long operatorId;
    private String operatorName;
    private String reason;
    private Long deptBeforeId;
    private String deptBeforeName;
    private Long deptAfterId;
    private String deptAfterName;
    private Long leaderBeforeId;
    private String leaderBeforeName;
    private Long leaderAfterId;
    private String leaderAfterName;
    private Long principalBeforeId;
    private String principalBeforeName;
    private Long principalAfterId;
    private String principalAfterName;
    private Date createTime;
}
