package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.vo.ErrorVO;
import org.example.service.AdminOverviewService;
import org.example.service.AdminManagementException;
import org.example.utils.JWTUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

/** 仅提供预定义的只读统计，不接收任意查询表达式。 */
@RestController
@RequestMapping("/admin/overview")
public class AdminOverviewController {
    private final AdminOverviewService overview;
    public AdminOverviewController(AdminOverviewService overview) { this.overview = overview; }

    @GetMapping("/summary")
    public Object summary(@RequestParam int year, @RequestParam int month,
                          @RequestParam(required=false) String module, HttpServletRequest request) {
        return overview.summary(year, month, module, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
    }

    @GetMapping("/analysis")
    public Object analysis(@RequestParam int year, @RequestParam int month, @RequestParam String metric,
                           @RequestParam(required=false) String dimension, @RequestParam(required=false) String groupKey,
                           @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="10") int pageSize,
                           @RequestParam org.springframework.util.MultiValueMap<String,String> filters, HttpServletRequest request) {
        return overview.analysis(year, month, metric, dimension, groupKey, page, pageSize,
                JWTUtil.getUserIdFromToken(request.getHeader("Authorization")),filters);
    }

    @ExceptionHandler(AdminManagementException.class)
    public ResponseEntity<ErrorVO> business(AdminManagementException error) {
        return ResponseEntity.status(error.getCode()).body(new ErrorVO(error.getMessage(), error.getCode()));
    }
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorVO> invalid(Exception error) {
        return ResponseEntity.badRequest().body(new ErrorVO("统计参数不正确",400));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorVO> unexpected(Exception error) {
        org.slf4j.LoggerFactory.getLogger(getClass()).error("Overview analysis failed",error);
        return ResponseEntity.internalServerError().body(new ErrorVO("统计加载失败，请重试",500));
    }
}

