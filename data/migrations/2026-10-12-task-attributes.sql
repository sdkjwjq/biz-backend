-- 任务管理属性：仅补充缺失字段，重复执行不覆盖管理员调整。
SET @task_attr_sql=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='biz_task' AND column_name='is_priority')=0,'ALTER TABLE biz_task ADD COLUMN is_priority tinyint NOT NULL DEFAULT 0','SELECT 1');
PREPARE task_attr_stmt FROM @task_attr_sql; EXECUTE task_attr_stmt; DEALLOCATE PREPARE task_attr_stmt;
SET @task_attr_sql=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='biz_task' AND column_name='is_difficult')=0,'ALTER TABLE biz_task ADD COLUMN is_difficult tinyint NOT NULL DEFAULT 0','SELECT 1');
PREPARE task_attr_stmt FROM @task_attr_sql; EXECUTE task_attr_stmt; DEALLOCATE PREPARE task_attr_stmt;
SET @task_attr_sql=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='biz_task' AND column_name='planned_completion_date')=0,'ALTER TABLE biz_task ADD COLUMN planned_completion_date date NULL','SELECT 1');
PREPARE task_attr_stmt FROM @task_attr_sql; EXECUTE task_attr_stmt; DEALLOCATE PREPARE task_attr_stmt;
SET @task_attr_sql=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='biz_task' AND column_name='actual_completion_date')=0,'ALTER TABLE biz_task ADD COLUMN actual_completion_date datetime NULL','SELECT 1');
PREPARE task_attr_stmt FROM @task_attr_sql; EXECUTE task_attr_stmt; DEALLOCATE PREPARE task_attr_stmt;
SET @task_attr_sql=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='biz_task' AND column_name='management_version')=0,'ALTER TABLE biz_task ADD COLUMN management_version bigint NOT NULL DEFAULT 0','SELECT 1');
PREPARE task_attr_stmt FROM @task_attr_sql; EXECUTE task_attr_stmt; DEALLOCATE PREPARE task_attr_stmt;
UPDATE biz_task SET planned_completion_date=STR_TO_DATE(CONCAT(phase,'-12-31'),'%Y-%m-%d') WHERE level=3 AND phase BETWEEN 2025 AND 2029 AND planned_completion_date IS NULL AND management_version=0;
-- 仅可靠的三级达标归档日志用于历史回填；既有完成日期不覆盖。
UPDATE biz_task t JOIN (
 SELECT s.task_id,MIN(l.create_time) first_completed FROM biz_material_submission s
 JOIN biz_task p ON p.task_id=s.task_id AND p.level=3 AND p.target_value>0
 JOIN biz_audit_log l ON l.sub_id=s.sub_id AND l.post_status=40 AND l.create_time>=s.submit_time
 WHERE s.reported_value>=p.target_value
 AND NOT EXISTS (SELECT 1 FROM biz_audit_snapshot sn JOIN biz_material_submission old ON old.sub_id=sn.sub_id WHERE sn.target_type='TASK' AND sn.target_id=p.task_id AND sn.previous_status='3' AND old.submit_time<l.create_time)
 GROUP BY s.task_id
) evidence ON evidence.task_id=t.task_id SET t.actual_completion_date=evidence.first_completed WHERE t.actual_completion_date IS NULL;
