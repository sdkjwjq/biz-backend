-- 仅随机隔离库；四个月用于连续、缺月及调账测试。
INSERT INTO biz_budget_sheet(sheet_id,year,month,locked,is_delete)
VALUES(970001,2026,1,1,0),(970002,2026,2,1,0),(970004,2026,4,1,0),(970005,2026,5,1,0);
INSERT INTO biz_budget_source_item(sheet_id,source_key,source_name,display_order,annual_plan)
SELECT sheet_id,'school','合成学校预算',1,120 FROM biz_budget_sheet;
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,core_output,target_value,achieved)
SELECT s.sheet_id,c.indicator_id,CONCAT('改革任务',c.sort_order),10,
CASE s.month WHEN 1 THEN 1 WHEN 2 THEN 3 WHEN 4 THEN 5 ELSE 4 END,0,0,
CONCAT('原始导入指标',c.indicator_code),'原始目标','原始导入文字应保留，不作为归档值'
FROM biz_budget_sheet s CROSS JOIN biz_core_indicator c;
