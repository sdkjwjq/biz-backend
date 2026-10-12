-- 仅供随机隔离测试库使用；不导入线上。
UPDATE sys_user SET force_password_change=0;
INSERT INTO biz_performance(perf_id,project_id,perf_code,perf_name,target_value,current_value,data_type,dept_id,principal_id,auditor_id,leader_id,is_delete)
SELECT 960000+indicator_id,1,indicator_code,CONCAT('核心指标测试',sort_order),100,7,'1',920001,910002,910003,910001,0
FROM biz_core_indicator;
INSERT INTO biz_level4_task(task_id,parent_id,phase,task_name,leader_id,dept_id,is_delete)
VALUES(940001,930002,2026,'四级责任人资格测试',910004,920001,0);
