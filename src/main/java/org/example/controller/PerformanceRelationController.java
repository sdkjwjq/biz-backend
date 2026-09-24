package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.dto.PerformanceRelationDTO;
import org.example.entity.vo.ErrorVO;
import org.example.service.PerformanceRelationService;
import org.example.service.TaskManagementException;
import org.example.utils.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

/** 管理员编辑绩效关联任务接口，身份来自已验证的登录 Token。 */
@RestController
@RequestMapping("/manage/performance-relation")
public class PerformanceRelationController {
    private static final Logger LOG = LoggerFactory.getLogger(PerformanceRelationController.class);

    private final PerformanceRelationService relations;

    public PerformanceRelationController(PerformanceRelationService relations) {
        this.relations = relations;
    }

    @GetMapping("/capabilities")
    public Object capabilities(HttpServletRequest request) {
        return Map.of("canManageRelation", relations.canManageRelation(user(request)));
    }

    @GetMapping("/candidates")
    public Object candidates(HttpServletRequest request, @RequestParam("perfId") Long perfId,
                             @RequestParam("year") Integer year) {
        return relations.candidates(perfId, year, user(request));
    }

    @GetMapping("/counts")
    public Object counts(HttpServletRequest request, @RequestParam("year") Integer year) {
        return relations.counts(year, user(request));
    }

    @GetMapping("/logs")
    public Object logs(HttpServletRequest request, @RequestParam("perfId") Long perfId,
                       @RequestParam("year") Integer year) {
        return relations.logs(perfId, year, user(request));
    }

    @PostMapping
    public Object save(@RequestBody PerformanceRelationDTO body, HttpServletRequest request) {
        return relations.save(body, user(request));
    }

    private Long user(HttpServletRequest request) {
        return JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
    }

    @ExceptionHandler(TaskManagementException.class)
    public ResponseEntity<ErrorVO> businessError(TaskManagementException error) {
        return ResponseEntity.status(error.getCode()).body(new ErrorVO(error.getMessage(), error.getCode()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorVO> parameterError(Exception error) {
        return ResponseEntity.badRequest().body(new ErrorVO("请求参数不正确", 400));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorVO> unexpected(Exception error) {
        LOG.error("绩效关联编辑失败", error);
        return ResponseEntity.internalServerError().body(new ErrorVO("绩效关联编辑失败，请稍后重试", 500));
    }
}
