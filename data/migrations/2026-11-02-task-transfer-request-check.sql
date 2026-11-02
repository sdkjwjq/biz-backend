SELECT 'missing_transfer_table' check_name,IF(EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_task_transfer_request'),0,1) errors;
SELECT 'duplicate_pending' check_name,COUNT(*) errors FROM (SELECT task_id,field_type FROM biz_task_transfer_request WHERE status='PENDING' GROUP BY task_id,field_type HAVING COUNT(*)>1) duplicates;
SELECT 'invalid_transfer_values' check_name,COUNT(*) errors FROM biz_task_transfer_request WHERE field_type NOT IN('leader','auditor','principal') OR status NOT IN('PENDING','ACCEPTED','REJECTED');
