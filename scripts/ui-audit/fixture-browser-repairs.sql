-- Only imported by serve.py into a newly created biz_review_test_<random> schema.
-- Distinct years make stale performance detail values observable.
UPDATE biz_performance_year SET actual_value=4 WHERE perf_id=950011 AND year=2027;
UPDATE biz_performance SET ancestors='0,2,2.1' WHERE perf_id=950011;
