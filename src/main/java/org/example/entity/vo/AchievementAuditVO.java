package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AchievementAuditVO {
    private Long subId;
    private Long achId;
    private Integer category;
    private String level;
    private String achName;
    private String department;
    private Date gotTime;
    private Long deptId;
    private Long fileId;
    private String achievementComment;
    private Long submitBy;
    private Date submitTime;
    private Integer flowStatus;
    private Long currentHandlerId;
    private String comment;
    private Integer isDelete;
}
