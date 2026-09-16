-- Requires batch 11, only loaded into a guarded disposable schema.
UPDATE biz_performance SET perf_code='1.1.1',ancestors='0,1,1.1' WHERE perf_id=950001;
INSERT INTO rel_task_performance (task_id,perf_id,year_id) VALUES
(932001,950001,950002),(932002,950001,950002);
INSERT INTO biz_performance
(perf_id,project_id,perf_code,perf_name,target_value,current_value,data_type,dept_id,principal_id,auditor_id,leader_id,ancestors)
VALUES (950041,1,'2.1.4','审计待填绩效D',20,0,'1',920001,910002,910003,910001,'0,2,2.1');
INSERT INTO biz_performance_year (year_id,perf_id,year,target_value,actual_value)
VALUES (950042,950041,2026,20,0);
