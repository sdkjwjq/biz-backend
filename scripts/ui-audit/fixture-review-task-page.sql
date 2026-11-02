-- Only serve.py's random isolated schema. No business data is copied.
-- The original performance tree groups actual indicators by their ancestors.
UPDATE biz_performance SET ancestors='0,1,1.1' WHERE perf_id IN(950001,950011);
UPDATE biz_task SET current_value=10,progress=100,comment='隔离测试完成说明',
 exp_target='隔离预期目标',exp_effect='隔离预期成效' WHERE task_id=930002;
INSERT INTO biz_level4_task(task_id,parent_id,dept_id,task_name,target_value,current_value,progress,data_type,is_delete)
VALUES(939991,930002,920001,'隔离超额完成目标',2,3,100,'1',0);
INSERT INTO biz_audit_log(sub_id,operator_id,action_type,pre_status,post_status,comment)
VALUES(970001,910001,'提交',0,10,'隔离提交证据');
INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete)
VALUES(930202,1,930001,2025,'1.1.3','隔离往年任务',3,910001,910003,910002,920001,'1',10,5,50,'1',0);
