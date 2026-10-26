-- Synthetic-only extension, imported exclusively into a new random test schema.
INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete)
WITH RECURSIVE sequence(n) AS (SELECT 1 UNION ALL SELECT n+1 FROM sequence WHERE n<40)
SELECT 998000+n,1,930001,2026,CONCAT('UX-',LPAD(n,3,'0')),CONCAT('体验检查任务',n),3,910001,910003,910002,920001,'1',10,1,10,'1',0 FROM sequence;
INSERT INTO biz_budget_sheet(sheet_id,year,month,locked,is_delete) VALUES(998900,2026,7,0,0);
INSERT INTO biz_budget_source_item(sheet_id,source_key,source_name,annual_plan,available,five_year_total,arrived) VALUES(998900,'central','合成资金来源',120.00,200.00,0,100.00);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,core_output,target_value,achieved)
VALUES(998900,1,'合成改革任务',200,100,0,0,'合成核心产出','10','');
