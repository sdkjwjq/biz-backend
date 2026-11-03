-- 平台通知：把“接收人已确认”的记录同步为收件箱已读。
-- 背景：确认接口遇到“已确认”会提前返回，不会回写 sys_notice.is_read，
-- 导致消息中心一直显示未阅读、点“已阅”也不变化（确认状态与已读状态不一致）。
-- 本脚本只修数据，不改表结构，可重复执行。

UPDATE sys_notice m
JOIN sys_platform_notice_recipient r
  ON r.notice_id = m.source_id AND r.user_id = m.to_user_id
SET m.is_read = '1'
WHERE m.source_type = '5'
  AND r.confirmed_at IS NOT NULL
  AND (m.is_read IS NULL OR m.is_read <> '1');

UPDATE sys_notice m
JOIN sys_platform_notice_recipient r
  ON r.inbox_id = m.notice_id AND r.user_id = m.to_user_id
SET m.is_read = '1'
WHERE r.confirmed_at IS NOT NULL
  AND (m.is_read IS NULL OR m.is_read <> '1');
