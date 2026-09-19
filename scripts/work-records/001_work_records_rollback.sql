-- 先回滚应用并备份纪实数据，再人工执行本脚本。删除后无法保留已填纪实。
DROP TABLE biz_work_record_snapshot;
DROP TABLE biz_work_record_entry;
DROP TABLE biz_work_record;
