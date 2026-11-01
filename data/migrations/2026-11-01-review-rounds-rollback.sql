-- Never delete rounds, assignments, drafts or formal evidence during rollback.
DROP PROCEDURE IF EXISTS rollback_review_rounds_20261101;
DELIMITER $$
CREATE PROCEDURE rollback_review_rounds_20261101()
BEGIN
 IF EXISTS(SELECT 1 FROM biz_review_round) OR EXISTS(SELECT 1 FROM biz_review_member) OR EXISTS(SELECT 1 FROM biz_review_scope)
 OR EXISTS(SELECT 1 FROM biz_round_opinion) OR EXISTS(SELECT 1 FROM biz_round_revision) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Review rollback stopped: new business data exists; back it up and migrate it before rollback';
 END IF;
 DROP TABLE biz_round_revision;
 DROP TABLE biz_round_opinion;
 DROP TABLE biz_review_scope;
 DROP TABLE biz_review_member;
 DROP TABLE biz_review_round;
END$$
DELIMITER ;
CALL rollback_review_rounds_20261101();
DROP PROCEDURE rollback_review_rounds_20261101;
