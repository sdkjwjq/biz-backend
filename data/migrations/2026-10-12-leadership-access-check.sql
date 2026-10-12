SELECT user_id,scope,version,update_by,update_time FROM sys_leadership_access ORDER BY user_id;
SELECT d.user_id,d.dept_id,s.dept_name FROM sys_leadership_department d LEFT JOIN sys_dept s ON s.dept_id=d.dept_id;
SELECT a.user_id,a.scope FROM sys_leadership_access a
WHERE a.scope NOT IN ('NONE','ALL','DEPARTMENTS')
   OR (a.scope='DEPARTMENTS' AND NOT EXISTS(SELECT 1 FROM sys_leadership_department d WHERE d.user_id=a.user_id));
SELECT COUNT(*) department_trend_rows FROM biz_department_trend;
