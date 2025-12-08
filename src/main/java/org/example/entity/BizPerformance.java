package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizPerformance {
    private Long perfId;
    private Long projectId;
    private Long parentId;
    private String ancestors;
    private String perfCode;
    private String perfName;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private String dataType;
    private Long deptId;
    private Long principalId;
    private Long leaderId;
    private Integer isDelete;
    private Date createTime;
    private Date updateTime;
}