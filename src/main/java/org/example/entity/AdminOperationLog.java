package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminOperationLog {
    private Long logId;
    private String module;
    private String action;
    private String targetType;
    private String targetId;
    private String targetName;
    private String batchId;
    private Long operatorId;
    private String reason;
    private String beforeJson;
    private String afterJson;
    private Date createTime;
}
