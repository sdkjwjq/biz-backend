-- Isolated monthly annual/execution fixtures; never copied into business databases.
INSERT INTO biz_budget_sheet(sheet_id,year,month,section,locked,is_delete) VALUES
(960001,2026,5,'annual',0,0),(960002,2026,5,'execution',0,0),(960003,2026,6,'annual',0,0);
INSERT INTO biz_budget_source_item(sheet_id,source_key,source_name,display_order,five_year_total,available,annual_plan,carryover,arrived)
SELECT sheet_id,'school','学校自筹资金',1,1000,100,100,0,100 FROM biz_budget_sheet WHERE sheet_id BETWEEN 960001 AND 960003;
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960001,1,'隔离改革任务1',10,5,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960001,2,'隔离改革任务2',10,5,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960001,3,'隔离改革任务3',10,5,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960001,4,'隔离改革任务4',10,5,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960001,5,'隔离改革任务5',10,5,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960001,6,'隔离改革任务6',10,5,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960001,7,'隔离改革任务7',10,5,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960001,8,'隔离改革任务8',10,5,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960001,9,'隔离改革任务9',10,5,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960001,10,'隔离改革任务10',10,5,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960002,1,'隔离改革任务1',10,2,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960002,2,'隔离改革任务2',10,2,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960002,3,'隔离改革任务3',10,2,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960002,4,'隔离改革任务4',10,2,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960002,5,'隔离改革任务5',10,2,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960002,6,'隔离改革任务6',10,2,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960002,7,'隔离改革任务7',10,2,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960002,8,'隔离改革任务8',10,2,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960002,9,'隔离改革任务9',10,2,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960002,10,'隔离改革任务10',10,2,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960003,1,'隔离改革任务1',10,4,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960003,2,'隔离改革任务2',10,4,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960003,3,'隔离改革任务3',10,4,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960003,4,'隔离改革任务4',10,4,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960003,5,'隔离改革任务5',10,4,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960003,6,'隔离改革任务6',10,4,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960003,7,'隔离改革任务7',10,4,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960003,8,'隔离改革任务8',10,4,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960003,9,'隔离改革任务9',10,4,0,0,10,5,0,0);
INSERT INTO biz_budget_task_item(sheet_id,task_index,task_name,amount,goods,capital,other,budget_amount,goods_budget,capital_budget,other_budget) VALUES(960003,10,'隔离改革任务10',10,4,0,0,10,5,0,0);
INSERT INTO biz_budget_import_version(sheet_id,year,month,version_no,operation_type,snapshot_json,created_by,is_delete)
SELECT s.sheet_id,s.year,s.month,1,'INITIAL',JSON_OBJECT('sheet',JSON_OBJECT('sheetId',s.sheet_id,'year',s.year,'month',s.month,'section',s.section,'locked',0,'isDelete',0),'currentVersionNo',1,'sources',(SELECT JSON_ARRAYAGG(JSON_OBJECT('sourceKey',b.source_key,'sourceName',b.source_name,'displayOrder',b.display_order,'fiveYearTotal',b.five_year_total,'available',b.available,'annualPlan',b.annual_plan,'carryover',b.carryover,'arrived',b.arrived)) FROM biz_budget_source_item b WHERE b.sheet_id=s.sheet_id),'tasks',(SELECT JSON_ARRAYAGG(JSON_OBJECT('taskIndex',t.task_index,'taskName',t.task_name,'amount',t.amount,'goods',t.goods,'capital',t.capital,'other',t.other,'budgetAmount',t.budget_amount,'goodsBudget',t.goods_budget,'capitalBudget',t.capital_budget,'otherBudget',t.other_budget)) FROM biz_budget_task_item t WHERE t.sheet_id=s.sheet_id)),110228,0
FROM biz_budget_sheet s WHERE s.sheet_id BETWEEN 960001 AND 960003;
