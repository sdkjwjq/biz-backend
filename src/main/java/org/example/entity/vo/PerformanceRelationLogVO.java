package org.example.entity.vo;

import lombok.Data;

import java.util.Date;

/** 绩效关联变更日志视图：操作人姓名在读取时联表取得。 */
@Data
public class PerformanceRelationLogVO {
    private Long logId;
    private Long perfId;
    private String perfCode;
    private String perfName;
    private Integer year;
    private String action;
    private Long taskId;
    private String taskCode;
    private String taskName;
    private String batchId;
    private Long operatorId;
    private String operatorName;
    private String reason;
    private Date createTime;
}
