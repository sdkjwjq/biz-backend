-- Synthetic task approvals used only by the guarded isolated-schema loader.
INSERT INTO biz_task
(task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete)
VALUES
(931001,1,930001,2026,'1.1.2','竞态任务A',3,910001,910003,910002,920001,'1',10,3,30,'2',0),
(931002,1,930001,2026,'1.1.3','竞态任务B',3,910001,910003,910002,920001,'1',10,7,70,'2',0);
INSERT INTO sys_file (file_id,file_name,file_path,file_url,file_suffix,upload_by)
VALUES (940101,'race.pdf','race.pdf','/uploads/race.pdf','pdf',910001);
INSERT INTO biz_material_submission
(sub_id,task_id,file_id,reported_value,data_type,submit_by,submit_dept_id,manage_dept_id,submit_time,file_suffix,flow_status,current_handler_id,is_delete)
VALUES
(981001,931001,940101,3,'1',910001,920001,920001,NOW(),'pdf',10,910003,0),
(981002,931002,940101,7,'1',910001,920001,920001,NOW(),'pdf',10,910003,0);
