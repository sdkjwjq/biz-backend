-- 仅用于随机隔离数据库的浏览便利性测试。
UPDATE biz_performance SET perf_code='1.1.1', ancestors='0,1,1.1' WHERE perf_id=950001;
INSERT INTO biz_achievement (ach_id,category,level,ach_name,department,got_time,dept_id,create_by,audit_status,comment,is_competition)
VALUES (979101,1,'省级','便利性合成成果','合成颁奖部门','2026-06-01',920001,1910001,30,'合成测试',0);
