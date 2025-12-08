package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizTask {
    private Long taskId;
    private Long projectId;
    private Long parentId;
    private String ancestors;
    private Integer phase;
    private String taskCode;
    private String taskName;
    private Integer level;

    private Long deptId;
    private Long principalId;
    private Long leaderId;

    private String expTarget;
    private String expLevel;
    private String expEffect;
    private String expMaterialDesc;
    private String dataType;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private BigDecimal weight;
    private Integer progress;
    private String status;
    private Integer isDelete;
    private Date createTime;
    private Date updateTime;
}