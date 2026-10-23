-- 全部为合成数据，仅用于 serve.py 创建的随机隔离库。
UPDATE sys_dept SET status='0';
UPDATE biz_performance SET ancestors=CASE WHEN perf_code LIKE '2.%' THEN '0,2,2.1' WHEN perf_code LIKE '1.2.%' THEN '0,1,1.2' ELSE '0,1,1.1' END;
INSERT INTO sys_dept(dept_id,dept_name,status,is_delete) VALUES(104,'合成财务处','0',0);
INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,password,role,status,is_delete,force_password_change)
VALUES(910007,104,'synthetic_finance','合成财务填报员','review-fixture-password','1','1',0,0),
(111651,920002,'synthetic_former_leader','合成校级领导','review-fixture-password','2','1',0,0);
INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete)
VALUES(930100,1,0,2026,'2','合成B一级任务',1,910004,910003,910002,920002,'1',10,0,0,'1',0),
(930101,1,930100,2026,'2.1','合成B二级任务',2,910004,910003,910002,920002,'1',10,0,0,'1',0),
(930102,1,930101,2026,'2.1.1','合成B部门独立任务',3,910004,910003,910002,920002,'1',10,0,0,'1',0);
INSERT INTO biz_achievement(ach_id,category,level,ach_name,department,got_time,dept_id,comment,is_competition,create_by,audit_status,is_delete,create_time,update_time)
VALUES(990101,1,'省级',CONCAT('合成长名称成果',REPEAT('产教融合示范项目与关键技术协同创新建设成果',3),'全名结尾'),'合成组织','2026-06-01',920001,'合成退回材料',1,1910001,-10,0,NOW(),NOW()),
(990102,1,'国家级','合成已归档成果','合成组织','2026-06-01',920002,'合成归档材料',0,1910001,30,0,NOW(),NOW()),
(990103,2,'省级','合成待归档成果','合成组织','2026-06-01',920001,'合成待审材料',1,1910001,10,0,NOW(),NOW());
INSERT INTO biz_achievement_submission(sub_id,ach_id,submit_by,submit_time,flow_status,current_handler_id,comment,is_delete)
VALUES(991101,990101,1910001,NOW(),-10,NULL,'合成提交',0);
INSERT INTO biz_achievement_audit_log(sub_id,operator_id,action_type,pre_status,post_status,comment,create_time)
VALUES(991101,110228,'reject',10,-10,'请补充上级发文依据（合成驳回原因）',NOW());
