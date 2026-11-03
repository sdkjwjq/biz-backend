-- 校验：平台通知的收件箱已读状态应与接收人确认记录一致。
-- 期望 remaining_drift 为 0；confirmed_unread 为 0。

SELECT COUNT(*) AS remaining_drift
FROM sys_notice m
JOIN sys_platform_notice_recipient r
  ON r.notice_id = m.source_id AND r.user_id = m.to_user_id
WHERE m.source_type = '5'
  AND r.confirmed_at IS NOT NULL
  AND (m.is_read IS NULL OR m.is_read <> '1');

SELECT COUNT(*) AS confirmed_unread
FROM sys_notice m
JOIN sys_platform_notice_recipient r
  ON r.inbox_id = m.notice_id AND r.user_id = m.to_user_id
WHERE r.confirmed_at IS NOT NULL
  AND (m.is_read IS NULL OR m.is_read <> '1');

SELECT COUNT(*) AS platform_inbox_total,
       SUM(CASE WHEN m.is_read = '1' THEN 1 ELSE 0 END) AS platform_inbox_read
FROM sys_notice m
WHERE m.source_type = '5' AND COALESCE(m.is_delete, 0) = 0;
