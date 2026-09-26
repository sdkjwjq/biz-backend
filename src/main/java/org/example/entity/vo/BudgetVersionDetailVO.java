package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetVersionDetailVO {
    private BudgetVersionVO version;
    private BudgetSheetVO budget;
}
