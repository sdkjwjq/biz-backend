package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.entity.BizOwnershipChangeLog;
import org.example.entity.vo.OwnershipChangeLogVO;

import java.util.Date;
import java.util.List;

/** 管理员归属移交：日志读写与三个归属字段的定向更新。 */
@Mapper
public interface OwnershipTransferMapper {

    @Insert("INSERT INTO biz_ownership_change_log(target_type, target_id, target_code, target_name, batch_id, operator_id, reason, "
            + "dept_before_id, dept_after_id, leader_before_id, leader_after_id, principal_before_id, principal_after_id, create_time) VALUES("
            + "#{targetType}, #{targetId}, #{targetCode}, #{targetName}, #{batchId}, #{operatorId}, #{reason}, "
            + "#{deptBeforeId}, #{deptAfterId}, #{leaderBeforeId}, #{leaderAfterId}, #{principalBeforeId}, #{principalAfterId}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "logId", keyColumn = "log_id")
    void insertLog(BizOwnershipChangeLog log);

    @Select("SELECT l.log_id AS logId, l.target_type AS targetType, l.target_id AS targetId, l.target_code AS targetCode, "
            + "l.target_name AS targetName, l.batch_id AS batchId, l.operator_id AS operatorId, "
            + "COALESCE(NULLIF(uo.nick_name,''), uo.user_name, '') AS operatorName, l.reason AS reason, "
            + "l.dept_before_id AS deptBeforeId, COALESCE(db.dept_name,'') AS deptBeforeName, "
            + "l.dept_after_id AS deptAfterId, COALESCE(da.dept_name,'') AS deptAfterName, "
            + "l.leader_before_id AS leaderBeforeId, COALESCE(NULLIF(ulb.nick_name,''), ulb.user_name, '') AS leaderBeforeName, "
            + "l.leader_after_id AS leaderAfterId, COALESCE(NULLIF(ula.nick_name,''), ula.user_name, '') AS leaderAfterName, "
            + "l.principal_before_id AS principalBeforeId, COALESCE(NULLIF(upb.nick_name,''), upb.user_name, '') AS principalBeforeName, "
            + "l.principal_after_id AS principalAfterId, COALESCE(NULLIF(upa.nick_name,''), upa.user_name, '') AS principalAfterName, "
            + "l.create_time AS createTime "
            + "FROM biz_ownership_change_log l "
            + "LEFT JOIN sys_user uo ON uo.user_id = l.operator_id "
            + "LEFT JOIN sys_dept db ON db.dept_id = l.dept_before_id "
            + "LEFT JOIN sys_dept da ON da.dept_id = l.dept_after_id "
            + "LEFT JOIN sys_user ulb ON ulb.user_id = l.leader_before_id "
            + "LEFT JOIN sys_user ula ON ula.user_id = l.leader_after_id "
            + "LEFT JOIN sys_user upb ON upb.user_id = l.principal_before_id "
            + "LEFT JOIN sys_user upa ON upa.user_id = l.principal_after_id "
            + "WHERE l.target_type = #{targetType} AND l.target_id = #{targetId} "
            + "ORDER BY l.log_id DESC LIMIT 50")
    List<OwnershipChangeLogVO> getLogs(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    /** 任务存在未删除且处于审核中（10/20/30）的单据时禁止移交。 */
    @Select("SELECT COUNT(*) FROM biz_material_submission WHERE task_id = #{taskId} "
            + "AND COALESCE(is_delete,0) = 0 AND flow_status IN (10, 20, 30)")
    int countActiveTaskAudits(@Param("taskId") Long taskId);

    @Select("SELECT COUNT(*) FROM biz_performance_submission WHERE perf_id = #{perfId} "
            + "AND COALESCE(is_delete,0) = 0 AND flow_status IN (10, 20)")
    int countActivePerformanceAudits(@Param("perfId") Long perfId);

    @Update("UPDATE biz_task SET dept_id = #{deptId}, leader_id = #{leaderId}, principal_id = #{principalId}, "
            + "update_time = #{time} WHERE task_id = #{taskId} AND COALESCE(is_delete,0) = 0")
    int updateTaskOwnership(@Param("taskId") Long taskId, @Param("deptId") Long deptId,
                            @Param("leaderId") Long leaderId, @Param("principalId") Long principalId,
                            @Param("time") Date time);

    @Update("UPDATE biz_performance SET dept_id = #{deptId}, leader_id = #{leaderId}, principal_id = #{principalId}, "
            + "update_time = #{time} WHERE perf_id = #{perfId} AND COALESCE(is_delete,0) = 0")
    int updatePerformanceOwnership(@Param("perfId") Long perfId, @Param("deptId") Long deptId,
                                   @Param("leaderId") Long leaderId, @Param("principalId") Long principalId,
                                   @Param("time") Date time);
}
