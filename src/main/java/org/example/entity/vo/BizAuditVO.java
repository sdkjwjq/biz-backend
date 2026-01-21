package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAuditVO {
    private Long subId; // 提交ID
    private Long taskId; // 任务ID
    private Long fileId; // 文件ID
    private String filename; // 文件名

    // 填报数据相关
    private BigDecimal reportedValue; // 本次填报完成值
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大
    private Long submitBy; // 提交人ID
    private Long submitDeptId; // 提交人部门ID
    private Long manageDeptId; // 归口部门ID
    private Date submitTime; // 提交时间
    private String fileSuffix; // 后缀（仅允许pdf/doc/docx）

    /**
     * 流程状态枚举：
     * 0:草稿
     * 10:待[所在部门]审批 20:待[归口部门]审批 30:待[管理员]审批
     * 40:已归档
     * -10:被所在部门退回 -20:被归口部门退回 -30:被管理员退回
     */
    private Integer flowStatus;
    private Long currentHandlerId; // 当前处理人ID
    private Integer isDelete; // 0:存在 1:删除


}
