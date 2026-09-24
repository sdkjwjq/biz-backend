package org.example.entity;

import lombok.Data;

import java.util.Date;

/** 绩效关联任务变更日志：一次增删关联写入一条。 */
@Data
public class BizPerformanceRelationLog {
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
    private String reason;
    private Date createTime;
}
