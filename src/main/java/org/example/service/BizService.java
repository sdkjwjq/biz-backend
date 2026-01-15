package org.example.service;

import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.*;
import org.example.entity.dto.BizAuditDTO;
import org.example.entity.dto.BizSubDTO;
import org.example.entity.dto.BizReSubDTO;
import org.example.entity.dto.BizTaskDTO;
import org.example.entity.vo.BizTaskVo;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class BizService {

    private Long ADMIN_ID = 110228L;

    @Autowired
    private BizMapper bizMapper;

    @Autowired
    private SysMapper sysMapper;



    // 获取全部任务
    public List<BizTask> getAllTasks() {
        return bizMapper.getAllTasks();
    }

    // 根据id获取任务
    public BizTask getTaskById(Long taskId) {
        try {
            if (taskId == null) {
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null) {
                throw new RuntimeException("该任务不存在");
            }
            return bizMapper.getTaskById(taskId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 获取当前任务的所有子任务
    public List<BizTask> getAllChildrenTasks(Long taskId) {
        try {
            if (taskId == null) {
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null) {
                throw new RuntimeException("该任务不存在");
            }
            return bizMapper.getAllChildrenTasks(taskId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 获取当前任务的父任务
    public BizTask getParentTask(Long taskId) {
        try {
            if (taskId == null) {
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null) {
                throw new RuntimeException("该任务不存在");
            }
            Long parentId = bizMapper.getTaskById(taskId).getParentId();
            return bizMapper.getTaskById(parentId);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public List<BizTask> getTasksByDeptId(Long deptId) {
        try {
            return bizMapper.getTasksByDeptId(deptId);
        } catch (Exception e) {
            throw new RuntimeException("获取任务失败,请检查部门id是否正确");
        }
    }

    // 根据用户角色获取任务，角色为0和2返回所有任务，角色为1返回leaderid以及为自己的任务
    public List<BizTaskVo> getTasksByUserRole(Long userId) {
        try {
            SysUser sysUser = sysMapper.getUserById(userId);
            if (sysUser == null) {
                throw new RuntimeException("用户不存在");
            }

            if (sysUser.getRole().equals("0")) {
                return taskListToTaskVoList(bizMapper.getAllTasks());
            } else if (sysUser.getRole().equals("1")) {
                return taskListToTaskVoList(bizMapper.getTasks(sysUser.getUserId()));
            } else if (sysUser.getRole().equals("2")) {
                return taskListToTaskVoList(bizMapper.getTasksByPrincipalId(sysUser.getUserId()));
            } else {
                throw new RuntimeException("用户角色错误");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    添加任务
    public void addTask(BizTaskDTO taskDTO) {
        try {
//            只能添加三级任务,根据parent字段判断二级任务是否正确
            if (bizMapper.getTaskById(taskDTO.getParentId()) == null) {
                throw new RuntimeException("该二级任务不存在");
            }
            if (bizMapper.getTaskById(taskDTO.getParentId()).getLevel() != 2) {
                throw new RuntimeException("该任务不是二级任务,无法添加");
            }
            if (bizMapper.getTaskById(taskDTO.getParentId()).getDeptId() != taskDTO.getDeptId()) {
                throw new RuntimeException("该任务所属部门与二级任务部门不一致");
            }

            BizTask task = taskDTO2Task(taskDTO);
            task.setIsDelete(0);
            task.setCreateTime(new Date());
            task.setUpdateTime(new Date());
            bizMapper.addTask(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//更新任务
    public void updateTask(BizTaskDTO taskDTO) {
        try {
            if (bizMapper.getTaskById(taskDTO.getTaskId()) == null) {
                throw new RuntimeException("该任务不存在");
            }
            BizTask task = taskDTO2Task(taskDTO);
            task.setUpdateTime(new Date());
            bizMapper.updateTask(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    @Transactional
    public String submitMaterial(BizSubDTO bizSubDTO, Long userId) {
        try {
            // 检查taskid是否存在
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()) == null) {
                throw new RuntimeException("该任务不存在");
            }
            // 验证任务必须为三级任务，否则无法提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getLevel() != 3) {
                throw new RuntimeException("该任务不是三级任务,无法提交");
            }
            // 验证任务状态，如果当前status为2，则禁止提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getStatus().equals("2")) {
                throw new RuntimeException("该任务状态未开始或正在审核中,无法提交");
            }
            // 验证任务状态，如果当前status为3，则禁止提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getStatus().equals("3")) {
                throw new RuntimeException("该任务状态已完成,无法提交");
            }
            // 检查文件是否存在
            SysFile sysFile = sysMapper.getFileById(bizSubDTO.getFile_id());
            if (sysFile == null) {
                throw new RuntimeException("该文件不存在");
            }
            // 验证文件后缀，只能为pdf,doc,docx
            if (!sysFile.getFileName().endsWith(".pdf") && !sysFile.getFileName().endsWith(".doc")
                    && !sysFile.getFileName().endsWith(".docx")) {
                throw new RuntimeException("文件格式错误,请上传pdf,doc,docx格式的文件");
            }

            BizTask task = bizMapper.getTaskById(bizSubDTO.getTask_id());
            BizMaterialSubmission bizMaterialSubmission = new BizMaterialSubmission();
            bizMaterialSubmission.setTaskId(bizSubDTO.getTask_id());
            bizMaterialSubmission.setFileId(sysMapper.getFileByName(sysFile.getFileName()).getFileId());

            // 本次填报值只保留整数，并写入任务 current_value（过程即显示进度）
            BigDecimal rv = bizSubDTO.getReported_value() != null ? bizSubDTO.getReported_value() : BigDecimal.ZERO;
            rv = rv.setScale(0, RoundingMode.HALF_UP);
            bizMaterialSubmission.setReportedValue(rv);
            bizMaterialSubmission.setDataType(bizSubDTO.getData_type());
            bizMaterialSubmission.setSubmitBy(userId);
            bizMaterialSubmission.setSubmitDeptId(sysMapper.getUserById(userId).getDeptId());
            bizMaterialSubmission.setManageDeptId(task.getDeptId());
            bizMaterialSubmission.setSubmitTime(new Date());
            bizMaterialSubmission.setFileSuffix(sysMapper.getFileByName(sysFile.getFileName()).getFileSuffix());
            bizMaterialSubmission.setFlowStatus(10);
            //已修改，修改内容及原因：将部门审核人从任务的AuditorId改为提交人所在部门的负责人，确保flowStatus=10时审核人能正确收到通知
            // flowStatus = 10 表示"待[所在部门]审批"，部门审核人应该是提交人所在部门的负责人
            Long handlerId = sysMapper.getDeptLeaderId(userId);
            bizMaterialSubmission.setCurrentHandlerId(handlerId);
            bizMaterialSubmission.setIsDelete(0);

            bizMapper.createAudit(bizMaterialSubmission);
            Long subId = bizMapper.getNewestSubId();

            // 提交后任务进入"审核中"，并把 current_value 覆盖写为本次填报值
            BizTask bizTask = bizMapper.getTaskById(bizSubDTO.getTask_id());
            if (bizTask != null) {
                bizTask.setCurrentValue(rv);
                bizTask.setStatus("2");
                bizMapper.updateCurrentTask(bizTask);
            }

            // 发送审批信息（使用封装方法）
            //已修改，修改内容及原因：添加null检查，避免handlerId为null时发送通知导致数据库约束错误
            // 只有当 handlerId 不为 null 时才发送通知
            if (handlerId != null) {
                sendNotice(userId,
                        handlerId,
                        "任务审核",
                        "任务审核",
                        "您有新的任务需要审核",
                        "0",
                        bizSubDTO.getTask_id());
            }

            // 创建审批日志（使用封装方法）
            createAuditLog(subId, userId, "提交", 0, 10, "提交任务");
            //已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
            String resultMsg = "提交成功";
            if (handlerId != null) {
                try {
                    SysUser handler = sysMapper.getUserById(handlerId);
                    if (handler != null) {
                        resultMsg += "，下一位审批人是" + handler.getUserName();
                    } else {
                        resultMsg += "，下一位审批人ID为" + handlerId;
                    }
                } catch (Exception e) {
                    resultMsg += "，下一位审批人ID为" + handlerId;
                }
            }
            return resultMsg;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 根据taskId查询审批单
    public List<BizMaterialSubmission> getAudit(Long taskId, Long userId) {
        try {
            return bizMapper.getAudit(taskId, userId);
        } catch (RuntimeException e) {
            throw new RuntimeException("获取审批单失败,请检查任务id是否正确");
        }
    }

    // 获取“待我审批”的审批单（按 current_handler_id 查询）
    public List<BizMaterialSubmission> getTodoAudits(Long userId) {
        try {
            if (userId == null)
                throw new RuntimeException("userId为空");
            return bizMapper.getTodoAudits(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 根据 taskId 查询该任务全部审批单（用于任务详情抽屉展示完整流程）
    public List<BizMaterialSubmission> getAuditByTaskId(Long taskId, Long userId) {
        try {
            if (taskId == null)
                throw new RuntimeException("taskId为空");
            BizTask task = bizMapper.getTaskById(taskId);
            if (task == null)
                throw new RuntimeException("该任务不存在");

            SysUser me = sysMapper.getUserById(userId);
            if (me == null)
                throw new RuntimeException("用户不存在");

            // 最小权限校验：管理员/任务负责人/归口负责人可查看；或本人提交过该任务审批单也可查看
            if ("0".equals(me.getRole())
                    || (task.getLeaderId() != null && task.getLeaderId().equals(userId))
                    || (task.getPrincipalId() != null && task.getPrincipalId().equals(userId))) {
                return bizMapper.getAuditsByTaskId(taskId);
            }

            List<BizMaterialSubmission> list = bizMapper.getAuditsByTaskId(taskId);
            boolean submittedByMe = false;
            for (BizMaterialSubmission s : list) {
                if (s != null && s.getSubmitBy() != null && s.getSubmitBy().equals(userId)) {
                    submittedByMe = true;
                    break;
                }
            }
            if (!submittedByMe) {
                throw new RuntimeException("无权限查看该任务的审批记录");
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 根据 subId 查询审批操作日志（biz_audit_log）
    public List<BizAuditLog> getAuditLogs(Long subId, Long userId) {
        try {
            if (subId == null)
                throw new RuntimeException("subId为空");
            BizMaterialSubmission submission = bizMapper.getAuditBySubId(subId);
            if (submission == null)
                throw new RuntimeException("审批单不存在");

            BizTask task = bizMapper.getTaskById(submission.getTaskId());
            if (task == null)
                throw new RuntimeException("任务不存在");

            SysUser me = sysMapper.getUserById(userId);
            if (me == null)
                throw new RuntimeException("用户不存在");

            // 最小权限校验：管理员/任务部门负责人/归口负责人/提交人可查看
            boolean allowed = "0".equals(me.getRole())
                    || (task.getLeaderId() != null && task.getLeaderId().equals(userId))
                    || (task.getPrincipalId() != null && task.getPrincipalId().equals(userId))
                    || (submission.getSubmitBy() != null && submission.getSubmitBy().equals(userId));
            if (!allowed) {
                throw new RuntimeException("无权限查看该审批单的操作日志");
            }

            return bizMapper.getAuditLogsBySubId(subId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // // 审批任务
    @Transactional
    public Object audit(BizAuditDTO bizAuditDTO, Long userId) {
        Long subId = bizAuditDTO.getSub_id();
        try {
            BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(subId);
            if (bizMaterialSubmission == null) {
                throw new RuntimeException("该任务不存在");
            }

            //已修改，修改内容及原因：添加currentHandlerId的null检查，避免在equals比较时出现空指针异常
            // 检查 currentHandlerId 是否为 null
            Long currentHandlerId = bizMaterialSubmission.getCurrentHandlerId();
            if (currentHandlerId == null) {
                throw new RuntimeException("审批单的当前处理人未设置，无法进行审核");
            }

            Map<Integer, Long> nextHandlerMap = Map.of(
                    10, bizMapper.getTaskPrincipalId(bizMaterialSubmission.getTaskId()),
                    20, ADMIN_ID);

            //已修改，修改内容及原因：安全获取任务的AuditorId，避免getTaskById返回null时出现空指针异常
            // 安全获取任务的 AuditorId
            BizTask task = bizMapper.getTaskById(bizMaterialSubmission.getTaskId());
            Long taskAuditorId = (task != null) ? task.getAuditorId() : null;

            Map<Integer, Long> backHandlerMap = Map.of(
                    10, bizMaterialSubmission.getSubmitBy(),
                    20, taskAuditorId != null ? taskAuditorId : bizMaterialSubmission.getSubmitBy(),
                    30, bizMapper.getTaskPrincipalId(bizMaterialSubmission.getTaskId()),
                    -20, bizMaterialSubmission.getSubmitBy(),
                    -30, sysMapper.getDeptLeaderId(bizMaterialSubmission.getSubmitBy()));

            // 分支1：当前用户是处理人
            if (currentHandlerId.equals(userId)) {
                if (bizMaterialSubmission.getFlowStatus() == 10) {
                    // 部门负责人审核逻辑
                    if (bizAuditDTO.getIs_pass()) {
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
                        createAuditLog(subId, userId, "通过", 10, 20, bizAuditDTO.getTitle());

                        System.out.println("已审批，下一位审批人id为" + nextHandlerId);
                        //已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已审批";
                        if (nextHandlerId != null) {
                            try {
                                SysUser nextUser = sysMapper.getUserById(nextHandlerId);
                                if (nextUser != null) {
                                    resultMsg += "，下一位审批人为" + nextUser.getUserName();
                                } else {
                                    resultMsg += "，下一位审批人ID为" + nextHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，下一位审批人ID为" + nextHandlerId;
                            }
                        }
                        return resultMsg;
                    } else {
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, backHandlerId, -10);
                        // 退回后任务不应保持“审核中”，恢复为“进行中”，允许重新提交
                        bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "退回", 10, -10, bizAuditDTO.getTitle());

                        System.out.println("已退回，退回到id为" + backHandlerId);
                        //已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已退回";
                        if (backHandlerId != null) {
                            try {
                                SysUser backUser = sysMapper.getUserById(backHandlerId);
                                if (backUser != null) {
                                    resultMsg += "，退回到" + backUser.getUserName();
                                } else {
                                    resultMsg += "，退回到ID为" + backHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，退回到ID为" + backHandlerId;
                            }
                        }
                        return resultMsg;
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == 20) {
                    if (bizAuditDTO.getIs_pass()) {
                        Long nextHandlerId = nextHandlerMap.get(bizMaterialSubmission.getFlowStatus());
                        ;// 管理员

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
                        createAuditLog(subId, userId, "通过", 20, 30, bizAuditDTO.getTitle());

                        System.out.println("已审批，下一位审批人id为" + nextHandlerId);
                        //已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已审批";
                        if (nextHandlerId != null) {
                            try {
                                SysUser nextUser = sysMapper.getUserById(nextHandlerId);
                                if (nextUser != null) {
                                    resultMsg += "，下一位审批人为" + nextUser.getUserName();
                                } else {
                                    resultMsg += "，下一位审批人ID为" + nextHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，下一位审批人ID为" + nextHandlerId;
                            }
                        }
                        return resultMsg;
                    } else {
                        // 退回到提交人的部门负责人
                        // 根据提交人id获取部门负责人id
                        Long submitBy = bizMaterialSubmission.getSubmitBy();
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, backHandlerId, -20);
                        // 退回后任务不应保持“审核中”，恢复为“进行中”，允许重新提交
                        bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "退回", 20, -20, bizAuditDTO.getTitle());

                        System.out.println("已退回，退回到id为" + backHandlerId);
                        //已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已退回";
                        if (backHandlerId != null) {
                            try {
                                SysUser backUser = sysMapper.getUserById(backHandlerId);
                                if (backUser != null) {
                                    resultMsg += "，退回到" + backUser.getUserName();
                                } else {
                                    resultMsg += "，退回到ID为" + backHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，退回到ID为" + backHandlerId;
                            }
                        }
                        return resultMsg;
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == 30) {
                    if (bizAuditDTO.getIs_pass()) {

                        // 更新审批单状态（使用封装方法）
                        bizMaterialSubmission.setFlowStatus(40);
                        bizMapper.updateAudit(bizMaterialSubmission);
                        // 更新任务状态
                        BizTask bizTask = bizMapper.getTaskById(bizMaterialSubmission.getTaskId());
                        // 口径调整：任务 current_value 以“本次填报值”为准（覆盖写），不再在最终通过时重复累加
                        BigDecimal rv = bizMaterialSubmission.getReportedValue() != null ? bizMaterialSubmission.getReportedValue() : BigDecimal.ZERO;
                        rv = rv.setScale(0, RoundingMode.HALF_UP);
                        bizTask.setCurrentValue(rv);
                        if (bizTask.getCurrentValue().compareTo(bizTask.getTargetValue()) >= 0) {
                            bizTask.setStatus("3");
                        } else {
                            bizTask.setStatus("1");
                        }
                        bizMapper.updateCurrentTask(bizTask);

                        // 发送通知，告知提交人审批过程已完成
                        sendNotice(userId,
                                bizMaterialSubmission.getSubmitBy(),
                                "任务审核完成",
                                "审核完成",
                                "您提交的审核任务已完成",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "通过", 30, 40, bizAuditDTO.getTitle());

                        System.out.println("审批完成");
                        return "审批完成";
                    } else {
                        // 退回到归口负责人
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, backHandlerId, -30);
                        // 退回后任务不应保持“审核中”，恢复为“进行中”，允许重新提交
                        bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "退回", 30, -30, bizAuditDTO.getTitle());

                        System.out.println("已退回，退回到id为" + backHandlerId);
                        //已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已退回";
                        if (backHandlerId != null) {
                            try {
                                SysUser backUser = sysMapper.getUserById(backHandlerId);
                                if (backUser != null) {
                                    resultMsg += "，退回到" + backUser.getUserName();
                                } else {
                                    resultMsg += "，退回到ID为" + backHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，退回到ID为" + backHandlerId;
                            }
                        }
                        return resultMsg;
                    }
                    // } else if (bizMaterialSubmission.getFlowStatus() == -10){
                    //
                } else if (bizMaterialSubmission.getFlowStatus() == -20) {
                    if (bizAuditDTO.getIs_pass()) {
                        throw new RuntimeException("请重新提交材料");
                    } else {
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());
                        updateAuditStatus(subId, backHandlerId, -10);
                        // 退回后任务不应保持“审核中”，恢复为“进行中”，允许重新提交
                        bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());
                        createAuditLog(subId, userId, "退回", -20, -10, bizAuditDTO.getTitle());
                        System.out.println("已退回，退回到id为" + backHandlerId);
                        //已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已退回";
                        if (backHandlerId != null) {
                            try {
                                SysUser backUser = sysMapper.getUserById(backHandlerId);
                                if (backUser != null) {
                                    resultMsg += "，退回到" + backUser.getUserName();
                                } else {
                                    resultMsg += "，退回到ID为" + backHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，退回到ID为" + backHandlerId;
                            }
                        }
                        return resultMsg;
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == -30) {
                    if (bizAuditDTO.getIs_pass()) {
                        throw new RuntimeException("请重新提交材料");
                    }
                    Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());
                    updateAuditStatus(subId, backHandlerId, -20);
                    // 退回后任务不应保持“审核中”，恢复为“进行中”，允许重新提交
                    bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");
                    sendNotice(userId,
                            backHandlerId,
                            "任务退回",
                            "任务退回",
                            "您提交的材料被退回",
                            "0",
                            bizMaterialSubmission.getTaskId());
                    createAuditLog(subId, userId, "退回", -30, -20, bizAuditDTO.getTitle());
                    System.out.println("已退回，退回到id为" + backHandlerId);
                    return "已退回，退回到" + sysMapper.getUserById(backHandlerId).getUserName();
                } else {// 补充：flowStatus不在枚举值范围内的返回值
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

    // 退回重新提交材料
    @Transactional
    public String reSubmitMaterial(BizReSubDTO resubDTOBiz, Long userId) {
        try {
            BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(resubDTOBiz.getSub_id());
            if (bizMaterialSubmission == null) {
                throw new RuntimeException("该任务不存在");
            }
            Map<Integer, Long> nextHandlerMap = Map.of(
                    -10, sysMapper.getDeptLeaderId(userId),
                    -20, bizMapper.getTaskPrincipalId(bizMaterialSubmission.getTaskId()),
                    -30, 1L);

            Long nextHandlerId = nextHandlerMap.get(bizMaterialSubmission.getFlowStatus());
            if (bizMaterialSubmission.getFlowStatus() >= 0) {
                throw new RuntimeException("该任务状态不是退回状态,无法重新提交");
            }
            // 重新提交同样只保留整数，并覆盖写任务 current_value
            BigDecimal rv = resubDTOBiz.getReported_value() != null ? resubDTOBiz.getReported_value() : BigDecimal.ZERO;
            rv = rv.setScale(0, RoundingMode.HALF_UP);
            bizMaterialSubmission.setReportedValue(rv);
            bizMaterialSubmission.setDataType(resubDTOBiz.getData_type());
            bizMaterialSubmission.setFileId(resubDTOBiz.getFile_id());
            bizMaterialSubmission.setSubmitTime(new Date());
            bizMaterialSubmission.setFlowStatus(-bizMaterialSubmission.getFlowStatus());
            bizMaterialSubmission.setCurrentHandlerId(nextHandlerId);
            bizMapper.updateAudit(bizMaterialSubmission);

            // 重新提交后恢复任务状态为“审核中”，并覆盖写 current_value
            BizTask bizTask = bizMapper.getTaskById(bizMaterialSubmission.getTaskId());
            if (bizTask != null) {
                bizTask.setCurrentValue(rv);
                bizTask.setStatus("2");
                bizMapper.updateCurrentTask(bizTask);
            }

            //已修改，修改内容及原因：添加null检查，避免nextHandlerId为null时发送通知导致数据库约束错误
            // 只有当 nextHandlerId 不为 null 时才发送通知
            if (nextHandlerId != null) {
                sendNotice(userId,
                        nextHandlerId,
                        "任务审核",
                        "任务审核",
                        "您有新的任务需要审核",
                        "0",
                        bizMaterialSubmission.getTaskId());
            }

            createAuditLog(resubDTOBiz.getSub_id(), userId, "重新提交", -bizMaterialSubmission.getFlowStatus(),
                    bizMaterialSubmission.getFlowStatus(), "重新提交");
            //已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
            String resultMsg = "已重新提交";
            if (nextHandlerId != null) {
                try {
                    SysUser handler = sysMapper.getUserById(nextHandlerId);
                    if (handler != null) {
                        resultMsg += ",审核人为" + handler.getUserName();
                    }
                } catch (Exception e) {
                    // 忽略获取用户信息失败的情况
                }
            }
            return resultMsg;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //    根据file_id下载文件
    public void downloadTaskFile(Long taskId, HttpServletResponse response) throws IOException {
        // 1. 根据任务id查询最新的审批单
        BizMaterialSubmission  bizMaterialSubmission = bizMapper.getLatestAuditByTaskId(taskId);
        SysFile sysFile = sysMapper.getFileById(bizMaterialSubmission.getFileId());
        if (sysFile == null) {
            throw new RuntimeException("文件不存在");
        }

        // 2. 构建文件路径
        String fullPath = System.getProperty("user.dir") + sysFile.getFilePath();
        File file = new File(fullPath);
        if (!file.exists()) {
            throw new RuntimeException("文件已被删除或移动");
        }

        // 3. 设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + URLEncoder.encode(sysFile.getFileName(), StandardCharsets.UTF_8.name()) + "\"");
        response.setContentLengthLong(file.length());

        // 4. 写入文件流
        try (InputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }
    }



    public List<BizTask> getThirdLevelTasksByParentId(Long parentId) {
        try {
            return bizMapper.getThirdLevelTasksByParentId(parentId);
        } catch (RuntimeException e) {
            throw new RuntimeException("获取任务失败,请检查任务id是否正确");
        }
    }

    public List<BizTask> getTasksByPrincipalId(Long principalId) {
        try {
            return bizMapper.getTasksByPrincipalId(principalId);
        } catch (Exception e) {
            throw new RuntimeException("获取任务失败,请检查负责人id是否正确");
        }

    }

    // 封装方法：发送系统通知
    private void sendNotice(Long fromUserId, Long toUserId, String triggerEvent,
                            String title, String content, String sourceType, Long sourceId) {
        //已修改，修改内容及原因：添加toUserId的null检查，避免toUserId为null时插入数据库导致约束错误
        // 如果 toUserId 为 null，不发送通知，避免数据库约束错误
        if (toUserId == null) {
            System.out.println("警告：无法发送通知，接收人ID为空。标题：" + title);
            return;
        }
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

    // TaskToTaskVo
    public BizTaskVo taskToTaskVo(BizTask task) {
        BizTaskVo taskVo = new BizTaskVo();
        taskVo.setTaskId(task.getTaskId());
        taskVo.setProjectId(task.getProjectId());
        taskVo.setParentId(task.getParentId());
        taskVo.setAncestors(task.getAncestors());
        taskVo.setPhase(task.getPhase());
        taskVo.setTaskCode(task.getTaskCode());
        taskVo.setTaskName(task.getTaskName());
        taskVo.setLevel(task.getLevel());
        taskVo.setComment(task.getComment());
        taskVo.setDeptId(task.getDeptId());
        taskVo.setDeptName(sysMapper.getDeptById(task.getDeptId()).getDeptName());
        taskVo.setPrincipalId(task.getPrincipalId());
        taskVo.setPrincipalName(sysMapper.getUserById(task.getPrincipalId()).getUserName());
//        避免空指针错误
        if(task.getAuditorId()!=null){
            taskVo.setAuditorId(task.getAuditorId());
            taskVo.setAuditorName(sysMapper.getUserById(task.getAuditorId()).getUserName());
        }else {
            taskVo.setAuditorId(null);
            taskVo.setAuditorName("无");
        }
//        taskVo.setAuditName(sysMapper.getUserById(task.getAuditorId()).getUserName());
        taskVo.setLeaderId(task.getLeaderId());
        taskVo.setLeaderName(sysMapper.getUserById(task.getLeaderId()).getUserName());
        taskVo.setExpTarget(task.getExpTarget());
        taskVo.setExpLevel(task.getExpLevel());
        taskVo.setExpEffect(task.getExpEffect());
        taskVo.setExpMaterialDesc(task.getExpMaterialDesc());
        taskVo.setDataType(task.getDataType());
        taskVo.setTargetValue(task.getTargetValue());
        taskVo.setCurrentValue(task.getCurrentValue());
        taskVo.setWeight(task.getWeight());
        taskVo.setProgress(task.getProgress());
        taskVo.setStatus(task.getStatus());
        taskVo.setIsDelete(task.getIsDelete());
        taskVo.setCreateTime(task.getCreateTime());
        taskVo.setUpdateTime(task.getUpdateTime());
        return taskVo;
    }

    // TaskListToTaskVoList
    public List<BizTaskVo> taskListToTaskVoList(List<BizTask> tasks) {
        List<BizTaskVo> taskVos = new ArrayList<>();
        for (BizTask task : tasks) {
            taskVos.add(taskToTaskVo(task));
        }
        return taskVos;
    }

//    TaskDTO2Task
    public BizTask taskDTO2Task(BizTaskDTO taskDTO) {
        BizTask task = new BizTask();
        task.setTaskId(taskDTO.getTaskId());
        task.setProjectId(taskDTO.getProjectId());
        task.setParentId(taskDTO.getParentId());
        task.setAncestors(taskDTO.getAncestors());
        task.setPhase(taskDTO.getPhase());
        task.setTaskCode(taskDTO.getTaskCode());
        task.setTaskName(taskDTO.getTaskName());
        task.setLevel(taskDTO.getLevel());
        task.setComment(taskDTO.getComment());
        task.setDeptId(taskDTO.getDeptId());
        task.setAuditorId(taskDTO.getAuditorId());
        task.setPrincipalId(taskDTO.getPrincipalId());
        task.setLeaderId(taskDTO.getLeaderId());
        task.setExpTarget(taskDTO.getExpTarget());
        task.setExpLevel(taskDTO.getExpLevel());
        task.setExpEffect(taskDTO.getExpEffect());
        task.setExpMaterialDesc(taskDTO.getExpMaterialDesc());
        task.setDataType(taskDTO.getDataType());
        task.setTargetValue(taskDTO.getTargetValue());
        task.setCurrentValue(taskDTO.getCurrentValue());
        task.setWeight(taskDTO.getWeight());
        task.setProgress(taskDTO.getProgress());
        task.setStatus(taskDTO.getStatus());
        return task;
    }

}