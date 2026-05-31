package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.dto.BizAuditDTO;
import org.example.entity.vo.ErrorVO;
import org.example.service.PerformanceService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/performance")
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    @GetMapping
    public Object getAllPerformance(HttpServletRequest request) {
        try {
            return performanceService.getAllPerformance(currentUserId(request));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @PostMapping("/refresh")
    public Object refreshPerformance() {
        try {
            return performanceService.calcuateAllPerformance();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @GetMapping("/{perfId}")
    public Object getPerformanceById(@PathVariable("perfId") Long perfId, HttpServletRequest request) {
        try {
            return performanceService.getPerformanceById(perfId, currentUserId(request));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @GetMapping("/task/{prefId}")
    public Object getTaskByPerfId(@PathVariable("prefId") Long prefId,
                                  @RequestParam(value = "year", required = false) Integer year,
                                  HttpServletRequest request) {
        try {
            if (year == null) {
                return performanceService.getTaskByPerfId(prefId, currentUserId(request));
            }
            return performanceService.getTaskByPerfIdForYear(prefId, year, currentUserId(request));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @GetMapping("/year/{year}")
    public Object getPerformanceByYear(@PathVariable("year") Integer year, HttpServletRequest request) {
        try {
            return performanceService.getPerformanceByYear(year, currentUserId(request));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @PostMapping("/submit")
    public Object submitPerformance(@RequestParam("pref_id") Long prefId,
                                    @RequestParam("actual_value") BigDecimal actualValue,
                                    @RequestParam("year") Integer year,
                                    @RequestParam(value = "comment", required = false) String comment,
                                    HttpServletRequest request) {
        try {
            return performanceService.submitPref(prefId, actualValue, year, currentUserId(request), comment);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @GetMapping("/audit/todo")
    public Object getTodoPerformanceAudits(HttpServletRequest request) {
        try {
            return performanceService.getTodoPerformanceAudits(currentUserId(request));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @GetMapping("/audit/records")
    public Object getPerformanceAuditRecords(HttpServletRequest request) {
        try {
            return performanceService.getPerformanceAuditRecords(currentUserId(request));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @GetMapping("/audit/perf/{perfId}")
    public Object getPerformanceAuditsByPerfAndYear(@PathVariable("perfId") Long perfId,
                                                     @RequestParam(value = "year", required = false) Integer year,
                                                     HttpServletRequest request) {
        try {
            return performanceService.getPerformanceAuditsByPerfAndYear(perfId, year, currentUserId(request));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @GetMapping("/audit/logs/{subId}")
    public Object getPerformanceAuditLogs(@PathVariable("subId") Long subId, HttpServletRequest request) {
        try {
            return performanceService.getPerformanceAuditLogs(subId, currentUserId(request));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @PostMapping("/audit")
    public Object auditPerformance(@RequestBody BizAuditDTO auditDTO, HttpServletRequest request) {
        try {
            return performanceService.auditPerformance(auditDTO, currentUserId(request));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @PostMapping("/audit/withdraw/{subId}")
    public Object withdrawPerformanceSubmission(@PathVariable("subId") Long subId, HttpServletRequest request) {
        try {
            return performanceService.withdrawPerformanceSubmission(subId, currentUserId(request));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    private Long currentUserId(HttpServletRequest request) {
        return JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
    }
}
