-- 扩展业务审计数据，仅在 --fixture business 的隔离库中加载。
UPDATE biz_performance SET perf_code='2.1.1' WHERE perf_id=950011;
INSERT INTO biz_performance (perf_id,project_id,perf_code,perf_name,target_value,data_type,dept_id,principal_id,auditor_id,leader_id) VALUES
(950021,1,'2.1.2','审计零值绩效B',10,'1',920001,910002,910003,910001),
(950031,1,'2.1.3','审计待填绩效C',10,'1',920001,910002,910003,910001);
INSERT INTO biz_performance_year (year_id,perf_id,year,target_value,actual_value) VALUES
(950022,950021,2026,10,0),(950032,950031,2026,10,0);
INSERT INTO biz_performance_submission (sub_id,perf_id,year_id,year,actual_value,submit_by,submit_time,flow_status,current_handler_id,comment,is_delete)
VALUES (971002,950021,950022,2026,0,910001,NOW(),10,910003,'绩效B已明确填报零',0);
UPDATE biz_performance SET ancestors='0,2,2.1' WHERE perf_id IN (950011,950021,950031);
