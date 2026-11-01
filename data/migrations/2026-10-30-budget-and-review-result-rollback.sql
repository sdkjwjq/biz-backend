-- Only remove empty structures. Existing totals, review results and formal history must first be backed up and handled explicitly.
DROP PROCEDURE IF EXISTS rollback_budget_review_20261030;
DELIMITER $$
CREATE PROCEDURE rollback_budget_review_20261030()
BEGIN
 IF EXISTS(SELECT 1 FROM biz_budget_total) OR EXISTS(SELECT 1 FROM biz_expert_review WHERE COALESCE(review_result,'')<>'') OR EXISTS(SELECT 1 FROM biz_expert_review_revision WHERE COALESCE(review_result,'')<>'') THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Rollback stopped: budget or review result data exists; preserve it before rollback';
 END IF;
 ALTER TABLE biz_expert_review DROP COLUMN review_result;
 ALTER TABLE biz_expert_review_revision DROP COLUMN review_result;
 DROP TABLE biz_budget_total;
END$$
DELIMITER ;
CALL rollback_budget_review_20261030();
DROP PROCEDURE rollback_budget_review_20261030;
