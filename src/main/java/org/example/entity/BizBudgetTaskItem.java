package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizBudgetTaskItem {
    private Long itemId;
    private Long sheetId;
    private Integer taskIndex;
    private String taskName;
    private BigDecimal amount;
    private BigDecimal goods;
    private BigDecimal capital;
    private BigDecimal other;
    private String coreOutput;
    private String targetValue;
    private String achieved;
}
