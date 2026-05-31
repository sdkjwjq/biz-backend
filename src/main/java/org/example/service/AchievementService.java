package org.example.service;

import org.example.entity.BizAchievement;
import org.example.entity.BizAchievementAuditLog;
import org.example.entity.BizAchievementSubmission;
import org.example.entity.SysNotice;
import org.example.entity.SysUser;
import org.example.entity.dto.BizAuditDTO;
import org.example.entity.vo.AchievementAuditVO;
import org.example.mapper.AchievementMapper;
import org.example.mapper.SysMapper;
import org.example.utils.BusinessLogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 标志性成果业务服务类
 * 处理标志性成果的基础增删查改业务逻辑
 */
@Service
public class AchievementService {
    private static final Long ADMIN_ID = 110228L;
    private static final Set<Long> ACHIEVEMENT_UPLOAD_USER_IDS = new HashSet<>();

    static {
        ACHIEVEMENT_UPLOAD_USER_IDS.add(800001L);
        ACHIEVEMENT_UPLOAD_USER_IDS.add(800002L);
        ACHIEVEMENT_UPLOAD_USER_IDS.add(800003L);
    }

    /**
     * 注入成果Mapper接口，操作数据库
     */
    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private SysMapper sysMapper;

    private boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        SysUser user = sysMapper.getUserById(userId);
        return user != null && "0".equals(user.getRole());
    }

    private boolean canUploadAchievement(Long userId) {
        return isAdmin(userId) || ACHIEVEMENT_UPLOAD_USER_IDS.contains(userId);
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
            if (canUploadAchievement(userId)) {
                return achievementMapper.listAllAchievements();
            }
            return achievementMapper.listApprovedAchievements();
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
        if (!canUploadAchievement(createBy)) {
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
        if (!canUploadAchievement(userId)) {
            throw new RuntimeException("仅成果上传账号或管理员可以修改成果");
        }
        // 核心参数非空校验
        if (achievement == null) {
            throw new RuntimeException("修改成果信息不能为空");
        }
        if (achievementMapper.getAchievementById(id) == null) {
            throw new RuntimeException("成果ID不存在，无法修改");
        }
        try {
            achievement.setAchId(id);
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
        if (!canUploadAchievement(userId)) {
            throw new RuntimeException("仅成果上传账号或管理员可以删除成果");
        }
        // 参数非空校验
        if (achId == null) {
            throw new RuntimeException("成果ID不能为空");
        }
        // 校验成果是否存在
        if (achievementMapper.getAchievementById(achId) == null) {
            throw new RuntimeException("标志性成果不存在或已被删除，无法删除");
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
        if (!isAdmin(userId)) {
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
        if (!Objects.equals(submission.getCurrentHandlerId(), userId) && !isAdmin(userId)) {
            throw new RuntimeException("当前用户不是该成果审核单处理人");
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
        if (!isAdmin(userId)) {
            return new ArrayList<AchievementAuditVO>();
        }
        return toAchievementAuditVOs(achievementMapper.getTodoAchievementSubmissions(userId));
    }

    public Object getAchievementAuditRecords(Long userId) {
        if (isAdmin(userId)) {
            return toAchievementAuditVOs(achievementMapper.getAchievementSubmissionRecords(userId));
        }
        return new ArrayList<AchievementAuditVO>();
    }

    public Object getAchievementAuditsByAchId(Long achId, Long userId) {
        BizAchievement achievement = achievementMapper.getAchievementById(achId);
        if (achievement == null) {
            throw new RuntimeException("成果不存在或已删除");
        }
        if (!canUploadAchievement(userId) && !Objects.equals(achievement.getCreateBy(), userId)) {
            throw new RuntimeException("无权查看该成果审核记录");
        }
        return toAchievementAuditVOs(achievementMapper.getAchievementSubmissionsByAchId(achId));
    }

    public Object getAchievementAuditLogs(Long subId, Long userId) {
        BizAchievementSubmission submission = achievementMapper.getAchievementSubmissionById(subId);
        if (submission == null) {
            throw new RuntimeException("成果审核单不存在");
        }
        if (!isAdmin(userId) && !Objects.equals(submission.getSubmitBy(), userId)) {
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
