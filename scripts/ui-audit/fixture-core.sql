-- 仅供随机隔离测试库使用；不导入线上。
UPDATE sys_user SET force_password_change=0;
INSERT INTO biz_performance(perf_id,project_id,perf_code,perf_name,target_value,current_value,data_type,dept_id,principal_id,auditor_id,leader_id,is_delete)
SELECT 960000+indicator_id,1,indicator_code,CONCAT('核心指标测试',sort_order),100,7,'1',920001,910002,910003,910001,0
FROM biz_core_indicator;
INSERT INTO biz_level4_task(task_id,parent_id,phase,task_name,leader_id,dept_id,is_delete)
VALUES(940001,930002,2026,'四级责任人资格测试',910004,920001,0);

UPDATE biz_performance SET perf_name='发布《工程机械产业发展报告》数量',target_value=5 WHERE perf_id=960010;
-- 模拟升级前错误关联的已归档和待审记录；旧记录只读保留，不参与新指标。
INSERT INTO biz_core_report(report_id,indicator_id,record_year,record_month,performance_code,indicator_name,target_value,data_type,actual_value,submit_by,submit_name,principal_id,current_handler_id,flow_status,submit_time,archive_time)
VALUES(980010,10,2026,8,'1.2.15','毕业生就业相关度',85,'2',85,910001,'审计责任人',910003,NULL,30,'2026-08-20','2026-08-21'),
(980011,10,2026,8,'1.2.15','毕业生就业相关度',85,'2',85,910001,'审计责任人',910003,910003,10,'2026-08-22',NULL);
