package org.example.utils;

import org.example.entity.SysDept;
import org.example.entity.SysUser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class AchievementPermissionUtil {
    public static final Long PRIMARY_AUDITOR_ID = 110228L;

    private static final Set<Long> ACHIEVEMENT_AUDITOR_IDS;
    private static final Set<Long> ACHIEVEMENT_UPLOAD_ACCOUNT_IDS;

    static {
        Set<Long> ids = new HashSet<>();
        ids.add(110050L);
        ids.add(110228L);
        ids.add(110038L);
        ids.add(110750L);
        ids.add(112327L);
        ids.add(110279L);
        ACHIEVEMENT_AUDITOR_IDS = Collections.unmodifiableSet(ids);

        Set<Long> uploadIds = new HashSet<>();
        uploadIds.add(800001L);
        uploadIds.add(800002L);
        uploadIds.add(800003L);
        ACHIEVEMENT_UPLOAD_ACCOUNT_IDS = Collections.unmodifiableSet(uploadIds);
    }

    private AchievementPermissionUtil() {
    }

    public static boolean isActiveUser(SysUser user) {
        return user != null && (user.getIsDelete() == null || user.getIsDelete() == 0);
    }

    public static boolean isActiveDept(SysDept dept) {
        return dept != null && (dept.getIsDelete() == null || dept.getIsDelete() == 0);
    }

    public static boolean isDepartmentAchievementAccount(SysUser user, SysDept dept) {
        if (!isActiveUser(user) || !isActiveDept(dept) || user.getUserId() == null || user.getDeptId() == null) {
            return false;
        }
        return user.getUserId().equals(990000L + user.getDeptId())
                && user.getDeptId().equals(dept.getDeptId());
    }

    public static boolean isLegacyAchievementUploadAccount(SysUser user, SysDept dept) {
        return isActiveUser(user)
                && isActiveDept(dept)
                && user.getUserId() != null
                && user.getDeptId() != null
                && ACHIEVEMENT_UPLOAD_ACCOUNT_IDS.contains(user.getUserId())
                && user.getDeptId().equals(dept.getDeptId());
    }

    public static boolean isAchievementUploadAccount(SysUser user, SysDept dept) {
        return isDepartmentAchievementAccount(user, dept) || isLegacyAchievementUploadAccount(user, dept);
    }

    public static boolean isAchievementAuditor(Long userId) {
        return userId != null && ACHIEVEMENT_AUDITOR_IDS.contains(userId);
    }

    public static boolean isAchievementAuditor(SysUser user) {
        return isActiveUser(user) && isAchievementAuditor(user.getUserId());
    }

    public static boolean canViewAchievement(SysUser user, SysDept dept) {
        return isAchievementUploadAccount(user, dept) || isAchievementAuditor(user);
    }

    public static boolean canUploadAchievement(SysUser user, SysDept dept) {
        return isAchievementUploadAccount(user, dept);
    }
}
