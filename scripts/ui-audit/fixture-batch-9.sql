-- 仅加载到全新 review 隔离库；不向真实用户发送通知。
INSERT INTO sys_notice
(notice_id, from_user_id, to_user_id, type, trigger_event, title, content, source_type, source_id, is_read, is_delete, create_time)
VALUES
(989101, 110228, 910001, '3', '绩效归档完成', '审计归档通知：绩效已完结归档', '请查看已归档绩效详情', '2', 972099, '0', 0, NOW()),
(989102, 910003, 910001, '3', 'TASK', '任务督办：请补充成果材料', '这是任务 930002 的督办通知，非成果审核单', '1', 930002, '0', 0, NOW());
