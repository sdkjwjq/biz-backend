package org.example.service;

import org.example.entity.BizOwnershipChangeLog;
import org.example.entity.BizPerformance;
import org.example.entity.BizTask;
import org.example.entity.SysDept;
import org.example.entity.SysNotice;
import org.example.entity.SysUser;
import org.example.entity.dto.OwnershipTransferDTO;
import org.example.entity.vo.OwnershipChangeLogVO;
import org.example.mapper.BizMapper;
import org.example.mapper.OwnershipTransferMapper;
import org.example.mapper.PerformanceMapper;
import org.example.mapper.SysMapper;
import org.example.utils.BusinessLogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 管理员归属移交：批量变更归口部门、责任人与归口审核人。
 * 整批校验，任一记录不满足条件即整批拒绝；变更与日志在同一事务提交。
 */
@Service
public class OwnershipTransferService {

    private static final String TYPE_TASK = "TASK";
    private static final String TYPE_PERFORMANCE = "PERFORMANCE";
    private static final int MAX_BATCH = 200;
    private static final int MAX_REASON = 200;
    private static final String EVENT = "归属移交";

    @Autowired
    private SysMapper sysMapper;

    @Autowired
    private BizMapper bizMapper;

    @Autowired
    private PerformanceMapper performanceMapper;

    @Autowired
    private OwnershipTransferMapper ownershipMapper;

    private record Prepared(List<Long> ids, Long deptId, String deptName, Long leaderId,
                            Long principalId, String reason) { }

    private record Target(String type, Long id, String code, String name,
                          Integer level, Long deptId, Long leaderId, Long principalId) { }

    public boolean canTransfer(Long userId) {
        SysUser user = userId == null ? null : sysMapper.getUserById(userId);
        return user != null && !Integer.valueOf(1).equals(user.getIsDelete()) && "0".equals(user.getRole());
    }

    @Transactional
    public Map<String, Object> transferTasks(OwnershipTransferDTO input, Long userId) {
        return transfer(input, userId, TYPE_TASK);
    }

    @Transactional
    public Map<String, Object> transferPerformances(OwnershipTransferDTO input, Long userId) {
        return transfer(input, userId, TYPE_PERFORMANCE);
    }

    public List<OwnershipChangeLogVO> logs(String targetType, Long targetId, Long userId) {
        requireAdmin(userId);
        String type = TYPE_TASK.equals(targetType) ? TYPE_TASK
                : TYPE_PERFORMANCE.equals(targetType) ? TYPE_PERFORMANCE : null;
        if (type == null) throw new TaskManagementException(400, "记录类型不正确");
        if (targetId == null || targetId <= 0) throw new TaskManagementException(400, "记录ID不正确");
        return ownershipMapper.getLogs(type, targetId);
    }

    private Map<String, Object> transfer(OwnershipTransferDTO input, Long userId, String type) {
        requireAdmin(userId);
        boolean task = TYPE_TASK.equals(type);
        String noun = task ? "任务" : "绩效指标";
        Prepared prepared = prepare(input, noun);

        List<Target> pending = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        List<String> wrongLevel = new ArrayList<>();
        List<String> active = new ArrayList<>();
        int skipped = 0;
        for (Long id : prepared.ids()) {
            Target current = task ? taskTarget(id) : performanceTarget(id);
            if (current == null) {
                missing.add("#" + id);
                continue;
            }
            if (task && !Integer.valueOf(3).equals(current.level())) {
                wrongLevel.add(label(current.code(), current.name()));
                continue;
            }
            int activeCount = task ? ownershipMapper.countActiveTaskAudits(id)
                    : ownershipMapper.countActivePerformanceAudits(id);
            if (activeCount > 0) {
                active.add(label(current.code(), current.name()));
                continue;
            }
            if (Objects.equals(current.deptId(), prepared.deptId())
                    && Objects.equals(current.leaderId(), prepared.leaderId())
                    && Objects.equals(current.principalId(), prepared.principalId())) {
                skipped++;
                continue;
            }
            pending.add(current);
        }

        if (!missing.isEmpty()) {
            throw new TaskManagementException(400, "以下" + noun + "不存在或已删除：" + join(missing));
        }
        if (!wrongLevel.isEmpty()) {
            throw new TaskManagementException(400, "仅三级任务可以移交：" + join(wrongLevel));
        }
        if (!active.isEmpty()) {
            throw new TaskManagementException(409, "以下" + noun + "存在审核中的单据，请先处理完成后再移交：" + join(active));
        }
        if (pending.isEmpty()) {
            throw new TaskManagementException(400, "没有需要变更的内容");
        }

        String batchId = UUID.randomUUID().toString();
        Date now = new Date();
        for (Target target : pending) {
            if (task) {
                ownershipMapper.updateTaskOwnership(target.id(), prepared.deptId(), prepared.leaderId(),
                        prepared.principalId(), now);
            } else {
                ownershipMapper.updatePerformanceOwnership(target.id(), prepared.deptId(), prepared.leaderId(),
                        prepared.principalId(), now);
            }
            BizOwnershipChangeLog log = new BizOwnershipChangeLog();
            log.setTargetType(type);
            log.setTargetId(target.id());
            log.setTargetCode(target.code());
            log.setTargetName(target.name());
            log.setBatchId(batchId);
            log.setOperatorId(userId);
            log.setReason(prepared.reason());
            log.setDeptBeforeId(target.deptId());
            log.setDeptAfterId(prepared.deptId());
            log.setLeaderBeforeId(target.leaderId());
            log.setLeaderAfterId(prepared.leaderId());
            log.setPrincipalBeforeId(target.principalId());
            log.setPrincipalAfterId(prepared.principalId());
            log.setCreateTime(now);
            ownershipMapper.insertLog(log);
            sendNotices(type, target, prepared, userId);
        }

        BusinessLogUtil.info("归属移交",
                "result", "成功",
                "operatorId", userId,
                "targetType", type,
                "batchId", batchId,
                "requested", prepared.ids().size(),
                "updated", pending.size(),
                "skipped", skipped,
                "deptId", prepared.deptId(),
                "leaderId", prepared.leaderId(),
                "principalId", prepared.principalId(),
                "reason", prepared.reason());
        return Map.of("batchId", batchId, "total", prepared.ids().size(),
                "updated", pending.size(), "skipped", skipped);
    }

    private void requireAdmin(Long userId) {
        if (!canTransfer(userId)) throw new TaskManagementException(403, "仅限管理员操作");
    }

    private Prepared prepare(OwnershipTransferDTO input, String noun) {
        if (input == null) throw new TaskManagementException(400, "请填写移交信息");
        List<Long> ids = input.getIds() == null ? List.of()
                : new ArrayList<>(new LinkedHashSet<>(input.getIds().stream().filter(Objects::nonNull).toList()));
        if (ids.isEmpty()) throw new TaskManagementException(400, "请选择需要移交的" + noun);
        if (ids.size() > MAX_BATCH) throw new TaskManagementException(400, "一次最多移交 " + MAX_BATCH + " 条");

        SysDept dept = input.getDeptId() == null ? null : sysMapper.getDeptById(input.getDeptId());
        if (dept == null || Integer.valueOf(1).equals(dept.getIsDelete())) {
            throw new TaskManagementException(400, "请选择有效的归口部门");
        }
        SysUser leader = user(input.getLeaderId());
        if (leader == null) throw new TaskManagementException(400, "请选择有效的责任人");
        SysUser principal = user(input.getPrincipalId());
        if (principal == null) throw new TaskManagementException(400, "请选择有效的归口审核人");

        String reason = input.getReason() == null ? "" : input.getReason().trim();
        if (reason.isEmpty()) throw new TaskManagementException(400, "请填写移交原因");
        if (reason.codePointCount(0, reason.length()) > MAX_REASON) {
            throw new TaskManagementException(400, "移交原因不能超过 " + MAX_REASON + " 字");
        }
        return new Prepared(ids, dept.getDeptId(), dept.getDeptName(),
                leader.getUserId(), principal.getUserId(), reason);
    }

    private SysUser user(Long userId) {
        SysUser user = userId == null ? null : sysMapper.getUserById(userId);
        return user == null || Integer.valueOf(1).equals(user.getIsDelete()) ? null : user;
    }

    private Target taskTarget(Long taskId) {
        BizTask task = bizMapper.getTaskByIdForUpdate(taskId);
        if (task == null || Integer.valueOf(1).equals(task.getIsDelete())) return null;
        return new Target(TYPE_TASK, task.getTaskId(), task.getTaskCode(), task.getTaskName(),
                task.getLevel(), task.getDeptId(), task.getLeaderId(), task.getPrincipalId());
    }

    private Target performanceTarget(Long perfId) {
        BizPerformance performance = performanceMapper.getPerformanceByIdForUpdate(perfId);
        if (performance == null || Integer.valueOf(1).equals(performance.getIsDelete())) return null;
        return new Target(TYPE_PERFORMANCE, performance.getPerfId(), performance.getPerfCode(),
                performance.getPerfName(), null, performance.getDeptId(), performance.getLeaderId(),
                performance.getPrincipalId());
    }

    /** 只通知实际发生变更的接收人；操作人自己不需要收到通知。 */
    private void sendNotices(String type, Target target, Prepared prepared, Long operatorId) {
        Map<Long, List<String>> recipients = new LinkedHashMap<>();
        if (!Objects.equals(target.leaderId(), prepared.leaderId())) {
            recipients.computeIfAbsent(prepared.leaderId(), key -> new ArrayList<>()).add("责任人");
        }
        if (!Objects.equals(target.principalId(), prepared.principalId())) {
            recipients.computeIfAbsent(prepared.principalId(), key -> new ArrayList<>()).add("归口审核人");
        }
        boolean task = TYPE_TASK.equals(type);
        String noun = task ? "任务" : "绩效指标";
        for (Map.Entry<Long, List<String>> entry : recipients.entrySet()) {
            if (Objects.equals(entry.getKey(), operatorId)) continue;
            SysNotice notice = new SysNotice();
            notice.setFromUserId(operatorId);
            notice.setToUserId(entry.getKey());
            notice.setType("1");
            notice.setTriggerEvent(EVENT);
            notice.setTitle(noun + "归属已变更");
            notice.setContent("您已成为" + noun + "「" + target.name() + "」的"
                    + String.join("、", entry.getValue()) + "，归口部门："
                    + (prepared.deptName() == null ? "—" : prepared.deptName()) + "。");
            notice.setSourceType(task ? "0" : "2");
            notice.setSourceId(target.id());
            notice.setIsRead("0");
            notice.setIsDelete(0);
            notice.setCreateTime(new Date());
            sysMapper.sendNotice(notice);
        }
    }

    private String label(String code, String name) {
        if (code == null || code.isBlank()) return name == null ? "—" : name;
        return name == null ? code : code + " " + name;
    }

    private String join(List<String> values) {
        List<String> trimmed = new ArrayList<>();
        for (String value : values) {
            if (trimmed.size() >= 10) { trimmed.add("…"); break; }
            trimmed.add(value);
        }
        return String.join("、", trimmed);
    }
}
