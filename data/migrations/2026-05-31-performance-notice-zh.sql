-- Normalize performance audit notifications after introducing the dedicated
-- performance audit flow. sys_notice.source_type is a short code column:
-- 0 = task, 1 = old/default business source, 2 = performance submission.

UPDATE sys_notice n
JOIN biz_performance_submission s ON s.sub_id = n.source_id
JOIN biz_performance p ON p.perf_id = s.perf_id
SET n.source_type = '2',
    n.trigger_event = '绩效审核',
    n.title = '绩效待审核',
    n.content = CONCAT('绩效「', p.perf_name, '」已提交，请进行专业群审核')
WHERE n.source_id = s.sub_id
  AND (
    n.trigger_event = 'performance audit'
    OR n.title = 'performance audit'
    OR n.content LIKE 'Performance submitted:%'
  );

UPDATE sys_notice n
JOIN biz_performance_submission s ON s.sub_id = n.source_id
JOIN biz_performance p ON p.perf_id = s.perf_id
SET n.source_type = '2',
    n.trigger_event = '绩效退回',
    n.title = '绩效已退回',
    n.content = CONCAT('绩效「', p.perf_name, '」已被退回，请查看处理意见')
WHERE n.source_id = s.sub_id
  AND (
    n.trigger_event = 'performance rejected'
    OR n.title = 'performance rejected'
    OR n.content LIKE 'Performance rejected:%'
  );

UPDATE sys_notice n
JOIN biz_performance_submission s ON s.sub_id = n.source_id
JOIN biz_performance p ON p.perf_id = s.perf_id
SET n.source_type = '2',
    n.trigger_event = '绩效归档',
    n.title = '绩效待完结归档',
    n.content = CONCAT('绩效「', p.perf_name, '」专业群审核已通过，请进行完结归档')
WHERE n.source_id = s.sub_id
  AND n.content LIKE 'Performance pending admin audit:%';

UPDATE sys_notice n
JOIN biz_performance_submission s ON s.sub_id = n.source_id
JOIN biz_performance p ON p.perf_id = s.perf_id
SET n.source_type = '2',
    n.trigger_event = '绩效归档完成',
    n.title = '绩效已完结归档',
    n.content = CONCAT('绩效「', p.perf_name, '」已完结归档')
WHERE n.source_id = s.sub_id
  AND (
    n.trigger_event = 'performance approved'
    OR n.title = 'performance approved'
    OR n.content LIKE 'Performance approved:%'
  );

-- Repair notifications that were already persisted as literal question marks
-- by a bad source-encoding run. Their order within the same submission matches
-- the flow: submit -> archive pending -> archive done.
UPDATE sys_notice n
JOIN (
  SELECT notice_id,
         ROW_NUMBER() OVER (PARTITION BY source_id ORDER BY notice_id) AS seq_no
  FROM sys_notice
  WHERE source_type = '2'
    AND (title LIKE '%?%' OR trigger_event LIKE '%?%' OR content LIKE '%?%')
) q ON q.notice_id = n.notice_id
JOIN biz_performance_submission s ON s.sub_id = n.source_id
JOIN biz_performance p ON p.perf_id = s.perf_id
SET n.source_type = '2',
    n.trigger_event = CASE
      WHEN q.seq_no = 1 THEN '绩效审核'
      WHEN q.seq_no = 2 THEN '绩效归档'
      ELSE '绩效归档完成'
    END,
    n.title = CASE
      WHEN q.seq_no = 1 THEN '绩效待审核'
      WHEN q.seq_no = 2 THEN '绩效待完结归档'
      ELSE '绩效已完结归档'
    END,
    n.content = CASE
      WHEN q.seq_no = 1 THEN CONCAT('绩效「', p.perf_name, '」已提交，请进行专业群审核')
      WHEN q.seq_no = 2 THEN CONCAT('绩效「', p.perf_name, '」专业群审核已通过，请进行完结归档')
      ELSE CONCAT('绩效「', p.perf_name, '」已完结归档')
    END;

-- Future-year performance submissions are cleared in the data cleanup migration.
-- Keep this compatible with both old source_type = 1 and new source_type = 2.
DELETE n
FROM sys_notice n
JOIN biz_performance_submission s ON s.sub_id = n.source_id
WHERE n.source_type IN ('1', '2')
  AND s.year >= 2027;
