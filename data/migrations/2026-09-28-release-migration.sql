-- Records one-time production data migrations so a later application update cannot
-- replay historical corrections over newer administrator changes.
CREATE TABLE IF NOT EXISTS `sys_release_migration` (
  `migration_key` varchar(100) NOT NULL COMMENT 'Stable migration identifier',
  `description` varchar(500) DEFAULT NULL COMMENT 'Migration summary',
  `applied_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Successful application time',
  PRIMARY KEY (`migration_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Release data migration history';
