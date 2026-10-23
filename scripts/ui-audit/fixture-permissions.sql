-- 仅供 serve.py 的随机隔离库；从未复制线上数据。
INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,password,role,status,is_delete,force_password_change)
VALUES(910009,NULL,'external_expert_fixture','隔离专家','review-fixture-password','1','1',0,0);
INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete)
VALUES(930102,1,930001,2026,'1.1.2','未获准浏览的合成三级任务',3,910004,910003,910002,920002,'1',10,0,0,'1',0);
INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json)
VALUES(910009,'CUSTOM','{}','{"tasks":{"mode":"CUSTOM","items":[930002]}}');
INSERT INTO sys_user_permission_role(user_id,role_code) VALUES(910009,'EXPERT');
INSERT INTO sys_file(file_id,file_name,file_path,file_url,file_suffix,upload_by,is_delete)
VALUES(940001,'permission-file-a.pdf','/uploads/permission-file-a.pdf','/uploads/permission-file-a.pdf','pdf',910001,0),
(940002,'permission-file-b.pdf','/uploads/permission-file-b.pdf','/uploads/permission-file-b.pdf','pdf',910004,0);
INSERT INTO biz_material_submission(sub_id,task_id,file_id,reported_value,submit_by,submit_dept_id,manage_dept_id,file_suffix,flow_status,current_handler_id,is_delete)
VALUES(970001,930002,940001,1,910001,920001,920001,'pdf',10,910003,0),
(970002,930102,940002,1,910004,920002,920002,'pdf',10,910003,0);
