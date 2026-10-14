SELECT c.indicator_id,c.indicator_code,p.perf_name,p.target_value,p.data_type,p.principal_id FROM biz_core_indicator c LEFT JOIN biz_performance p ON BINARY p.perf_code=BINARY c.indicator_code AND COALESCE(p.is_delete,0)=0 WHERE c.indicator_id=10;
SELECT 'mapping_error',IF(COUNT(*)=1 AND SUM(c.indicator_code='1.2.45' AND p.perf_name='发布《工程机械产业发展报告》数量' AND p.data_type='1')=1,0,1) FROM biz_core_indicator c JOIN biz_performance p ON BINARY p.perf_code=BINARY c.indicator_code AND COALESCE(p.is_delete,0)=0 WHERE c.indicator_id=10;
SELECT 'missing_source',COUNT(*) FROM biz_core_report WHERE performance_code IS NULL;
SELECT 'obsolete_todo_notices',COUNT(*) FROM sys_notice n JOIN biz_core_report r ON r.report_id=n.source_id WHERE n.source_type='4' AND n.trigger_event='CORE_TODO' AND r.indicator_id=10 AND r.performance_code='1.2.15' AND COALESCE(n.is_delete,0)=0;
SELECT 'retained_old_reports',COUNT(*) FROM biz_core_report WHERE indicator_id=10 AND performance_code='1.2.15';
