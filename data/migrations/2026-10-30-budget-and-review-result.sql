-- 2026-10-30：预算三表与专家评审结果字段
-- 由《总预算11111.docx》生成，可安全用于全新初始化的数据库。

CREATE TABLE IF NOT EXISTS biz_budget_total (
  item VARCHAR(100) NOT NULL,
  source VARCHAR(50) NOT NULL,
  amount DECIMAL(18,2) NULL,
  ratio DECIMAL(8,2) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  PRIMARY KEY (item, source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='五年项目总预算（全系统一份）';

INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('合计','项目预算总额',36100.00,NULL,0) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('合计','中央财政投入资金',5000.00,13.85,0) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('合计','地方财政投入资金',5000.00,13.85,0) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('合计','举办方投入资金',0.00,0.00,0) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('合计','行业企业支持资金',16000.00,44.32,0) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('合计','学校自筹资金',10100.00,27.98,0) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('1 落实立德树人根本任务','项目预算总额',780.00,2.16,10) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('1 落实立德树人根本任务','中央财政投入资金',120.00,2.40,10) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('1 落实立德树人根本任务','地方财政投入资金',150.00,3.00,10) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('1 落实立德树人根本任务','举办方投入资金',0.00,0.00,10) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('1 落实立德树人根本任务','行业企业支持资金',100.00,0.63,10) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('1 落实立德树人根本任务','学校自筹资金',410.00,4.06,10) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('2 创新产教融合机制','项目预算总额',5920.00,16.40,20) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('2 创新产教融合机制','中央财政投入资金',240.00,4.80,20) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('2 创新产教融合机制','地方财政投入资金',480.00,9.60,20) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('2 创新产教融合机制','举办方投入资金',0.00,0.00,20) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('2 创新产教融合机制','行业企业支持资金',5000.00,31.25,20) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('2 创新产教融合机制','学校自筹资金',200.00,1.98,20) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('3 打造高水平专业群','项目预算总额',4500.00,12.47,30) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('3 打造高水平专业群','中央财政投入资金',770.00,15.40,30) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('3 打造高水平专业群','地方财政投入资金',1430.00,28.60,30) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('3 打造高水平专业群','举办方投入资金',0.00,0.00,30) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('3 打造高水平专业群','行业企业支持资金',0.00,0.00,30) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('3 打造高水平专业群','学校自筹资金',2300.00,22.77,30) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('4 建设一流核心课程','项目预算总额',940.00,2.60,40) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('4 建设一流核心课程','中央财政投入资金',290.00,5.80,40) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('4 建设一流核心课程','地方财政投入资金',280.00,5.60,40) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('4 建设一流核心课程','举办方投入资金',0.00,0.00,40) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('4 建设一流核心课程','行业企业支持资金',200.00,1.25,40) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('4 建设一流核心课程','学校自筹资金',170.00,1.68,40) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('5 开发优质新形态教材','项目预算总额',860.00,2.38,50) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('5 开发优质新形态教材','中央财政投入资金',120.00,2.40,50) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('5 开发优质新形态教材','地方财政投入资金',190.00,3.80,50) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('5 开发优质新形态教材','举办方投入资金',0.00,0.00,50) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('5 开发优质新形态教材','行业企业支持资金',100.00,0.63,50) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('5 开发优质新形态教材','学校自筹资金',450.00,4.46,50) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('6 建设高水平双师队伍','项目预算总额',4600.00,12.74,60) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('6 建设高水平双师队伍','中央财政投入资金',1050.00,21.00,60) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('6 建设高水平双师队伍','地方财政投入资金',520.00,10.40,60) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('6 建设高水平双师队伍','举办方投入资金',0.00,0.00,60) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('6 建设高水平双师队伍','行业企业支持资金',0.00,0.00,60) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('6 建设高水平双师队伍','学校自筹资金',3030.00,30.00,60) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('7 建设产教融合实训基地','项目预算总额',9900.00,27.42,70) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('7 建设产教融合实训基地','中央财政投入资金',1610.00,32.20,70) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('7 建设产教融合实训基地','地方财政投入资金',1010.00,20.20,70) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('7 建设产教融合实训基地','举办方投入资金',0.00,0.00,70) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('7 建设产教融合实训基地','行业企业支持资金',4600.00,28.75,70) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('7 建设产教融合实训基地','学校自筹资金',2680.00,26.53,70) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('8 构建数字化教学新生态','项目预算总额',4000.00,11.08,80) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('8 构建数字化教学新生态','中央财政投入资金',360.00,7.20,80) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('8 构建数字化教学新生态','地方财政投入资金',170.00,3.40,80) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('8 构建数字化教学新生态','举办方投入资金',0.00,0.00,80) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('8 构建数字化教学新生态','行业企业支持资金',3000.00,18.75,80) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('8 构建数字化教学新生态','学校自筹资金',470.00,4.65,80) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('9 拓展国际交流与合作','项目预算总额',4000.00,11.08,90) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('9 拓展国际交流与合作','中央财政投入资金',440.00,8.80,90) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('9 拓展国际交流与合作','地方财政投入资金',270.00,5.40,90) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('9 拓展国际交流与合作','举办方投入资金',0.00,0.00,90) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('9 拓展国际交流与合作','行业企业支持资金',3000.00,18.75,90) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('9 拓展国际交流与合作','学校自筹资金',290.00,2.87,90) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('10 打造一流区域型高端智库','项目预算总额',600.00,1.66,100) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('10 打造一流区域型高端智库','中央财政投入资金',0.00,0.00,100) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('10 打造一流区域型高端智库','地方财政投入资金',500.00,10.00,100) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('10 打造一流区域型高端智库','举办方投入资金',0.00,0.00,100) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('10 打造一流区域型高端智库','行业企业支持资金',0.00,0.00,100) ON DUPLICATE KEY UPDATE item=VALUES(item);
INSERT INTO biz_budget_total(item,source,amount,ratio,sort_order) VALUES('10 打造一流区域型高端智库','学校自筹资金',100.00,0.99,100) ON DUPLICATE KEY UPDATE item=VALUES(item);

SET @ddl=IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_expert_review' AND COLUMN_NAME='review_result')=0,'ALTER TABLE biz_expert_review ADD COLUMN review_result VARCHAR(16) NULL COMMENT \'pass/reject\'','SELECT 1');
PREPARE stmt FROM @ddl;EXECUTE stmt;DEALLOCATE PREPARE stmt;
SET @ddl=IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_expert_review_revision' AND COLUMN_NAME='review_result')=0,'ALTER TABLE biz_expert_review_revision ADD COLUMN review_result VARCHAR(16) NULL COMMENT \'pass/reject\'','SELECT 1');
PREPARE stmt FROM @ddl;EXECUTE stmt;DEALLOCATE PREPARE stmt;
