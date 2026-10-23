-- Synthetic data only; serve.py imports this into a fresh random local schema.
INSERT INTO sys_dept(dept_id,dept_name,status,is_delete) VALUES
(920003,'纪实范围部门C','0',0),(920004,'纪实无任务部门D','0',0),(920005,'纪实目录归口部门E','0',0);
UPDATE sys_dept SET status='0' WHERE dept_id IN(920001,920002);
INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,password,role,status,is_delete,force_password_change) VALUES
(910011,920001,'record_dept_a','部门A填报员','review-fixture-password','1','1',0,0),
(910012,920002,'record_dept_b','部门B填报员','review-fixture-password','1','1',0,0),
(910013,920004,'record_no_scope','无任务部门账号','review-fixture-password','1','1',0,0),
(910014,920003,'record_dept_c','部门C填报员','review-fixture-password','1','1',0,0),
(910017,920004,'record_read_leader','仅查阅校领导','review-fixture-password','1','1',0,0),
(910018,920005,'record_dept_e','部门E填报员','review-fixture-password','1','1',0,0),
(910021,920003,'record_professional_b','专业群填报员B','review-fixture-password','1','1',0,0);
INSERT INTO sys_leadership_access(user_id,scope,version) VALUES(910017,'ALL',1);
INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete) VALUES
(930003,1,930001,2026,'1.1.2','一级A的部门B任务',3,910001,910003,910002,920002,'1',10,0,0,'1',0),
(930100,1,0,2025,'2','纪实一级任务B',1,910001,910003,910002,920002,'1',10,0,0,'1',0),
(930101,1,930100,2025,'2.1','纪实二级目录B',2,910001,910003,910002,920002,'1',10,0,0,'1',0),
(930102,1,930101,2026,'2.1.1','一级B的部门B任务',3,910001,910003,910002,920002,'1',10,0,0,'1',0),
(930200,1,0,2026,'3','纪实一级任务C',1,910001,910003,910002,920003,'1',10,0,0,'1',0),
(930201,1,930200,2026,'3.1','纪实二级目录C',2,910001,910003,910002,920003,'1',10,0,0,'1',0),
(930202,1,930201,2026,'3.1.1','一级C的部门C任务',3,910001,910003,910002,920003,'1',10,0,0,'1',0),
(930300,1,0,2025,'4','纪实一级任务D',1,910001,910003,910002,920003,'1',10,0,0,'1',0),
(930301,1,930300,2025,'4.1','纪实二级目录D',2,910001,910003,910002,920003,'1',10,0,0,'1',0),
(930302,1,930301,2026,'4.1.1','一级D的四级归口命中父任务',3,910001,910003,910002,920003,'1',10,0,0,'1',0),
(930303,1,930301,2026,'4.1.2','一级D的部门C兄弟任务',3,910001,910003,910002,920003,'1',10,0,0,'1',0);
UPDATE biz_task SET leader_id=910021 WHERE task_id IN(930003,930102,930202,930302,930303);
UPDATE biz_task SET dept_id=920005,phase=2025 WHERE task_id=930201;
INSERT INTO biz_level4_task(task_id,parent_id,phase,task_name,leader_id,dept_id,data_type,target_value,current_value,progress,status,is_delete) VALUES
(940001,930002,2026,'仅四级责任人资格',910004,920002,'1',10,0,0,'1',0),
(940002,930302,2026,'仅四级部门A归口',910021,920001,'1',10,0,0,'1',0);
-- A previously submitted record deliberately freezes the old one-task scope.
INSERT INTO biz_work_record(record_id,owner_id,owner_name,record_year,record_month,status,version,create_time,update_time,submit_time)
VALUES(990101,910011,'部门A填报员',2026,1,1,1,'2026-01-30','2026-01-30','2026-01-30');
INSERT INTO biz_work_record_entry(record_id,reform_task_id,reform_task_name,sort_order,key_progress,stage_results,typical_practices)
VALUES(990101,930000,'审计专用A一级任务',0,'历史冻结进展','','');
INSERT INTO biz_work_record_snapshot(record_id,statistics_json,generated_time)
VALUES(990101,'{"schemaVersion":1,"year":2026,"month":1,"generatedAt":"2026-01-30T12:00:00+08:00","cutoffAt":"2026-01-30T12:00:00+08:00","totalTasks":1,"newCompleted":0,"cumulativeCompleted":0,"completionRate":0,"unverifiedTasks":0,"notice":"旧版冻结统计：本人负责的任务，共1条。","tasks":[{"taskId":930002,"taskCode":"1.1.1","taskName":"仅用户A可见的三级任务","leaderName":"审计用户A","reformTaskId":930000,"auditState":"未填报","firstCompletedAt":null,"completed":false,"completedThisMonth":false,"needsVerification":false,"verificationReason":"","other":""}],"reformTasks":[{"taskId":930000,"taskCode":"1","taskName":"审计专用A一级任务"}]}','2026-01-30');
