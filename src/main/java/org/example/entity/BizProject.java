package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizProject {
    private Long projectId;
    private String projectName;
    private String projectCode;
    private Long leaderId;
    private Date startDate;
    private Date endDate;
    private String status;
    private Integer isDelete;
    private Date createTime;
    private Date updateTime;
}