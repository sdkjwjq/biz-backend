package org.example.service;

import org.example.entity.*;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.example.mapper.TokenBlacklistMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import org.example.utils.BusinessLogUtil;

/**
 * 定时任务服务类
 * 负责执行定期提醒、清理等后台任务
 */
@Service
public class ScheduledTaskService {

    private Long ADMIN_ID = 110228L;

    @Autowired
    private BizMapper bizMapper;

    @Autowired
    private SysMapper sysMapper;

    @Autowired
    private TokenBlacklistMapper tokenBlacklistMapper;

    /**
     * 每月初提醒审核任务
     * 每月1号上午9:00执行
     * 提醒所有部门负责人本部门任务完成情况
     */
    @Scheduled(cron = "0 0 9 1 * ?")  // 每月1号上午9:00
    @Transactional
    public String monthlyDeptLeaderReminder() {
        try {
            System.out.println("[" + new Date() + "] 开始执行月度部门负责人审核任务提醒...");

            Set<Long> deptLeaderIds = getDeptLeadersId();

            if (deptLeaderIds.isEmpty()) {
                System.out.println("没有需要发送年度提醒的用户");
                return "没有需要发送年度提醒的用户";
            }

            int successCount = 0;
            for (Long deptLeaderId: deptLeaderIds){
                for (Long deptId : sysMapper.getDeptIdsByLeaderId(deptLeaderId)) {
                    SysDept dept = sysMapper.getDeptById(deptId);
                    if (dept == null || Integer.valueOf(1).equals(dept.getIsDelete())) {
                        System.err.println("跳过部门提醒：负责人未关联有效部门，userId=" + deptLeaderId);
                        continue;
                    }
                    // 获取当前时间，将年份转为Integer
                    int currentYear = java.time.LocalDate.now().getYear();
                    // 获取本年度本部门的所有任务
                    List<BizTask> currentYearTasks = bizMapper.getTasksByDeptIdAndPhase(dept.getDeptId(),currentYear);
                    if (currentYearTasks.isEmpty()) {
                        continue;
                    }
                    // 统计截止本月有进展的任务数量
                    Integer monthCount = 0;
                    for (BizTask task: currentYearTasks){
                        if (task.getCurrentValue().signum() != 0) {
                            monthCount++;
                        }
                    }

                    // 统计已完成任务数量
                    Integer completeCount = 0;
                    for (BizTask task: currentYearTasks){
                        if (task.getStatus().equals("3")) {
                            completeCount++;
                        }
                    }

                    // 统计待审核任务数量
                    Integer auditCount = 0;
                    for (BizTask task: currentYearTasks){
                        if (task.getStatus().equals("2")) {
                            auditCount++;
                        }
                    }

                    String title = "部门双高建设任务完成情况月度提醒";
                    String content = "尊敬的 "+ sysMapper.getUserById(deptLeaderId).getNickName() +
                            " 您好,您负责的部门「" + dept.getDeptName() + "」本年度有 " + currentYearTasks.size() + " 个双高建设任务，\n" +
                            "截至本月，共" +  monthCount + "个任务已有进展。\n"+
                            "已完成任务" + completeCount + " 个，" + auditCount + " 个任务尚未审核\n"+
                            "请及时关注本部门双高建设任务进展情况。";
                    sendSystemNotice(deptLeaderId, title, content, "月度提醒");
                    System.out.println("向用户ID " + deptLeaderId + " 发送月度提醒成功");
                    successCount++;
                }
            }
            return "月度提醒完成，成功发送 " + successCount + " 条提醒";

        } catch (Exception e) {
            System.err.println("执行月度审核提醒任务时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


    /**
     * 每月初提醒审核任务
     * 每月1号上午9:00执行
     * 提醒所有有待审核任务的负责人
     */
    @Scheduled(cron = "0 0 9 1 * ?")  // 每月1号上午9:00
    @Transactional
    public String monthlyAuditorReminder() {
        try {
            System.out.println("[" + new Date() + "] 开始执行月度审核人审核任务提醒...");
            List<BizMaterialSubmission> allPendingAudits = bizMapper.getAllPendingAudits();


            Map<Long, Integer> auditorCounts = new HashMap<>();
            int skippedCount = 0;
            for (BizMaterialSubmission pendingAudit: allPendingAudits){
                Long handlerId = pendingAudit.getCurrentHandlerId();
                SysUser handler = handlerId == null ? null : sysMapper.getUserById(handlerId);
                if (handler == null || Integer.valueOf(1).equals(handler.getIsDelete())) {
                    skippedCount++;
                    BusinessLogUtil.info("月度审核提醒", "result", "跳过无有效处理人的审核单",
                            "subId", pendingAudit.getSubId(), "handlerId", handlerId);
                    continue;
                }
                auditorCounts.merge(handlerId, 1, Integer::sum);
            }

            if (auditorCounts.isEmpty()) {
                return "没有需要发送月度审核提醒的用户，跳过 " + skippedCount + " 条异常审核单";
            }
            int successCount = 0;
            for (Long auditorId: auditorCounts.keySet()){
//              获取待审核任务数量
                Integer auditCount = auditorCounts.get(auditorId);

                String title = "月度审核任务提醒";
                String content = "尊敬的 " + sysMapper.getUserById(auditorId).getNickName() +
                        " 您好,截至目前，您有 " + auditCount + " 个待审核任务。\n" +
                        "请及时处理。";

                sendSystemNotice(auditorId, title, content, "月度提醒");
                System.out.println("向用户ID " + auditorId + " 发送月度提醒成功");
                successCount++;
            }
            return "向"+successCount+"名用户发送月度提醒成功，跳过 " + skippedCount + " 条异常审核单";
        } catch (Exception e) {
            System.err.println("执行月度审核提醒任务时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
//
    /**
     * 年度提醒任务
     * 每年1月1日上午10:00执行
     * 提醒所有部门负责人年度任务情况
     */
    @Scheduled(cron = "0 0 10 1 1 ?")  // 每年1月1日上午10:00
    @Transactional
    public String yearlyReminder() {
        try {
            System.out.println("[" + new Date() + "] 开始执行年度提醒任务...");

            // 获取所有需要发送年度提醒的用户ID
            Set<Long> leadersId = getDeptLeadersId();

            if (leadersId.isEmpty()) {
                System.out.println("没有需要发送年度提醒的用户");
                return "没有需要发送年度提醒的用户";
            }
            int currentYear = java.time.LocalDate.now().getYear();
            int successCount = 0;
            for(Long leaderId: leadersId){
                for (Long deptId : sysMapper.getDeptIdsByLeaderId(leaderId)) {
                    SysDept dept = sysMapper.getDeptById(deptId);
                    if (dept == null || Integer.valueOf(1).equals(dept.getIsDelete())) {
                        System.err.println("跳过部门提醒：负责人未关联有效部门，userId=" + leaderId);
                        continue;
                    }
                    // 获取本年度本部门的所有任务
                    List<BizTask> currentYearTasks = bizMapper.getTasksByDeptIdAndPhase(dept.getDeptId(),currentYear);
                    if (currentYearTasks.isEmpty()) {
                        continue;
                    }

                    String title = "部门双高建设任务年度提醒";
                    String content = "尊敬的 "+ sysMapper.getUserById(leaderId).getNickName() +
                            " 您好,您负责的部门「" + dept.getDeptName() + "」本年度有 " + currentYearTasks.size() + " 个双高建设任务，\n" +
                            "请及时关注本部门双高建设任务进展情况。";
                    sendSystemNotice(leaderId, title, content, "月度提醒");
                    successCount++;
                    System.out.println("向用户ID " + leaderId + " 发送月度提醒成功");

                }
            }

            System.out.println("年度提醒完成，成功发送 " + successCount + " 条提醒");
            return "年度提醒完成，成功发送 " + successCount + " 条提醒";

        } catch (Exception e) {
            System.err.println("执行年度提醒任务时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
//
    /**
     * 封装方法：发送系统通知
     *
     * @param toUserId 接收用户ID
     * @param title 通知标题
     * @param content 通知内容
     * @param triggerEvent 触发事件类型
     */
    private void sendSystemNotice(Long toUserId, String title, String content, String triggerEvent) {
        SysNotice notice = new SysNotice();
        notice.setFromUserId(ADMIN_ID);
        notice.setToUserId(toUserId);
        notice.setType("1");  // 提醒类型
        notice.setTriggerEvent(triggerEvent);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setSourceType("0");
        notice.setSourceId(0L);
        notice.setIsRead("0");
        notice.setIsDelete(0);
        notice.setCreateTime(new Date());

        sysMapper.sendNotice(notice);

        System.out.println("系统通知已发送 - 接收人ID: " + toUserId + ", 标题: " + title);
    }

    /**
     * 获取需要发送年度提醒的所有用户ID
     * 您可以自定义这里的逻辑
     *
     * @return 用户ID集合
     */
    private Set<Long> getDeptLeadersId() {
        Set<Long> userIds = new HashSet<>();

        try {
            // 获取所有部门负责人
            List<Long> deptLeaders = sysMapper.getAllDeptLeaders();
            if (deptLeaders != null) {
                for (Long leaderId : deptLeaders) {
                    SysUser leader = leaderId == null ? null : sysMapper.getUserById(leaderId);
                    if (leader == null || Integer.valueOf(1).equals(leader.getIsDelete())) {
                        System.err.println("跳过部门提醒：负责人为空、不存在或已删除，userId=" + leaderId);
                        continue;
                    }
                    userIds.add(leaderId);
                }
            }

            // 可以根据需要添加其他用户组
            // 例如：所有管理员、所有用户等

        } catch (Exception e) {
            System.err.println("获取用户列表时发生错误: " + e.getMessage());
        }

        return userIds;
    }
    /**
     * 测试方法：手动触发月度提醒
     */
    public String triggerMonthDeptLeaderReminder() {
        System.out.println("手动触发月度审核提醒...");
        return monthlyDeptLeaderReminder();
    }

    /**
     * 测试方法：手动触发月度提醒
     */
    public String triggerMonthAuditorReminder() {
        System.out.println("手动触发月度审核提醒...");
        return monthlyAuditorReminder();
    }


    /**
     * 测试方法：手动触发年度提醒
     */
    public String triggerYearlyReminder() {
        System.out.println("手动触发年度提醒...");
        return yearlyReminder();
    }
    /**
     * 每日清理过期的Token黑名单
     * 每天凌晨2:00执行
     */
    @Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2:00
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            System.out.println("[" + new Date() + "] 开始清理过期Token...");
            tokenBlacklistMapper.cleanupExpiredTokens();
            System.out.println("Token清理完成");
        } catch (Exception e) {
            System.err.println("清理过期Token时发生错误: " + e.getMessage());
        }
    }


    /**
     * 启用当年未开始的任务
     * 每年1月1日上午10:00执行
     */
    @Scheduled(cron = "0 0 10 1 1 ?")
    @Transactional
    public void updateTaskStatus() {
        try {
            System.out.println("[" + new Date() + "] 开始更新任务状态...");
            int currentYear = java.time.LocalDate.now().getYear();
            int updatedCount = bizMapper.startPendingTasksByPhase(currentYear);
            System.out.println("已将"+currentYear+"年"+updatedCount+"个未开始任务的状态修改为进行中");
        } catch (Exception e) {
            System.err.println("更新任务状态时发生错误: " + e.getMessage());
            throw new RuntimeException("更新任务状态失败", e);
        }
    }


    @Autowired
    private TrendDataService trendDataService;
    /**
     * 趋势数据定时任务
     * 每天23:30执行
     */
    @Scheduled(cron = "0 30 23 * * ?")
    public void dailyTrendRecord() {
        System.out.println("[" + new Date() + "] 开始执行趋势数据记录...");
        trendDataService.recordDailyTrendData();
    }

}
