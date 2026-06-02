package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.vo.ErrorVO;
import org.example.service.BudgetService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/budget")
public class BudgetController {
    @Autowired
    private BudgetService budgetService;

    @GetMapping
    public Object getBudget(@RequestParam("year") Integer year,
                            @RequestParam("month") Integer month) {
        try {
            return budgetService.getBudget(year, month);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @PostMapping("/import")
    public Object importBudget(@RequestParam("year") Integer year,
                               @RequestParam("month") Integer month,
                               @RequestParam("file") MultipartFile file,
                               HttpServletRequest request) {
        try {
            return budgetService.importBudget(year, month, file, currentUserId(request));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @PostMapping("/lock")
    public Object updateLock(@RequestParam("year") Integer year,
                             @RequestParam("month") Integer month,
                             @RequestParam("locked") Boolean locked,
                             HttpServletRequest request) {
        try {
            return budgetService.updateLock(year, month, locked, currentUserId(request));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @GetMapping("/export")
    public Object exportBudget(@RequestParam("year") Integer year,
                               @RequestParam("month") Integer month,
                               HttpServletResponse response) {
        try {
            budgetService.exportBudget(year, month, response);
            return null;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    private Long currentUserId(HttpServletRequest request) {
        return JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
    }
}
