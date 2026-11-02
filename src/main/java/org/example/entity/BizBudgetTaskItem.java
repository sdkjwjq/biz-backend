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
    /** 预算金额（金额上方的计划值） */
    private BigDecimal budgetAmount;
    private BigDecimal goods;
    private BigDecimal capital;
    private BigDecimal other;
    /** 商品和服务预算（与商品和服务支出并列的计划值） */
    private BigDecimal goodsBudget;
    /** 资本性预算 */
    private BigDecimal capitalBudget;
    /** 其他支出预算 */
    private BigDecimal otherBudget;
    private String coreOutput;
    private String targetValue;
    private String achieved;
}
