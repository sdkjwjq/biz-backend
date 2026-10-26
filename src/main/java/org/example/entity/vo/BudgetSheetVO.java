package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.BizBudgetSheet;
import org.example.entity.BizBudgetSourceItem;
import org.example.entity.BizBudgetTaskItem;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetSheetVO {
    private BizBudgetSheet sheet;
    private List<BizBudgetSourceItem> sources = new ArrayList<>();
    private List<BizBudgetTaskItem> tasks = new ArrayList<>();
    private Integer currentVersionNo = 0;
    // 仅当前展示和导出使用；原导入文本及版本快照不改写。
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY)
    private java.util.Map<Integer, java.util.Map<String,Object>> archivedIndicators = new java.util.LinkedHashMap<>();
}
