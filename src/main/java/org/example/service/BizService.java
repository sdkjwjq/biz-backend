package org.example.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.*;
import org.example.entity.dto.AuditDTO;
import org.example.entity.dto.BizSubDTO;
import org.example.entity.dto.FileUploadDTO;
import org.example.entity.dto.SysNoticeDTO;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.example.utils.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Service
public class BizService {

    @Autowired
    private BizMapper bizMapper;

    @Autowired
    private SysMapper sysMapper;

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
    @Transactional
    public void submitMaterial(BizSubDTO bizSubDTO,Long userId){
        try{
//            检查taskid是否存在
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()) == null){
                throw new RuntimeException("该任务不存在");
            }
//            验证任务必须为三级任务，否则无法提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getLevel() != 3){
                throw new RuntimeException("该任务不是三级任务,无法提交");
            }
//            验证任务状态，如果当前status为0或者2，则禁止提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getStatus().equals("0") || bizMapper.getTaskById(bizSubDTO.getTask_id()).getStatus().equals("2")){
                throw new RuntimeException("该任务状态未开始或正在审核中,无法提交");
            }
//            检查文件名是否存在
            if (sysMapper.getFileByName(bizSubDTO.getFilename()) == null){
                throw new RuntimeException("该文件名不存在");
            }
//            验证文件后缀，只能为pdf,doc,docx
            if (!bizSubDTO.getFilename().endsWith(".pdf") && !bizSubDTO.getFilename().endsWith(".doc") && !bizSubDTO.getFilename().endsWith(".docx")){
                throw new RuntimeException("文件格式错误,请上传pdf,doc,docx格式的文件");
            }
            BizMaterialSubmission bizMaterialSubmission = new BizMaterialSubmission();
            bizMaterialSubmission.setTaskId(bizSubDTO.getTask_id());
            bizMaterialSubmission.setFileId(sysMapper.getFileByName(bizSubDTO.getFilename()).getFileId());
            bizMaterialSubmission.setReportedValue(bizSubDTO.getReported_value());
            bizMaterialSubmission.setDataType(bizSubDTO.getData_type());
            bizMaterialSubmission.setSubmitBy(userId);
            bizMaterialSubmission.setSubmitDeptId(sysMapper.getUserById(userId).getDeptId());
            bizMaterialSubmission.setManageDeptId(bizMapper.getTaskById(bizSubDTO.getTask_id()).getPrincipalId());
            bizMaterialSubmission.setSubmitTime(new Date());
            bizMaterialSubmission.setFileSuffix(sysMapper.getFileByName(bizSubDTO.getFilename()).getFileSuffix());
            bizMaterialSubmission.setFlowStatus(10);
            bizMaterialSubmission.setCurrentHandlerId(bizMapper.getTaskLeaderId(bizSubDTO.getTask_id()));
            bizMaterialSubmission.setIsDelete(0);
            bizMapper.createAudit(bizMaterialSubmission);
//          更新task
            bizMapper.updateTaskStatus(bizSubDTO.getTask_id(),"2");
            System.out.println(2);
//            发送审批信息,审批顺序为：1.所在部门负责人，2.归口负责人，3.管理员。这里只需要给部门负责人
            SysNotice sysNotice = new SysNotice();
            sysNotice.setFromUserId(userId);
            sysNotice.setToUserId(bizMaterialSubmission.getCurrentHandlerId());

//            sysNotice.setType("审批");
            sysNotice.setTriggerEvent("任务审核");
            sysNotice.setTitle("任务审核");
            sysNotice.setContent("您有新的任务需要审核");
            sysNotice.setSourceType("0");
            sysNotice.setSourceId(bizSubDTO.getTask_id());
            sysNotice.setIsRead("0");
            //            输出一下，id=?的用户收到了一条审批信息
            System.out.println("id="+sysNotice.getToUserId()+"的用户收到一条审批信息");
            sysMapper.sendNotice(sysNotice);
//            创建审批日志
            BizAuditLog bizAuditLog = new BizAuditLog();
            bizAuditLog.setLogId(null);
            bizAuditLog.setSubId(bizMaterialSubmission.getSubId());
            bizAuditLog.setOperatorId(userId);
            bizAuditLog.setActionType("提交");
            bizAuditLog.setPreStatus(0);
            bizAuditLog.setPostStatus(10);
            bizAuditLog.setComment("提交任务");
            bizAuditLog.setCreateTime(new Date());
            bizMapper.createAuditLog(bizAuditLog);
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

//    审批任务
//    @Transactional
//    public Object audit(Long subId, AuditDTO auditDTO, Long userId) {
//        try {
//            BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(subId);
//            if (bizMaterialSubmission == null) {
//                throw new RuntimeException("该任务不存在");
//            }
//
//            // 分支1：当前用户是处理人
//            if (bizMaterialSubmission.getCurrentHandlerId().equals(userId)) {
//                if (auditDTO.getIsPass()) {
//                    if (bizMaterialSubmission.getFlowStatus() == 10) {
//                        // 部门负责人审核逻辑
//                        BizAuditLog bizAuditLog = new BizAuditLog(
//                                null,
//                                subId,
//                                userId,
//                                "通过",
//                                10,
//                                20,
//                                auditDTO.getTitle(),
//                                new Date()
//                        );
//                        bizMapper.createAuditLog(bizAuditLog);
//                        bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "20");
//                        SysNotice sysNotice = new SysNotice();
//                        sysNotice.setFromUserId(userId);
//                        sysNotice.setToUserId(bizMaterialSubmission.getManageDeptId());
//                        sysNotice.setType("审批");
//                        sysNotice.setTriggerEvent("任务审核");
//                        sysNotice.setTitle("任务审核");
//                        sysNotice.setContent("您有新的任务需要审核");
//                        sysNotice.setSourceType("0");
//                        sysNotice.setSourceId(bizMaterialSubmission.getTaskId());
//                        sysNotice.setIsRead("0");
//                        sysMapper.sendNotice(sysNotice);
//                        bizMapper.updateAuditFlowStatus(subId, 20);
//                        return "已审批，下一位审批人为" + sysMapper.getUserById(bizMaterialSubmission.getCurrentHandlerId()).getUserName();
//                    } else if (bizMaterialSubmission.getFlowStatus() == 20) {
//                        return "pass";
//                    } else if (bizMaterialSubmission.getFlowStatus() == 30) {
//                        return "pass";
//                    } else if (bizMaterialSubmission.getFlowStatus() == -10) {
//                        return "pass";
//                    } else if (bizMaterialSubmission.getFlowStatus() == -20) {
//                        return "pass";
//                    } else if (bizMaterialSubmission.getFlowStatus() == -30) {
//                        return "pass";
//                    } else {
//                        // 补充：flowStatus不在枚举值范围内的返回值
//                        throw new RuntimeException("不支持的审批流程状态：" + bizMaterialSubmission.getFlowStatus());
//                    }
//                } else {
//                    // 审核不通过的分支
//                    return "fail";
//                }
//            } else {
//                // 分支2：当前用户不是处理人
//                throw new RuntimeException("您不是该任务的当前审批人，无法操作");
//            }
//        } catch (RuntimeException e) {
//            throw new RuntimeException(e);
//        }
//    }


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

}
