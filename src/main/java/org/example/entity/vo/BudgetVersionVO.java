package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetVersionVO {
    private Long versionId;
    private Long sheetId;
    private Integer year;
    private Integer month;
    private Integer versionNo;
    private String operationType;
    private Long sourceVersionId;
    private Long fileId;
    private String fileName;
    private Long createdBy;
    private String createdByName;
    private Date createTime;
    private String remark;
    private Boolean current;
}
