-- 归属移交性能夹具：在基础夹具上补 130 条三级任务（对齐线上约 131 条的规模），
-- 用于测量进入/退出批量移交、单条勾选与整树级联勾选的耗时。需要压测更大规模时把下面的 130 调大即可。
-- 仅在 --fixture ownership-transfer-scale 的随机隔离库中加载，全部为合成数据。
INSERT INTO biz_task (task_id,project_id,parent_id,phase,task_code,task_name,level,leader_id,auditor_id,principal_id,dept_id,data_type,target_value,current_value,progress,status,is_delete)
SELECT 941000 + t.n, 1, 930001, 2026, CONCAT('1.1.', 100 + t.n), CONCAT('压测三级任务', t.n), 3,
       910001, 910003, 910002, 920001, '1', 10, 0, 0, '1', 0
FROM (SELECT a.d + b.d * 10 + c.d * 100 + 1 AS n FROM
  (SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
   UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a,
  (SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
   UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) b,
  (SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
   UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) c
) t WHERE t.n <= 130;
