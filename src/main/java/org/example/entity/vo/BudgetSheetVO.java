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
}
