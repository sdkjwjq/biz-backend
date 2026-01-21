package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.entity.BizAuditLog;
import org.example.entity.BizMaterialSubmission;
import org.example.entity.BizTask;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Mapper
public interface BizMapper {
    // 获取全部任务
    @Select("SELECT * FROM biz_task")
    List<BizTask> getAllTasks();

    // 根据id获取任务
    @Select("SELECT * FROM biz_task WHERE task_id = #{taskId}")
    BizTask getTaskById(Long taskId);

    // 根据部门id获取任务
    @Select("SELECT * FROM biz_task WHERE dept_id = #{deptId}")
    List<BizTask> getTasksByDeptId(Long deptId);

    // 根据归口负责人获取任务
    @Select("SELECT * FROM biz_task WHERE principal_id = #{principalId}")
    List<BizTask> getTasksByPrincipalId(Long principalId);

    // getTasksByLeaderId
    @Select("SELECT * FROM biz_task WHERE leader_id = #{leaderId}")
    List<BizTask> getTasksByLeaderId(Long leaderId);

    // getTasksByLeaderIdOrPrincipleId
    @Select("SELECT * FROM biz_task WHERE leader_id = #{userId} OR principal_id = #{userId} OR auditor_id=#{auditorId}")
    List<BizTask> getTasks(Long userId);

    // 获取所有一级任务
    @Select("SELECT * FROM biz_task WHERE level=1")
    List<BizTask> getAllFirstLevelTasks();

    // 获取当前任务的所有子任务
    @Select("SELECT * FROM biz_task WHERE parent_id = #{taskId}")
    List<BizTask> getAllChildrenTasks(Long taskId);

    // 根据一级任务id获取其二级子任务
    @Select("SELECT * FROM biz_task WHERE parent_id = #{parentId} AND level=2")
    List<BizTask> getSecondLevelTasksByParentId(Long parentId);

    // 根据二级任务id获取其三级子任务
    @Select("SELECT * FROM biz_task WHERE parent_id = #{parentId} AND level=3")
    List<BizTask> getThirdLevelTasksByParentId(Long parentId);

    /**
     * 新增任务
     * @param task 任务实体（taskId会自增，无需传入）
     * @return 影响的行数（1表示新增成功）
     */
    @Insert("INSERT INTO biz_task (" +
            "project_id, parent_id, ancestors, phase, task_code, task_name, level, comment, " +
            "leader_id, auditor_id, principal_id, dept_id, exp_target, exp_level, exp_effect, " +
            "exp_material_desc, data_type, target_value, current_value, weight, progress, " +
            "status, is_delete, create_time, update_time" +
            ") VALUES (" +
            "#{projectId}, #{parentId}, #{ancestors}, #{phase}, #{taskCode}, #{taskName}, #{level}, #{comment}, " +
            "#{leaderId}, #{auditorId}, #{principalId}, #{deptId}, #{expTarget}, #{expLevel}, #{expEffect}, " +
            "#{expMaterialDesc}, #{dataType}, #{targetValue}, #{currentValue}, #{weight}, #{progress}, " +
            "#{status}, #{isDelete}, #{createTime}, #{updateTime}" +
            ")")
    // 新增注解：返回自增的taskId到实体对象中
    @Options(useGeneratedKeys = true, keyProperty = "taskId", keyColumn = "task_id")
    void addTask(BizTask task);

//    更新任务
// 全字段更新任务（根据taskId更新所有字段）
    @Update("UPDATE biz_task SET project_id=#{projectId},parent_id=#{parentId},ancestors=#{ancestors},phase=#{phase},task_code=#{taskCode},task_name=#{taskName},level=#{level},comment=#{comment},leader_id=#{leaderId},auditor_id=#{auditorId},principal_id=#{principalId},dept_id=#{deptId},exp_target=#{expTarget},exp_level=#{expLevel},exp_effect=#{expEffect},exp_material_desc=#{expMaterialDesc},data_type=#{dataType},target_value=#{targetValue},current_value=#{currentValue},weight=#{weight},progress=#{progress},status=#{status},is_delete=#{isDelete},create_time=#{createTime},update_time=NOW() WHERE task_id=#{taskId}")
    int updateTask(BizTask task);

    // 提交后更新任务
    @Update("UPDATE biz_task SET current_value = #{currentValue}, status = #{status}, update_time = NOW() WHERE task_id = #{taskId}")
    void updateCurrentTask(BizTask task);

    // 根据任务id获取部门leaderid
    @Select("SELECT leader_id FROM sys_dept WHERE dept_id = (SELECT dept_id FROM biz_task WHERE task_id = #{taskId})")
    Long getTaskLeaderId(Long taskId);

    // 根据任务id获取归口负责人Id
    @Select("SELECT principal_id FROM biz_task WHERE task_id = #{taskId}")
    Long getTaskPrincipalId(Long taskId);

    // 创建审批单日志
    @Insert("insert into biz_audit_log(sub_id,operator_id,action_type,pre_status,post_status,comment,create_time) values(#{subId},#{operatorId},#{actionType},#{preStatus},#{postStatus},#{comment},#{createTime})")
    void createAuditLog(BizAuditLog auditLog);

    // 更新日志
    @Update("update biz_audit_log set sub_id=#{subId},operator_id=#{operatorId},action_type=#{actionType},pre_status=#{preStatus},post_status=#{postStatus},comment=#{comment},create_time=#{createTime} where log_id=#{logId}")
    void updateAuditLog(BizAuditLog auditLog);

    // 根据 subId 获取审批操作日志（biz_audit_log）
    @Select("SELECT " +
            "log_id AS logId, " +
            "sub_id AS subId, " +
            "operator_id AS operatorId, " +
            "action_type AS actionType, " +
            "pre_status AS preStatus, " +
            "post_status AS postStatus, " +
            "comment, " +
            "create_time AS createTime " +
            "FROM biz_audit_log WHERE sub_id = #{subId} " +
            "ORDER BY create_time DESC, log_id DESC")
    List<BizAuditLog> getAuditLogsBySubId(@Param("subId") Long subId);

    // 创建审批流程
    @Insert("insert into biz_material_submission(" +
            "task_id, file_id, reported_value, data_type, submit_by, submit_dept_id, " +
            "manage_dept_id, submit_time, file_suffix, flow_status, current_handler_id, is_delete" +
            ") values(" +
            "#{taskId}, #{fileId}, #{reportedValue}, #{dataType}, #{submitBy}, #{submitDeptId}, " +
            "#{manageDeptId}, #{submitTime}, #{fileSuffix}, #{flowStatus}, #{currentHandlerId}, #{isDelete}" +
            ")")
    Long createAudit(BizMaterialSubmission bizMaterialSubmission);

    // 获取最新的审批单id
    @Select("SELECT sub_id FROM biz_material_submission ORDER BY sub_id DESC LIMIT 1")
    Long getNewestSubId();

    // 更新审批单 【核心修复点1：注解改为@Update；核心修复点2：where前加空格】
    @Update("update biz_material_submission set task_id=#{taskId},file_id=#{fileId},reported_value=#{reportedValue},data_type=#{dataType},"
            + "submit_by=#{submitBy},submit_dept_id=#{submitDeptId},manage_dept_id=#{manageDeptId},submit_time=#{submitTime},file_suffix=#{fileSuffix},"
            + "flow_status=#{flowStatus},current_handler_id=#{currentHandlerId},is_delete=#{isDelete} " + // 此处添加空格
            "where sub_id=#{subId}")
    void updateAudit(BizMaterialSubmission bizMaterialSubmission);

    // 更新任务状态 【优化：参数名统一为驼峰风格，与Java规范一致】
    @Update("UPDATE biz_task SET status = #{status} WHERE task_id = #{taskId}")
    void updateTaskStatus(@Param("taskId") Long taskId, @Param("status") String status);

    // 更新审批单flow_status 【优化：参数名统一为驼峰风格】
    @Update("UPDATE biz_material_submission SET flow_status = #{flowStatus} WHERE sub_id = #{subId}")
    void updateAuditFlowStatus(@Param("subId") Long subId, @Param("flowStatus") Integer flowStatus);

    // 根据taskId及current_handler_id获取审批单
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND current_handler_id = #{currentHandlerId}")
    List<BizMaterialSubmission> getAudit(@Param("taskId") Long taskId,
            @Param("currentHandlerId") Long currentHandlerId);

    // 获取“待我审批”的审批单（按当前处理人查询）
    @Select("SELECT * FROM biz_material_submission " +
            "WHERE current_handler_id = #{userId} AND is_delete = 0 AND flow_status >= 10 AND flow_status < 40 " +
            "ORDER BY submit_time DESC, sub_id DESC")
    List<BizMaterialSubmission> getTodoAudits(@Param("userId") Long userId);

    // 按 task_id 获取该任务全部审批单（用于任务详情抽屉展示完整流程）
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND is_delete = 0 ORDER BY sub_id DESC")
    List<BizMaterialSubmission> getAuditsByTaskId(@Param("taskId") Long taskId);

    // 根据subId获取审批单
    @Select("SELECT * FROM biz_material_submission WHERE sub_id = #{subId}")
    BizMaterialSubmission getAuditBySubId(Long subId);

    // 根据subId获取提交人
    @Select("SELECT submit_by FROM biz_material_submission WHERE sub_id = #{subId}")
    Long getAuditSubmitBy(Long subId);

//    根据任务id获取最近的审批单
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} ORDER BY sub_id DESC LIMIT 1")
    BizMaterialSubmission getLatestAuditByTaskId(Long taskId);

//    根据任务id获取最近的、且状态为40的审批单
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND flow_status = 40 ORDER BY sub_id DESC LIMIT 1")
    BizMaterialSubmission getLatestApprovedAuditByTaskId(Long taskId);





}