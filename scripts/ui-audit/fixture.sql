-- 仅由隔离审计脚本导入新建测试库。所有人员、任务、通知均为合成数据。
INSERT INTO sys_dept (dept_id,dept_name,is_delete) VALUES (920001,'审计测试部门A',0),(920002,'审计测试部门B',0);
INSERT INTO sys_user (user_id,dept_id,user_name,nick_name,email,password,role,status,is_delete) VALUES
(110228,920001,'audit_admin','审计管理员','admin@example.invalid','review-fixture-password','0','1',0),
(910001,920001,'audit_user_a','审计用户A','a@example.invalid','review-fixture-password','1','1',0),
(910002,920001,'audit_leader','审计部门负责人','leader@example.invalid','review-fixture-password','2','1',0),
(910003,920001,'audit_reviewer','审计审核人','reviewer@example.invalid','review-fixture-password','1','1',0),
(910004,920002,'audit_user_b','审计用户B','b@example.invalid','review-fixture-password','1','1',0),
(1910001,920001,'audit_achievement','审计成果账号','achievement@example.invalid','review-fixture-password','1','1',0);
UPDATE sys_dept SET leader_id=910002 WHERE dept_id=920001;
INSERT INTO biz_project (project_id,project_name,leader_id) VALUES (1,'审计专用项目',110228);
INSERT INTO biz_task (task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete) VALUES
(930000,1,0,2026,'1','审计专用A一级任务',1,910001,910003,910002,920001,'1',10,0,0,'1',0),
(930001,1,930000,2026,'1.1','审计专用A二级任务',2,910001,910003,910002,920001,'1',10,0,0,'1',0),
(930002,1,930001,2026,'1.1.1','仅用户A可见的三级任务',3,910001,910003,910002,920001,'1',10,0,0,'1',0);
INSERT INTO biz_performance (perf_id,project_id,perf_code,perf_name,target_value,data_type,dept_id,principal_id,auditor_id,leader_id) VALUES
(950001,1,'1.1.review','审计自动绩效',10,'1',920001,910002,910003,910001),
(950011,1,'2.review','审计手动绩效',10,'1',920001,910002,910003,910001);
INSERT INTO biz_performance_year (year_id,perf_id,year,target_value) VALUES (950002,950001,2026,10),(950012,950011,2026,10),(950013,950011,2027,10);
INSERT INTO rel_task_performance (task_id,perf_id,year_id) VALUES (930002,950001,950002);
UPDATE biz_performance_year SET actual_value=3 WHERE year_id=950012;
UPDATE biz_performance SET current_value=3 WHERE perf_id=950011;
INSERT INTO biz_performance_submission (sub_id,perf_id,year_id,year,actual_value,submit_by,submit_time,flow_status,current_handler_id,comment,is_delete)
VALUES (971001,950011,950012,2026,3,910001,NOW(),10,910003,'审计待办样本',0);
INSERT INTO sys_notice (notice_id,from_user_id,to_user_id,type,title,content,source_type,is_read,is_delete,create_time)
SELECT 980000+n,110228,910001,'1',CONCAT('审计通知',LPAD(n,2,'0')),'合成通知正文','0',IF(n>10,'1','0'),0,DATE_ADD('2026-09-16 08:00:00',INTERVAL n SECOND)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15) t;
