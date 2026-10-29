SELECT 'platform_notice_tables',COUNT(*) FROM information_schema.TABLES
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN('sys_platform_notice','sys_platform_notice_recipient','sys_platform_notice_view_log','sys_platform_notice_attachment');
SELECT 'platform_notice_orphan_recipients',COUNT(*) FROM sys_platform_notice_recipient r LEFT JOIN sys_platform_notice n ON n.notice_id=r.notice_id WHERE n.notice_id IS NULL;
SELECT 'platform_notice_invalid_confirmations',COUNT(*) FROM sys_platform_notice_recipient r JOIN sys_platform_notice n ON n.notice_id=r.notice_id WHERE r.confirmed_at IS NOT NULL AND (r.first_opened_at IS NULL OR r.confirmed_version<>n.content_version);
