package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.dto.OwnershipTransferDTO;
import org.example.entity.vo.ErrorVO;
import org.example.service.OwnershipTransferService;
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

/** 管理员归属移交接口：任务与绩效共用，身份来自已验证的登录 Token。 */
@RestController
@RequestMapping("/manage/transfer")
public class OwnershipTransferController {
    private static final Logger LOG = LoggerFactory.getLogger(OwnershipTransferController.class);

    private final OwnershipTransferService transfers;

    public OwnershipTransferController(OwnershipTransferService transfers) {
        this.transfers = transfers;
    }

    @GetMapping("/capabilities")
    public Object capabilities(HttpServletRequest request) {
        return Map.of("canTransfer", transfers.canTransfer(user(request)));
    }

    @PostMapping("/tasks")
    public Object transferTasks(@RequestBody OwnershipTransferDTO body, HttpServletRequest request) {
        return transfers.transferTasks(body, user(request));
    }

    @PostMapping("/performances")
    public Object transferPerformances(@RequestBody OwnershipTransferDTO body, HttpServletRequest request) {
        return transfers.transferPerformances(body, user(request));
    }

    @GetMapping("/logs")
    public Object logs(HttpServletRequest request, @RequestParam("targetType") String targetType,
                       @RequestParam("targetId") Long targetId) {
        return transfers.logs(targetType, targetId, user(request));
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
        LOG.error("归属移交操作失败", error);
        return ResponseEntity.internalServerError().body(new ErrorVO("归属移交失败，请稍后重试", 500));
    }
}
