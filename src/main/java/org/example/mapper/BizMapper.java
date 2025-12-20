package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.entity.BizAuditLog;
import org.example.entity.BizMaterialSubmission;
import org.example.entity.BizTask;
import org.example.entity.SysFile;

import java.util.List;

@Mapper
public interface BizMapper {
//    获取全部任务
    @Select("SELECT * FROM biz_task")
    public List<BizTask> getAllTasks();
//    根据id获取任务
    @Select("SELECT * FROM biz_task WHERE task_id = #{taskId}")
    public BizTask getTaskById(Long taskId);

//    根据部门id获取任务
    @Select("SELECT * FROM biz_task WHERE dept_id = #{deptId}")
    public List<BizTask> getTasksByDeptId(Long deptId);

//    根据归口负责人获取任务
    @Select("SELECT * FROM biz_task WHERE principal_id = #{principalId}")
    public List<BizTask> getTasksByPrincipalId(Long principalId);

//  获取所有一级任务
    @Select("SELECT * FROM biz_task WHERE level=1")
    public List<BizTask> getAllFirstLevelTasks();

//    获取当前任务的所有子任务
    @Select("SELECT * FROM biz_task WHERE parent_id = #{taskId}")
    public List<BizTask> getAllChildrenTasks(Long taskId);

//    根据一级任务id获取其二级子任务
    @Select("SELECT * FROM biz_task WHERE parent_id = #{parentId} AND level=2")
    public List<BizTask> getSecondLevelTasksByParentId(Long parentId);

//    根据二级任务id获取其三级子任务
    @Select("SELECT * FROM biz_task WHERE parent_id = #{parentId} AND level=3")
    public List<BizTask> getThirdLevelTasksByParentId(Long parentId);
//    根据任务id获取部门leaderid
    @Select("SELECT leader_id FROM sys_dept WHERE dept_id = (SELECT dept_id FROM biz_task WHERE task_id = #{taskId})")
    public Long getTaskLeaderId(Long taskId);



    //    创建审批单日志
    @Insert("insert into biz_audit_log(sub_id,operator_id,action_type,pre_status,post_status,comment,create_time) values(#{subId},#{operatorId},#{actionType},#{preStatus},#{postStatus},#{comment},#{createTime})")
    public void createAuditLog(BizAuditLog auditLog);
//    更新日志
    @Update("update biz_audit_log set sub_id=#{subId},operator_id=#{operatorId},action_type=#{actionType},pre_status=#{preStatus},post_status=#{postStatus},comment=#{comment},create_time=#{createTime} where log_id=#{logId}")
    public void updateAuditLog(BizAuditLog auditLog);

    //    创建审批流程
    @Insert("insert into biz_material_submission(" +
            "task_id, file_id, reported_value, data_type, submit_by, submit_dept_id, " +
            "manage_dept_id, submit_time, file_suffix, flow_status, current_handler_id, is_delete" +
            ") values(" +
            "#{taskId}, #{fileId}, #{reportedValue}, #{dataType}, #{submitBy}, #{submitDeptId}, " +
            "#{manageDeptId}, #{submitTime}, #{fileSuffix}, #{flowStatus}, #{currentHandlerId}, #{isDelete}" +
            ")")
    public void createAudit(BizMaterialSubmission bizMaterialSubmission);

    //    更新审批单
    @Insert("update biz_material_submission set task_id=#{taskId},file_id=#{fileId},reported_value=#{reportedValue},data_type=#{dataType},"
            +"submit_by=#{submitBy},submit_dept_id=#{submitDeptId},manage_dept_id=#{manageDeptId},submit_time=#{submitTime},file_suffix=#{fileSuffix},"
            +"flow_status=#{flowStatus},current_handler_id=#{currentHandlerId},is_delete=#{isDelete}"+
            "where sub_id=#{subId}"
    )
    public void updateAudit(BizMaterialSubmission bizMaterialSubmission);

//    updateTaskStatus
// 必须加 @Param 注解，参数名与 SQL 占位符严格匹配
    @Update("UPDATE biz_task SET status = #{status} WHERE task_id = #{task_id}")
    void updateTaskStatus(@Param("task_id") Long taskId, @Param("status") String status);

//    更新审批单flow_status
    @Update("UPDATE biz_material_submission SET flow_status = #{flow_status} WHERE sub_id = #{sub_id}")
    void updateAuditFlowStatus(@Param("sub_id") Long subId, @Param("flow_status") Integer flow_status);

//    根据taskId及current_handler_id获取审批单
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND current_handler_id = #{currentHandlerId}")
    public List<BizMaterialSubmission> getAudit(@Param("taskId") Long taskId, @Param("currentHandlerId") Long currentHandlerId);

//    根据subId获取审批单
    @Select("SELECT * FROM biz_material_submission WHERE sub_id = #{subId}")
    public BizMaterialSubmission getAuditBySubId(Long subId);
}
