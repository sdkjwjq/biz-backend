SELECT task_id,task_code,is_priority,is_difficult,planned_completion_date,actual_completion_date,management_version FROM biz_task WHERE level=3 AND COALESCE(is_delete,0)=0 ORDER BY task_id;
SELECT COUNT(*) invalid_flags FROM biz_task WHERE is_priority NOT IN (0,1) OR is_difficult NOT IN (0,1);
