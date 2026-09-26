package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/** 预算导入或恢复操作形成的不可变快照。 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizBudgetImportVersion {
    private Long versionId;
    private Long sheetId;
    private Integer year;
    private Integer month;
    private Integer versionNo;
    private String operationType;
    private Long sourceVersionId;
    private Long fileId;
    private String fileName;
    private String snapshotJson;
    private Long createdBy;
    private Date createTime;
    private String remark;
    private Integer isDelete;
}
