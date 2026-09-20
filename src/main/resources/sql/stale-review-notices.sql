COALESCE(n.is_delete, 0) = 0 AND (
  (n.source_type = '0' AND n.trigger_event = '任务审核' AND n.title = '任务审核'
   AND NOT EXISTS (
     SELECT 1 FROM biz_material_submission s JOIN biz_task t ON t.task_id = s.task_id
     WHERE s.task_id = n.source_id AND s.current_handler_id = n.to_user_id
       AND s.flow_status IN (10, 20, 30) AND COALESCE(s.is_delete, 0) = 0
       AND COALESCE(t.is_delete, 0) = 0
   ))
  OR (n.source_type = '2'
   AND ((n.trigger_event = '绩效审核' AND n.title = '绩效待审核')
     OR (n.trigger_event = '绩效归档' AND n.title = '绩效待完结归档'))
   AND NOT EXISTS (
     SELECT 1 FROM biz_performance_submission s JOIN biz_performance p ON p.perf_id = s.perf_id
     WHERE s.sub_id = n.source_id AND s.current_handler_id = n.to_user_id
       AND s.flow_status IN (10, 20) AND COALESCE(s.is_delete, 0) = 0
       AND COALESCE(p.is_delete, 0) = 0
   ))
  OR (n.source_type = '3' AND n.trigger_event = '成果归档审核' AND n.title = '成果待归档审核'
   AND NOT EXISTS (
     SELECT 1 FROM biz_achievement_submission s JOIN biz_achievement a ON a.ach_id = s.ach_id
     WHERE s.sub_id = n.source_id AND s.current_handler_id = n.to_user_id
       AND s.flow_status = 10 AND COALESCE(s.is_delete, 0) = 0
       AND COALESCE(a.is_delete, 0) = 0
   ))
)
