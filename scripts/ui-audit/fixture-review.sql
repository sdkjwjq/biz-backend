-- 第七批：15 条待审绩效与 1 条已归档绩效，仅用于随机隔离库。
CREATE TEMPORARY TABLE review_numbers (n INT PRIMARY KEY);
INSERT INTO review_numbers VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13);
INSERT INTO biz_performance (perf_id,project_id,perf_code,perf_name,target_value,data_type,dept_id,principal_id,auditor_id,leader_id,ancestors)
SELECT 951000+n,1,CONCAT('2.1.',100+n),CONCAT('分页待审绩效',LPAD(n,2,'0')),10,'1',920001,910002,910003,910001,'0,2,2.1' FROM review_numbers;
INSERT INTO biz_performance_year (year_id,perf_id,year,target_value,actual_value)
SELECT 952000+n,951000+n,2026,10,1 FROM review_numbers;
INSERT INTO biz_performance_submission (sub_id,perf_id,year_id,year,actual_value,submit_by,submit_time,flow_status,current_handler_id,comment,is_delete)
SELECT 972000+n,951000+n,952000+n,2026,1,910001,NOW(),10,910003,'分页验证样本',0 FROM review_numbers;
UPDATE biz_performance SET current_value=1 WHERE perf_id BETWEEN 951001 AND 951013;
DROP TEMPORARY TABLE review_numbers;
INSERT INTO biz_performance (perf_id,project_id,perf_code,perf_name,target_value,current_value,data_type,dept_id,principal_id,auditor_id,leader_id,ancestors)
VALUES (951099,1,'2.1.199','已归档历史绩效',10,2,'1',920001,910002,910003,910001,'0,2,2.1');
INSERT INTO biz_performance_year (year_id,perf_id,year,target_value,actual_value) VALUES (952099,951099,2026,10,2);
INSERT INTO biz_performance_submission (sub_id,perf_id,year_id,year,actual_value,submit_by,submit_time,flow_status,current_handler_id,comment,is_delete)
VALUES (972099,951099,952099,2026,2,910001,NOW(),30,110228,'归档验证样本',0);
INSERT INTO biz_performance_audit_log (sub_id,operator_id,action_type,pre_status,post_status,comment,create_time)
VALUES (972099,910003,'pass',10,20,'专业群已通过',NOW()),(972099,110228,'pass',20,30,'已归档',NOW());
