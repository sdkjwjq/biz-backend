package org.example.entity.vo;

import lombok.Data;

/** 绩效可关联任务候选：已关联的任务即使不再满足汇总条件也返回，便于解除。 */
@Data
public class PerformanceRelationCandidateVO {
    private Long taskId;
    private String taskCode;
    private String taskName;
    private Integer phase;
    private Integer level;
    private String dataType;
    private Integer taskDeleted;
    private String deptName;
    private String leaderName;
    private Integer linked;
    private String skipReason;
}
