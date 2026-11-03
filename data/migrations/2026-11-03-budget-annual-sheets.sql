-- 年度预算：为 2025/2027/2028/2029 各复制一张与 2026 年结构、数据一致的年度表。
-- 幂等：已存在的年度表/任务行/资金来源行不会重复插入；2026 年年度预算不存在时不做任何复制。
SET NAMES utf8mb4;

INSERT INTO biz_budget_sheet (year, month, section, locked, file_id, is_delete)
SELECT y.year, 5, 'annual', 0, NULL, 0
FROM (SELECT 2025 AS year UNION ALL SELECT 2027 UNION ALL SELECT 2028 UNION ALL SELECT 2029) y
WHERE EXISTS (
    SELECT 1 FROM biz_budget_sheet a
    WHERE a.year = 2026 AND a.month = 5 AND a.section = 'annual' AND COALESCE(a.is_delete, 0) = 0
  )
  AND NOT EXISTS (
    SELECT 1 FROM biz_budget_sheet s
    WHERE s.year = y.year AND s.month = 5 AND s.section = 'annual' AND COALESCE(s.is_delete, 0) = 0
  );

INSERT INTO biz_budget_task_item (sheet_id, task_index, task_name, amount, budget_amount, goods, capital, other, goods_budget, capital_budget, other_budget, core_output, target_value, achieved)
SELECT tgt.sheet_id, src.task_index, src.task_name, src.amount, src.budget_amount, src.goods, src.capital, src.other, src.goods_budget, src.capital_budget, src.other_budget, src.core_output, src.target_value, src.achieved
FROM biz_budget_task_item src
JOIN biz_budget_sheet so ON so.sheet_id = src.sheet_id
    AND so.year = 2026 AND so.month = 5 AND so.section = 'annual' AND COALESCE(so.is_delete, 0) = 0
JOIN biz_budget_sheet tgt ON tgt.year IN (2025, 2027, 2028, 2029)
    AND tgt.month = 5 AND tgt.section = 'annual' AND COALESCE(tgt.is_delete, 0) = 0
WHERE NOT EXISTS (
    SELECT 1 FROM biz_budget_task_item x WHERE x.sheet_id = tgt.sheet_id AND x.task_index = src.task_index
  );

INSERT INTO biz_budget_source_item (sheet_id, source_key, source_name, display_order, five_year_total, available, annual_plan, carryover, arrived)
SELECT tgt.sheet_id, src.source_key, src.source_name, src.display_order, src.five_year_total, src.available, src.annual_plan, src.carryover, src.arrived
FROM biz_budget_source_item src
JOIN biz_budget_sheet so ON so.sheet_id = src.sheet_id
    AND so.year = 2026 AND so.month = 5 AND so.section = 'annual' AND COALESCE(so.is_delete, 0) = 0
JOIN biz_budget_sheet tgt ON tgt.year IN (2025, 2027, 2028, 2029)
    AND tgt.month = 5 AND tgt.section = 'annual' AND COALESCE(tgt.is_delete, 0) = 0
WHERE NOT EXISTS (
    SELECT 1 FROM biz_budget_source_item x WHERE x.sheet_id = tgt.sheet_id AND x.source_key = src.source_key
  );
