package org.example.service;

import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.*;
import org.example.entity.dto.AuditDTO;
import org.example.entity.dto.BizSubDTO;
import org.example.entity.dto.ReSubDTO;
import org.example.entity.dto.FileUploadDTO;
import org.example.entity.dto.SysNoticeDTO;
import org.example.entity.vo.BizTaskVo;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.example.utils.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.util.*;

@Service
public class BizService {

    @Autowired
    private BizMapper bizMapper;

    @Autowired
    private SysMapper sysMapper;
    @Autowired
    private StandardServletMultipartResolver standardServletMultipartResolver;


    //    获取全部任务
    public List<BizTask> getAllTasks(){
        return bizMapper.getAllTasks();
    }



    //根据id获取任务
    public BizTask getTaskById(Long taskId){
        try{
            if (taskId == null){
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null){
                throw new RuntimeException("该任务不存在");
            }
            return bizMapper.getTaskById(taskId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //    获取当前任务的所有子任务
    public List<BizTask> getAllChildrenTasks(Long taskId){
        try{
            if (taskId == null){
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null){
                throw new RuntimeException("该任务不存在");
            }
            return bizMapper.getAllChildrenTasks(taskId);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    //  获取当前任务的父任务
    public BizTask getParentTask(Long taskId){
        try{
            if (taskId == null){
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null){
                throw new RuntimeException("该任务不存在");
            }
            Long parentId = bizMapper.getTaskById(taskId).getParentId();
            return bizMapper.getTaskById(parentId);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public List<BizTask> getTasksByDeptId(Long deptId){
        try{
            return bizMapper.getTasksByDeptId(deptId);
        }catch (Exception e){
            throw new RuntimeException("获取任务失败,请检查部门id是否正确");
        }
    }

//    根据用户角色获取任务，角色为0和2返回所有任务，角色为1返回leaderid以及为自己的任务
    public List<BizTaskVo> getTasksByUserRole(Long userId){
        try{
            SysUser sysUser = sysMapper.getUserById(userId);
            if (sysUser == null){
                throw new RuntimeException("用户不存在");
            }
            if(sysUser.getRole().equals("0")){
                return TaskListToTaskVoList(bizMapper.getAllTasks());
            }else if (sysUser.getRole().equals("1")){
                return TaskListToTaskVoList(bizMapper.getTasksByLeaderIdOrPrincipalId(sysUser.getUserId()));
            }else if(sysUser.getRole().equals("2")){
                return TaskListToTaskVoList(bizMapper.getTasksByPrincipalId(sysUser.getUserId()));
            }else {
                throw new RuntimeException("用户角色错误");
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public String submitMaterial(BizSubDTO bizSubDTO,Long userId){
        try{
            // 检查taskid是否存在
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()) == null){
                throw new RuntimeException("该任务不存在");
            }
            // 验证任务必须为三级任务，否则无法提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getLevel() != 3){
                throw new RuntimeException("该任务不是三级任务,无法提交");
            }
            // 验证任务状态，如果当前status为2，则禁止提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getStatus().equals("2")){
                throw new RuntimeException("该任务状态未开始或正在审核中,无法提交");
            }
            // 验证任务状态，如果当前status为3，则禁止提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getStatus().equals("3")){
                throw new RuntimeException("该任务状态已完成,无法提交");
            }
            // 检查文件是否存在
            SysFile sysFile = sysMapper.getFileById(bizSubDTO.getFile_id());
            if (sysFile == null){
                throw new RuntimeException("该文件不存在");
            }
            // 验证文件后缀，只能为pdf,doc,docx
            if (!sysFile.getFileName().endsWith(".pdf") && !sysFile.getFileName().endsWith(".doc") && !sysFile.getFileName().endsWith(".docx")){
                throw new RuntimeException("文件格式错误,请上传pdf,doc,docx格式的文件");
            }

            BizMaterialSubmission bizMaterialSubmission = new BizMaterialSubmission();
            bizMaterialSubmission.setTaskId(bizSubDTO.getTask_id());
            bizMaterialSubmission.setFileId(sysMapper.getFileByName(sysFile.getFileName()).getFileId());
            bizMaterialSubmission.setReportedValue(bizSubDTO.getReported_value());
            bizMaterialSubmission.setDataType(bizSubDTO.getData_type());
            bizMaterialSubmission.setSubmitBy(userId);
            bizMaterialSubmission.setSubmitDeptId(sysMapper.getUserById(userId).getDeptId());
            bizMaterialSubmission.setManageDeptId(bizMapper.getTaskById(bizSubDTO.getTask_id()).getDeptId());
            bizMaterialSubmission.setSubmitTime(new Date());
            bizMaterialSubmission.setFileSuffix(sysMapper.getFileByName(sysFile.getFileName()).getFileSuffix());
            bizMaterialSubmission.setFlowStatus(10);
            bizMaterialSubmission.setCurrentHandlerId(sysMapper.getDeptLeaderId(userId));
            bizMaterialSubmission.setIsDelete(0);

            bizMapper.createAudit(bizMaterialSubmission);
            Long subId = bizMapper.getNewestAuditId();

            // 更新task
            bizMapper.updateTaskStatus(bizSubDTO.getTask_id(),"2");


            // 发送审批信息（使用封装方法）
            sendNotice(userId,
                    bizMaterialSubmission.getCurrentHandlerId(),
                    "任务审核",
                    "任务审核",
                    "您有新的任务需要审核",
                    "0",
                    bizSubDTO.getTask_id());

            // 创建审批日志（使用封装方法）
            createAuditLog(subId, userId, "提交", 0, 10, "提交任务");
            return "提交成功，下一位审批人是"+ sysMapper.getUserById(bizMaterialSubmission.getCurrentHandlerId()).getUserName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //    根据taskId查询审批单
    public List<BizMaterialSubmission> getAudit(Long taskId,Long userId){
        try{
            return bizMapper.getAudit(taskId,userId);
        } catch (RuntimeException e) {
            throw new RuntimeException("获取审批单失败,请检查任务id是否正确");
        }
    }




//    //    审批任务
    @Transactional
    public Object audit(AuditDTO auditDTO, Long userId) {
        Long subId = auditDTO.getSub_id();
        try {
            BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(subId);
            if (bizMaterialSubmission == null) {
                throw new RuntimeException("该任务不存在");
            }

            Map<Integer,Long> nextHandlerMap = Map.of(
                    10, bizMapper.getTaskPrincipalId(bizMaterialSubmission.getTaskId()),
                    20, 110228L
            );
            Map<Integer,Long> backHandlerMap = Map.of(
                    10,bizMaterialSubmission.getSubmitBy(),
                    20,sysMapper.getDeptLeaderId(bizMaterialSubmission.getSubmitBy()),
                    30,bizMapper.getTaskPrincipalId(bizMaterialSubmission.getTaskId()),
                    -20,bizMaterialSubmission.getSubmitBy(),
                    -30,sysMapper.getDeptLeaderId(bizMaterialSubmission.getSubmitBy())
            );

            // 分支1：当前用户是处理人
            if (bizMaterialSubmission.getCurrentHandlerId().equals(userId)) {
                if (bizMaterialSubmission.getFlowStatus() == 10) {
                    // 部门负责人审核逻辑
                    if(auditDTO.getIs_pass()){
                        Long nextHandlerId = nextHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）

                        updateAuditStatus(subId, nextHandlerId, 20);

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                nextHandlerId,
                                "任务审核",
                                "任务审核",
                                "您有新的任务需要审核",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "通过", 10, 20, auditDTO.getTitle());

                        System.out.println("已审批，下一位审批人id为"+ nextHandlerId);
                        return "已审批，下一位审批人为" + sysMapper.getUserById(nextHandlerId).getUserName();
                    } else {
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, backHandlerId, -10);

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "退回", 10, -10, auditDTO.getTitle());

                        System.out.println("已退回，退回到id为" + backHandlerId);
                        return "已退回，退回到" + sysMapper.getUserById(backHandlerId).getUserName();
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == 20) {
                    if(auditDTO.getIs_pass()){
                        Long nextHandlerId = nextHandlerMap.get(bizMaterialSubmission.getFlowStatus());;// 管理员

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, nextHandlerId, 30);

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                nextHandlerId,
                                "任务审核",
                                "任务审核",
                                "您有新的任务需要审核",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "通过", 20, 30, auditDTO.getTitle());

                        System.out.println("已审批，下一位审批人id为"+ nextHandlerId);
                        return "已审批，下一位审批人为" + sysMapper.getUserById(nextHandlerId).getUserName();
                    }else {
//                      退回到提交人的部门负责人
//                        根据提交人id获取部门负责人id
                        Long submitBy = bizMaterialSubmission.getSubmitBy();
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, backHandlerId, -20);

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "退回", 20, -20, auditDTO.getTitle());

                        System.out.println("已退回，退回到id为" + backHandlerId);
                        return "已退回，退回到" + sysMapper.getUserById(backHandlerId).getUserName();
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == 30) {
                    if(auditDTO.getIs_pass()){

                        // 更新审批单状态（使用封装方法）
                        bizMaterialSubmission.setFlowStatus(40);
                        bizMapper.updateAudit(bizMaterialSubmission);
//                        更新任务状态
                        BizTask bizTask = bizMapper.getTaskById(bizMaterialSubmission.getTaskId());
                        bizTask.setCurrentValue(bizTask.getCurrentValue().add(bizMaterialSubmission.getReportedValue()));
                        if(bizTask.getCurrentValue().compareTo(bizTask.getTargetValue()) >= 0){
                            bizTask.setStatus("3");
                        }else {
                            bizTask.setStatus("1");
                        }
                        bizMapper.updateTask(bizTask);

                        // 发送通知，告知提交人审批过程已完成
                        sendNotice(userId,
                                bizMaterialSubmission.getSubmitBy(),
                                "任务审核完成",
                                "审核完成",
                                "您提交的审核任务已完成",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "通过", 30, 40, auditDTO.getTitle());

                        System.out.println("审批完成");
                        return "审批完成" ;
                    }else {
//                      退回到归口负责人
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, backHandlerId, -30);

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "退回", 30, -30, auditDTO.getTitle());

                        System.out.println("已退回，退回到id为" + backHandlerId);
                        return "已退回，退回到" + sysMapper.getUserById(backHandlerId).getUserName();
                    }
//                } else if (bizMaterialSubmission.getFlowStatus() == -10){
//
                }else if (bizMaterialSubmission.getFlowStatus() == -20) {
                    if(auditDTO.getIs_pass()){
                        throw new RuntimeException("请重新提交材料");
                    }else {
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());
                        updateAuditStatus(subId, backHandlerId, -10);
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());
                        createAuditLog(subId, userId, "退回", -20, -10, auditDTO.getTitle());
                        System.out.println("已退回，退回到id为" + backHandlerId);
                        return "已退回，退回到" + sysMapper.getUserById(backHandlerId).getUserName();
                    }
                }
                else if (bizMaterialSubmission.getFlowStatus() == -30) {
                    if(auditDTO.getIs_pass()){
                        throw new RuntimeException("请重新提交材料");
                    }
                    Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());
                    updateAuditStatus(subId, backHandlerId, -20);
                    sendNotice(userId,
                            backHandlerId,
                            "任务退回",
                            "任务退回",
                            "您提交的材料被退回",
                            "0",
                            bizMaterialSubmission.getTaskId());
                    createAuditLog(subId, userId, "退回", -30, -20, auditDTO.getTitle());
                    System.out.println("已退回，退回到id为" + backHandlerId);
                    return "已退回，退回到" + sysMapper.getUserById(backHandlerId).getUserName();
                }
                    else {// 补充：flowStatus不在枚举值范围内的返回值
                        throw new RuntimeException("不支持的审批流程状态：" + bizMaterialSubmission.getFlowStatus());
                    }
            } else {
                // 分支2：当前用户不是处理人
                throw new RuntimeException("您不是该任务的当前审批人，无法操作");
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
    //    退回重新提交材料
    @Transactional
    public String reSubmitMaterial(ReSubDTO resubDTO,Long userId){
        try{
            BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(resubDTO.getSub_id());
            Map<Integer, Long> nextHandlerMap= Map.of(
                    -10, sysMapper.getDeptLeaderId(userId),
                    -20, bizMapper.getTaskPrincipalId(bizMaterialSubmission.getTaskId()),
                    -30, 1L
            );

            Long nextHandlerId = nextHandlerMap.get(bizMaterialSubmission.getFlowStatus());

            if (bizMaterialSubmission == null){
                throw new RuntimeException("该任务不存在");
            }
            if (bizMaterialSubmission.getFlowStatus()>=0){
                throw new RuntimeException("该任务状态不是退回状态,无法重新提交");
            }
            bizMaterialSubmission.setReportedValue(resubDTO.getReported_value());
            bizMaterialSubmission.setDataType(resubDTO.getData_type());
            bizMaterialSubmission.setFileId(resubDTO.getFile_id());
            bizMaterialSubmission.setSubmitTime(new Date());
            bizMaterialSubmission.setFlowStatus(-bizMaterialSubmission.getFlowStatus());
            bizMaterialSubmission.setCurrentHandlerId(nextHandlerId);
            bizMapper.updateAudit(bizMaterialSubmission);

            sendNotice(userId,
                    nextHandlerId,
                    "任务审核完成",
                    "审核完成",
                    "您提交的审核任务已完成",
                    "0",
                    bizMaterialSubmission.getTaskId());

            createAuditLog(resubDTO.getSub_id(), userId, "重新提交", -bizMaterialSubmission.getFlowStatus(), bizMaterialSubmission.getFlowStatus(), "重新提交");
            return "已重新提交,审核人为"+sysMapper.getUserById(nextHandlerId).getUserName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public List<BizTask> getThirdLevelTasksByParentId(Long parentId){
        try {
            return bizMapper.getThirdLevelTasksByParentId(parentId);
        } catch (RuntimeException e) {
            throw new RuntimeException("获取任务失败,请检查任务id是否正确");
        }
    }

    public List<BizTask> getTasksByPrincipalId(Long principalId){
        try{
            return bizMapper.getTasksByPrincipalId(principalId);
        }catch (Exception e){
            throw new RuntimeException("获取任务失败,请检查负责人id是否正确");
        }


    }
    // 封装方法：发送系统通知
    private void sendNotice(Long fromUserId, Long toUserId, String triggerEvent,
                            String title, String content, String sourceType, Long sourceId) {
        SysNotice sysNotice = new SysNotice();
        sysNotice.setFromUserId(fromUserId);
        sysNotice.setToUserId(toUserId);
        sysNotice.setTriggerEvent(triggerEvent);
        sysNotice.setTitle(title);
        sysNotice.setContent(content);
        sysNotice.setSourceType(sourceType);
        sysNotice.setSourceId(sourceId);
        sysNotice.setIsRead("0");

        sysMapper.sendNotice(sysNotice);
        System.out.println("id=" + toUserId + "的用户收到一条通知：" + title);
    }

    // 封装方法：创建审批日志
    private void createAuditLog(Long subId, Long operatorId, String actionType,
                                Integer preStatus, Integer postStatus, String comment) {
        BizAuditLog bizAuditLog = new BizAuditLog();
        bizAuditLog.setLogId(null);
        bizAuditLog.setSubId(subId);
        bizAuditLog.setOperatorId(operatorId);
        bizAuditLog.setActionType(actionType);
        bizAuditLog.setPreStatus(preStatus);
        bizAuditLog.setPostStatus(postStatus);
        bizAuditLog.setComment(comment);
        bizAuditLog.setCreateTime(new Date());

        bizMapper.createAuditLog(bizAuditLog);
    }

    // 封装方法：更新审批单状态和处理人
    private void updateAuditStatus(Long subId, Long currentHandlerId, Integer flowStatus) {
        BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(subId);
        if (bizMaterialSubmission != null) {
            bizMaterialSubmission.setCurrentHandlerId(currentHandlerId);
            bizMaterialSubmission.setFlowStatus(flowStatus);
            bizMapper.updateAudit(bizMaterialSubmission);
        }
    }

//    TaskToTaskVo
    public BizTaskVo TaskToTaskVo(BizTask task){
        return new BizTaskVo(
                task.getTaskId(),
                task.getProjectId(),
                task.getParentId(),
                task.getAncestors(),
                task.getPhase(),
                task.getTaskCode(),
                task.getTaskName(),
                task.getLevel(),
                task.getDeptId(),
                sysMapper.getDeptById(task.getDeptId()).getDeptName(),
                task.getPrincipalId(),
                sysMapper.getUserById(task.getPrincipalId()).getUserName(),
                task.getLeaderId(),
                sysMapper.getUserById(task.getLeaderId()).getUserName(),
                task.getExpTarget(),
                task.getExpLevel(),
                task.getExpEffect(),
                task.getExpMaterialDesc(),
                task.getDataType(),
                task.getTargetValue(),
                task.getCurrentValue(),
                task.getWeight(),
                task.getProgress(),
                task.getStatus(),
                task.getIsDelete(),
                task.getCreateTime(),
                task.getUpdateTime()
        );
    }
//TaskListToTaskVoList
    public List<BizTaskVo> TaskListToTaskVoList(List<BizTask> tasks){
        List<BizTaskVo> taskVos = new ArrayList<>();
        for (BizTask task : tasks) {
            taskVos.add(TaskToTaskVo(task));
        }
        return taskVos;
    }


}