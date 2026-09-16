INSERT INTO biz_task
(task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete)
VALUES (933001,1,930001,2026,'1.1.6','四级填报任务',3,910001,910003,910002,920001,'1',5,0,0,'1',0);
INSERT INTO biz_level4_task
(task_id,parent_id,phase,task_name,leader_id,dept_id,data_type,target_value,current_value,progress,status)
VALUES (963001,933001,2026,'合成四级子任务',910001,920001,'1',5,0,0,'1');
INSERT INTO rel_task_performance (task_id,perf_id,year_id) VALUES (933001,950001,950002);
