-- Keep the newly added management dates while restoring original task timestamps.
UPDATE biz_task t JOIN _first_run_task_times original ON original.task_id = t.task_id
SET t.update_time = original.update_time
WHERE NOT (t.update_time <=> original.update_time);
DROP TABLE _first_run_task_times;
