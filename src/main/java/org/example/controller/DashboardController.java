package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.vo.*;
import org.example.service.BizService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 看板数据控制器
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private BizService bizService;

    /**
     * 获取看板数据汇总
     * @param request HTTP请求
     * @return 看板数据汇总
     */
    @GetMapping("/summary")
    public Object getDashboardSummary(HttpServletRequest request) {
        try {
            // 验证token（可选，如果需要权限控制）
            String token = request.getHeader("Authorization");
            if (token != null) {
                JWTUtil.verifyJwtToken(token);
            }

            return bizService.getDashboardSummary();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取所有任务完成率
     * @return 所有任务完成率
     */
    @GetMapping("/completion/overall")
    public Object getAllTaskCompletionRate() {
        try {
            return bizService.getAllTaskCompletionRate();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取本年度任务完成率
     * @param year 年份（可选，默认当前年份）
     * @return 本年度任务完成率
     */
    @GetMapping("/completion/year")
    public Object getYearTaskCompletionRate(@RequestParam(required = false,value = "year") Integer year) {
        try {
            return bizService.getYearTaskCompletionRate(year);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取中期任务完成率
     * @param endYear 截止年份（可选，默认2028）
     * @return 中期任务完成率
     */
    @GetMapping("/completion/midterm")
    public Object getMidTermTaskCompletionRate(@RequestParam(required = false,value = "year") Integer endYear) {
        try {
            return bizService.getMidTermTaskCompletionRate(endYear);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取一级任务完成率
     * @return 一级任务完成率
     */
    @GetMapping("/completion/first-level")
    public Object getFirstLevelTaskCompletionRate() {
        try {
            return bizService.getFirstLevelTaskCompletionRate();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取各部门整体任务完成率
     * @return 各部门任务完成率列表
     */
    @GetMapping("/dept/overall")
    public Object getDeptTaskCompletionRates() {
        try {
            return bizService.getDeptTaskCompletionRates();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取各部门本年度任务完成率
     * @param year 年份（可选，默认当前年份）
     * @return 各部门本年度任务完成率列表
     */
    @GetMapping("/dept/year")
    public Object getDeptYearTaskCompletionRates(@RequestParam(required = false,value = "year") Integer year) {
        try {
            return bizService.getDeptYearTaskCompletionRates(year);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取各部门中期任务完成率
     * @param endYear 截止年份（可选，默认2028）
     * @return 各部门中期任务完成率列表
     */
    @GetMapping("/dept/midterm")
    public Object getDeptMidTermTaskCompletionRates(@RequestParam(required = false,value = "endYear") Integer endYear) {
        try {
            return bizService.getDeptMidTermTaskCompletionRates(endYear);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取一级任务详细情况
     * @return 一级任务详情列表
     */
    @GetMapping("/tasks/first-level")
    public Object getFirstLevelTaskDetails() {
        try {
            return bizService.getFirstLevelTaskDetails();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

//    获取全量任务详细情况
    @GetMapping("/tasks/all_level")
    public Object getAllTaskDetails() {
        try {
            return bizService.getAllTaskDetails();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取单个部门详细统计信息
     * @param deptId 部门ID
     * @return 部门统计详情
     */
    @GetMapping("/dept/{deptId}")
    public Object getDeptStatsDetail(@PathVariable("deptId") Long deptId) {
        try {
            return bizService.getDeptStatsDetail(deptId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 批量获取多个部门统计信息
     * @param deptIds 部门ID列表（逗号分隔）
     * @return 部门统计列表
     */
    @GetMapping("/dept/batch")
    public Object getBatchDeptStats(@RequestParam("deptIds") String deptIds) {
        try {
            String[] ids = deptIds.split(",");
            List<Map<String, Object>> result = new java.util.ArrayList<>();

            for (String idStr : ids) {
                try {
                    Long deptId = Long.parseLong(idStr.trim());
                    Map<String, Object> stats = bizService.getDeptStatsDetail(deptId);
                    result.add(stats);
                } catch (NumberFormatException e) {
                    // 跳过无效的ID
                }
            }

            return result;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取对比数据：不同维度的完成率对比
     * @param dimension 对比维度：dept, year, level
     * @return 对比数据
     */
    @GetMapping("/comparison/{dimension}")
    public Object getCompletionComparison(@PathVariable("dimension") String dimension) {
        try {
            Map<String, Object> result = new java.util.HashMap<>();

            switch (dimension.toLowerCase()) {
                case "dept":
                    // 各部门完成率对比
                    result.put("data", bizService.getDeptTaskCompletionRates());
                    result.put("dimension", "部门");
                    break;

                case "year":
                    // 历年完成率对比（需要扩展Mapper）
                    result.put("data", getYearComparisonData());
                    result.put("dimension", "年度");
                    break;

                case "level":
                    // 各级别任务完成率对比
                    result.put("data", getLevelComparisonData());
                    result.put("dimension", "任务级别");
                    break;

                default:
                    throw new RuntimeException("不支持的对比维度: " + dimension);
            }

            return result;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    // 辅助方法：获取年度对比数据（需要扩展实现）
    private List<Map<String, Object>> getYearComparisonData() {
        // 这里简化实现，实际需要从数据库查询历年数据
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        // 示例数据
        for (int year = 2023; year <= 2025; year++) {
            Map<String, Object> yearData = new java.util.HashMap<>();
            yearData.put("year", year);
            yearData.put("totalTasks", 50 + (year - 2023) * 10);
            yearData.put("completedTasks", 30 + (year - 2023) * 15);
            yearData.put("completionRate", 60 + (year - 2023) * 10);
            result.add(yearData);
        }

        return result;
    }

    // 辅助方法：获取级别对比数据
    private List<Map<String, Object>> getLevelComparisonData() {
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        // 一级任务
        TaskCompletionVO firstLevel = bizService.getFirstLevelTaskCompletionRate();
        result.add(createLevelData("一级任务", firstLevel));

        // 二级任务（需要扩展Mapper）
        result.add(createLevelData("二级任务", 100, 60));

        // 三级任务（需要扩展Mapper）
        result.add(createLevelData("三级任务", 200, 120));

        return result;
    }

    private Map<String, Object> createLevelData(String levelName, TaskCompletionVO vo) {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("level", levelName);
        data.put("totalTasks", vo.getTotalTasks());
        data.put("completedTasks", vo.getCompletedTasks());
        data.put("completionRate", vo.getCompletionRate());
        return data;
    }

    private Map<String, Object> createLevelData(String levelName, int totalTasks, int completedTasks) {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("level", levelName);
        data.put("totalTasks", totalTasks);
        data.put("completedTasks", completedTasks);

        if (totalTasks > 0) {
            data.put("completionRate",
                    java.math.BigDecimal.valueOf(completedTasks * 100.0 / totalTasks)
                            .setScale(2, java.math.BigDecimal.ROUND_HALF_UP)
            );
        } else {
            data.put("completionRate", java.math.BigDecimal.ZERO);
        }

        return data;
    }
}