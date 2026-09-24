-- 任务页统计带验收夹具：覆盖进行中/审核中/已完成、跨年度逾期与两个归口部门。
-- 仅在 --fixture task-stats 的随机隔离库中加载，全部为合成数据。
INSERT INTO biz_task (task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete) VALUES
(930006,1,930001,2026,'1.1.5','统计夹具已退回任务',3,910001,910003,910002,920001,'1',10,0,0,'1',0),
(930007,1,930001,2026,'1.1.6','统计夹具进行中任务',3,910001,910003,910002,920001,'1',10,5,50,'1',0),
(930008,1,930001,2026,'1.1.7','统计夹具已完成任务',3,910001,910003,910002,920002,'1',10,10,100,'3',0),
(930009,1,930001,2025,'1.1.8','统计夹具逾期任务',3,910001,910003,910002,920002,'1',10,2,20,'1',0),
(930010,1,930001,2026,'1.1.9','统计夹具审核中任务',3,910001,910003,910002,920002,'1',10,1,10,'2',0);
INSERT INTO sys_file (file_id,file_name,file_path,file_url,file_suffix,upload_by)
VALUES (940002,'task-stats.pdf','task-stats.pdf','/uploads/task-stats.pdf','pdf',910001);
-- 930006 最新单据为已退回（is_delete=1 且 flow_status<0），用于“已退回”计数。
INSERT INTO biz_material_submission (sub_id,task_id,file_id,submit_by,submit_dept_id,manage_dept_id,file_suffix,flow_status,current_handler_id,is_delete)
VALUES (979011,930006,940002,910001,920001,920001,'pdf',-10,910001,1);
-- 930010 待管理员处理，用于“我的待办”计数。
INSERT INTO biz_material_submission (sub_id,task_id,file_id,submit_by,submit_dept_id,manage_dept_id,file_suffix,flow_status,current_handler_id,is_delete)
VALUES (979012,930010,940002,910001,920001,920002,'pdf',10,110228,0);
