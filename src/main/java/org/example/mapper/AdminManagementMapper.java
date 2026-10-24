package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.entity.AdminOperationLog;
import org.example.entity.SysDept;
import org.example.entity.SysUser;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminManagementMapper {
    @Select("SELECT * FROM sys_user WHERE user_id=#{userId} AND COALESCE(is_delete,0)=0 FOR UPDATE")
    SysUser userForUpdate(Long userId);

    @Select("SELECT * FROM sys_dept WHERE dept_id=#{deptId} AND COALESCE(is_delete,0)=0 FOR UPDATE")
    SysDept deptForUpdate(Long deptId);

    @Select("SELECT u.user_id userId,u.dept_id deptId,u.user_name userName,u.nick_name nickName,u.email,"
            + "u.role,u.status,u.force_password_change forcePasswordChange,u.create_time createTime,u.update_time updateTime,"
            + "d.dept_name deptName FROM sys_user u LEFT JOIN sys_dept d ON d.dept_id=u.dept_id "
            + "WHERE COALESCE(u.is_delete,0)=0 ORDER BY u.user_id")
    List<Map<String, Object>> users();

    @Select("SELECT d.dept_id deptId,d.dept_name deptName,d.leader_id leaderId,d.status,d.create_time createTime,"
            + "d.update_time updateTime,u.nick_name leaderName,"
            + "(SELECT COUNT(*) FROM sys_user x WHERE x.dept_id=d.dept_id AND COALESCE(x.is_delete,0)=0 AND x.status='1') activeUsers,"
            + "(SELECT COUNT(*) FROM biz_task t WHERE t.level=3 AND COALESCE(t.is_delete,0)=0 AND t.dept_id=d.dept_id) activeTasks,"
            + "(SELECT COUNT(*) FROM biz_performance p WHERE COALESCE(p.is_delete,0)=0 AND p.dept_id=d.dept_id) activePerformances,"
            + "(SELECT COUNT(*) FROM biz_task t WHERE t.level=3 AND COALESCE(t.is_delete,0)=0 AND t.dept_id=d.dept_id "
            + " AND NOT (t.principal_id <=> d.leader_id)) taskLeaderMismatches,"
            + "(SELECT COUNT(*) FROM biz_performance p WHERE COALESCE(p.is_delete,0)=0 AND p.dept_id=d.dept_id "
            + " AND NOT (p.principal_id <=> d.leader_id)) performanceLeaderMismatches "
            + "FROM sys_dept d LEFT JOIN sys_user u ON u.user_id=d.leader_id WHERE COALESCE(d.is_delete,0)=0 ORDER BY d.dept_id")
    List<Map<String, Object>> departments();

    @Select("SELECT dept_id deptId,dept_name deptName FROM sys_dept WHERE COALESCE(is_delete,0)=0 ORDER BY dept_id")
    List<Map<String, Object>> departmentNames();


    @Select("SELECT COUNT(*) FROM sys_user WHERE role='0' AND status='1' AND COALESCE(is_delete,0)=0")
    int activeAdminCount();

    @Select("SELECT COUNT(*) FROM sys_user WHERE status=#{status} AND COALESCE(is_delete,0)=0")
    int userCountByStatus(String status);

    @Select("SELECT COUNT(*) FROM sys_user WHERE user_name=#{userName} AND user_id<>#{userId} AND COALESCE(is_delete,0)=0")
    int duplicateUserName(@Param("userName") String userName, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM sys_dept WHERE dept_id=#{deptId} AND status='0' AND COALESCE(is_delete,0)=0")
    int activeDept(Long deptId);

    @Select("SELECT COUNT(*) FROM sys_user WHERE user_id=#{userId} AND dept_id=#{deptId} AND status='1' AND COALESCE(is_delete,0)=0")
    int activeUserInDept(@Param("userId") Long userId, @Param("deptId") Long deptId);

    @Update("UPDATE sys_user SET dept_id=#{deptId},user_name=#{userName},nick_name=#{nickName},email=#{email},"
            + "role=#{role},update_time=GREATEST(NOW(),DATE_ADD(COALESCE(update_time,'1970-01-01'),INTERVAL 1 SECOND)) WHERE user_id=#{userId} AND COALESCE(is_delete,0)=0 "
            + "AND (update_time <=> #{expectedUpdateTime})")
    int updateUserProfile(@Param("userId") Long userId, @Param("deptId") Long deptId,
                          @Param("userName") String userName, @Param("nickName") String nickName,
                          @Param("email") String email, @Param("role") String role,
                          @Param("expectedUpdateTime") java.util.Date expectedUpdateTime);

    @Update("UPDATE sys_user SET status=#{status},update_time=GREATEST(NOW(),DATE_ADD(COALESCE(update_time,'1970-01-01'),INTERVAL 1 SECOND)) WHERE user_id=#{userId} "
            + "AND COALESCE(is_delete,0)=0 AND (update_time <=> #{expectedUpdateTime})")
    int updateUserStatus(@Param("userId") Long userId, @Param("status") String status,
                         @Param("expectedUpdateTime") java.util.Date expectedUpdateTime);

    @Update("UPDATE sys_user SET password=CAST(user_id AS CHAR),force_password_change=1,update_time=GREATEST(NOW(),DATE_ADD(COALESCE(update_time,'1970-01-01'),INTERVAL 1 SECOND)) "
            + "WHERE user_id=#{userId} AND COALESCE(is_delete,0)=0 AND (update_time <=> #{expectedUpdateTime})")
    int resetUserPassword(@Param("userId") Long userId,
                          @Param("expectedUpdateTime") java.util.Date expectedUpdateTime);

    @Update("UPDATE sys_dept SET leader_id=#{leaderId},status=#{status},update_time=GREATEST(NOW(),DATE_ADD(COALESCE(update_time,'1970-01-01'),INTERVAL 1 SECOND)) WHERE dept_id=#{deptId} "
            + "AND COALESCE(is_delete,0)=0 AND (update_time <=> #{expectedUpdateTime})")
    int updateDepartment(@Param("deptId") Long deptId, @Param("leaderId") Long leaderId,
                         @Param("status") String status,
                         @Param("expectedUpdateTime") java.util.Date expectedUpdateTime);

    @Select("SELECT "
            + "(SELECT COUNT(*) FROM sys_dept WHERE leader_id=#{userId} AND status='0' AND COALESCE(is_delete,0)=0) departmentLeaderCount,"
            + "(SELECT COUNT(*) FROM biz_task WHERE level=3 AND COALESCE(is_delete,0)=0 AND (leader_id=#{userId} OR auditor_id=#{userId} OR principal_id=#{userId})) taskResponsibilityCount,"
            + "(SELECT COUNT(*) FROM biz_performance WHERE COALESCE(is_delete,0)=0 AND (leader_id=#{userId} OR auditor_id=#{userId} OR principal_id=#{userId})) performanceResponsibilityCount,"
            + "(SELECT COUNT(*) FROM biz_material_submission WHERE COALESCE(is_delete,0)=0 AND flow_status IN (10,20,30) AND current_handler_id=#{userId}) taskPendingCount,"
            + "(SELECT COUNT(*) FROM biz_performance_submission WHERE COALESCE(is_delete,0)=0 AND flow_status IN (10,20) AND current_handler_id=#{userId}) performancePendingCount,"
            + "(SELECT COUNT(*) FROM biz_achievement WHERE COALESCE(is_delete,0)=0 AND audit_status=10 AND current_handler_id=#{userId}) achievementPendingCount,"
            + "(SELECT COUNT(*) FROM biz_work_record WHERE owner_id=#{userId} AND status=0 AND COALESCE(delete_marker,0)=0) draftWorkRecordCount")
    Map<String, Object> userImpact(Long userId);

    @Select("SELECT "
            + "(SELECT COUNT(*) FROM sys_user WHERE dept_id=#{deptId} AND status='1' AND COALESCE(is_delete,0)=0) activeUsers,"
            + "(SELECT COUNT(*) FROM biz_task WHERE level=3 AND dept_id=#{deptId} AND COALESCE(is_delete,0)=0) activeTasks,"
            + "(SELECT COUNT(*) FROM biz_performance WHERE dept_id=#{deptId} AND COALESCE(is_delete,0)=0) activePerformances,"
            + "(SELECT COUNT(*) FROM biz_material_submission WHERE manage_dept_id=#{deptId} AND COALESCE(is_delete,0)=0 AND flow_status IN (10,20,30)) taskPendingCount")
    Map<String, Object> departmentImpact(Long deptId);

    @Select("SELECT t.task_id taskId,t.task_code taskCode,t.task_name taskName,t.phase,t.dept_id deptId,d.dept_name deptName,"
            + "t.leader_id leaderId,ul.nick_name leaderName,t.auditor_id auditorId,ua.nick_name auditorName,"
            + "t.principal_id principalId,up.nick_name principalName,"
            + "TRIM(BOTH '、' FROM CONCAT(IF(t.dept_id IS NULL OR d.dept_id IS NULL OR d.status<>'0','归口部门无效、',''),"
            + "IF(t.leader_id IS NULL OR ul.user_id IS NULL OR ul.status<>'1','责任人无效、',''),"
            + "IF(t.auditor_id IS NULL OR ua.user_id IS NULL OR ua.status<>'1','专业群审核人无效、',''),"
            + "IF(t.principal_id IS NULL OR up.user_id IS NULL OR up.status<>'1','归口审核人无效、',''),"
            + "IF(EXISTS (SELECT 1 FROM biz_material_submission s LEFT JOIN sys_user hu ON hu.user_id=s.current_handler_id AND COALESCE(hu.is_delete,0)=0 WHERE s.task_id=t.task_id AND COALESCE(s.is_delete,0)=0 AND s.flow_status IN (10,20,30) AND (hu.user_id IS NULL OR hu.status<>'1' OR (s.flow_status=10 AND NOT (s.current_handler_id <=> t.auditor_id)) OR (s.flow_status=20 AND NOT (s.current_handler_id <=> t.principal_id)) OR (s.flow_status=30 AND hu.role<>'0'))),'待办处理人异常、',''))) issue "
            + "FROM biz_task t LEFT JOIN sys_dept d ON d.dept_id=t.dept_id AND COALESCE(d.is_delete,0)=0 "
            + "LEFT JOIN sys_user ul ON ul.user_id=t.leader_id AND COALESCE(ul.is_delete,0)=0 "
            + "LEFT JOIN sys_user ua ON ua.user_id=t.auditor_id AND COALESCE(ua.is_delete,0)=0 "
            + "LEFT JOIN sys_user up ON up.user_id=t.principal_id AND COALESCE(up.is_delete,0)=0 "
            + "WHERE t.level=3 AND t.phase=#{year} AND COALESCE(t.is_delete,0)=0 AND "
            + "(t.dept_id IS NULL OR d.dept_id IS NULL OR d.status<>'0' OR t.leader_id IS NULL OR ul.user_id IS NULL OR ul.status<>'1' "
            + "OR t.auditor_id IS NULL OR ua.user_id IS NULL OR ua.status<>'1' OR t.principal_id IS NULL OR up.user_id IS NULL OR up.status<>'1' "
            + "OR EXISTS (SELECT 1 FROM biz_material_submission s LEFT JOIN sys_user hu ON hu.user_id=s.current_handler_id AND COALESCE(hu.is_delete,0)=0 WHERE s.task_id=t.task_id AND COALESCE(s.is_delete,0)=0 AND s.flow_status IN (10,20,30) AND (hu.user_id IS NULL OR hu.status<>'1' OR (s.flow_status=10 AND NOT (s.current_handler_id <=> t.auditor_id)) OR (s.flow_status=20 AND NOT (s.current_handler_id <=> t.principal_id)) OR (s.flow_status=30 AND hu.role<>'0')))) "
            + "ORDER BY t.task_code")
    List<Map<String, Object>> taskIssues(Integer year);

    @Select("SELECT p.perf_id perfId,p.perf_code perfCode,p.perf_name perfName,p.dept_id deptId,d.dept_name deptName,"
            + "p.leader_id leaderId,ul.nick_name leaderName,p.auditor_id auditorId,ua.nick_name auditorName,"
            + "p.principal_id principalId,up.nick_name principalName,"
            + "CASE WHEN (p.perf_code LIKE '1.1.%' OR p.perf_code LIKE '1.2.%') AND NOT EXISTS ("
            + "SELECT 1 FROM rel_task_performance r JOIN biz_performance_year py ON py.year_id=r.year_id "
            + "WHERE r.perf_id=p.perf_id AND py.year=#{year} AND COALESCE(r.is_delete,0)=0 AND COALESCE(py.is_delete,0)=0) "
            + "THEN 1 ELSE 0 END noRelation,"
            + "TRIM(BOTH '、' FROM CONCAT(IF(p.dept_id IS NULL OR d.dept_id IS NULL OR d.status<>'0','部门无效、',''),"
            + "IF(p.leader_id IS NULL OR ul.user_id IS NULL OR ul.status<>'1','责任人无效、',''),"
            + "IF(p.auditor_id IS NULL OR ua.user_id IS NULL OR ua.status<>'1','审核人无效、',''),"
            + "IF((p.perf_code LIKE '1.1.%' OR p.perf_code LIKE '1.2.%') AND NOT EXISTS (SELECT 1 FROM rel_task_performance r JOIN biz_performance_year py ON py.year_id=r.year_id WHERE r.perf_id=p.perf_id AND py.year=#{year} AND COALESCE(r.is_delete,0)=0 AND COALESCE(py.is_delete,0)=0),'无关联任务、',''))) issue "
            + "FROM biz_performance p LEFT JOIN sys_dept d ON d.dept_id=p.dept_id AND COALESCE(d.is_delete,0)=0 "
            + "LEFT JOIN sys_user ul ON ul.user_id=p.leader_id AND COALESCE(ul.is_delete,0)=0 "
            + "LEFT JOIN sys_user ua ON ua.user_id=p.auditor_id AND COALESCE(ua.is_delete,0)=0 "
            + "LEFT JOIN sys_user up ON up.user_id=p.principal_id AND COALESCE(up.is_delete,0)=0 "
            + "WHERE COALESCE(p.is_delete,0)=0 AND NOT EXISTS (SELECT 1 FROM biz_performance c WHERE c.parent_id=p.perf_id AND COALESCE(c.is_delete,0)=0) "
            + "AND (p.dept_id IS NULL OR d.dept_id IS NULL OR d.status<>'0' OR p.leader_id IS NULL OR ul.user_id IS NULL OR ul.status<>'1' "
            + "OR p.auditor_id IS NULL OR ua.user_id IS NULL OR ua.status<>'1' OR ((p.perf_code LIKE '1.1.%' OR p.perf_code LIKE '1.2.%') "
            + "AND NOT EXISTS (SELECT 1 FROM rel_task_performance r JOIN biz_performance_year py ON py.year_id=r.year_id WHERE r.perf_id=p.perf_id AND py.year=#{year} AND COALESCE(r.is_delete,0)=0 AND COALESCE(py.is_delete,0)=0))) "
            + "ORDER BY p.perf_code")
    List<Map<String, Object>> performanceIssues(Integer year);

    @Select("SELECT COUNT(*) FROM ("
            + "SELECT s.sub_id FROM biz_material_submission s JOIN biz_task t ON t.task_id=s.task_id "
            + "LEFT JOIN sys_user u ON u.user_id=s.current_handler_id AND COALESCE(u.is_delete,0)=0 "
            + "WHERE COALESCE(s.is_delete,0)=0 AND s.flow_status IN (10,20,30) AND "
            + "(u.user_id IS NULL OR u.status<>'1' OR (s.flow_status=10 AND NOT (s.current_handler_id <=> t.auditor_id)) "
            + "OR (s.flow_status=20 AND NOT (s.current_handler_id <=> t.principal_id)) OR (s.flow_status=30 AND u.role<>'0')) "
            + "UNION ALL SELECT s.sub_id FROM biz_performance_submission s JOIN biz_performance p ON p.perf_id=s.perf_id "
            + "LEFT JOIN sys_user u ON u.user_id=s.current_handler_id AND COALESCE(u.is_delete,0)=0 "
            + "WHERE COALESCE(s.is_delete,0)=0 AND s.flow_status IN (10,20) AND "
            + "(u.user_id IS NULL OR u.status<>'1' OR (s.flow_status=10 AND NOT (s.current_handler_id <=> p.auditor_id)) OR (s.flow_status=20 AND u.role<>'0')) "
            + "UNION ALL SELECT a.ach_id FROM biz_achievement a "
            + "LEFT JOIN sys_user u ON u.user_id=a.current_handler_id AND COALESCE(u.is_delete,0)=0 "
            + "WHERE COALESCE(a.is_delete,0)=0 AND a.audit_status=10 AND (u.user_id IS NULL OR u.status<>'1' OR u.role<>'0')"
            + ") x")
    int invalidPendingCount();

    @Select("SELECT s.sheet_id sheetId,s.year,s.month,s.locked,s.import_time importTime,u.nick_name importByName,"
            + "(SELECT COUNT(*) FROM biz_budget_import_version v WHERE v.sheet_id=s.sheet_id AND COALESCE(v.is_delete,0)=0) versionCount "
            + "FROM biz_budget_sheet s LEFT JOIN sys_user u ON u.user_id=s.import_by "
            + "WHERE s.year=#{year} AND s.month=#{month} AND COALESCE(s.is_delete,0)=0 LIMIT 1")
    Map<String, Object> budgetSummary(@Param("year") Integer year, @Param("month") Integer month);

    @Select("SELECT COUNT(DISTINCT owner_id) FROM biz_work_record WHERE record_year=#{year} AND record_month=#{month} "
            + "AND status=1 AND COALESCE(delete_marker,0)=0")
    int submittedWorkRecordAuthors(@Param("year") Integer year, @Param("month") Integer month);

    @Insert("INSERT INTO sys_admin_operation_log(module,action,target_type,target_id,target_name,batch_id,operator_id,reason,before_json,after_json,create_time) "
            + "VALUES(#{module},#{action},#{targetType},#{targetId},#{targetName},#{batchId},#{operatorId},#{reason},#{beforeJson},#{afterJson},#{createTime})")
    @Options(useGeneratedKeys=true,keyProperty="logId",keyColumn="log_id")
    void insertOperation(AdminOperationLog log);

    @Select("SELECT l.log_id recordId,'ADMIN' recordType,l.module,l.action,l.target_type targetType,l.target_id targetId,"
            + "l.target_name targetName,l.batch_id batchId,l.operator_id operatorId,u.nick_name operatorName,l.reason,"
            + "l.before_json beforeJson,l.after_json afterJson,l.create_time createTime FROM sys_admin_operation_log l "
            + "LEFT JOIN sys_user u ON u.user_id=l.operator_id")
    List<Map<String, Object>> adminOperations();

    @Select("SELECT l.log_id recordId,'OWNERSHIP' recordType,'OWNERSHIP' module,'TRANSFER' action,l.target_type targetType,"
            + "CAST(l.target_id AS CHAR) targetId,l.target_name targetName,l.batch_id batchId,l.operator_id operatorId,"
            + "u.nick_name operatorName,l.reason,NULL beforeJson,NULL afterJson,l.create_time createTime "
            + "FROM biz_ownership_change_log l LEFT JOIN sys_user u ON u.user_id=l.operator_id")
    List<Map<String, Object>> ownershipOperations();

    @Select("SELECT l.log_id recordId,'RELATION' recordType,'PERFORMANCE_RELATION' module,l.action,'PERFORMANCE_RELATION' targetType,"
            + "CAST(l.perf_id AS CHAR) targetId,l.perf_name targetName,l.batch_id batchId,l.operator_id operatorId,"
            + "u.nick_name operatorName,l.reason,NULL beforeJson,NULL afterJson,l.create_time createTime "
            + "FROM biz_performance_relation_log l LEFT JOIN sys_user u ON u.user_id=l.operator_id")
    List<Map<String, Object>> relationOperations();

    @Select("SELECT v.version_id recordId,'BUDGET' recordType,'BUDGET' module,v.operation_type action,'BUDGET_SHEET' targetType,"
            + "CAST(v.sheet_id AS CHAR) targetId,CONCAT(v.year,'年',v.month,'月预算 V',v.version_no) targetName,NULL batchId,"
            + "v.created_by operatorId,u.nick_name operatorName,v.remark reason,NULL beforeJson,NULL afterJson,v.create_time createTime "
            + "FROM biz_budget_import_version v LEFT JOIN sys_user u ON u.user_id=v.created_by WHERE COALESCE(v.is_delete,0)=0")
    List<Map<String, Object>> budgetOperations();

    @Select("SELECT r.record_id recordId,'WORK_RECORD' recordType,'WORK_RECORD' module,'DELETE' action,'WORK_RECORD' targetType,"
            + "CAST(r.record_id AS CHAR) targetId,CONCAT(r.owner_name,' ',r.record_year,'年',r.record_month,'月工作纪实') targetName,"
            + "NULL batchId,r.deleted_by operatorId,u.nick_name operatorName,r.delete_reason reason,NULL beforeJson,NULL afterJson,"
            + "r.deleted_time createTime FROM biz_work_record r LEFT JOIN sys_user u ON u.user_id=r.deleted_by "
            + "WHERE COALESCE(r.delete_marker,0)<>0")
    List<Map<String, Object>> deletedWorkRecordOperations();

    @Select("SELECT r.record_id recordId,r.owner_id ownerId,r.owner_name ownerName,r.record_year recordYear,"
            + "r.record_month recordMonth,r.submit_time submitTime,r.deleted_by deletedBy,u.nick_name deletedByName,"
            + "r.deleted_time deletedTime,r.delete_reason deleteReason FROM biz_work_record r "
            + "LEFT JOIN sys_user u ON u.user_id=r.deleted_by WHERE COALESCE(r.delete_marker,0)<>0 ORDER BY r.deleted_time DESC")
    List<Map<String, Object>> deletedWorkRecords();
}
