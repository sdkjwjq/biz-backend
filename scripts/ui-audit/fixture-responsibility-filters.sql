-- 人员筛选及预算年月浏览验收夹具，仅导入随机隔离库。
INSERT INTO biz_task (task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete)
VALUES (930050,1,930001,2026,'1.1.50','责任人筛选任务B',3,910004,910003,910002,920002,'1',10,2,20,'1',0);

INSERT INTO biz_performance (perf_id,project_id,perf_code,perf_name,target_value,current_value,data_type,dept_id,principal_id,auditor_id,leader_id,ancestors)
VALUES
  (950050,1,'2.1.50','责任人筛选绩效B',10,2,'1',920002,910002,910003,910004,'0,2,2.1'),
  (950052,1,'2.1.51','责任人筛选绩效A',10,3,'1',920001,910002,910003,910001,'0,2,2.1');
INSERT INTO biz_performance_year (year_id,perf_id,year,target_value,actual_value)
VALUES (950051,950050,2026,10,2),(950053,950052,2026,10,3);

INSERT INTO biz_achievement (ach_id,category,level,ach_name,department,got_time,dept_id,create_by,audit_status,comment)
VALUES
  (979050,1,'省级','填报人筛选成果A','审计测试部门A','2026-06-01',920001,910001,30,'合成人员筛选数据'),
  (979051,1,'省级','填报人筛选成果B','审计测试部门B','2026-06-02',920002,910004,30,'合成人员筛选数据');
