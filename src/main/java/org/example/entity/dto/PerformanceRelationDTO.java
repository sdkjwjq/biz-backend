package org.example.entity.dto;

import lombok.Data;

import java.util.List;

/** 管理员编辑绩效关联任务请求：按指标 + 年度新增与解除关联。 */
@Data
public class PerformanceRelationDTO {
    private Long perfId;
    private Integer year;
    private List<Long> addTaskIds;
    private List<Long> removeTaskIds;
    private String reason;
}
