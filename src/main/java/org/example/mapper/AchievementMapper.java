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

    /**
     * 根据成果ID查询单条成果信息
     * @param achId 成果ID
     * @return 标志性成果对象
     */
    @Select("SELECT * FROM biz_achievement WHERE ach_id = #{achId} AND is_delete = 0")
    BizAchievement getAchievementById(Long achId);

    /**
     * 查询所有未删除的标志性成果列表
     * @return 标志性成果列表
     */
    @Select("SELECT * FROM biz_achievement WHERE is_delete = 0")
    List<BizAchievement> listAllAchievements();

    @Select("SELECT * FROM biz_achievement WHERE is_delete = 0 AND dept_id = #{deptId}")
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

    @Update("UPDATE biz_achievement_submission SET ach_id=#{achId}, submit_by=#{submitBy}, submit_time=#{submitTime}, " +
            "flow_status=#{flowStatus}, current_handler_id=#{currentHandlerId}, comment=#{comment}, is_delete=#{isDelete} " +
            "WHERE sub_id=#{subId}")
    void updateAchievementSubmission(BizAchievementSubmission submission);

    @Select("SELECT * FROM biz_achievement_submission WHERE sub_id = #{subId}")
    BizAchievementSubmission getAchievementSubmissionById(Long subId);

    @Select("SELECT * FROM biz_achievement_submission WHERE ach_id = #{achId} AND is_delete = 0 ORDER BY submit_time DESC, sub_id DESC")
    List<BizAchievementSubmission> getAchievementSubmissionsByAchId(Long achId);

    @Select("SELECT * FROM biz_achievement_submission WHERE current_handler_id = #{userId} AND is_delete = 0 AND flow_status = 10 ORDER BY submit_time DESC")
    List<BizAchievementSubmission> getTodoAchievementSubmissions(Long userId);

    @Select("SELECT * FROM biz_achievement_submission WHERE is_delete = 0 AND flow_status = 10 ORDER BY submit_time DESC")
    List<BizAchievementSubmission> getAllTodoAchievementSubmissions();

    @Select("SELECT * FROM biz_achievement_submission WHERE (submit_by = #{userId} OR current_handler_id = #{userId}) " +
            "AND is_delete = 0 AND (flow_status = 30 OR flow_status < 0) ORDER BY submit_time DESC")
    List<BizAchievementSubmission> getAchievementSubmissionRecords(Long userId);

    @Select("SELECT * FROM biz_achievement_submission WHERE is_delete = 0 AND (flow_status = 30 OR flow_status < 0) ORDER BY submit_time DESC")
    List<BizAchievementSubmission> getAllAchievementSubmissionRecords();

    @Select("SELECT s.sub_id AS subId, s.ach_id AS achId, a.category, a.level, a.ach_name AS achName, " +
            "a.department, a.got_time AS gotTime, a.dept_id AS deptId, a.file_id AS fileId, " +
            "a.comment AS achievementComment, s.submit_by AS submitBy, s.submit_time AS submitTime, " +
            "s.flow_status AS flowStatus, s.current_handler_id AS currentHandlerId, s.comment, s.is_delete AS isDelete " +
            "FROM biz_achievement_submission s JOIN biz_achievement a ON s.ach_id = a.ach_id " +
            "WHERE s.sub_id = #{subId} AND a.is_delete = 0")
    AchievementAuditVO getAchievementAuditVO(Long subId);

    @Insert("INSERT INTO biz_achievement_audit_log(sub_id, operator_id, action_type, pre_status, post_status, comment, create_time) " +
            "VALUES(#{subId}, #{operatorId}, #{actionType}, #{preStatus}, #{postStatus}, #{comment}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "logId", keyColumn = "log_id")
    void createAchievementAuditLog(BizAchievementAuditLog log);

    @Select("SELECT * FROM biz_achievement_audit_log WHERE sub_id = #{subId} ORDER BY create_time DESC, log_id DESC")
    List<BizAchievementAuditLog> getAchievementAuditLogs(Long subId);
}
