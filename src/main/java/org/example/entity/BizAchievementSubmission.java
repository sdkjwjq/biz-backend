package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAchievementSubmission {
    private Long subId;
    private Long achId;
    private Long submitBy;
    private Date submitTime;
    private Integer flowStatus;
    private Long currentHandlerId;
    private String comment;
    private Integer isDelete;
}
