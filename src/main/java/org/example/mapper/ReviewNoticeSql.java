package org.example.mapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** 运行时与发布清理使用同一判定，结果通知及普通提醒不在清理范围内。 */
public final class ReviewNoticeSql {
    private static final String STALE;

    static {
        try (InputStream input = ReviewNoticeSql.class.getResourceAsStream("/sql/stale-review-notices.sql")) {
            if (input == null) throw new IllegalStateException("缺少审核通知清理规则");
            STALE = new String(input.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static String visible() {
        return "SELECT n.* FROM sys_notice n WHERE n.to_user_id = #{userId} "
                + "AND COALESCE(n.is_delete, 0) = 0 AND NOT (COALESCE((" + STALE + "), 0))";
    }

    public static String retireTask() {
        return retire("n.source_type = '0' AND n.source_id IN "
                + "(SELECT task_id FROM biz_material_submission WHERE sub_id = #{subId})");
    }

    public static String retirePerformance() {
        return retire("n.source_type = '2' AND n.source_id = #{subId}");
    }

    public static String retireAchievement() {
        return retire("n.source_type = '3' AND n.source_id = #{subId}");
    }

    private static String retire(String scope) {
        // 先普通读取候选 ID，避免 UPDATE 子查询对其它业务表加共享锁。
        // 调用方持有对应审核业务锁，状态变化与通知删除在同一事务提交。
        return "SELECT n.notice_id FROM sys_notice n WHERE " + scope + " AND (" + STALE + ")";
    }
}
