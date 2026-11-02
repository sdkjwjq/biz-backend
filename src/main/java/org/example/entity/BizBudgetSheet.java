package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizBudgetSheet {
    private Long sheetId;
    private Integer year;
    private Integer month;
    /** annual=年度预算 execution=年度预算执行 */
    private String section;
    private Integer locked;
    private Long fileId;
    private Long importBy;
    private Date importTime;
    private Date updateTime;
    private Integer isDelete;
}
