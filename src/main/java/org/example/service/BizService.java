package org.example.service;

import org.example.entity.*;
import org.example.entity.dto.BizAuditDTO;
import org.example.entity.dto.BizSubDTO;
import org.example.entity.dto.BizReSubDTO;
import org.example.entity.dto.BizTaskDTO;
import org.example.entity.vo.*;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 业务服务类：处理所有业务逻辑
 */
@Service
public class BizService {

    /** 管理员ID */
    private Long ADMIN_ID = 110228L;

    @Autowired
    private BizMapper bizMapper;

    @Autowired
    private SysMapper sysMapper;

    /**
     * 获取全部任务
     * @return 任务列表
     */
    public List<BizTask> getAllTasks() {
        return bizMapper.getAllTasks();
    }

    /**
     * 根据id获取任务
     * @param taskId 任务ID
     * @return 任务对象
     */
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

    /**
     * 获取当前任务的所有子任务
     * @param taskId 任务ID
     * @return 子任务列表
     */
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

    /**
     * 获取当前任务的父任务
     * @param taskId 任务ID
     * @return 父任务对象
     */
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

    /**
     * 根据部门ID获取任务
     * @param deptId 部门ID
     * @return 任务列表
     */
    public List<BizTask> getTasksByDeptId(Long deptId) {
        try {
            return bizMapper.getTasksByDeptId(deptId);
        } catch (Exception e) {
            throw new RuntimeException("获取任务失败,请检查部门id是否正确");
        }
    }

    /**
     * 根据用户角色获取任务
     * 角色为0和2返回所有任务，角色为1返回leaderid以及为自己的任务
     * @param userId 用户ID
     * @return 任务视图对象列表
     */
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

    /**
     * 添加任务
     * 只能添加三级任务,根据parent字段判断二级任务是否正确
     * @param taskDTO 任务数据
     */
    public void addTask(BizTaskDTO taskDTO) {
        try {
            // 只能添加三级任务,根据parent字段判断二级任务是否正确
            if (bizMapper.getTaskById(taskDTO.getParentId()) == null) {
                throw new RuntimeException("该二级任务不存在");
            }
            if (bizMapper.getTaskById(taskDTO.getParentId()).getLevel() != 2) {
                throw new RuntimeException("该任务不是二级任务,无法添加");
            }
            if (bizMapper.getTaskById(taskDTO.getParentId()).getDeptId() != taskDTO.getDeptId()) {
                throw new RuntimeException("该任务所属部门与二级任务部门不一致");
            }
            if(taskDTO.getProjectId()!=1){
                throw new RuntimeException("该任务所属项目id不为1");
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

    /**
     * 更新任务
     * @param taskDTO 任务数据
     */
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

    /**
     * 完成任务
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    public String finishTask(Long taskId, Long userId){
        try {
            if (userId == null) {
                throw new RuntimeException("用户ID为空");
            }
            SysUser sysUser = sysMapper.getUserById(userId);
            if (sysUser == null) {
                throw new RuntimeException("用户不存在");
            }
            if (!sysUser.getRole().equals("0")) {
                throw new RuntimeException("仅限管理员访问");
            }
            if (bizMapper.getTaskById(taskId) == null) {
                throw new RuntimeException("该任务不存在");
            }
            if (bizMapper.getTaskById(taskId).getStatus().equals("2")) {
                throw new RuntimeException("该任务正在审核中，请先完成审核");
            }
            if (bizMapper.getTaskById(taskId).getStatus().equals("3")) {
                throw new RuntimeException("该任务已完成，请勿重复提交");
            }
            BizTask taskById = bizMapper.getTaskById(taskId);
            taskById.setStatus("3");
            taskById.setCurrentValue(taskById.getTargetValue());
            bizMapper.updateTask(taskById);
            sendNotice(userId, taskById.getLeaderId(), "任务完成", "任务已完成", "任务"+taskById.getTaskName()+"已完成", "0", taskId);
            createAuditLog(taskId, userId, "任务完成", 1, 3, "任务完成");
            return "任务"+taskById.getTaskName()+"已完成";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 提交材料
     * @param bizSubDTO 提交数据
     * @param userId 用户ID
     * @return 操作结果
     */
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
            // 已修改，修改内容及原因：将部门审核人从任务的AuditorId改为提交人所在部门的负责人，确保flowStatus=10时审核人能正确收到通知
            // flowStatus = 10 表示"待[所在部门]审批"
            Long handlerId = task.getAuditorId();
            bizMaterialSubmission.setCurrentHandlerId(handlerId);
            bizMaterialSubmission.setIsDelete(0);

            bizMapper.createAudit(bizMaterialSubmission);
            Long subId = bizMapper.getNewestSubId();

            // 提交后任务进入"审核中"，并把 current_value 覆盖写为本次填报值
            BizTask bizTask = bizMapper.getTaskById(bizSubDTO.getTask_id());
            if (bizTask != null) {
                bizTask.setCurrentValue(rv);
                bizTask.setStatus("2");
                bizTask.setComment(bizSubDTO.getComment());
                bizTask.setUpdateTime(new Date());
                bizMapper.updateTask(bizTask);
            }

            // 发送审批信息（使用封装方法）
            // 已修改，修改内容及原因：添加null检查，避免handlerId为null时发送通知导致数据库约束错误
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
            // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
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

    /**
     * 根据taskId查询审批单
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 审批视图对象列表
     */
    public List<BizAuditVO> getAudit(Long taskId, Long userId) {
        try {
            return auditListToAuditVoList(bizMapper.getAudit(taskId, userId));
        } catch (RuntimeException e) {
            throw new RuntimeException("获取审批单失败,请检查任务id是否正确");
        }
    }

    /**
     * 获取"待我审批"的审批单（按 current_handler_id 查询）
     * @param userId 用户ID
     * @return 审批视图对象列表
     */
    public List<BizAuditVO> getTodoAudits(Long userId) {
        try {
            if (userId == null)
                throw new RuntimeException("userId为空");
            return auditListToAuditVoList(bizMapper.getTodoAudits(userId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据 taskId 查询该任务全部审批单（用于任务详情抽屉展示完整流程）
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 审批视图对象列表
     */
    public List<BizAuditVO> getAuditByTaskId(Long taskId, Long userId) {
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
                    || (task.getAuditorId() != null && task.getAuditorId().equals(userId))
                    || (task.getPrincipalId() != null && task.getPrincipalId().equals(userId))) {
                return auditListToAuditVoList(bizMapper.getAuditsByTaskId(taskId));
            }

            List<BizAuditVO> list = auditListToAuditVoList(bizMapper.getAuditsByTaskId(taskId));
            boolean submittedByMe = false;
            for (BizAuditVO s : list) {
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

    /**
     * 根据 subId 查询审批操作日志（biz_audit_log）
     * @param subId 提交ID
     * @param userId 用户ID
     * @return 审批日志列表
     */
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
                    || (task.getAuditorId() != null && task.getAuditorId().equals(userId))
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

    /**
     * 审批任务
     * @param bizAuditDTO 审批数据
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    public Object audit(BizAuditDTO bizAuditDTO, Long userId) {
        Long subId = bizAuditDTO.getSub_id();
        try {
            BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(subId);
            if (bizMaterialSubmission == null) {
                throw new RuntimeException("该任务不存在");
            }

            // 已修改，修改内容及原因：添加currentHandlerId的null检查，避免在equals比较时出现空指针异常
            // 检查 currentHandlerId 是否为 null
            Long currentHandlerId = bizMaterialSubmission.getCurrentHandlerId();
            if (currentHandlerId == null) {
                throw new RuntimeException("审批单的当前处理人未设置，无法进行审核");
            }

            Map<Integer, Long> nextHandlerMap = Map.of(
                    10, bizMapper.getTaskPrincipalId(bizMaterialSubmission.getTaskId()),
                    20, ADMIN_ID);

            // 已修改，修改内容及原因：安全获取任务的AuditorId，避免getTaskById返回null时出现空指针异常
            // 安全获取任务的 AuditorId
            BizTask task = bizMapper.getTaskById(bizMaterialSubmission.getTaskId());
            Long taskAuditorId = (task != null) ? task.getAuditorId() : null;

            Map<Integer, Long> backHandlerMap = Map.of(
                    10, bizMaterialSubmission.getSubmitBy(),
                    20, taskAuditorId != null ? taskAuditorId : bizMaterialSubmission.getSubmitBy(),
                    30, bizMapper.getTaskPrincipalId(bizMaterialSubmission.getTaskId()),
                    -20, bizMaterialSubmission.getSubmitBy(),
                    -30, taskAuditorId != null ? taskAuditorId : bizMaterialSubmission.getSubmitBy());

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
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
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
                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
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
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
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
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
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
                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
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
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
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
                        // 口径调整：任务 current_value 以"本次填报值"为准（覆盖写），不再在最终通过时重复累加
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
                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
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
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
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
                } else if (bizMaterialSubmission.getFlowStatus() == -20) {
                    if (bizAuditDTO.getIs_pass()) {
                        throw new RuntimeException("请重新提交材料");
                    } else {
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());
                        updateAuditStatus(subId, backHandlerId, -10);
                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
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
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
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
                    // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
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

    /**
     * 退回重新提交材料
     * @param resubDTOBiz 重新提交数据
     * @param userId 用户ID
     * @return 操作结果
     */
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

            // 重新提交后恢复任务状态为"审核中"，并覆盖写 current_value
            BizTask bizTask = bizMapper.getTaskById(bizMaterialSubmission.getTaskId());
            if (bizTask != null) {
                bizTask.setCurrentValue(rv);
                bizTask.setStatus("2");
                bizMapper.updateCurrentTask(bizTask);
            }

            // 已修改，修改内容及原因：添加null检查，避免nextHandlerId为null时发送通知导致数据库约束错误
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
            // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
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

    /**
     * 撤回提交申请
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 操作结果
     */
    public Object drawbackSubmit(Long taskId, Long userId){
        try{
            BizMaterialSubmission bizMaterialSubmission = bizMapper.getNewestAudit(taskId);
            if (bizMaterialSubmission == null) {
                throw new RuntimeException("该任务不存在");
            }
            if (!bizMaterialSubmission.getSubmitBy().equals(userId)){
                throw new RuntimeException("您不是该任务的提交人，无法撤回");
            }
            bizMaterialSubmission.setIsDelete(1);
            bizMapper.updateAudit(bizMaterialSubmission);
            createAuditLog(bizMaterialSubmission.getSubId(), userId, "撤回提交", bizMaterialSubmission.getFlowStatus(), -bizMaterialSubmission.getFlowStatus(), "撤回提交");
            return "已撤回提交";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取该任务上一次审批通过的文件
     * @param taskId 任务ID
     * @return 文件上传视图对象
     */
    public FileUploadVO getLastCycleFiles(Long taskId) {
        try{
            BizTask task = bizMapper.getTaskById(taskId);
            if (task == null) {
                throw new RuntimeException("任务不存在");
            }
            BizMaterialSubmission submission = bizMapper.getLatestApprovedAuditByTaskId(taskId);
            if (submission == null) {
                return null;
            }
            FileUploadVO fileUploadVo = new FileUploadVO();
            fileUploadVo.setFileId(submission.getFileId());
            fileUploadVo.setFilename(sysMapper.getFileById(submission.getFileId()).getFileName());
            fileUploadVo.setFilepath(sysMapper.getFileById(submission.getFileId()).getFileUrl());
            return fileUploadVo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据父任务ID获取三级任务
     * @param parentId 父任务ID
     * @return 任务列表
     */
    public List<BizTask> getThirdLevelTasksByParentId(Long parentId) {
        try {
            return bizMapper.getThirdLevelTasksByParentId(parentId);
        } catch (RuntimeException e) {
            throw new RuntimeException("获取任务失败,请检查任务id是否正确");
        }
    }

    /**
     * 根据归口负责人ID获取任务
     * @param principalId 归口负责人ID
     * @return 任务列表
     */
    public List<BizTask> getTasksByPrincipalId(Long principalId) {
        try {
            return bizMapper.getTasksByPrincipalId(principalId);
        } catch (Exception e) {
            throw new RuntimeException("获取任务失败,请检查负责人id是否正确");
        }
    }

    /**
     * 封装方法：发送系统通知
     * @param fromUserId 发送人ID
     * @param toUserId 接收人ID
     * @param triggerEvent 触发事件
     * @param title 标题
     * @param content 内容
     * @param sourceType 来源类型
     * @param sourceId 来源ID
     */
    private void sendNotice(Long fromUserId, Long toUserId, String triggerEvent,
                            String title, String content, String sourceType, Long sourceId) {
        // 已修改，修改内容及原因：添加toUserId的null检查，避免toUserId为null时插入数据库导致约束错误
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

    /**
     * 封装方法：创建审批日志
     * @param subId 提交ID
     * @param operatorId 操作人ID
     * @param actionType 动作类型
     * @param preStatus 前状态
     * @param postStatus 后状态
     * @param comment 意见
     */
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

    /**
     * 封装方法：更新审批单状态和处理人
     * @param subId 提交ID
     * @param currentHandlerId 当前处理人ID
     * @param flowStatus 流程状态
     */
    private void updateAuditStatus(Long subId, Long currentHandlerId, Integer flowStatus) {
        BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(subId);
        if (bizMaterialSubmission != null) {
            bizMaterialSubmission.setCurrentHandlerId(currentHandlerId);
            bizMaterialSubmission.setFlowStatus(flowStatus);
            bizMapper.updateAudit(bizMaterialSubmission);
        }
    }

    /**
     * TaskToTaskVo转换方法
     * @param task 任务实体
     * @return 任务视图对象
     */
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
        // 避免空指针错误
        if(task.getAuditorId()!=null){
            taskVo.setAuditorId(task.getAuditorId());
            taskVo.setAuditorName(sysMapper.getUserById(task.getAuditorId()).getUserName());
        }else {
            taskVo.setAuditorId(null);
            taskVo.setAuditorName("无");
        }
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

    /**
     * TaskListToTaskVoList转换方法
     * @param tasks 任务实体列表
     * @return 任务视图对象列表
     */
    public List<BizTaskVo> taskListToTaskVoList(List<BizTask> tasks) {
        List<BizTaskVo> taskVos = new ArrayList<>();
        for (BizTask task : tasks) {
            taskVos.add(taskToTaskVo(task));
        }
        return taskVos;
    }

    /**
     * TaskDTO转Task转换方法
     * @param taskDTO 任务数据传输对象
     * @return 任务实体
     */
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

    /**
     * Audit转AuditVO转换方法
     * @param audit 材料提交实体
     * @return 审批视图对象
     */
    public BizAuditVO auditToAuditVo(BizMaterialSubmission audit){
        BizAuditVO bizAuditVO = new BizAuditVO();
        bizAuditVO.setSubId(audit.getSubId());
        bizAuditVO.setTaskId(audit.getTaskId());
        bizAuditVO.setFileId(audit.getFileId());
        bizAuditVO.setFilename(sysMapper.getFileById(audit.getFileId()).getFileName());
        bizAuditVO.setReportedValue(audit.getReportedValue());
        bizAuditVO.setDataType(audit.getDataType());
        bizAuditVO.setSubmitBy(audit.getSubmitBy());
        bizAuditVO.setSubmitDeptId(audit.getSubmitDeptId());
        bizAuditVO.setManageDeptId(audit.getManageDeptId());
        bizAuditVO.setSubmitTime(audit.getSubmitTime());
        bizAuditVO.setFileSuffix(audit.getFileSuffix());
        bizAuditVO.setFlowStatus(audit.getFlowStatus());
        bizAuditVO.setCurrentHandlerId(audit.getCurrentHandlerId());
        bizAuditVO.setIsDelete(audit.getIsDelete());
        return bizAuditVO;
    }

    /**
     * AuditList转AuditVoList转换方法
     * @param audits 材料提交实体列表
     * @return 审批视图对象列表
     */
    public List<BizAuditVO> auditListToAuditVoList(List<BizMaterialSubmission> audits){
        List<BizAuditVO> auditVos = new ArrayList<>();
        for (BizMaterialSubmission audit : audits) {
            auditVos.add(auditToAuditVo(audit));
        }
        return auditVos;
    }

    // 中期截止年份
    private static final Integer MID_TERM_END_YEAR = 2028;

    /**
     * 获取看板数据汇总
     * @return 看板数据汇总
     */
    public DashboardSummaryVO getDashboardSummary() {
        DashboardSummaryVO summary = new DashboardSummaryVO();

        try {
            // 设置统计时间
            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(Calendar.YEAR);
            summary.setCurrentYear(String.valueOf(currentYear));
            summary.setMidTermEndYear(String.valueOf(MID_TERM_END_YEAR));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            summary.setUpdateTime(sdf.format(new Date()));

            // 1. 计算整体数据
            calculateOverallData(summary, currentYear);

            // 2. 计算部门数据
            calculateDeptData(summary, currentYear);

            // 3. 获取一级任务详情
            List<FirstLevelTaskDetailVO> firstLevelTasks = bizMapper.getFirstLevelTaskDetails();
            summary.setFirstLevelTasks(firstLevelTasks);

        } catch (Exception e) {
            throw new RuntimeException("获取看板数据失败: " + e.getMessage());
        }

        return summary;
    }

    /**
     * 计算整体数据（在Service中查询各状态数量）
     */
    private void calculateOverallData(DashboardSummaryVO summary, int currentYear) {
        // 1.1 所有任务完成率（在Service中查询各状态数量）
        Integer totalTasks = bizMapper.getTotalTaskCount();
        Integer completedTasks = bizMapper.getCompletedTaskCount();

        // 在Service中查询各状态数量
        Integer notStartedCount = bizMapper.getTaskCountByStatus("0");
        Integer inProgressCount = bizMapper.getTaskCountByStatus("1");
        Integer inReviewCount = bizMapper.getTaskCountByStatus("2");
        Integer finishedCount = bizMapper.getTaskCountByStatus("3");

        summary.setOverallCompletion(new TaskCompletionVO(
                totalTasks, completedTasks, "all", "所有任务完成率",
                notStartedCount != null ? notStartedCount : 0,
                inProgressCount != null ? inProgressCount : 0,
                inReviewCount != null ? inReviewCount : 0,
                finishedCount != null ? finishedCount : 0
        ));

        // 1.2 本年度任务完成率（在Service中查询各状态数量）
        Integer yearTotalTasks = bizMapper.getYearTaskCount(currentYear);
        Integer yearCompletedTasks = bizMapper.getYearCompletedTaskCount(currentYear);

        // 在Service中查询本年度各状态数量
        Integer yearNotStartedCount = bizMapper.getYearTaskCountByStatus(currentYear, "0");
        Integer yearInProgressCount = bizMapper.getYearTaskCountByStatus(currentYear, "1");
        Integer yearInReviewCount = bizMapper.getYearTaskCountByStatus(currentYear, "2");
        Integer yearFinishedCount = bizMapper.getYearTaskCountByStatus(currentYear, "3");

        summary.setYearCompletion(new TaskCompletionVO(
                yearTotalTasks, yearCompletedTasks, "year", currentYear + "年度任务完成率",
                yearNotStartedCount != null ? yearNotStartedCount : 0,
                yearInProgressCount != null ? yearInProgressCount : 0,
                yearInReviewCount != null ? yearInReviewCount : 0,
                yearFinishedCount != null ? yearFinishedCount : 0
        ));

        // 1.3 中期任务完成率（在Service中查询各状态数量）
        Integer midTermTotalTasks = bizMapper.getMidTermTaskCount(MID_TERM_END_YEAR);
        Integer midTermCompletedTasks = bizMapper.getMidTermCompletedTaskCount(MID_TERM_END_YEAR);

        // 在Service中查询中期各状态数量
        Integer midTermNotStartedCount = bizMapper.getMidTermTaskCountByStatus(MID_TERM_END_YEAR, "0");
        Integer midTermInProgressCount = bizMapper.getMidTermTaskCountByStatus(MID_TERM_END_YEAR, "1");
        Integer midTermInReviewCount = bizMapper.getMidTermTaskCountByStatus(MID_TERM_END_YEAR, "2");
        Integer midTermFinishedCount = bizMapper.getMidTermTaskCountByStatus(MID_TERM_END_YEAR, "3");

        summary.setMidTermCompletion(new TaskCompletionVO(
                midTermTotalTasks, midTermCompletedTasks, "midterm", "中期（" + MID_TERM_END_YEAR + "年前）任务完成率",
                midTermNotStartedCount != null ? midTermNotStartedCount : 0,
                midTermInProgressCount != null ? midTermInProgressCount : 0,
                midTermInReviewCount != null ? midTermInReviewCount : 0,
                midTermFinishedCount != null ? midTermFinishedCount : 0
        ));

        // 1.4 一级任务完成率（在Service中查询各状态数量）
        Integer firstLevelTotalTasks = bizMapper.getFirstLevelTaskCount();
        Integer firstLevelCompletedTasks = bizMapper.getFirstLevelCompletedTaskCount();

        // 在Service中查询一级各状态数量
        Integer firstLevelNotStartedCount = bizMapper.getFirstLevelTaskCountByStatus("0");
        Integer firstLevelInProgressCount = bizMapper.getFirstLevelTaskCountByStatus("1");
        Integer firstLevelInReviewCount = bizMapper.getFirstLevelTaskCountByStatus("2");
        Integer firstLevelFinishedCount = bizMapper.getFirstLevelTaskCountByStatus("3");

        summary.setFirstLevelCompletion(new TaskCompletionVO(
                firstLevelTotalTasks, firstLevelCompletedTasks, "firstLevel", "一级任务完成率",
                firstLevelNotStartedCount != null ? firstLevelNotStartedCount : 0,
                firstLevelInProgressCount != null ? firstLevelInProgressCount : 0,
                firstLevelInReviewCount != null ? firstLevelInReviewCount : 0,
                firstLevelFinishedCount != null ? firstLevelFinishedCount : 0
        ));
    }

    /**
     * 计算部门数据（在Service中补充状态统计）
     */
    private void calculateDeptData(DashboardSummaryVO summary, int currentYear) {
        // 2.1 各部门整体完成率（在Service中补充状态统计）
        List<DeptTaskStatsVO> deptOverallStats = bizMapper.getDeptTaskStats();
        // 补充每个部门的状态统计
        for (DeptTaskStatsVO stats : deptOverallStats) {
            if (stats.getDeptId() != null) {
                // 查询该部门的各状态任务数量
                List<BizTask> deptTasks = bizMapper.getTasksByDeptId(stats.getDeptId());
                // 统计各状态数量
                int notStarted = 0, inProgress = 0, inReview = 0, finished = 0;
                for (BizTask task : deptTasks) {
                    String status = task.getStatus();
                    if (status == null) continue;

                    switch (status) {
                        case "0": notStarted++; break;
                        case "1": inProgress++; break;
                        case "2": inReview++; break;
                        case "3": finished++; break;
                    }
                }
                stats.setNotStartedCount(notStarted);
                stats.setInProgressCount(inProgress);
                stats.setInReviewCount(inReview);
                stats.setFinishedCount(finished);
            }
        }
        deptOverallStats.forEach(DeptTaskStatsVO::calculateCompletionRate);
        summary.setDeptOverallStats(deptOverallStats);

        // 2.2 各部门本年度完成率（在Service中补充状态统计）
        List<DeptTaskStatsVO> deptYearStats = bizMapper.getDeptYearTaskStats(currentYear);
        // 补充每个部门的本年度状态统计
        for (DeptTaskStatsVO stats : deptYearStats) {
            if (stats.getDeptId() != null) {
                // 查询该部门本年度各状态任务数量
                List<BizTask> deptYearTasks = bizMapper.getTasksByDeptIdAndPhase(stats.getDeptId(), currentYear);
                // 统计各状态数量
                int notStarted = 0, inProgress = 0, inReview = 0, finished = 0;
                for (BizTask task : deptYearTasks) {
                    String status = task.getStatus();
                    if (status == null) continue;

                    switch (status) {
                        case "0": notStarted++; break;
                        case "1": inProgress++; break;
                        case "2": inReview++; break;
                        case "3": finished++; break;
                    }
                }
                stats.setNotStartedCount(notStarted);
                stats.setInProgressCount(inProgress);
                stats.setInReviewCount(inReview);
                stats.setFinishedCount(finished);
            }
        }
        deptYearStats.forEach(DeptTaskStatsVO::calculateCompletionRate);
        summary.setDeptYearStats(deptYearStats);

        // 2.3 各部门中期完成率（在Service中补充状态统计）
        List<DeptTaskStatsVO> deptMidTermStats = bizMapper.getDeptMidTermTaskStats(MID_TERM_END_YEAR);
        // 补充每个部门的中期状态统计
        for (DeptTaskStatsVO stats : deptMidTermStats) {
            if (stats.getDeptId() != null) {
                // 查询该部门中期各状态任务数量（需要单独查询）
                List<BizTask> deptTasks = bizMapper.getTasksByDeptId(stats.getDeptId());
                int notStarted = 0, inProgress = 0, inReview = 0, finished = 0;
                for (BizTask task : deptTasks) {
                    // 添加空值检查
                    if (task.getPhase() != null && task.getPhase() < MID_TERM_END_YEAR) {
                        String status = task.getStatus();
                        if (status == null) continue;

                        switch (status) {
                            case "0": notStarted++; break;
                            case "1": inProgress++; break;
                            case "2": inReview++; break;
                            case "3": finished++; break;
                        }
                    }
                }
                stats.setNotStartedCount(notStarted);
                stats.setInProgressCount(inProgress);
                stats.setInReviewCount(inReview);
                stats.setFinishedCount(finished);
            }
        }
        deptMidTermStats.forEach(DeptTaskStatsVO::calculateCompletionRate);
        summary.setDeptMidTermStats(deptMidTermStats);
    }

    /**
     * 获取所有任务完成率（单独的接口）
     * @return 所有任务完成率
     */
    public TaskCompletionVO getAllTaskCompletionRate() {
        try {
            Integer totalTasks = bizMapper.getTotalTaskCount();
            Integer completedTasks = bizMapper.getCompletedTaskCount();

            // 在Service中查询各状态数量
            Integer notStartedCount = bizMapper.getTaskCountByStatus("0");
            Integer inProgressCount = bizMapper.getTaskCountByStatus("1");
            Integer inReviewCount = bizMapper.getTaskCountByStatus("2");
            Integer finishedCount = bizMapper.getTaskCountByStatus("3");

            return new TaskCompletionVO(
                    totalTasks, completedTasks, "all", "所有任务完成率",
                    notStartedCount != null ? notStartedCount : 0,
                    inProgressCount != null ? inProgressCount : 0,
                    inReviewCount != null ? inReviewCount : 0,
                    finishedCount != null ? finishedCount : 0
            );
        } catch (Exception e) {
            throw new RuntimeException("获取所有任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取本年度任务完成率（单独的接口）
     * @param year 年份，为空时使用当前年份
     * @return 本年度任务完成率
     */
    public TaskCompletionVO getYearTaskCompletionRate(Integer year) {
        try {
            if (year == null) {
                Calendar cal = Calendar.getInstance();
                year = cal.get(Calendar.YEAR);
            }

            Integer totalTasks = bizMapper.getYearTaskCount(year);
            Integer completedTasks = bizMapper.getYearCompletedTaskCount(year);

            // 在Service中查询本年度各状态数量
            Integer notStartedCount = bizMapper.getYearTaskCountByStatus(year, "0");
            Integer inProgressCount = bizMapper.getYearTaskCountByStatus(year, "1");
            Integer inReviewCount = bizMapper.getYearTaskCountByStatus(year, "2");
            Integer finishedCount = bizMapper.getYearTaskCountByStatus(year, "3");

            return new TaskCompletionVO(
                    totalTasks, completedTasks, "year", year + "年度任务完成率",
                    notStartedCount != null ? notStartedCount : 0,
                    inProgressCount != null ? inProgressCount : 0,
                    inReviewCount != null ? inReviewCount : 0,
                    finishedCount != null ? finishedCount : 0
            );
        } catch (Exception e) {
            throw new RuntimeException("获取本年度任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取中期任务完成率（单独的接口）
     * @param endYear 截止年份，为空时使用默认值2028
     * @return 中期任务完成率
     */
    public TaskCompletionVO getMidTermTaskCompletionRate(Integer endYear) {
        try {
            if (endYear == null) {
                endYear = MID_TERM_END_YEAR;
            }

            Integer totalTasks = bizMapper.getMidTermTaskCount(endYear);
            Integer completedTasks = bizMapper.getMidTermCompletedTaskCount(endYear);

            // 在Service中查询中期各状态数量
            Integer notStartedCount = bizMapper.getMidTermTaskCountByStatus(endYear, "0");
            Integer inProgressCount = bizMapper.getMidTermTaskCountByStatus(endYear, "1");
            Integer inReviewCount = bizMapper.getMidTermTaskCountByStatus(endYear, "2");
            Integer finishedCount = bizMapper.getMidTermTaskCountByStatus(endYear, "3");

            return new TaskCompletionVO(
                    totalTasks, completedTasks, "midterm", "中期（" + endYear + "年前）任务完成率",
                    notStartedCount != null ? notStartedCount : 0,
                    inProgressCount != null ? inProgressCount : 0,
                    inReviewCount != null ? inReviewCount : 0,
                    finishedCount != null ? finishedCount : 0
            );
        } catch (Exception e) {
            throw new RuntimeException("获取中期任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取一级任务完成率（单独的接口）
     * @return 一级任务完成率
     */
    public TaskCompletionVO getFirstLevelTaskCompletionRate() {
        try {
            Integer totalTasks = bizMapper.getFirstLevelTaskCount();
            Integer completedTasks = bizMapper.getFirstLevelCompletedTaskCount();

            // 在Service中查询一级各状态数量
            Integer notStartedCount = bizMapper.getFirstLevelTaskCountByStatus("0");
            Integer inProgressCount = bizMapper.getFirstLevelTaskCountByStatus("1");
            Integer inReviewCount = bizMapper.getFirstLevelTaskCountByStatus("2");
            Integer finishedCount = bizMapper.getFirstLevelTaskCountByStatus("3");

            return new TaskCompletionVO(
                    totalTasks, completedTasks, "firstLevel", "一级任务完成率",
                    notStartedCount != null ? notStartedCount : 0,
                    inProgressCount != null ? inProgressCount : 0,
                    inReviewCount != null ? inReviewCount : 0,
                    finishedCount != null ? finishedCount : 0
            );
        } catch (Exception e) {
            throw new RuntimeException("获取一级任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取各部门任务完成率（单独的接口）
     * @return 各部门任务完成率列表
     */
    public List<DeptTaskStatsVO> getDeptTaskCompletionRates() {
        try {
            List<DeptTaskStatsVO> stats = bizMapper.getDeptTaskStats();
            // 补充每个部门的状态统计
            for (DeptTaskStatsVO stat : stats) {
                if (stat.getDeptId() != null) {
                    // 查询该部门的各状态任务数量
                    List<BizTask> deptTasks = bizMapper.getTasksByDeptId(stat.getDeptId());
                    // 统计各状态数量
                    int notStarted = 0, inProgress = 0, inReview = 0, finished = 0;
                    for (BizTask task : deptTasks) {
                        switch (task.getStatus()) {
                            case "0": notStarted++; break;
                            case "1": inProgress++; break;
                            case "2": inReview++; break;
                            case "3": finished++; break;
                        }
                    }
                    stat.setNotStartedCount(notStarted);
                    stat.setInProgressCount(inProgress);
                    stat.setInReviewCount(inReview);
                    stat.setFinishedCount(finished);
                }
            }
            stats.forEach(DeptTaskStatsVO::calculateCompletionRate);
            return stats;
        } catch (Exception e) {
            throw new RuntimeException("获取各部门任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取各部门本年度任务完成率（单独的接口）
     * @param year 年份，为空时使用当前年份
     * @return 各部门本年度任务完成率列表
     */
    public List<DeptTaskStatsVO> getDeptYearTaskCompletionRates(Integer year) {
        try {
            if (year == null) {
                Calendar cal = Calendar.getInstance();
                year = cal.get(Calendar.YEAR);
            }

            List<DeptTaskStatsVO> stats = bizMapper.getDeptYearTaskStats(year);
            // 补充每个部门的本年度状态统计
            for (DeptTaskStatsVO stat : stats) {
                if (stat.getDeptId() != null) {
                    // 查询该部门本年度各状态任务数量
                    List<BizTask> deptYearTasks = bizMapper.getTasksByDeptIdAndPhase(stat.getDeptId(), year);
                    // 统计各状态数量
                    int notStarted = 0, inProgress = 0, inReview = 0, finished = 0;
                    for (BizTask task : deptYearTasks) {
                        switch (task.getStatus()) {
                            case "0": notStarted++; break;
                            case "1": inProgress++; break;
                            case "2": inReview++; break;
                            case "3": finished++; break;
                        }
                    }
                    stat.setNotStartedCount(notStarted);
                    stat.setInProgressCount(inProgress);
                    stat.setInReviewCount(inReview);
                    stat.setFinishedCount(finished);
                }
            }
            stats.forEach(DeptTaskStatsVO::calculateCompletionRate);
            return stats;
        } catch (Exception e) {
            throw new RuntimeException("获取各部门本年度任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取各部门中期任务完成率（单独的接口）
     * @param endYear 截止年份，为空时使用默认值2028
     * @return 各部门中期任务完成率列表
     */
    public List<DeptTaskStatsVO> getDeptMidTermTaskCompletionRates(Integer endYear) {
        try {
            if (endYear == null) {
                endYear = MID_TERM_END_YEAR;
            }

            List<DeptTaskStatsVO> stats = bizMapper.getDeptMidTermTaskStats(endYear);
            // 补充每个部门的中期状态统计
            for (DeptTaskStatsVO stat : stats) {
                if (stat.getDeptId() != null) {
                    // 查询该部门中期各状态任务数量（需要单独查询）
                    List<BizTask> deptTasks = bizMapper.getTasksByDeptId(stat.getDeptId());
                    int notStarted = 0, inProgress = 0, inReview = 0, finished = 0;
                    for (BizTask task : deptTasks) {
                        // 添加空值检查
                        if (task.getPhase() != null && task.getPhase() < endYear) {
                            String status = task.getStatus();
                            if (status == null) continue;

                            switch (status) {
                                case "0": notStarted++; break;
                                case "1": inProgress++; break;
                                case "2": inReview++; break;
                                case "3": finished++; break;
                            }
                        }
                    }
                    stat.setNotStartedCount(notStarted);
                    stat.setInProgressCount(inProgress);
                    stat.setInReviewCount(inReview);
                    stat.setFinishedCount(finished);
                }
            }
            stats.forEach(DeptTaskStatsVO::calculateCompletionRate);
            return stats;
        } catch (Exception e) {
            throw new RuntimeException("获取各部门中期任务完成率失败: " + e.getMessage());
        }
    }
    /**
     * 获取一级任务详细完成情况（单独的接口）
     * @return 一级任务详情列表
     */
    public List<FirstLevelTaskDetailVO> getFirstLevelTaskDetails() {
        try {
            return bizMapper.getFirstLevelTaskDetails();
        } catch (Exception e) {
            throw new RuntimeException("获取一级任务详情失败: " + e.getMessage());
        }
    }

//    获取全量任务详细情况
    public List<BizTask> getAllTaskDetails() {

        try {
            return bizMapper.getAllTasks();
        } catch (Exception e) {
            throw new RuntimeException("获取全量任务详情失败: " + e.getMessage());
        }    }

    /**
     * 获取单个部门的所有统计信息
     * @param deptId 部门ID
     * @return 部门统计信息
     */
    public Map<String, Object> getDeptStatsDetail(Long deptId) {
        try {
            Map<String, Object> result = new HashMap<>();

            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(Calendar.YEAR);

            // 获取部门信息
            String deptName = sysMapper.getDeptNameByDeptId(deptId);
            if (deptName == null) {
                throw new RuntimeException("部门不存在");
            }

            result.put("deptId", deptId);
            result.put("deptName", deptName);

            // 计算各种完成率（包含状态统计）
            result.put("overall", calculateDeptCompletionRate(deptId, null, null));
            result.put("year", calculateDeptCompletionRate(deptId, currentYear, null));
            result.put("midterm", calculateDeptCompletionRate(deptId, null, MID_TERM_END_YEAR));

            // 获取部门负责人
            Long leaderId = sysMapper.getDeptLeaderId(deptId);
            if (leaderId != null) {
                String leaderName = sysMapper.getUserById(leaderId).getNickName();
                result.put("leaderId", leaderId);
                result.put("leaderName", leaderName);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("获取部门统计详情失败: " + e.getMessage());
        }
    }

    /**
     * 计算部门的任务完成率（辅助方法）
     */
    private Map<String, Object> calculateDeptCompletionRate(Long deptId, Integer year, Integer endYear) {
        Map<String, Object> result = new HashMap<>();

        // 根据条件查询部门任务
        List<BizTask> deptTasks;
        if (year != null) {
            deptTasks = bizMapper.getTasksByDeptIdAndPhase(deptId, year);
        } else if (endYear != null) {
            // 中期任务需要单独过滤
            List<BizTask> allDeptTasks = bizMapper.getTasksByDeptId(deptId);
            deptTasks = new ArrayList<>();
            for (BizTask task : allDeptTasks) {
                if (task.getPhase() < endYear) {
                    deptTasks.add(task);
                }
            }
        } else {
            deptTasks = bizMapper.getTasksByDeptId(deptId);
        }

        // 统计任务数量
        int totalTasks = deptTasks.size();
        int completedTasks = 0;
        int notStarted = 0, inProgress = 0, inReview = 0, finished = 0;

        for (BizTask task : deptTasks) {
            switch (task.getStatus()) {
                case "0": notStarted++; break;
                case "1": inProgress++; break;
                case "2": inReview++; break;
                case "3":
                    finished++;
                    completedTasks++;
                    break;
            }
        }

        result.put("totalTasks", totalTasks);
        result.put("completedTasks", completedTasks);
        result.put("notStartedCount", notStarted);
        result.put("inProgressCount", inProgress);
        result.put("inReviewCount", inReview);
        result.put("finishedCount", finished);

        // 计算完成率和占比
        if (totalTasks > 0) {
            BigDecimal completionRate = BigDecimal.valueOf(completedTasks * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP);
            result.put("completionRate", completionRate);

            result.put("notStartedRate", BigDecimal.valueOf(notStarted * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP));
            result.put("inProgressRate", BigDecimal.valueOf(inProgress * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP));
            result.put("inReviewRate", BigDecimal.valueOf(inReview * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP));
            result.put("finishedRate", BigDecimal.valueOf(finished * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP));
        } else {
            result.put("completionRate", BigDecimal.ZERO);
            result.put("notStartedRate", BigDecimal.ZERO);
            result.put("inProgressRate", BigDecimal.ZERO);
            result.put("inReviewRate", BigDecimal.ZERO);
            result.put("finishedRate", BigDecimal.ZERO);
        }

        return result;
    }
}