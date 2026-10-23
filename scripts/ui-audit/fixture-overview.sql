-- 仅由 serve.py 导入随机隔离库；涵盖分页、归档月份、旧资格及预算缺月。
UPDATE biz_task SET progress=50,is_priority=1,is_difficult=1 WHERE task_id=930002;
INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete)
SELECT 930200+indicator_id,1,930001,2026,CONCAT('Overview-',sort_order),CONCAT('看板合成任务',sort_order),3,910001,910003,910002,920001,'1',10,5,50,CASE MOD(sort_order,3) WHEN 0 THEN '3' WHEN 1 THEN '2' ELSE '1' END,0 FROM biz_core_indicator;
UPDATE biz_task SET is_priority=1 WHERE task_id=930201;
UPDATE biz_task SET is_difficult=1 WHERE task_id=930202;
UPDATE biz_task SET progress=100,current_value=10 WHERE task_id BETWEEN 930201 AND 930210 AND status='3';
INSERT INTO biz_core_report(report_id,indicator_id,record_year,record_month,performance_code,indicator_name,target_value,data_type,actual_value,submit_by,submit_name,principal_id,flow_status,submit_time,archive_time)
VALUES(980020,1,2026,2,'1.1.5','归档时核心指标一',100,'1',50,910001,'合成填报人',910002,30,'2026-02-15','2026-02-16'),
(980021,1,2026,5,'1.1.5','归档时核心指标一',100,'1',150,910001,'合成填报人',910002,30,'2026-05-15','2026-05-16');
UPDATE biz_achievement SET yi_deng_jiang=3 WHERE ach_id=990102;
INSERT INTO biz_work_record(record_id,owner_id,owner_name,record_year,record_month,status,create_time,update_time,submit_time)
VALUES(992001,910001,'审计责任人',2026,4,1,NOW(),NOW(),NOW()),
(992002,999999,'原资格填报人',2026,4,1,NOW(),NOW(),NOW()),
(992003,910002,'草稿保密',2026,4,0,NOW(),NOW(),NULL);
INSERT INTO biz_work_record_snapshot(record_id,statistics_json,generated_time)
SELECT record_id,JSON_OBJECT('schemaVersion',1,'year',2026,'month',4,'generatedAt','2026-04-30T15:59:59.000+00:00','cutoffAt','2026-04-30T15:59:59.000+00:00','totalTasks',1,'newCompleted',0,'cumulativeCompleted',0,'completionRate',0,'unverifiedTasks',0,'notice','合成冻结快照','tasks',JSON_ARRAY(),'reformTasks',JSON_ARRAY()),NOW() FROM biz_work_record WHERE status=1;
UPDATE biz_work_record SET problems='SECRET-DRAFT-DO-NOT-EXPOSE' WHERE record_id=992003;
