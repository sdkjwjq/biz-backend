-- 仅导入新建隔离库，用于任务顺序浏览测试。
INSERT INTO biz_task (task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete) VALUES
(930003,1,930001,2026,'1.1.2','导航测试B',3,910001,910003,910002,920001,'1',10,2,20,'1',0),
(930011,1,930000,2026,'1.2','导航测试第二目录',2,910001,910003,910002,920001,'1',10,0,0,'1',0),
(930012,1,930011,2026,'1.2.1','导航测试C',3,910001,910003,910002,920001,'1',10,4,40,'1',0),
(930022,1,930011,2027,'1.2.2','导航测试其他年份',3,910001,910003,910002,920001,'1',10,0,0,'1',0);
INSERT INTO biz_level4_task (task_id,parent_id,phase,task_name,leader_id,dept_id,data_type,target_value,current_value,progress,status)
VALUES (931003,930003,2026,'导航B四级子目标',910001,920001,'1',10,2,20,'1');
