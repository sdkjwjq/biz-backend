package org.example.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;

/** 预算三张表（总预算 / 年度预算 / 预算执行）的读写。数据按年度与类型存放在 biz_budget_plan。 */
@Service
public class BudgetPlanService {
    private final JdbcTemplate jdbc;
    private final PermissionService access;

    public BudgetPlanService(JdbcTemplate jdbc, PermissionService access) { this.jdbc = jdbc; this.access = access; }

    /** 五年项目总预算（全系统一份，不分年度）。 */
    public List<Map<String, Object>> total(Long actor) {
        access.require(actor, "budget.read");
        if(java.util.stream.IntStream.rangeClosed(2025,2029).noneMatch(y->access.budgetScope(actor,y)))throw new AdminManagementException(403,"没有预算查阅范围");
        return jdbc.queryForList("SELECT item,source,amount,ratio FROM biz_budget_total ORDER BY sort_order,item,source");
    }

}
