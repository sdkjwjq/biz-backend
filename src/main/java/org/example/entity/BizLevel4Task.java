package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizLevel4Task {
    private Long taskId;
    private Long parentId;
    private Integer phase;
    private String taskName;
    private Long leaderId;
    private Long deptId;
    private String dataType;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private Integer progress;
    /** 实际完成率，可超过100%，仅用于接口展示。 */
    private BigDecimal actualProgress;
    /** 计入父任务汇总的完成率，范围0-100，仅用于接口展示。 */
    private BigDecimal countedProgress;
    private String status;
    private Integer isDelete;
    private Date createTime;
    private Date updateTime;

}
