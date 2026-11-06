package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import org.example.entity.BizAchievement;
import org.example.entity.BizAchievementAuditLog;
import org.example.entity.BizAchievementSubmission;
import org.example.entity.vo.AchievementAuditVO;

import java.util.List;

/**
 * 标志性成果数据访问接口
 * 对应表：biz_achievement
 */
@Mapper
public interface AchievementMapper {
    String REPORT_COLUMNS = "SELECT a.*, "
            + "CASE WHEN a.audit_status<0 THEN (SELECT l.comment FROM biz_achievement_audit_log l "
            + "JOIN biz_achievement_submission s ON s.sub_id=l.sub_id WHERE s.ach_id=a.ach_id AND s.is_delete=0 "
            + "AND l.post_status<0 ORDER BY l.create_time DESC,l.log_id DESC LIMIT 1) ELSE NULL END AS rejectionReason, "
            + "(SELECT MAX(s.submit_time) FROM biz_achievement_submission s WHERE s.ach_id=a.ach_id AND s.is_delete=0) AS submittedTime, "
            + "CASE WHEN a.audit_status=30 THEN (SELECT MAX(l.create_time) FROM biz_achievement_audit_log l "
            + "JOIN biz_achievement_submission s ON s.sub_id=l.sub_id WHERE s.ach_id=a.ach_id AND s.is_delete=0 "
            + "AND l.action_type='pass' AND l.post_status=30) ELSE NULL END AS archivedTime FROM biz_achievement a ";

    /**
     * 根据成果ID查询单条成果信息
     * @param achId 成果ID
     * @return 标志性成果对象
     */
    @Select(REPORT_COLUMNS + "WHERE a.ach_id = #{achId} AND a.is_delete = 0")
    BizAchievement getAchievementById(Long achId);

    @Select("SELECT * FROM biz_achievement WHERE ach_id=#{achId} AND is_delete=0 FOR UPDATE")
    BizAchievement getAchievementByIdForUpdate(Long achId);

    @Select("SELECT COUNT(*) FROM biz_achievement_submission WHERE ach_id=#{achId} AND is_delete=0 AND flow_status=10")
    long countPendingAchievementSubmissions(Long achId);

    /**
     * 查询所有未删除的标志性成果列表
     * @return 标志性成果列表
     */
    @Select(REPORT_COLUMNS + "WHERE a.is_delete = 0")
    List<BizAchievement> listAllAchievements();

    @Select(REPORT_COLUMNS + "WHERE a.is_delete = 0 AND a.dept_id = #{deptId}")
    List<BizAchievement> listAchievementsByDeptId(Long deptId);

    @Select("SELECT * FROM biz_achievement WHERE is_delete = 0 AND COALESCE(audit_status, 30) = 30")
    List<BizAchievement> listApprovedAchievements();


    /**
     * 查询最新的一个标志性成果
     * @return 最新的标志性成果对象
     */
    @Select("SELECT * FROM biz_achievement WHERE is_delete = 0 ORDER BY create_time DESC LIMIT 1")
    BizAchievement getLatestAchievement();

    /**
     * 新增标志性成果（自增主键返回）
     * @param achievement 标志性成果实体
     * @return 自增的成果ID
     */
    @Insert("INSERT INTO biz_achievement (category, level, ach_name, department, got_time, dept_id, file_id, " +
            "comment, is_competition, te_deng_jiang, yi_deng_jiang, er_deng_jiang, san_deng_jiang, jin_jiang, " +
            "yin_jiang, tong_jiang, you_sheng_jiang, bud_deng_deng_ci, create_by, audit_status, current_handler_id, " +
            "is_delete, create_time, update_time) " +
            "VALUES (#{category}, #{level}, #{achName}, #{department}, #{gotTime}, #{deptId}, #{fileId}, " +
            "#{comment}, #{isCompetition}, #{teDengJiang}, #{yiDengJiang}, #{erDengJiang}, #{sanDengJiang}, #{jinJiang}, " +
            "#{yinJiang}, #{tongJiang}, #{youShengJiang}, #{budDengDengCi}, #{createBy}, #{auditStatus}, #{currentHandlerId}, " +
            "#{isDelete}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "achId", keyColumn = "ach_id")
    void addAchievement(BizAchievement achievement);

    /**
     * 根据成果ID修改标志性成果信息
     * @param achievement 标志性成果实体（含需修改的成果ID）
     */
    @Update("UPDATE biz_achievement SET category = #{category}, level = #{level}, ach_name = #{achName}, " +
            "department = #{department}, got_time = #{gotTime}, dept_id = #{deptId}, file_id = #{fileId}, " +
            "comment = #{comment}, is_competition = #{isCompetition}, te_deng_jiang = #{teDengJiang}, " +
            "yi_deng_jiang = #{yiDengJiang}, er_deng_jiang = #{erDengJiang}, san_deng_jiang = #{sanDengJiang}, " +
            "jin_jiang = #{jinJiang}, yin_jiang = #{yinJiang}, tong_jiang = #{tongJiang}, you_sheng_jiang = #{youShengJiang}, " +
            "bud_deng_deng_ci = #{budDengDengCi}, create_by = #{createBy}, audit_status = #{auditStatus}, " +
            "current_handler_id = #{currentHandlerId}, update_time = #{updateTime} " +
            "WHERE ach_id = #{achId} AND is_delete = 0")
    void updateAchievement(BizAchievement achievement);

    @Update("UPDATE biz_achievement SET audit_status = #{auditStatus}, current_handler_id = #{currentHandlerId}, update_time = #{updateTime} " +
            "WHERE ach_id = #{achId} AND is_delete = 0")
    void updateAchievementAuditState(BizAchievement achievement);

    /**
     * 逻辑删除标志性成果（置is_delete=1）
     * @param achId 成果ID
     */
    @Update("UPDATE biz_achievement SET is_delete = 1 WHERE ach_id = #{achId}")
    void deleteAchievement(Long achId);

    @Insert("INSERT INTO biz_achievement_submission(ach_id, submit_by, submit_time, flow_status, current_handler_id, comment, is_delete) " +
            "VALUES(#{achId}, #{submitBy}, #{submitTime}, #{flowStatus}, #{currentHandlerId}, #{comment}, #{isDelete})")
    @Options(useGeneratedKeys = true, keyProperty = "subId", keyColumn = "sub_id")
    void createAchievementSubmission(BizAchievementSubmission submission);

    /** Freeze the submitted achievement fields after the submission row is created. */
    @Insert("INSERT INTO biz_achievement_submission_snapshot(sub_id,ach_id,category,level,ach_name,department,got_time,dept_id,file_id,achievement_comment,is_competition,te_deng_jiang,yi_deng_jiang,er_deng_jiang,san_deng_jiang,jin_jiang,yin_jiang,tong_jiang,you_sheng_jiang,bud_deng_deng_ci) " +
            "SELECT #{subId},ach_id,category,level,ach_name,department,got_time,dept_id,file_id,comment,is_competition,te_deng_jiang,yi_deng_jiang,er_deng_jiang,san_deng_jiang,jin_jiang,yin_jiang,tong_jiang,you_sheng_jiang,bud_deng_deng_ci FROM biz_achievement WHERE ach_id=#{achId} AND is_delete=0")
    int snapshotAchievementSubmission(@org.apache.ibatis.annotations.Param("subId") Long subId,
                                      @org.apache.ibatis.annotations.Param("achId") Long achId);

    @Update("UPDATE biz_achievement_submission SET ach_id=#{achId}, submit_by=#{submitBy}, submit_time=#{submitTime}, " +
            "flow_status=#{flowStatus}, current_handler_id=#{currentHandlerId}, comment=#{comment}, is_delete=#{isDelete} " +
            "WHERE sub_id=#{subId}")
    void updateAchievementSubmission(BizAchievementSubmission submission);

    @Select("SELECT * FROM biz_achievement_submission WHERE sub_id = #{subId}")
    BizAchievementSubmission getAchievementSubmissionById(Long subId);

    @Select("SELECT * FROM biz_achievement_submission WHERE sub_id = #{subId} FOR UPDATE")
    BizAchievementSubmission getAchievementSubmissionByIdForUpdate(Long subId);

    @Select("SELECT * FROM biz_achievement_submission WHERE ach_id = #{achId} AND is_delete = 0 ORDER BY submit_time DESC, sub_id DESC")
    List<BizAchievementSubmission> getAchievementSubmissionsByAchId(Long achId);

    @Select("SELECT s.* FROM biz_achievement_submission s JOIN biz_achievement a ON a.ach_id=s.ach_id AND a.is_delete=0 "
            + "WHERE s.current_handler_id = #{userId} AND s.is_delete = 0 AND s.flow_status = 10 ORDER BY s.submit_time DESC")
    List<BizAchievementSubmission> getTodoAchievementSubmissions(Long userId);

    @Select("SELECT s.* FROM biz_achievement_submission s JOIN biz_achievement a ON a.ach_id=s.ach_id AND a.is_delete=0 "
            + "WHERE s.is_delete = 0 AND s.flow_status = 10 ORDER BY s.submit_time DESC")
    List<BizAchievementSubmission> getAllTodoAchievementSubmissions();

    @Select("SELECT COUNT(*) FROM biz_achievement_submission s JOIN biz_achievement a ON a.ach_id=s.ach_id AND a.is_delete=0 WHERE s.is_delete=0 AND s.flow_status=10")
    long countAllTodoAchievementSubmissions();

    @Select("SELECT s.ach_id objectId,COUNT(*) amount FROM biz_achievement_submission s JOIN biz_achievement a ON a.ach_id=s.ach_id AND a.is_delete=0 WHERE s.is_delete=0 AND s.flow_status=10 GROUP BY s.ach_id")
    List<java.util.Map<String,Object>> todoAchievementCountGroups();

    @Select("SELECT s.ach_id objectId,COUNT(*) amount FROM biz_achievement_submission s JOIN biz_achievement a ON a.ach_id=s.ach_id AND a.is_delete=0 WHERE s.is_delete=0 AND s.flow_status=10 AND s.current_handler_id=#{userId} GROUP BY s.ach_id")
    List<java.util.Map<String,Object>> personalTodoAchievementCountGroups(Long userId);



    @Select("SELECT * FROM biz_achievement_submission WHERE (submit_by = #{userId} OR current_handler_id = #{userId}) " +
            "AND is_delete = 0 AND (flow_status = 30 OR flow_status < 0) ORDER BY submit_time DESC")
    List<BizAchievementSubmission> getAchievementSubmissionRecords(Long userId);

    @Select("SELECT * FROM biz_achievement_submission WHERE is_delete = 0 AND (flow_status = 30 OR flow_status < 0) ORDER BY submit_time DESC")
    List<BizAchievementSubmission> getAllAchievementSubmissionRecords();

    String AUDIT_DETAIL_SELECT = "SELECT s.sub_id AS subId, s.ach_id AS achId, " +
            "CASE WHEN d.sub_id IS NOT NULL THEN d.category WHEN s.flow_status=10 THEN a.category END AS category, " +
            "CASE WHEN d.sub_id IS NOT NULL THEN d.level WHEN s.flow_status=10 THEN a.level END AS level, " +
            "CASE WHEN d.sub_id IS NOT NULL THEN d.ach_name WHEN s.flow_status=10 THEN a.ach_name ELSE '历史名称未留存' END AS achName, " +
            "CASE WHEN d.sub_id IS NOT NULL THEN d.department WHEN s.flow_status=10 THEN a.department END AS department, " +
            "CASE WHEN d.sub_id IS NOT NULL THEN d.got_time WHEN s.flow_status=10 THEN a.got_time END AS gotTime, " +
            "CASE WHEN d.sub_id IS NOT NULL THEN d.dept_id WHEN s.flow_status=10 THEN a.dept_id END AS deptId, " +
            "CASE WHEN d.sub_id IS NOT NULL THEN d.file_id WHEN s.flow_status=10 THEN a.file_id END AS fileId, " +
            "CASE WHEN d.sub_id IS NOT NULL THEN d.achievement_comment WHEN s.flow_status=10 THEN a.comment " +
            "ELSE (SELECT l.comment FROM biz_achievement_audit_log l WHERE l.sub_id=s.sub_id AND l.action_type='submit' AND l.pre_status=0 AND l.post_status=10 ORDER BY l.log_id DESC LIMIT 1) END AS achievementComment, " +
            "(d.sub_id IS NOT NULL) AS historicalDetailsAvailable, " +
            "(d.sub_id IS NOT NULL OR EXISTS(SELECT 1 FROM biz_achievement_audit_log l WHERE l.sub_id=s.sub_id AND l.action_type='submit' AND l.pre_status=0 AND l.post_status=10)) AS historicalCommentAvailable, " +
            "s.submit_by AS submitBy, s.submit_time AS submitTime, " +
            "s.flow_status AS flowStatus, s.current_handler_id AS currentHandlerId, s.comment, s.is_delete AS isDelete " +
            "FROM biz_achievement_submission s JOIN biz_achievement a ON s.ach_id=a.ach_id " +
            "LEFT JOIN biz_achievement_submission_snapshot d ON d.sub_id=s.sub_id ";

    @Select(AUDIT_DETAIL_SELECT + "WHERE s.sub_id = #{subId}")
    AchievementAuditVO getAchievementAuditVO(Long subId);

    @Select("<script>" + AUDIT_DETAIL_SELECT + "WHERE s.sub_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<AchievementAuditVO> getAchievementAuditVOs(@org.apache.ibatis.annotations.Param("ids") List<Long> ids);

    @Insert("INSERT INTO biz_achievement_audit_log(sub_id, operator_id, action_type, pre_status, post_status, comment, create_time) " +
            "VALUES(#{subId}, #{operatorId}, #{actionType}, #{preStatus}, #{postStatus}, #{comment}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "logId", keyColumn = "log_id")
    void createAchievementAuditLog(BizAchievementAuditLog log);

    @Select("SELECT * FROM biz_achievement_audit_log WHERE sub_id = #{subId} ORDER BY create_time DESC, log_id DESC")
    List<BizAchievementAuditLog> getAchievementAuditLogs(Long subId);
}
