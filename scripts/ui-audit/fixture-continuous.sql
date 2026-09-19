-- 仅用于随机隔离库，管理员的任务和成果使用相同审核单 ID，验证跨业务区分。
INSERT INTO biz_performance_audit_snapshot (sub_id,perf_id,year_id,previous_year_actual_value,previous_year_target_value)
SELECT sub_id,perf_id,year_id,0,10 FROM biz_performance_submission WHERE flow_status=10;
INSERT INTO sys_file (file_id,file_name,file_path,file_url,file_suffix,upload_by)
VALUES (940001,'continuous.pdf','continuous.pdf','/uploads/continuous.pdf','pdf',910001);
INSERT INTO biz_task (task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete)
VALUES (930003,1,930001,2026,'1.1.2','连续审核任务B',3,910001,910003,910002,920001,'1',10,1,10,'2',0);
INSERT INTO biz_material_submission (sub_id,task_id,file_id,submit_by,submit_dept_id,manage_dept_id,file_suffix,flow_status,current_handler_id,is_delete)
VALUES (979001,930002,940001,910001,920001,920001,'pdf',30,110228,0),(979002,930003,940001,910001,920001,920001,'pdf',30,110228,0);
INSERT INTO biz_achievement (ach_id,category,level,ach_name,department,got_time,dept_id,create_by,audit_status,comment,is_competition)
VALUES (979001,1,'省级','连续审核成果A','合成部门','2026-06-01',920001,1910001,10,'合成测试',0),
(979002,1,'省级','连续审核成果B','合成部门','2026-06-02',920001,1910001,10,'合成测试',0);
INSERT INTO biz_achievement_submission (sub_id,ach_id,submit_by,flow_status,current_handler_id,is_delete)
VALUES (979001,979001,1910001,10,110228,0),(979002,979002,1910001,10,110228,0);
