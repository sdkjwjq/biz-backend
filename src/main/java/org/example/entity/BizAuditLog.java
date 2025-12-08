package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAuditLog {
    private Long logId;
    private Long subId;
    private Long operatorId;
    private String actionType;
    private Integer preStatus;
    private Integer postStatus;
    private String comment;
    private Date createTime;
}