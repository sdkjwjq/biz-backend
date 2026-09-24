-- 管理员归属移交验收夹具：基础夹具的 930002 无审核单据用于成功移交，再补一条待审任务与绩效用于整批拒绝与绩效入口检查。
-- 仅在 --fixture ownership-transfer 的随机隔离库中加载，全部为合成数据。
UPDATE biz_performance SET ancestors='0,2,2.1' WHERE perf_id IN (950001,950011);
INSERT INTO biz_task (task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete)
VALUES (930006,1,930001,2026,'1.1.2','移交夹具待审任务',3,910001,910003,910002,920001,'1',10,1,10,'2',0);
INSERT INTO sys_file (file_id,file_name,file_path,file_url,file_suffix,upload_by)
VALUES (940002,'ownership-transfer.pdf','ownership-transfer.pdf','/uploads/ownership-transfer.pdf','pdf',910001);
-- 930006 存在未删除且处于审核中（20）的单据，用于整批拒绝断言。
INSERT INTO biz_material_submission (sub_id,task_id,file_id,submit_by,submit_dept_id,manage_dept_id,file_suffix,flow_status,current_handler_id,is_delete)
VALUES (979011,930006,940002,910001,920001,920001,'pdf',20,910002,0);
