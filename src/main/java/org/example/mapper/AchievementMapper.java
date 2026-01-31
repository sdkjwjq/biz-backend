package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import org.example.entity.BizAchievement;

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
            "yin_jiang, tong_jiang, you_sheng_jiang, bud_deng_deng_ci, create_by, is_delete, create_time) " +
            "VALUES (#{category}, #{level}, #{achName}, #{department}, #{gotTime}, #{deptId}, #{fileId}, " +
            "#{comment}, #{isCompetition}, #{teDengJiang}, #{yiDengJiang}, #{erDengJiang}, #{sanDengJiang}, #{jinJiang}, " +
            "#{yinJiang}, #{tongJiang}, #{youShengJiang}, #{budDengDengCi}, #{createBy}, #{isDelete}, #{createTime})")
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
            "bud_deng_deng_ci = #{budDengDengCi}, create_by = #{createBy} " +
            "WHERE ach_id = #{achId} AND is_delete = 0")
    void updateAchievement(BizAchievement achievement);

    /**
     * 逻辑删除标志性成果（置is_delete=1）
     * @param achId 成果ID
     */
    @Update("UPDATE biz_achievement SET is_delete = 1 WHERE ach_id = #{achId}")
    void deleteAchievement(Long achId);
}