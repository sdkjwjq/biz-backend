package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.dto.AdminActionDTO;
import org.example.entity.dto.AdminDepartmentDTO;
import org.example.entity.dto.AdminUserDTO;
import org.example.entity.vo.ErrorVO;
import org.example.service.AdminManagementException;
import org.example.service.AdminManagementService;
import org.example.utils.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequestMapping("/admin")
public class AdminManagementController {
    private static final Logger LOG = LoggerFactory.getLogger(AdminManagementController.class);
    private final AdminManagementService admin;

    public AdminManagementController(AdminManagementService admin) {
        this.admin = admin;
    }

    @GetMapping("/overview")
    public Object overview(@RequestParam("year") Integer year, @RequestParam("month") Integer month,
                           HttpServletRequest request) {
        return admin.overview(year, month, user(request));
    }

    @GetMapping("/users")
    public Object users(@RequestParam(value="keyword", required=false) String keyword,
                        @RequestParam(value="deptId", required=false) Long deptId,
                        @RequestParam(value="role", required=false) String role,
                        @RequestParam(value="status", required=false) String status,
                        @RequestParam(value="page", defaultValue="1") Integer page,
                        @RequestParam(value="pageSize", defaultValue="10") Integer pageSize,
                        HttpServletRequest request) {
        return admin.users(keyword, deptId, role, status, page, pageSize, user(request));
    }

    @PostMapping("/users")
    public Object createUser(@RequestBody AdminUserDTO body, HttpServletRequest request) {
        return admin.createUser(body, user(request));
    }

    @PutMapping("/users/{userId}")
    public Object updateUser(@PathVariable Long userId, @RequestBody AdminUserDTO body, HttpServletRequest request) {
        return admin.updateUser(userId, body, user(request));
    }

    @GetMapping("/users/{userId}/impact")
    public Object userImpact(@PathVariable Long userId, HttpServletRequest request) {
        return admin.userImpact(userId, user(request));
    }

    @PostMapping("/users/{userId}/status")
    public Object updateUserStatus(@PathVariable Long userId, @RequestBody AdminActionDTO body,
                                   HttpServletRequest request) {
        return admin.updateUserStatus(userId, body, user(request));
    }

    @PostMapping("/users/{userId}/reset-password")
    public Object resetPassword(@PathVariable Long userId, @RequestBody AdminActionDTO body,
                                HttpServletRequest request) {
        admin.resetPassword(userId, body, user(request));
        return java.util.Map.of("message", "密码已重置为工号，用户下次使用时必须修改密码");
    }

    @GetMapping("/departments")
    public Object departments(HttpServletRequest request) {
        return admin.departments(user(request));
    }

    @GetMapping("/departments/{deptId}/impact")
    public Object departmentImpact(@PathVariable Long deptId, HttpServletRequest request) {
        return admin.departmentImpact(deptId, user(request));
    }

    @PutMapping("/departments/{deptId}")
    public Object updateDepartment(@PathVariable Long deptId, @RequestBody AdminDepartmentDTO body,
                                   HttpServletRequest request) {
        return admin.updateDepartment(deptId, body, user(request));
    }

    @GetMapping("/operations")
    public Object operations(@RequestParam(value="module", required=false) String module,
                             @RequestParam(value="operatorId", required=false) Long operatorId,
                             @RequestParam(value="keyword", required=false) String keyword,
                             @RequestParam(value="action", required=false) String action,
                             @RequestParam(value="startDate", required=false) String startDate,
                             @RequestParam(value="endDate", required=false) String endDate,
                             @RequestParam(value="page", defaultValue="1") Integer page,
                             @RequestParam(value="pageSize", defaultValue="10") Integer pageSize,
                             HttpServletRequest request) {
        return admin.operations(module, operatorId, keyword, action, startDate, endDate, page, pageSize, user(request));
    }

    @GetMapping("/operations/{type}/{id}")
    public Object operation(@PathVariable String type, @PathVariable Long id, HttpServletRequest request) {
        return admin.operation(type, id, user(request));
    }

    @org.springframework.beans.factory.annotation.Autowired private org.example.service.BusinessListFilterService listFilters;
    @PostMapping("/operations/export")
    public void exportOperations(@RequestParam(value="module", required=false) String module,
                                 @RequestParam(value="operatorId", required=false) Long operatorId,
                                 @RequestParam(value="keyword", required=false) String keyword,
                                 @RequestParam(value="action", required=false) String action,
                                 @RequestParam(value="startDate", required=false) String startDate,
                                 @RequestParam(value="endDate", required=false) String endDate,
                                 @RequestParam org.springframework.util.MultiValueMap<String,String> filters, HttpServletRequest request, HttpServletResponse response) {
        if(filters.keySet().stream().anyMatch(k->k.startsWith("f."))){
            filters.set("purpose","export");
            var result=listFilters.query("admin-operations",user(request),filters);
            admin.writeOperationExport((java.util.List<java.util.Map<String,Object>>)result.get("items"),response,user(request));
        } else admin.exportOperations(module, operatorId, keyword, action, startDate, endDate, response, user(request));
    }

    @GetMapping("/work-records/deleted")
    public Object deletedWorkRecords(@RequestParam(value="year", required=false) Integer year,
                                     @RequestParam(value="month", required=false) Integer month,
                                     @RequestParam(value="keyword", required=false) String keyword,
                                     @RequestParam(value="page", defaultValue="1") Integer page,
                                     @RequestParam(value="pageSize", defaultValue="10") Integer pageSize,
                                     HttpServletRequest request) {
        return admin.deletedWorkRecords(year, month, keyword, page, pageSize, user(request));
    }

    private Long user(HttpServletRequest request) {
        return JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
    }

    @ExceptionHandler(AdminManagementException.class)
    public ResponseEntity<ErrorVO> business(AdminManagementException error) {
        return ResponseEntity.status(error.getCode()).body(new ErrorVO(error.getMessage(), error.getCode()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorVO> parameter(Exception error) {
        return ResponseEntity.badRequest().body(new ErrorVO("请求参数不正确", 400));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorVO> unexpected(Exception error) {
        LOG.error("管理中心操作失败", error);
        return ResponseEntity.internalServerError().body(new ErrorVO("管理中心操作失败，请稍后重试", 500));
    }
}
