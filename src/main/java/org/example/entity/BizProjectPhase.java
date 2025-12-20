package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizProjectPhase {
    private Long phaseId; // 阶段ID
    private Long projectId; // 项目ID
    private String phaseName; // 阶段名称
    private Date startDate; // 开始日期
    private Date endDate; // 结束日期
    private String isCurrent; // 是否当前阶段 0:否 1:是
    private Integer isDelete; // 0:存在 1:删除
}