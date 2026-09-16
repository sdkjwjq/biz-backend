-- Run after batch 10 seed, in the same disposable schema.
UPDATE biz_performance SET perf_code='1.1.1',ancestors='0,1,1.1' WHERE perf_id=950001;
INSERT INTO rel_task_performance (task_id,perf_id,year_id) VALUES
(931001,950001,950002),(931002,950001,950002);
