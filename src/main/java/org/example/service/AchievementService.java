package org.example.service;

import org.example.entity.BizAchievement;
import org.example.entity.BizAchievementAuditLog;
import org.example.entity.BizAchievementSubmission;
import org.example.entity.SysDept;
import org.example.entity.SysNotice;
import org.example.entity.SysUser;
import org.example.entity.dto.BizAuditDTO;
import org.example.entity.vo.AchievementAuditVO;
import org.example.mapper.AchievementMapper;
import org.example.mapper.SysMapper;
import org.example.utils.AchievementPermissionUtil;
import org.example.utils.BusinessLogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 标志性成果业务服务类
 * 处理标志性成果的基础增删查改业务逻辑
 */
@Service
public class AchievementService {
    private static final Long ADMIN_ID = AchievementPermissionUtil.PRIMARY_AUDITOR_ID;

    /**
     * 注入成果Mapper接口，操作数据库
     */
    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private SysMapper sysMapper;

    private SysUser getActiveUser(Long userId) {
        if (userId == null) {
            return null;
        }
        SysUser user = sysMapper.getUserById(userId);
        return AchievementPermissionUtil.isActiveUser(user) ? user : null;
    }

    private SysDept getActiveDept(SysUser user) {
        if (user == null || user.getDeptId() == null) {
            return null;
        }
        SysDept dept = sysMapper.getDeptById(user.getDeptId());
        return AchievementPermissionUtil.isActiveDept(dept) ? dept : null;
    }

    private boolean isDepartmentAchievementAccount(Long userId) {
        SysUser user = getActiveUser(userId);
        return AchievementPermissionUtil.isDepartmentAchievementAccount(user, getActiveDept(user));
    }

    private boolean canUploadAchievement(Long userId) {
        SysUser user = getActiveUser(userId);
        return AchievementPermissionUtil.canUploadAchievement(user, getActiveDept(user));
    }

    private boolean canViewAchievement(Long userId) {
        SysUser user = getActiveUser(userId);
        return AchievementPermissionUtil.canViewAchievement(user, getActiveDept(user));
    }

    private boolean canAuditAchievement(Long userId) {
        return AchievementPermissionUtil.isAchievementAuditor(getActiveUser(userId));
    }

    /**
     * 根据成果ID查询未删除的标志性成果
     * @param achId 成果ID
     * @return 标志性成果实体
     */
    public BizAchievement getAchievementById(Long achId) {
        // 参数非空校验
        if (achId == null) {
            throw new RuntimeException("成果ID不能为空");
        }
        // 查询成果
        BizAchievement achievement = achievementMapper.getAchievementById(achId);
        // 成果不存在校验
        if (achievement == null) {
            throw new RuntimeException("标志性成果不存在或已被删除");
        }
        return achievement;
    }

    /**
     * 查询所有未删除的标志性成果列表
     * @return 标志性成果实体列表
     */
    public List<BizAchievement> listAllAchievements() {
        try {
            return achievementMapper.listAllAchievements();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<BizAchievement> listAllAchievements(Long userId) {
        try {
            SysUser user = getActiveUser(userId);
            SysDept dept = getActiveDept(user);
            if (!AchievementPermissionUtil.canViewAchievement(user, dept)) {
                throw new RuntimeException("无权访问成果模块");
            }
            return achievementMapper.listAllAchievements();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 新增标志性成果
     * 自动填充创建时间、默认删除状态，返回自增成果ID
     * @param achievement 标志性成果实体（含核心业务字段）
     * @return 自增的成果ID
     */
    @Transactional
    public Long addAchievement(BizAchievement achievement, Long createBy) {
        SysUser user = getActiveUser(createBy);
        SysDept dept = getActiveDept(user);
        if (!AchievementPermissionUtil.canUploadAchievement(user, dept)) {
            throw new RuntimeException("仅成果上传账号或管理员可以新增成果");
        }
        // 核心参数非空校验
        if (achievement == null) {
            throw new RuntimeException("新增成果信息不能为空");
        }
        if (achievement.getCategory() == null) {
            throw new RuntimeException("成果类别不能为空");
        }
        if (achievement.getLevel() == null || achievement.getLevel().trim().isEmpty()) {
            throw new RuntimeException("成果级别不能为空");
        }
        if (achievement.getAchName() == null || achievement.getAchName().trim().isEmpty()) {
            throw new RuntimeException("成果名称不能为空");
        }
        if (achievement.getGotTime() == null) {
            throw new RuntimeException("成果颁发时间不能为空");
        }
        // 自动填充公共字段
        Date now = new Date();
        achievement.setCreateBy(createBy);
        achievement.setDeptId(user.getDeptId());
        achievement.setAuditStatus(10);
        achievement.setCurrentHandlerId(ADMIN_ID);
        achievement.setIsDelete(0); // 默认未删除
        achievement.setCreateTime(now); // 填充当前创建时间
        achievement.setUpdateTime(now);
        try {
            // 调用Mapper新增，返回自增ID
            achievementMapper.addAchievement(achievement);
            Long achId = achievement.getAchId();
            if (achId == null) {
                achId = achievementMapper.getLatestAchievement().getAchId();
            }

            BizAchievementSubmission submission = new BizAchievementSubmission();
            submission.setAchId(achId);
            submission.setSubmitBy(createBy);
            submission.setSubmitTime(now);
            submission.setFlowStatus(10);
            submission.setCurrentHandlerId(ADMIN_ID);
            submission.setComment(achievement.getComment());
            submission.setIsDelete(0);
            achievementMapper.createAchievementSubmission(submission);
            createAchievementAuditLog(submission.getSubId(), createBy, "submit", 0, 10, achievement.getComment());
            sendNotice(createBy, ADMIN_ID, "成果归档审核", "成果待归档审核",
                    "成果「" + achievement.getAchName() + "」已提交，请进行归档审核", "3", submission.getSubId());
            BusinessLogUtil.info("成果提交",
                    "result", "成功",
                    "userId", createBy,
                    "achId", achId,
                    "achName", achievement.getAchName(),
                    "subId", submission.getSubId(),
                    "nextHandlerId", ADMIN_ID);
            return achId;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改标志性成果信息
     * 仅修改未删除的成果，根据成果ID更新
     * @param achievement 标志性成果实体（含成果ID和需修改字段）
     */
    public void updateAchievement(Long id, BizAchievement achievement, Long userId) {
        SysUser user = getActiveUser(userId);
        SysDept dept = getActiveDept(user);
        if (!AchievementPermissionUtil.canUploadAchievement(user, dept)) {
            throw new RuntimeException("仅成果上传账号或管理员可以修改成果");
        }
        // 核心参数非空校验
        if (achievement == null) {
            throw new RuntimeException("修改成果信息不能为空");
        }
        BizAchievement existing = achievementMapper.getAchievementById(id);
        if (existing == null) {
            throw new RuntimeException("成果ID不存在，无法修改");
        }
        if (!Objects.equals(existing.getCreateBy(), userId) || !Objects.equals(existing.getDeptId(), user.getDeptId())) {
            throw new RuntimeException("仅允许修改本部门账号创建的成果");
        }
        try {
            achievement.setAchId(id);
            achievement.setCreateBy(existing.getCreateBy());
            achievement.setDeptId(user.getDeptId());
            achievement.setAuditStatus(existing.getAuditStatus());
            achievement.setCurrentHandlerId(existing.getCurrentHandlerId());
            achievement.setUpdateTime(new Date());
            achievementMapper.updateAchievement(achievement);
            BusinessLogUtil.info("成果修改",
                    "result", "成功",
                    "userId", userId,
                    "achId", id,
                    "achName", achievement.getAchName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateAchievement(Long id,BizAchievement achievement) {
        updateAchievement(id, achievement, null);
    }

    /**
     * 逻辑删除标志性成果
     * 置is_delete=1，并非物理删除
     * @param achId 成果ID
     */
    public void deleteAchievement(Long achId, Long userId) {
        SysUser user = getActiveUser(userId);
        SysDept dept = getActiveDept(user);
        if (!AchievementPermissionUtil.canUploadAchievement(user, dept)) {
            throw new RuntimeException("仅成果上传账号或管理员可以删除成果");
        }
        // 参数非空校验
        if (achId == null) {
            throw new RuntimeException("成果ID不能为空");
        }
        // 校验成果是否存在
        BizAchievement existing = achievementMapper.getAchievementById(achId);
        if (existing == null) {
            throw new RuntimeException("标志性成果不存在或已被删除，无法删除");
        }
        if (!Objects.equals(existing.getCreateBy(), userId) || !Objects.equals(existing.getDeptId(), user.getDeptId())) {
            throw new RuntimeException("仅允许删除本部门账号创建的成果");
        }
        try {
            achievementMapper.deleteAchievement(achId);
            BusinessLogUtil.info("成果删除",
                    "result", "成功",
                    "userId", userId,
                    "achId", achId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAchievement(Long achId) {
        deleteAchievement(achId, null);
    }

    @Transactional
    public Object auditAchievement(BizAuditDTO auditDTO, Long userId) {
        if (auditDTO == null || auditDTO.getSub_id() == null) {
            throw new RuntimeException("成果审核单ID不能为空");
        }
        if (!canAuditAchievement(userId)) {
            throw new RuntimeException("仅管理员可以审核成果归档");
        }
        BizAchievementSubmission submission = achievementMapper.getAchievementSubmissionById(auditDTO.getSub_id());
        if (submission == null || (submission.getIsDelete() != null && submission.getIsDelete() == 1)) {
            throw new RuntimeException("成果审核单不存在");
        }
        BizAchievement achievement = achievementMapper.getAchievementById(submission.getAchId());
        if (achievement == null) {
            throw new RuntimeException("成果不存在或已删除");
        }
        Integer preStatus = submission.getFlowStatus();
        if (preStatus == null || preStatus != 10) {
            throw new RuntimeException("当前状态不可审核");
        }
        boolean pass = Boolean.TRUE.equals(auditDTO.getIs_pass());
        String auditComment = auditDTO.getTitle() != null ? auditDTO.getTitle() : auditDTO.getContent();
        if (pass) {
            submission.setFlowStatus(30);
            submission.setCurrentHandlerId(userId);
            submission.setComment(auditComment);
            achievementMapper.updateAchievementSubmission(submission);
            achievement.setAuditStatus(30);
            achievement.setCurrentHandlerId(null);
            achievement.setUpdateTime(new Date());
            achievementMapper.updateAchievementAuditState(achievement);
            createAchievementAuditLog(submission.getSubId(), userId, "pass", 10, 30, auditComment);
            sendNotice(userId, submission.getSubmitBy(), "成果归档完成", "成果已归档",
                    "成果「" + achievement.getAchName() + "」已完成归档审核", "3", submission.getSubId());
            BusinessLogUtil.info("成果审核",
                    "result", "归档通过",
                    "operatorId", userId,
                    "achId", achievement.getAchId(),
                    "achName", achievement.getAchName(),
                    "subId", submission.getSubId(),
                    "preStatus", 10,
                    "postStatus", 30);
            return "成果已归档";
        }

        submission.setFlowStatus(-10);
        submission.setCurrentHandlerId(submission.getSubmitBy());
        submission.setComment(auditComment);
        achievementMapper.updateAchievementSubmission(submission);
        achievement.setAuditStatus(-10);
        achievement.setCurrentHandlerId(submission.getSubmitBy());
        achievement.setUpdateTime(new Date());
        achievementMapper.updateAchievementAuditState(achievement);
        createAchievementAuditLog(submission.getSubId(), userId, "reject", 10, -10, auditComment);
        sendNotice(userId, submission.getSubmitBy(), "成果归档退回", "成果已退回",
                "成果「" + achievement.getAchName() + "」已被退回，请查看处理意见", "3", submission.getSubId());
        BusinessLogUtil.info("成果审核",
                "result", "退回",
                "operatorId", userId,
                "achId", achievement.getAchId(),
                "achName", achievement.getAchName(),
                "subId", submission.getSubId(),
                "preStatus", 10,
                "postStatus", -10,
                "nextHandlerId", submission.getSubmitBy());
        return "成果已退回";
    }

    public Object getTodoAchievementAudits(Long userId) {
        if (!canAuditAchievement(userId)) {
            return new ArrayList<AchievementAuditVO>();
        }
        return toAchievementAuditVOs(achievementMapper.getAllTodoAchievementSubmissions());
    }

    public Object getAchievementAuditRecords(Long userId) {
        if (canAuditAchievement(userId)) {
            return toAchievementAuditVOs(achievementMapper.getAllAchievementSubmissionRecords());
        }
        return new ArrayList<AchievementAuditVO>();
    }

    public Object getAchievementAuditsByAchId(Long achId, Long userId) {
        BizAchievement achievement = achievementMapper.getAchievementById(achId);
        if (achievement == null) {
            throw new RuntimeException("成果不存在或已删除");
        }
        SysUser user = getActiveUser(userId);
        SysDept dept = getActiveDept(user);
        boolean canSee = AchievementPermissionUtil.isAchievementAuditor(user)
                || (AchievementPermissionUtil.isAchievementUploadAccount(user, dept)
                && Objects.equals(achievement.getDeptId(), user.getDeptId()));
        if (!canSee) {
            throw new RuntimeException("无权查看该成果审核记录");
        }
        return toAchievementAuditVOs(achievementMapper.getAchievementSubmissionsByAchId(achId));
    }

    public Object getAchievementAuditLogs(Long subId, Long userId) {
        BizAchievementSubmission submission = achievementMapper.getAchievementSubmissionById(subId);
        if (submission == null) {
            throw new RuntimeException("成果审核单不存在");
        }
        SysUser user = getActiveUser(userId);
        SysDept dept = getActiveDept(user);
        BizAchievement achievement = achievementMapper.getAchievementById(submission.getAchId());
        boolean canSee = AchievementPermissionUtil.isAchievementAuditor(user)
                || (achievement != null
                && AchievementPermissionUtil.isAchievementUploadAccount(user, dept)
                && Objects.equals(achievement.getDeptId(), user.getDeptId())
                && Objects.equals(submission.getSubmitBy(), userId));
        if (!canSee) {
            throw new RuntimeException("无权查看该成果审核日志");
        }
        return achievementMapper.getAchievementAuditLogs(subId);
    }

    private List<AchievementAuditVO> toAchievementAuditVOs(List<BizAchievementSubmission> submissions) {
        List<AchievementAuditVO> result = new ArrayList<>();
        if (submissions == null) {
            return result;
        }
        for (BizAchievementSubmission submission : submissions) {
            AchievementAuditVO vo = achievementMapper.getAchievementAuditVO(submission.getSubId());
            if (vo != null) {
                result.add(vo);
            }
        }
        return result;
    }

    private void createAchievementAuditLog(Long subId, Long operatorId, String actionType,
                                           Integer preStatus, Integer postStatus, String comment) {
        BizAchievementAuditLog log = new BizAchievementAuditLog();
        log.setSubId(subId);
        log.setOperatorId(operatorId);
        log.setActionType(actionType);
        log.setPreStatus(preStatus);
        log.setPostStatus(postStatus);
        log.setComment(comment);
        log.setCreateTime(new Date());
        achievementMapper.createAchievementAuditLog(log);
    }

    private void sendNotice(Long fromUserId, Long toUserId, String triggerEvent,
                            String title, String content, String sourceType, Long sourceId) {
        if (toUserId == null) {
            return;
        }
        SysNotice notice = new SysNotice();
        notice.setFromUserId(fromUserId);
        notice.setToUserId(toUserId);
        notice.setTriggerEvent(triggerEvent);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setSourceType(sourceType);
        notice.setSourceId(sourceId);
        notice.setIsRead("0");
        notice.setIsDelete(0);
        notice.setCreateTime(new Date());
        sysMapper.sendNotice(notice);
    }
}
