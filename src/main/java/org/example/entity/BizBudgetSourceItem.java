package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizBudgetSourceItem {
    private Long itemId;
    private Long sheetId;
    private String sourceKey;
    private String sourceName;
    private Integer displayOrder;
    private BigDecimal fiveYearTotal;
    private BigDecimal available;
    private BigDecimal annualPlan;
    private BigDecimal carryover;
    private BigDecimal arrived;
}
