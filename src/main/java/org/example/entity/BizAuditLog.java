package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAuditLog {
    private Long logId; // 日志ID
    private Long subId; // 提交ID
    private Long operatorId; // 操作人ID
    private String actionType; // 动作
    /**
     * 流程状态参考 BizMaterialSubmission 的 flowStatus 枚举
     */
    private Integer preStatus; // 前状态
    private Integer postStatus; // 后状态
    private String comment; // 意见
    private Date createTime; // 创建时间
}