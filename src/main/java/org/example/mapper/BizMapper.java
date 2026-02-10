package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.entity.BizAuditLog;
import org.example.entity.BizMaterialSubmission;
import org.example.entity.BizTask;
import org.example.entity.vo.DeptTaskStatsVO;
import org.example.entity.vo.FirstLevelTaskDetailVO;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 业务数据访问接口
 */
@Mapper
public interface BizMapper {

    /**
     * 任务相关操作
     */

    /**
     * 获取全部任务
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task")
    List<BizTask> getAllTasks();

    /**
     * 根据id获取任务
     * @param taskId 任务ID
     * @return 任务对象
     */
    @Select("SELECT * FROM biz_task WHERE task_id = #{taskId}")
    BizTask getTaskById(Long taskId);

    /**
     * 根据部门id获取任务
     * @param deptId 部门ID
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE dept_id = #{deptId}")
    List<BizTask> getTasksByDeptId(Long deptId);

    // getTasksByDeptIdAndPhase
    @Select("SELECT * FROM biz_task WHERE dept_id = #{deptId} AND phase = #{phase}")
    List<BizTask> getTasksByDeptIdAndPhase(@Param("deptId") Long deptId, @Param("phase") Integer phase);
    /**
     * 根据归口负责人获取任务
     * @param principalId 负责人ID
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE principal_id = #{principalId}")
    List<BizTask> getTasksByPrincipalId(Long principalId);

    /**
     * 根据负责人ID获取任务
     * @param leaderId 负责人ID
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE leader_id = #{leaderId}")
    List<BizTask> getTasksByLeaderId(Long leaderId);

    /**
     * 根据负责人ID或归口负责人ID或审核人ID获取任务
     * @param userId 用户ID
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE leader_id = #{userId} OR principal_id = #{userId} OR auditor_id=#{userId}")
    List<BizTask> getTasks(Long userId);

    /**
     * 获取所有一级任务
     * @return 一级任务列表
     */
    @Select("SELECT * FROM biz_task WHERE level=1")
    List<BizTask> getAllFirstLevelTasks();

    /**
     * 获取当前任务的所有子任务
     * @param taskId 任务ID
     * @return 子任务列表
     */
    @Select("SELECT * FROM biz_task WHERE parent_id = #{taskId}")
    List<BizTask> getAllChildrenTasks(Long taskId);

    /**
     * 根据一级任务id获取其二级子任务
     * @param parentId 父任务ID
     * @return 二级任务列表
     */
    @Select("SELECT * FROM biz_task WHERE parent_id = #{parentId} AND level=2")
    List<BizTask> getSecondLevelTasksByParentId(Long parentId);

    /**
     * 根据二级任务id获取其三级子任务
     * @param parentId 父任务ID
     * @return 三级任务列表
     */
    @Select("SELECT * FROM biz_task WHERE parent_id = #{parentId} AND level=3")
    List<BizTask> getThirdLevelTasksByParentId(Long parentId);

    /**
     * 根据任务阶段获取任务
     * @param phase 任务阶段
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE phase = #{phase}")
    List<BizTask> getTasksByPhase(Integer phase);

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

    /**
     * 更新任务
     * 全字段更新任务（根据taskId更新所有字段）
     * @param task 任务实体
     * @return 影响的行数
     */
    @Update("UPDATE biz_task SET project_id=#{projectId},parent_id=#{parentId},ancestors=#{ancestors},phase=#{phase},task_code=#{taskCode},task_name=#{taskName},level=#{level},comment=#{comment},leader_id=#{leaderId},auditor_id=#{auditorId},principal_id=#{principalId},dept_id=#{deptId},exp_target=#{expTarget},exp_level=#{expLevel},exp_effect=#{expEffect},exp_material_desc=#{expMaterialDesc},data_type=#{dataType},target_value=#{targetValue},current_value=#{currentValue},weight=#{weight},progress=#{progress},status=#{status},is_delete=#{isDelete},create_time=#{createTime},update_time=NOW() WHERE task_id=#{taskId}")
    int updateTask(BizTask task);

    /**
     * 提交后更新任务
     * @param task 任务实体
     */
    @Update("UPDATE biz_task SET current_value = #{currentValue}, status = #{status}, update_time = NOW() WHERE task_id = #{taskId}")
    void updateCurrentTask(BizTask task);

    /**
     * 根据任务id获取部门leaderid
     * @param taskId 任务ID
     * @return 负责人ID
     */
    @Select("SELECT leader_id FROM sys_dept WHERE dept_id = (SELECT dept_id FROM biz_task WHERE task_id = #{taskId})")
    Long getTaskLeaderId(Long taskId);

    /**
     * 根据任务id获取归口负责人Id
     * @param taskId 任务ID
     * @return 归口负责人ID
     */
    @Select("SELECT principal_id FROM biz_task WHERE task_id = #{taskId}")
    Long getTaskPrincipalId(Long taskId);

    /**
     * 日志相关操作
     */

    /**
     * 创建审批单日志
     * @param auditLog 审批日志实体
     */
    @Insert("insert into biz_audit_log(sub_id,operator_id,action_type,pre_status,post_status,comment,create_time) values(#{subId},#{operatorId},#{actionType},#{preStatus},#{postStatus},#{comment},#{createTime})")
    void createAuditLog(BizAuditLog auditLog);

    /**
     * 更新日志
     * @param auditLog 审批日志实体
     */
    @Update("update biz_audit_log set sub_id=#{subId},operator_id=#{operatorId},action_type=#{actionType},pre_status=#{preStatus},post_status=#{postStatus},comment=#{comment},create_time=#{createTime} where log_id=#{logId}")
    void updateAuditLog(BizAuditLog auditLog);

    /**
     * 根据 subId 获取审批操作日志（biz_audit_log）
     * @param subId 提交ID
     * @return 审批日志列表
     */
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

    /**
     * 材料提交相关操作
     */

    /**
     * 创建审批流程
     * @param bizMaterialSubmission 材料提交实体
     * @return 提交ID
     */
    @Insert("insert into biz_material_submission(" +
            "task_id, file_id, reported_value, data_type, submit_by, submit_dept_id, " +
            "manage_dept_id, submit_time, file_suffix, flow_status, current_handler_id, is_delete" +
            ") values(" +
            "#{taskId}, #{fileId}, #{reportedValue}, #{dataType}, #{submitBy}, #{submitDeptId}, " +
            "#{manageDeptId}, #{submitTime}, #{fileSuffix}, #{flowStatus}, #{currentHandlerId}, #{isDelete}" +
            ")")
    void createAudit(BizMaterialSubmission bizMaterialSubmission);

    /**
     * 获取最新的审批单id
     * @return 最新的提交ID
     */
    @Select("SELECT sub_id FROM biz_material_submission ORDER BY sub_id DESC LIMIT 1")
    Long getNewestSubId();

    /**
     * 更新审批单 【核心修复点1：注解改为@Update；核心修复点2：where前加空格】
     * @param bizMaterialSubmission 材料提交实体
     */
    @Update("update biz_material_submission set task_id=#{taskId},file_id=#{fileId},reported_value=#{reportedValue},data_type=#{dataType},"
            + "submit_by=#{submitBy},submit_dept_id=#{submitDeptId},manage_dept_id=#{manageDeptId},submit_time=#{submitTime},file_suffix=#{fileSuffix},"
            + "flow_status=#{flowStatus},current_handler_id=#{currentHandlerId},is_delete=#{isDelete} " + // 此处添加空格
            "where sub_id=#{subId}")
    void updateAudit(BizMaterialSubmission bizMaterialSubmission);

    /**
     * 更新任务状态 【优化：参数名统一为驼峰风格，与Java规范一致】
     * @param taskId 任务ID
     * @param status 状态
     */
    @Update("UPDATE biz_task SET status = #{status} WHERE task_id = #{taskId}")
    void updateTaskStatus(@Param("taskId") Long taskId, @Param("status") String status);

    /**
     * 更新审批单flow_status 【优化：参数名统一为驼峰风格】
     * @param subId 提交ID
     * @param flowStatus 流程状态
     */
    @Update("UPDATE biz_material_submission SET flow_status = #{flowStatus} WHERE sub_id = #{subId}")
    void updateAuditFlowStatus(@Param("subId") Long subId, @Param("flowStatus") Integer flowStatus);

    /**
     * 根据taskId及current_handler_id获取审批单
     * @param taskId 任务ID
     * @param currentHandlerId 当前处理人ID
     * @return 材料提交列表
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND current_handler_id = #{currentHandlerId} AND is_delete = 0")
    List<BizMaterialSubmission> getAudit(@Param("taskId") Long taskId,
                                         @Param("currentHandlerId") Long currentHandlerId);

    /**
     * 根据任务id获取最新的审批单
     * @param taskId 任务ID
     * @return 材料提交对象
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND is_delete = 0 ORDER BY sub_id DESC LIMIT 1")
    BizMaterialSubmission getNewestAudit(@Param("taskId") Long taskId);

    /**
     * 获取"待我审批"的审批单（按当前处理人查询）
     * @param userId 用户ID
     * @return 待审批列表
     */
    @Select("SELECT * FROM biz_material_submission " +
            "WHERE current_handler_id = #{userId} AND is_delete = 0 AND flow_status >= 10 AND flow_status < 40 AND is_delete = 0 " +
            "ORDER BY submit_time DESC, sub_id DESC")
    List<BizMaterialSubmission> getTodoAudits(@Param("userId") Long userId);

    /**
     * 按 task_id 获取该任务全部审批单（用于任务详情抽屉展示完整流程）
     * @param taskId 任务ID
     * @return 审批单列表
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND is_delete = 0 ORDER BY sub_id DESC")
    List<BizMaterialSubmission> getAuditsByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据subId获取审批单
     * @param subId 提交ID
     * @return 材料提交对象
     */
    @Select("SELECT * FROM biz_material_submission WHERE sub_id = #{subId} AND is_delete = 0")
    BizMaterialSubmission getAuditBySubId(Long subId);

    /**
     * 根据subId获取提交人
     * @param subId 提交ID
     * @return 提交人ID
     */
    @Select("SELECT submit_by FROM biz_material_submission WHERE sub_id = #{subId} AND is_delete = 0")
    Long getAuditSubmitBy(Long subId);

    /**
     * 根据任务id获取最近的审批单
     * @param taskId 任务ID
     * @return 材料提交对象
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND is_delete = 0 ORDER BY sub_id DESC LIMIT 1")
    BizMaterialSubmission getLatestAuditByTaskId(Long taskId);

    /**
     * 根据任务id获取最近的、且状态为40的审批单
     * @param taskId 任务ID
     * @return 材料提交对象
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND flow_status = 40 AND is_delete = 0 ORDER BY sub_id DESC LIMIT 1")
    BizMaterialSubmission getLatestApprovedAuditByTaskId(Long taskId);

    /**
     * 获取所有待审核的审批单（flow_status在10-30之间）
     * @return 待审核审批单列表
     */
    @Select("SELECT * FROM biz_material_submission " +
            "WHERE flow_status >= 10 AND flow_status < 40 AND is_delete = 0 " +
            "ORDER BY submit_time DESC")
    List<BizMaterialSubmission> getAllPendingAudits();

    /**
     * 根据处理人ID统计待审核任务数量
     * @param handlerId 处理人ID
     * @return 待审核任务数量
     */
    @Select("SELECT COUNT(*) FROM biz_material_submission " +
            "WHERE current_handler_id = #{handlerId} " +
            "AND flow_status >= 10 AND flow_status < 40 AND is_delete = 0")
    Integer countPendingAuditsByHandler(@Param("handlerId") Long handlerId);

    /**
     * 获取所有任务数量
     * @return 任务总数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE is_delete = 0")
    Integer getTotalTaskCount();

    /**
     * 获取特定状态的任务数量（新增通用方法）
     * @param status 状态码
     * @return 特定状态的任务数量
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE status = #{status} AND is_delete = 0")
    Integer getTaskCountByStatus(@Param("status") String status);

    /**
     * 根据年份获取特定状态的任务数量（新增）
     * @param year 年份
     * @param status 状态码
     * @return 特定状态的任务数量
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE phase = #{year} AND status = #{status} AND is_delete = 0")
    Integer getYearTaskCountByStatus(@Param("year") Integer year, @Param("status") String status);

    /**
     * 获取中期特定状态的任务数量（新增）
     * @param endYear 截止年份
     * @param status 状态码
     * @return 特定状态的任务数量
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE phase < #{endYear} AND status = #{status} AND is_delete = 0")
    Integer getMidTermTaskCountByStatus(@Param("endYear") Integer endYear, @Param("status") String status);

    /**
     * 获取一级特定状态的任务数量（新增）
     * @param status 状态码
     * @return 特定状态的任务数量
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE level = 1 AND status = #{status} AND is_delete = 0")
    Integer getFirstLevelTaskCountByStatus(@Param("status") String status);

    /**
     * 获取已完成任务数量
     * @return 已完成任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE status = '3' AND is_delete = 0")
    Integer getCompletedTaskCount();

    /**
     * 获取本年度任务数量
     * @param year 年份
     * @return 本年度任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE phase = #{year} AND is_delete = 0")
    Integer getYearTaskCount(@Param("year") Integer year);

    /**
     * 获取本年度已完成任务数量
     * @param year 年份
     * @return 本年度已完成任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE phase = #{year} AND status = '3' AND is_delete = 0")
    Integer getYearCompletedTaskCount(@Param("year") Integer year);

    /**
     * 获取中期任务数量（phase在2028年之前）
     * @param endYear 截止年份
     * @return 中期任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE phase < #{endYear} AND is_delete = 0")
    Integer getMidTermTaskCount(@Param("endYear") Integer endYear);

    /**
     * 获取中期已完成任务数量
     * @param endYear 截止年份
     * @return 中期已完成任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE phase < #{endYear} AND status = '3' AND is_delete = 0")
    Integer getMidTermCompletedTaskCount(@Param("endYear") Integer endYear);

    /**
     * 获取一级任务数量
     * @return 一级任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE level = 1 AND is_delete = 0")
    Integer getFirstLevelTaskCount();

    /**
     * 获取一级已完成任务数量
     * @return 一级已完成任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE level = 1 AND status = '3' AND is_delete = 0")
    Integer getFirstLevelCompletedTaskCount();

    /**
     * 获取部门任务数量统计（不带状态统计，Service层处理）
     * @return 部门任务统计列表
     */
    @Select("SELECT " +
            "d.dept_id as deptId, " +
            "d.dept_name as deptName, " +
            "COUNT(t.task_id) as totalTasks, " +
            "SUM(CASE WHEN t.status = '3' THEN 1 ELSE 0 END) as completedTasks " +
            "FROM sys_dept d " +
            "LEFT JOIN biz_task t ON d.dept_id = t.dept_id AND t.is_delete = 0 AND t.level = 3 " +
            "WHERE d.is_delete = 0 " +
            "GROUP BY d.dept_id, d.dept_name " +
            "HAVING COUNT(t.task_id) > 0 "+
            "ORDER BY d.dept_id")
    List<DeptTaskStatsVO> getDeptTaskStats();

    /**
     * 获取部门本年度任务统计（不带状态统计，Service层处理）
     * @param year 年份
     * @return 部门本年度任务统计
     */
    @Select("SELECT " +
            "d.dept_id as deptId, " +
            "d.dept_name as deptName, " +
            "COUNT(t.task_id) as totalTasks, " +
            "SUM(CASE WHEN t.status = '3' THEN 1 ELSE 0 END) as completedTasks " +
            "FROM sys_dept d " +
            "LEFT JOIN biz_task t ON d.dept_id = t.dept_id AND t.phase = #{year} AND t.level = 3 AND t.is_delete = 0 " +
            "WHERE d.is_delete = 0 " +
            "GROUP BY d.dept_id, d.dept_name " +
            "HAVING COUNT(t.task_id) > 0 "+
            "ORDER BY d.dept_id")
    List<DeptTaskStatsVO> getDeptYearTaskStats(@Param("year") Integer year);

    /**
     * 获取部门中期任务统计（不带状态统计，Service层处理）
     * @param endYear 截止年份
     * @return 部门中期任务统计
     */
    @Select("SELECT " +
            "d.dept_id as deptId, " +
            "d.dept_name as deptName, " +
            "COUNT(t.task_id) as totalTasks, " +
            "SUM(CASE WHEN t.status = '3' THEN 1 ELSE 0 END) as completedTasks " +
            "FROM sys_dept d " +
            "LEFT JOIN biz_task t ON d.dept_id = t.dept_id AND t.phase < #{endYear} AND t.level = 3 AND t.is_delete = 0 " +
            "WHERE d.is_delete = 0 " +
            "GROUP BY d.dept_id, d.dept_name " +
            "HAVING COUNT(t.task_id) > 0 "+
            "ORDER BY d.dept_id")
    List<DeptTaskStatsVO> getDeptMidTermTaskStats(@Param("endYear") Integer endYear);

    /**
     * 获取一级任务的详细完成情况
     * @return 一级任务完成情况列表
     */
    @Select("SELECT " +
            "task_id as taskId, " +
            "task_name as taskName, " +
            "dept_id as deptId, " +
            "(SELECT dept_name FROM sys_dept WHERE dept_id = t.dept_id) as deptName, " +
            "status, " +
            "target_value as targetValue, " +
            "current_value as currentValue, " +
            "progress, " +
            "create_time as createTime, " +
            "update_time as updateTime " +
            "FROM biz_task t " +
            "WHERE level = 1 AND is_delete = 0 " +
            "ORDER BY task_id")
    List<FirstLevelTaskDetailVO> getFirstLevelTaskDetails();
}