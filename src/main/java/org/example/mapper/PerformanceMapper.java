package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.entity.*;
import org.example.entity.vo.PerformanceAuditVO;

import java.util.List;

@Mapper
public interface PerformanceMapper {
    @Select("select * from biz_performance ")
    List<BizPerformance> getAllPerformance();

    @Select("select * from biz_performance where is_delete = 0 and " +
            "(leader_id = #{userId} or auditor_id = #{userId} or principal_id = #{userId} " +
            "or dept_id in (select dept_id from sys_dept where leader_id = #{userId} and is_delete = 0))")
    List<BizPerformance> getVisiblePerformance(@Param("userId") Long userId);

    @Select("select * from biz_performance where perf_id = #{perfId}")
    BizPerformance getPerformanceById(Long perfId);

    @Update(
            "update biz_performance set " +
                    "perf_code = #{perfCode}, " +
                    "perf_name = #{perfName}, " +
                    "target_value = #{targetValue}, " +
                    "current_value = #{currentValue}, " +
                    "data_type = #{dataType}, " +
                    "dept_id = #{deptId}, " +
                    "principal_id = #{principalId}, " +
                    "auditor_id = #{auditorId}, " +
                    "leader_id = #{leaderId}, " +
                    "is_delete = #{isDelete}, " +
                    "update_time = #{updateTime} " +
                    "where perf_id = #{perfId}"
    )
    void updatePerformance(BizPerformance bizPerformance);

    @Select("select * from rel_task_performance where perf_id = #{perfId}")
    List<RelTaskPerformance> getAllTaskPerformanceByPerfId(Long perfId);

    @Select("select * from rel_task_performance where task_id = #{taskId}")
    List<RelTaskPerformance> getAllPerformanceByTaskId(Long taskId);

    // 修复：添加 @Param 注解
    @Select("select * from rel_task_performance where task_id = #{taskId} and perf_id = #{perfId}")
    List<RelTaskPerformance> getRelTaskPerformanceByTaskIdAndPerfId(@Param("taskId") Long taskId, @Param("perfId") Long perfId);

    @Select("select perf_id from rel_task_performance where task_id = #{taskId}")
    List<Long> getPerfIdByTaskId(@Param("taskId") Long taskId);

    @Select("select * from biz_performance where perf_id in (select perf_id from rel_task_performance where task_id = #{taskId})")
    List<BizPerformance> getPerformanceByTaskId(@Param("taskId") Long taskId);

    @Select("select task_id from rel_task_performance where perf_id = #{perfId}")
    List<Long> getTaskIdByPerfId(@Param("perfId") Long perfId);

    @Select("select r.task_id from rel_task_performance r " +
            "join biz_performance_year y on y.year_id = r.year_id " +
            "join biz_task t on t.task_id = r.task_id " +
            "where r.perf_id = #{perfId} and y.year = #{year} " +
            "and t.phase = #{year} and (t.is_delete is null or t.is_delete = 0)")
    List<Long> getTaskIdByPerfIdAndYear(@Param("perfId") Long perfId, @Param("year") Integer year);


    // 一次性查询所有关联关系
    @Select("select * from rel_task_performance")
    List<RelTaskPerformance> getAllRelTaskPerformance();



    @Select("select * from biz_performance_year")
    List<BizPerformanceYear> getAllPerformanceYear();

    @Select("select * from biz_performance_year where year_id = #{yearId}")
    BizPerformanceYear getPerformanceYearById(Long yearId);

    @Select("select * from biz_performance_year where year = #{year}")
    List<BizPerformanceYear> getPerformanceYearByYear(@Param("year") Integer year);

    @Select("select * from biz_performance_year where perf_id = #{perfId} and year = #{year}")
    BizPerformanceYear getPerformanceYearByPerfIdAndYear(@Param("perfId") Long perfId, @Param("year") Integer year);

    @Select("select * from biz_performance_year where perf_id = #{perfId}")
    List<BizPerformanceYear> getPerformanceYearByPerfId(@Param("perfId") Long perfId);

    @Update(
            "UPDATE biz_performance_year SET " +
                    "target_value = #{targetValue}, " +
                    "actual_value = #{actualValue}, " +
                    "data_type = #{dataType}, " +
                    "is_delete = #{isDelete} " +
                    "WHERE year_id = #{yearId}"
    )
    void updatePerformanceYear(BizPerformanceYear bizPerformanceYear);

    @Insert("INSERT INTO biz_performance_submission(" +
            "perf_id, year_id, year, actual_value, submit_by, submit_time, flow_status, current_handler_id, comment, is_delete" +
            ") VALUES(" +
            "#{perfId}, #{yearId}, #{year}, #{actualValue}, #{submitBy}, #{submitTime}, #{flowStatus}, #{currentHandlerId}, #{comment}, #{isDelete}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "subId", keyColumn = "sub_id")
    void createPerformanceSubmission(BizPerformanceSubmission submission);

    @Update("UPDATE biz_performance_submission SET " +
            "perf_id=#{perfId}, year_id=#{yearId}, year=#{year}, actual_value=#{actualValue}, submit_by=#{submitBy}, " +
            "submit_time=#{submitTime}, flow_status=#{flowStatus}, current_handler_id=#{currentHandlerId}, " +
            "comment=#{comment}, is_delete=#{isDelete} WHERE sub_id=#{subId}")
    void updatePerformanceSubmission(BizPerformanceSubmission submission);

    @Select("SELECT * FROM biz_performance_submission WHERE sub_id = #{subId}")
    BizPerformanceSubmission getPerformanceSubmissionById(@Param("subId") Long subId);

    @Select("SELECT * FROM biz_performance_submission " +
            "WHERE perf_id = #{perfId} AND year = #{year} AND is_delete = 0 AND flow_status IN (10, 20) " +
            "ORDER BY sub_id DESC LIMIT 1")
    BizPerformanceSubmission getActivePerformanceSubmission(@Param("perfId") Long perfId, @Param("year") Integer year);

    @Select("SELECT * FROM biz_performance_submission " +
            "WHERE current_handler_id = #{userId} AND is_delete = 0 AND flow_status IN (10, 20) " +
            "ORDER BY submit_time DESC")
    List<BizPerformanceSubmission> getTodoPerformanceSubmissions(@Param("userId") Long userId);

    @Select("SELECT * FROM biz_performance_submission " +
            "WHERE (submit_by = #{userId} OR current_handler_id = #{userId}) AND is_delete = 0 " +
            "AND (flow_status = 30 OR flow_status < 0) ORDER BY submit_time DESC")
    List<BizPerformanceSubmission> getPerformanceSubmissionRecords(@Param("userId") Long userId);

    @Select("SELECT * FROM biz_performance_submission " +
            "WHERE perf_id = #{perfId} AND (#{year} IS NULL OR year = #{year}) AND is_delete = 0 " +
            "ORDER BY submit_time DESC, sub_id DESC")
    List<BizPerformanceSubmission> getPerformanceSubmissionsByPerfAndYear(@Param("perfId") Long perfId,
                                                                           @Param("year") Integer year);

    @Select("SELECT s.sub_id AS subId, s.perf_id AS perfId, s.year_id AS yearId, s.year, " +
            "p.perf_code AS perfCode, p.perf_name AS perfName, s.actual_value AS actualValue, " +
            "s.submit_by AS submitBy, s.submit_time AS submitTime, s.flow_status AS flowStatus, " +
            "s.current_handler_id AS currentHandlerId, s.comment, s.is_delete AS isDelete, " +
            "p.leader_id AS leaderId, p.auditor_id AS auditorId, p.principal_id AS principalId " +
            "FROM biz_performance_submission s JOIN biz_performance p ON s.perf_id = p.perf_id " +
            "WHERE s.sub_id = #{subId}")
    PerformanceAuditVO getPerformanceAuditVO(@Param("subId") Long subId);

    @Insert("INSERT INTO biz_performance_audit_snapshot(" +
            "sub_id, perf_id, year_id, previous_performance_value, previous_performance_update_time, " +
            "previous_year_actual_value, previous_year_target_value, create_time" +
            ") VALUES(" +
            "#{subId}, #{perfId}, #{yearId}, #{previousPerformanceValue}, #{previousPerformanceUpdateTime}, " +
            "#{previousYearActualValue}, #{previousYearTargetValue}, #{createTime}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "snapshotId", keyColumn = "snapshot_id")
    void createPerformanceAuditSnapshot(BizPerformanceAuditSnapshot snapshot);

    @Select("SELECT * FROM biz_performance_audit_snapshot WHERE sub_id = #{subId} LIMIT 1")
    BizPerformanceAuditSnapshot getPerformanceAuditSnapshot(@Param("subId") Long subId);

    @Insert("INSERT INTO biz_performance_audit_log(" +
            "sub_id, operator_id, action_type, pre_status, post_status, comment, create_time" +
            ") VALUES(" +
            "#{subId}, #{operatorId}, #{actionType}, #{preStatus}, #{postStatus}, #{comment}, #{createTime}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "logId", keyColumn = "log_id")
    void createPerformanceAuditLog(BizPerformanceAuditLog log);

    @Select("SELECT * FROM biz_performance_audit_log WHERE sub_id = #{subId} ORDER BY create_time DESC, log_id DESC")
    List<BizPerformanceAuditLog> getPerformanceAuditLogs(@Param("subId") Long subId);
}
