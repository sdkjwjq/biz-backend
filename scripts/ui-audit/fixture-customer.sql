-- 隔离客户反馈复现与验收夹具：处理部门不同于任务所属部门。
UPDATE sys_user SET dept_id=920002 WHERE user_id=910002;
UPDATE biz_task SET status='2' WHERE task_id=930002;
INSERT INTO biz_task (task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete) VALUES
(930004,1,930001,2026,'1.1.3','客户待专业群审核任务',3,910001,910003,910002,920001,'1',10,1,10,'2',0),
(930005,1,930001,2026,'1.1.4','客户待归口审核任务',3,910001,910003,910002,920001,'1',10,1,10,'2',0);
INSERT INTO biz_material_submission (sub_id,task_id,file_id,submit_by,submit_dept_id,manage_dept_id,file_suffix,flow_status,current_handler_id,is_delete) VALUES
(979003,930004,940001,910001,920001,920001,'pdf',10,910003,0),
(979004,930005,940001,910001,920001,920001,'pdf',20,910002,0);
INSERT INTO biz_achievement (ach_id,category,level,ach_name,department,got_time,dept_id,create_by,audit_status,comment,is_competition,yi_deng_jiang)
VALUES (979003,2,'省级','无审核证据的历史成果','合成部门','2026-06-03',920001,1910001,30,'合成历史数据',1,2);
UPDATE biz_achievement_submission SET submit_time='2026-07-01 12:00:00' WHERE ach_id IN (979001,979002);
