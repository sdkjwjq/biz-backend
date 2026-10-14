-- 第十项修正为发布产业发展报告；不修改 biz_performance，不改原填报值、名称及审核日志。
SET @core_code_ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='biz_core_report' AND column_name='performance_code')=0,'ALTER TABLE biz_core_report ADD COLUMN performance_code varchar(30) NULL','SELECT 1');
PREPARE core_code_stmt FROM @core_code_ddl; EXECUTE core_code_stmt; DEALLOCATE PREPARE core_code_stmt;

-- 拒绝覆盖未知配置；缺失绩效由应用提示，并由发布核对脚本阻止上线。
CREATE TEMPORARY TABLE core_mapping_guard(ok int NOT NULL CHECK(ok=1));
INSERT INTO core_mapping_guard SELECT IF(COUNT(*)=1 AND SUM(indicator_code IN('1.2.15','1.2.45'))=1,1,0) FROM biz_core_indicator WHERE indicator_id=10;
INSERT INTO core_mapping_guard SELECT IF(COUNT(*)=0 OR (COUNT(*)=1 AND SUM(perf_name='发布《工程机械产业发展报告》数量' AND data_type='1')=1),1,0) FROM biz_performance WHERE perf_code='1.2.45' AND COALESCE(is_delete,0)=0;
DROP TEMPORARY TABLE core_mapping_guard;

CREATE TABLE IF NOT EXISTS bak_20261014_core_mapping AS SELECT * FROM biz_core_indicator WHERE indicator_id=10;
CREATE TABLE IF NOT EXISTS bak_20261014_core_report_source AS SELECT report_id,performance_code,draft_key FROM biz_core_report;
CREATE TABLE IF NOT EXISTS bak_20261014_core_notice AS SELECT n.notice_id,n.is_delete FROM sys_notice n JOIN biz_core_report r ON r.report_id=n.source_id WHERE n.source_type='4' AND n.trigger_event='CORE_TODO' AND r.indicator_id=10;

START TRANSACTION;
-- 先固定原关联来源，再修正映射；旧就业数据仍留在原记录，不能计入报告数量。
UPDATE biz_core_report r JOIN biz_core_indicator c ON c.indicator_id=r.indicator_id SET r.performance_code=c.indicator_code WHERE r.performance_code IS NULL;
UPDATE biz_core_indicator SET indicator_code='1.2.45' WHERE indicator_id=10 AND indicator_code='1.2.15';
-- 释放旧草稿占位，保留旧草稿内容，允许创建正确指标的新草稿。
UPDATE biz_core_report SET draft_key=NULL WHERE indicator_id=10 AND performance_code='1.2.15' AND draft_key IS NOT NULL;
UPDATE sys_notice n JOIN biz_core_report r ON r.report_id=n.source_id SET n.is_delete=1 WHERE n.source_type='4' AND n.trigger_event='CORE_TODO' AND r.indicator_id=10 AND r.performance_code='1.2.15' AND COALESCE(n.is_delete,0)=0;
COMMIT;
