-- 2026-11-03：为「年度预算执行」补齐 2025、2027、2028、2029 年的月度表。
-- 1) 若 2026 年执行表不存在，先以 2026 年年度预算为模板建立（只复制任务/资金来源结构，金额置 0）；
-- 2) 再把 2026 年执行表复制到 2025、2027、2028、2029（沿用 5 月定位）。
-- 表头「20xx年可使用资金」「20xx年预算安排」按所选年度自动显示，无需改文案。
-- 脚本可重复执行：已存在的 sheet / 行不会重复插入。

SET NAMES utf8mb4;

-- 1. 确保 2026 年执行表存在
INSERT INTO biz_budget_sheet (year, month, section, locked, is_delete)
SELECT 2026, 5, 'execution', 0, 0
WHERE NOT EXISTS (
  SELECT 1 FROM biz_budget_sheet s
  WHERE s.year = 2026 AND s.month = 5 AND s.section = 'execution' AND COALESCE(s.is_delete, 0) = 0
);

-- 2. 2026 年执行表缺任务行时，从 2026 年年度预算补齐结构（金额为 0）
INSERT INTO biz_budget_task_item
  (sheet_id, task_index, task_name, amount, budget_amount, goods, capital, other,
   goods_budget, capital_budget, other_budget, core_output, target_value, achieved)
SELECT e.sheet_id, t.task_index, t.task_name, 0, 0, 0, 0, 0, 0, 0, 0,
       t.core_output, t.target_value, t.achieved
FROM biz_budget_sheet e
JOIN biz_budget_sheet a
  ON a.year = 2026 AND a.month = 5 AND a.section = 'annual' AND COALESCE(a.is_delete, 0) = 0
JOIN biz_budget_task_item t ON t.sheet_id = a.sheet_id
WHERE e.year = 2026 AND e.month = 5 AND e.section = 'execution' AND COALESCE(e.is_delete, 0) = 0
  AND NOT EXISTS (
    SELECT 1 FROM biz_budget_task_item x WHERE x.sheet_id = e.sheet_id AND x.task_index = t.task_index
  );

-- 3. 2026 年执行表缺资金来源行时，从 2026 年年度预算补齐结构（金额为 0）
INSERT INTO biz_budget_source_item
  (sheet_id, source_key, source_name, display_order, five_year_total, available, annual_plan, carryover, arrived)
SELECT e.sheet_id, s.source_key, s.source_name, s.display_order, 0, 0, 0, 0, 0
FROM biz_budget_sheet e
JOIN biz_budget_sheet a
  ON a.year = 2026 AND a.month = 5 AND a.section = 'annual' AND COALESCE(a.is_delete, 0) = 0
JOIN biz_budget_source_item s ON s.sheet_id = a.sheet_id
WHERE e.year = 2026 AND e.month = 5 AND e.section = 'execution' AND COALESCE(e.is_delete, 0) = 0
  AND NOT EXISTS (
    SELECT 1 FROM biz_budget_source_item x WHERE x.sheet_id = e.sheet_id AND x.source_key = s.source_key
  );

-- 4. 为 2025、2027、2028、2029 建立执行表
INSERT INTO biz_budget_sheet (year, month, section, locked, is_delete)
SELECT y.year, 5, 'execution', 0, 0
FROM (SELECT 2025 AS year UNION ALL SELECT 2027 UNION ALL SELECT 2028 UNION ALL SELECT 2029) y
WHERE NOT EXISTS (
  SELECT 1 FROM biz_budget_sheet s
  WHERE s.year = y.year AND s.month = 5 AND s.section = 'execution' AND COALESCE(s.is_delete, 0) = 0
);

-- 5. 复制改革任务行（金额从 0 开始）
INSERT INTO biz_budget_task_item
  (sheet_id, task_index, task_name, amount, budget_amount, goods, capital, other,
   goods_budget, capital_budget, other_budget, core_output, target_value, achieved)
SELECT n.sheet_id, t.task_index, t.task_name, 0, 0, 0, 0, 0, 0, 0, 0,
       t.core_output, t.target_value, t.achieved
FROM biz_budget_sheet n
JOIN biz_budget_sheet src
  ON src.year = 2026 AND src.month = 5 AND src.section = 'execution' AND COALESCE(src.is_delete, 0) = 0
JOIN biz_budget_task_item t ON t.sheet_id = src.sheet_id
WHERE n.year IN (2025, 2027, 2028, 2029) AND n.month = 5 AND n.section = 'execution' AND COALESCE(n.is_delete, 0) = 0
  AND NOT EXISTS (
    SELECT 1 FROM biz_budget_task_item x WHERE x.sheet_id = n.sheet_id AND x.task_index = t.task_index
  );

-- 6. 复制资金来源行（金额从 0 开始）
INSERT INTO biz_budget_source_item
  (sheet_id, source_key, source_name, display_order, five_year_total, available, annual_plan, carryover, arrived)
SELECT n.sheet_id, s.source_key, s.source_name, s.display_order, 0, 0, 0, 0, 0
FROM biz_budget_sheet n
JOIN biz_budget_sheet src
  ON src.year = 2026 AND src.month = 5 AND src.section = 'execution' AND COALESCE(src.is_delete, 0) = 0
JOIN biz_budget_source_item s ON s.sheet_id = src.sheet_id
WHERE n.year IN (2025, 2027, 2028, 2029) AND n.month = 5 AND n.section = 'execution' AND COALESCE(n.is_delete, 0) = 0
  AND NOT EXISTS (
    SELECT 1 FROM biz_budget_source_item x WHERE x.sheet_id = n.sheet_id AND x.source_key = s.source_key
  );
