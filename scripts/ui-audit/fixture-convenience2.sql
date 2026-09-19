-- 只用于随机隔离库：当前退回、历史退回后已重提、跨年度和不同创建人。
INSERT INTO sys_file (file_id,file_name,file_path,file_url,file_suffix,upload_by) VALUES (940001,'fixture.pdf','fixture.pdf','/uploads/fixture.pdf','pdf',910001);
INSERT INTO biz_material_submission (sub_id,task_id,submit_by,submit_time,flow_status,current_handler_id,is_delete,file_id,submit_dept_id,manage_dept_id,file_suffix) VALUES
(972001,930003,910001,'2026-09-01',-10,910001,1,940001,920001,920001,'pdf'),
(972002,930012,910001,'2026-09-01',-10,910001,1,940001,920001,920001,'pdf'),
(972003,930012,910001,'2026-09-02',10,910003,0,940001,920001,920001,'pdf');
INSERT INTO biz_performance_submission (sub_id,perf_id,year_id,year,actual_value,submit_by,submit_time,flow_status,current_handler_id,is_delete) VALUES
(972101,950031,950032,2026,1,910001,'2026-09-01',-10,910001,0),
(972102,950011,950013,2027,1,910001,'2026-09-01',-10,910001,0);
INSERT INTO biz_achievement (ach_id,category,level,ach_name,department,got_time,dept_id,create_by,audit_status,comment,is_competition) VALUES
(979102,2,'省级','便利性退回成果B','合成部门','2026-06-02',920001,1910001,-10,'合成退回',0),
(979103,1,'省级','便利性他人成果C','合成部门','2026-06-03',920001,910001,30,'合成其他创建人',0);
