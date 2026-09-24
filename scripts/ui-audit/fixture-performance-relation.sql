-- 绩效关联任务编辑验收夹具：一条已关联、一条可新增、一条年度不一致、一条数据类型为 0 的三级任务。
-- 仅在 --fixture performance-relation 的随机隔离库中加载，全部为合成数据。
UPDATE biz_performance SET ancestors='0,2,2.1' WHERE perf_id=950001;
INSERT INTO biz_task (task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete) VALUES
(930006,1,930001,2026,'1.1.5','关联验收任务A',3,910001,910003,910002,920001,'1',10,5,50,'1',0),
(930007,1,930001,2027,'1.1.6','关联验收任务B',3,910001,910003,910002,920001,'1',10,0,0,'1',0),
(930008,1,930001,2026,'1.1.7','关联验收任务C',3,910001,910003,910002,920001,'0',10,0,0,'1',0);
-- 另一个归口部门下没有关联任务的自动指标，用于验证“筛选后隐藏无关联指标”
INSERT INTO biz_performance (perf_id,project_id,perf_code,perf_name,target_value,data_type,dept_id,principal_id,auditor_id,leader_id)
VALUES (950041,1,'1.1.8','审计无关联绩效',10,'1',920002,910002,910003,910004);
INSERT INTO biz_performance_year (year_id,perf_id,year,target_value) VALUES (950042,950041,2026,10);
UPDATE biz_performance SET ancestors='0,2,2.1' WHERE perf_id=950041;
