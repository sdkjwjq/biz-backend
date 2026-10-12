SELECT c.indicator_id,c.indicator_code,COUNT(p.perf_id) matching_performances FROM biz_core_indicator c LEFT JOIN biz_performance p ON p.perf_code=c.indicator_code AND COALESCE(p.is_delete,0)=0 GROUP BY c.indicator_id,c.indicator_code ORDER BY c.indicator_id;
SELECT indicator_id,record_year,record_month,COUNT(*) active_count FROM biz_core_report WHERE flow_status IN(10,20) GROUP BY indicator_id,record_year,record_month HAVING COUNT(*)>1;
SELECT flow_status,COUNT(*) FROM biz_core_report GROUP BY flow_status;
