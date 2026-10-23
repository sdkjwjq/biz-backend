package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.vo.ErrorVO;
import org.example.service.BudgetService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/budget")
public class BudgetController {
    @Autowired
    private BudgetService budgetService;
    @Autowired
    private org.example.service.BudgetTrendService budgetTrends;
    @Autowired
    private org.example.service.LeadershipAccessService budgetAccess;

    @GetMapping("/trends")
    public org.springframework.http.ResponseEntity<?> trends(@RequestParam Integer year,
            @RequestParam(defaultValue="0") Integer taskIndex,HttpServletRequest request) {
        try {
            if(!Boolean.TRUE.equals(budgetAccess.capabilities(currentUserId(request)).get("canViewBudget")))
                throw new org.example.service.TaskManagementException(403,"没有预算查阅权限");
            return org.springframework.http.ResponseEntity.ok(budgetTrends.trends(year,taskIndex));
        }
        catch(org.example.service.TaskManagementException error) {
            return org.springframework.http.ResponseEntity.status(error.getCode()).body(new ErrorVO(error.getMessage(),error.getCode()));
        }
    }

    @GetMapping
    public Object getBudget(@RequestParam("year") Integer year,
                            @RequestParam("month") Integer month) {
        try {
            return budgetService.getBudget(year, month);
        } catch (Exception e) {
            var authorizationError=org.example.config.PermissionErrors.response(e);if(authorizationError!=null)return authorizationError;
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
            var authorizationError=org.example.config.PermissionErrors.response(e);if(authorizationError!=null)return authorizationError;
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
            var authorizationError=org.example.config.PermissionErrors.response(e);if(authorizationError!=null)return authorizationError;
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
            var authorizationError=org.example.config.PermissionErrors.response(e);if(authorizationError!=null)return authorizationError;
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @GetMapping("/versions")
    public Object versions(@RequestParam("year") Integer year,
                           @RequestParam("month") Integer month,
                           HttpServletRequest request) {
        try {
            return budgetService.getVersions(year, month, currentUserId(request));
        } catch (Exception e) {
            var authorizationError=org.example.config.PermissionErrors.response(e);if(authorizationError!=null)return authorizationError;
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @GetMapping("/versions/{versionId}")
    public Object versionDetail(@PathVariable("versionId") Long versionId,
                                HttpServletRequest request) {
        try {
            return budgetService.getVersion(versionId, currentUserId(request));
        } catch (Exception e) {
            var authorizationError=org.example.config.PermissionErrors.response(e);if(authorizationError!=null)return authorizationError;
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @PostMapping("/versions/{versionId}/restore")
    public Object restoreVersion(@PathVariable("versionId") Long versionId,
                                 @RequestBody(required = false) Map<String, String> body,
                                 HttpServletRequest request) {
        try {
            String remark = body == null ? null : body.get("remark");
            return budgetService.restoreVersion(versionId, remark, currentUserId(request));
        } catch (Exception e) {
            var authorizationError=org.example.config.PermissionErrors.response(e);if(authorizationError!=null)return authorizationError;
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    private Long currentUserId(HttpServletRequest request) {
        return JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
    }
}
