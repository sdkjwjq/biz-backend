-- 合成账号，没有填报或审核职责；仅用于随机隔离库。
INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,email,password,role,status,is_delete,force_password_change) VALUES
(910010,920001,'school_reader','合成全校领导','school@example.invalid','LeaderTest123','1','1',0,0),
(910011,920002,'department_reader','合成分管领导','department@example.invalid','LeaderTest123','1','1',0,0);
INSERT INTO biz_task(task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete) VALUES
(930010,1,930001,2026,'1.1.2','范围外部门B任务',3,910004,910003,910002,920002,'1',10,9,90,'1',0);
INSERT INTO biz_performance(perf_id,project_id,perf_code,perf_name,target_value,data_type,dept_id,principal_id,auditor_id,leader_id,is_delete) VALUES
(950041,1,'2.1.99','范围外部门B绩效',10,'1',920002,910002,910003,910004,0);
INSERT INTO biz_performance_year(year_id,perf_id,year,target_value,actual_value,is_delete) VALUES (950042,950041,2026,10,9,0);
INSERT INTO sys_notice(notice_id,from_user_id,to_user_id,type,title,content,source_type,source_id,is_read,is_delete,create_time)
VALUES(980100,110228,910010,'1','领导浏览通知','合成个人通知，不提供前往处理业务按钮','1',930002,'0',0,NOW());
