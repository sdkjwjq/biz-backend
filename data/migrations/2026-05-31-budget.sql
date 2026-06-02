-- 预算模块：Excel导入、结构化保存、锁定状态

CREATE TABLE IF NOT EXISTS `biz_budget_sheet` (
    `sheet_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '预算表ID',
    `year` int(4) NOT NULL COMMENT '年度',
    `month` int(2) NOT NULL COMMENT '月份',
    `locked` tinyint(1) NOT NULL DEFAULT 1 COMMENT '0未锁定 1已锁定',
    `file_id` bigint(20) DEFAULT NULL COMMENT '原始Excel文件ID',
    `import_by` bigint(20) DEFAULT NULL COMMENT '导入人ID',
    `import_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '导入时间',
    `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` tinyint(1) DEFAULT 0 COMMENT '0存在 1删除',
    PRIMARY KEY (`sheet_id`),
    UNIQUE KEY `uk_budget_sheet_year_month` (`year`, `month`, `is_delete`),
    KEY `idx_budget_sheet_file` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预算导入主表';

CREATE TABLE IF NOT EXISTS `biz_budget_source_item` (
    `item_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '资金来源明细ID',
    `sheet_id` bigint(20) NOT NULL COMMENT '预算表ID',
    `source_key` varchar(50) DEFAULT NULL COMMENT '资金来源标识',
    `source_name` varchar(100) NOT NULL COMMENT '资金来源名称',
    `display_order` int(11) DEFAULT 0 COMMENT '展示顺序',
    `five_year_total` decimal(20,4) DEFAULT 0.0000 COMMENT '五年项目总预算',
    `available` decimal(20,4) DEFAULT 0.0000 COMMENT '年度可使用资金',
    `annual_plan` decimal(20,4) DEFAULT 0.0000 COMMENT '年度预算安排',
    `carryover` decimal(20,4) DEFAULT 0.0000 COMMENT '上年结转结余资金',
    `arrived` decimal(20,4) DEFAULT 0.0000 COMMENT '到位数',
    PRIMARY KEY (`item_id`),
    KEY `idx_budget_source_sheet` (`sheet_id`),
    CONSTRAINT `fk_budget_source_sheet` FOREIGN KEY (`sheet_id`) REFERENCES `biz_budget_sheet` (`sheet_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预算资金来源明细';

CREATE TABLE IF NOT EXISTS `biz_budget_task_item` (
    `item_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务预算明细ID',
    `sheet_id` bigint(20) NOT NULL COMMENT '预算表ID',
    `task_index` int(11) DEFAULT NULL COMMENT '改革任务序号',
    `task_name` varchar(255) DEFAULT NULL COMMENT '改革任务名称',
    `amount` decimal(20,4) DEFAULT 0.0000 COMMENT '金额',
    `goods` decimal(20,4) DEFAULT 0.0000 COMMENT '商品和服务支出',
    `capital` decimal(20,4) DEFAULT 0.0000 COMMENT '资本性支出',
    `other` decimal(20,4) DEFAULT 0.0000 COMMENT '其他支出',
    `core_output` varchar(500) DEFAULT NULL COMMENT '核心产出指标名称',
    `target_value` varchar(255) DEFAULT NULL COMMENT '项目期满目标值',
    `achieved` varchar(1000) DEFAULT NULL COMMENT '目标累计实现情况',
    PRIMARY KEY (`item_id`),
    KEY `idx_budget_task_sheet` (`sheet_id`),
    CONSTRAINT `fk_budget_task_sheet` FOREIGN KEY (`sheet_id`) REFERENCES `biz_budget_sheet` (`sheet_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预算改革任务明细';
