package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.entity.BizPerformanceRelationLog;
import org.example.entity.vo.PerformanceRelationCandidateVO;
import org.example.entity.vo.PerformanceRelationLogVO;

import java.util.List;
import java.util.Map;

/** 绩效关联任务编辑：候选查询、关联增删（软删除）与变更日志。 */
@Mapper
public interface PerformanceRelationMapper {

    /** 可关联的任务：三级、未删除、数据类型不为 0、年度一致；已关联的任务始终返回以便解除。 */
    @Select("SELECT t.task_id AS taskId, t.task_code AS taskCode, t.task_name AS taskName, t.phase AS phase, "
            + "t.level AS level, t.data_type AS dataType, COALESCE(t.is_delete,0) AS taskDeleted, "
            + "COALESCE(d.dept_name,'') AS deptName, COALESCE(NULLIF(u.nick_name,''), u.user_name, '') AS leaderName, "
            + "CASE WHEN EXISTS(SELECT 1 FROM rel_task_performance r WHERE r.task_id = t.task_id "
            + "AND r.perf_id = #{perfId} AND r.year_id = #{yearId} AND COALESCE(r.is_delete,0) = 0) THEN 1 ELSE 0 END AS linked "
            + "FROM biz_task t "
            + "LEFT JOIN sys_dept d ON d.dept_id = t.dept_id "
            + "LEFT JOIN sys_user u ON u.user_id = t.leader_id "
            + "WHERE (COALESCE(t.is_delete,0) = 0 AND t.level = 3 AND COALESCE(t.data_type,'1') <> '0' AND t.phase = #{year}) "
            + "OR EXISTS(SELECT 1 FROM rel_task_performance r2 WHERE r2.task_id = t.task_id "
            + "AND r2.perf_id = #{perfId} AND r2.year_id = #{yearId} AND COALESCE(r2.is_delete,0) = 0) "
            + "ORDER BY linked DESC, t.task_code")
    List<PerformanceRelationCandidateVO> getCandidates(@Param("perfId") Long perfId, @Param("yearId") Long yearId,
                                                       @Param("year") Integer year);

    @Select("SELECT id FROM rel_task_performance WHERE task_id = #{taskId} AND perf_id = #{perfId} "
            + "AND year_id = #{yearId} ORDER BY id LIMIT 1")
    Long findRelationId(@Param("taskId") Long taskId, @Param("perfId") Long perfId, @Param("yearId") Long yearId);

    /** 该指标该年度存在审核中（10/20）单据时禁止编辑关联。 */
    @Select("SELECT COUNT(*) FROM biz_performance_submission WHERE perf_id = #{perfId} AND year = #{year} "
            + "AND COALESCE(is_delete,0) = 0 AND flow_status IN (10, 20)")
    int countActiveAudits(@Param("perfId") Long perfId, @Param("year") Integer year);

    /** 当前年度每个指标的关联任务数（仅统计未解除关联）。 */
    @Select("SELECT r.perf_id AS perfId, COUNT(*) AS relationCount FROM rel_task_performance r "
            + "JOIN biz_performance_year y ON y.year_id = r.year_id "
            + "WHERE y.year = #{year} AND COALESCE(r.is_delete,0) = 0 AND COALESCE(y.is_delete,0) = 0 "
            + "GROUP BY r.perf_id")
    List<Map<String, Object>> countRelationByYear(@Param("year") Integer year);

    @Select("SELECT COALESCE(is_delete,0) FROM rel_task_performance WHERE id = #{id}")
    Integer getRelationDeleted(@Param("id") Long id);

    @Insert("INSERT INTO rel_task_performance(task_id, perf_id, year_id, weight, contribution_value, data_type, is_delete) "
            + "VALUES(#{taskId}, #{perfId}, #{yearId}, 1.00, 0.0000, #{dataType}, 0)")
    int insertRelation(@Param("taskId") Long taskId, @Param("perfId") Long perfId, @Param("yearId") Long yearId,
                       @Param("dataType") String dataType);

    @Update("UPDATE rel_task_performance SET is_delete = #{isDelete} WHERE id = #{id}")
    int updateRelationDeleted(@Param("id") Long id, @Param("isDelete") Integer isDelete);

    @Insert("INSERT INTO biz_performance_relation_log(perf_id, perf_code, perf_name, year, action, task_id, task_code, "
            + "task_name, batch_id, operator_id, reason, create_time) VALUES(#{perfId}, #{perfCode}, #{perfName}, #{year}, "
            + "#{action}, #{taskId}, #{taskCode}, #{taskName}, #{batchId}, #{operatorId}, #{reason}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "logId", keyColumn = "log_id")
    void insertLog(BizPerformanceRelationLog log);

    @Select("SELECT l.log_id AS logId, l.perf_id AS perfId, l.perf_code AS perfCode, l.perf_name AS perfName, "
            + "l.year AS year, l.action AS action, l.task_id AS taskId, l.task_code AS taskCode, l.task_name AS taskName, "
            + "l.batch_id AS batchId, l.operator_id AS operatorId, "
            + "COALESCE(NULLIF(u.nick_name,''), u.user_name, '') AS operatorName, l.reason AS reason, "
            + "l.create_time AS createTime "
            + "FROM biz_performance_relation_log l LEFT JOIN sys_user u ON u.user_id = l.operator_id "
            + "WHERE l.perf_id = #{perfId} AND l.year = #{year} ORDER BY l.log_id DESC LIMIT 100")
    List<PerformanceRelationLogVO> getLogs(@Param("perfId") Long perfId, @Param("year") Integer year);
}
