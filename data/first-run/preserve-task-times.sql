-- First-run only: schema backfills must not replace the snapshot's update_time.
-- The original task table updates this column automatically on any UPDATE.
CREATE TABLE _first_run_task_times AS SELECT task_id, update_time FROM biz_task;
