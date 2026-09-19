package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.*;
import org.example.entity.vo.ErrorVO;
import org.example.mapper.*;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/** 快捷筛选仅返回现有列表权限范围内的 ID 和状态摘要。 */
@RestController
public class WorkShortcutController {
    @Autowired private SysMapper users;
    @Autowired private BizMapper tasks;
    @Autowired private PerformanceMapper performances;
    @Autowired private WorkShortcutMapper shortcuts;

    @GetMapping("/biz/tasks/quick-filters")
    public Object taskFilters(HttpServletRequest request) {
        try {
            Long userId = JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            SysUser user = users.getUserById(userId);
            List<BizTask> visible = "0".equals(user.getRole()) ? tasks.getAllTasks() : tasks.getVisibleTasks(userId);
            List<Long> ids = visible.stream().filter(t -> !Integer.valueOf(1).equals(t.getIsDelete()) && Integer.valueOf(3).equals(t.getLevel()))
                    .map(BizTask::getTaskId).toList();
            Set<Long> returned = new LinkedHashSet<>(), todo = new LinkedHashSet<>();
            List<Map<String, Object>> pending = new ArrayList<>();
            for (Map<String, Object> row : ids.isEmpty() ? List.<Map<String, Object>>of() : shortcuts.taskStates(ids)) {
                int status = ((Number) row.get("flowStatus")).intValue();
                Long id = ((Number) row.get("taskId")).longValue();
                if (status < 0) returned.add(id);
                if (List.of(10, 20, 30).contains(status)) pending.add(row);
                if (List.of(10, 20, 30).contains(status) && sameUser(row.get("handlerId"), userId)) todo.add(id);
            }
            return Map.of("returnedIds", returned, "todoIds", todo, "pending", pending);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorVO("任务快捷筛选加载失败", 500));
        }
    }

    @GetMapping("/performance/quick-filters")
    public Object performanceFilters(HttpServletRequest request) {
        try {
            Long userId = JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            SysUser user = users.getUserById(userId);
            List<BizPerformance> visible = "0".equals(user.getRole()) ? performances.getAllPerformance() : performances.getVisiblePerformance(userId);
            List<Long> ids = visible.stream().filter(p -> !Integer.valueOf(1).equals(p.getIsDelete()))
                    .filter(p -> p.getPerfCode() == null || !(p.getPerfCode().startsWith("1.1.") || p.getPerfCode().startsWith("1.2.")))
                    .map(BizPerformance::getPerfId).toList();
            List<Map<String, Object>> returned = new ArrayList<>(), todo = new ArrayList<>(), pending = new ArrayList<>();
            for (Map<String, Object> row : ids.isEmpty() ? List.<Map<String, Object>>of() : shortcuts.performanceStates(ids)) {
                int status = ((Number) row.get("flowStatus")).intValue();
                Map<String, Object> key = Map.of("perfId", row.get("perfId"), "year", row.get("year"));
                if (status < 0) returned.add(key);
                if (List.of(10, 20).contains(status)) pending.add(row);
                if (List.of(10, 20).contains(status) && sameUser(row.get("handlerId"), userId)) todo.add(key);
            }
            return Map.of("returned", returned, "todo", todo, "pending", pending);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorVO("绩效快捷筛选加载失败", 500));
        }
    }

    private boolean sameUser(Object handler, Long userId) {
        return handler instanceof Number && ((Number) handler).longValue() == userId;
    }
}
