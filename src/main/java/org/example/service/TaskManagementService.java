package org.example.service;

import org.example.entity.*;
import org.example.entity.dto.BizTaskDTO;
import org.example.mapper.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class TaskManagementService {
    @Autowired private SysMapper sysMapper;
    @Autowired private PermissionService permissions;
    @Autowired private PermissionManagementService permissionManagement;
    @Autowired private BizMapper bizMapper;
    @Autowired private TaskManagementMapper mapper;
    @Autowired private AdminManagementService adminManagementService;

    public boolean canManage(Long userId) {
        if(userId!=null&&permissions.enforced(userId))return permissions.allowed(userId,"tasks.create")||permissions.allowed(userId,"tasks.transfer");
        SysUser user = userId == null ? null : sysMapper.getUserById(userId);
        return user != null && !Integer.valueOf(1).equals(user.getIsDelete())
                && "1".equals(user.getStatus()) && "0".equals(user.getRole());
    }

    private void requireAdmin(Long userId) {
        if (!canManage(userId)) throw new TaskManagementException(403, "仅限管理员操作");
    }

    public Map<String, Object> options(Long userId) {
        requireAdmin(userId);
        List<Map<String, Object>> users = new ArrayList<>();
        for (SysUser user : sysMapper.getAllUsers()) {
            if (Integer.valueOf(1).equals(user.getIsDelete()) || !"1".equals(user.getStatus())) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", user.getUserId()); item.put("nickName", user.getNickName()); item.put("deptId", user.getDeptId());
            users.add(item);
        }
        return Map.of("users", users, "departments", mapper.departments(), "directories", mapper.directories());
    }

    public Map<String,Object> optionStructure(Long actor){requireAdmin(actor);return Map.of("users",List.of(),"directories",List.of(),"departments",mapper.departments());}

    @Transactional
    public BizTask create(BizTaskDTO input, Long userId) {
        requireAdmin(userId);
        if (input == null) fail("请填写任务信息");
        if (!Integer.valueOf(3).equals(input.getLevel())) fail("只能新增三级任务");
        if (input.getTaskId() != null) fail("新增任务不能指定任务ID");
        if (input.getPhase() == null || input.getPhase() < 2025 || input.getPhase() > 2029) fail("年度必须为2025—2029年");
        if (!Long.valueOf(1).equals(input.getProjectId()) || mapper.lockProject(input.getProjectId()) == null) fail("项目不存在");
        BizTask parent = input.getParentId() == null ? null : bizMapper.getTaskByIdForUpdate(input.getParentId());
        if (!active(parent) || !Integer.valueOf(2).equals(parent.getLevel()) || !Objects.equals(parent.getProjectId(), input.getProjectId())) fail("请选择本项目有效的二级目录");
        // 目录为跨年度共用结构，不用目录自身的 phase 限制新增任务年度。
        BizTask root = parent.getParentId() == null ? null : bizMapper.getTaskByIdForUpdate(parent.getParentId());
        if (!active(root) || !Integer.valueOf(1).equals(root.getLevel()) || !Objects.equals(root.getProjectId(), input.getProjectId())) fail("二级目录的上级目录无效，请重新选择");
        String code = required(input.getTaskCode(), 64, "任务编号");
        String name = required(input.getTaskName(), 500, "任务名称");
        if (!mapper.matchingCodes(input.getProjectId(), input.getPhase(), code).isEmpty()) throw new TaskManagementException(409, "同项目、同年度已存在该任务编号");
        SysDept department = input.getDeptId() == null ? null : sysMapper.getDeptById(input.getDeptId());
        if (department == null || Integer.valueOf(1).equals(department.getIsDelete())) fail("请选择有效的归口部门");
        validUser(input.getLeaderId(), "责任人"); validUser(input.getAuditorId(), "专业群审核人"); validUser(input.getPrincipalId(), "归口审核人");
        if (!List.of("0", "1", "2").contains(input.getDataType() == null ? "" : input.getDataType())) fail("请选择有效的数据类型");
        BigDecimal target = input.getTargetValue();
        if (target == null || target.signum() < 0 || target.stripTrailingZeros().scale() > 4 || target.compareTo(new BigDecimal("10000000000000000")) >= 0) fail("目标值须为非负数，最多16位整数和4位小数");
        if ((input.getCurrentValue() != null && input.getCurrentValue().signum() != 0) || (input.getProgress() != null && input.getProgress() != 0)
                || (input.getStatus() != null && !"0".equals(input.getStatus()))) fail("新增任务不能指定完成值、进度或审核状态");
        optional(input.getComment(), 500, "任务描述"); optional(input.getExpLevel(), 20, "预期成果级别");
        optional(input.getExpTarget(), 10000, "预期达成情况"); optional(input.getExpEffect(), 10000, "预期效果"); optional(input.getExpMaterialDesc(), 10000, "材料要求");
        BizTask task = new BizTask(); BeanUtils.copyProperties(input, task);
        task.setTaskId(null); task.setTaskCode(code); task.setTaskName(name);
        task.setAncestors("0," + root.getTaskId() + "," + parent.getTaskId());
        task.setCurrentValue(BigDecimal.ZERO); task.setProgress(0); task.setStatus("0"); task.setWeight(BigDecimal.ONE);
        task.setIsDelete(0); task.setCreateTime(new Date()); task.setUpdateTime(new Date());
        bizMapper.addTask(task);
        if(permissions.enforced(userId)&&!permissions.canRead(userId,"tasks",task.getTaskId(),task.getPhase()))throw new AdminManagementException(403,"新任务不在您的授权范围内");
        permissionManagement.requireHandler(task.getAuditorId(),"tasks",task.getTaskId(),task.getPhase(),10);
        permissionManagement.requireHandler(task.getPrincipalId(),"tasks",task.getTaskId(),task.getPhase(),20);
        adminManagementService.recordOperation("TASK", "CREATE", "TASK", String.valueOf(task.getTaskId()),
                task.getTaskName(), userId, "管理员新增三级任务", null,
                Map.of("taskId", task.getTaskId(), "taskCode", task.getTaskCode(), "phase", task.getPhase(),
                        "deptId", task.getDeptId(), "leaderId", task.getLeaderId(), "auditorId", task.getAuditorId(),
                        "principalId", task.getPrincipalId()));
        return task;
    }

    private boolean active(BizTask task) { return task != null && !Integer.valueOf(1).equals(task.getIsDelete()); }
    private void validUser(Long id, String label) {
        SysUser user = id == null ? null : sysMapper.getUserById(id);
        if (user == null || Integer.valueOf(1).equals(user.getIsDelete()) || !"1".equals(user.getStatus())) fail("请选择有效的" + label);
    }
    private String required(String value, int max, String label) {
        if (value == null || value.trim().isEmpty()) fail("请填写" + label);
        String result = value.trim(); optional(result, max, label); return result;
    }
    private void optional(String value, int max, String label) {
        if (value != null && value.codePointCount(0, value.length()) > max) fail(label + "最多" + max + "字");
    }
    private void fail(String message) { throw new TaskManagementException(400, message); }
}
