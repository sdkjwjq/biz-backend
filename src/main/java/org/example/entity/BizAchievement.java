package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAchievement {
    private Long achId;
    private Long projectId;
    private String achName;
    private String level;
    private String sourceType;
    private Long sourceId;
    private Date obtainDate;
    private Long createBy;
    private Integer isDelete;
    private Date createTime;
}