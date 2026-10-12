package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.vo.ErrorVO;
import org.example.service.TaskAttributesService;
import org.example.service.TaskManagementException;
import org.example.utils.JWTUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manage/tasks")
public class TaskAttributesController {
    private final TaskAttributesService service;
    public TaskAttributesController(TaskAttributesService service) {this.service=service;}
    @PutMapping("/{id}/attributes")
    public Object save(@PathVariable Long id, @RequestBody TaskAttributesService.Change body, HttpServletRequest request) {
        return service.save(id,body,JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
    }
    @ExceptionHandler(TaskManagementException.class)
    public ResponseEntity<ErrorVO> business(TaskManagementException error) {
        return ResponseEntity.status(error.getCode()).body(new ErrorVO(error.getMessage(),error.getCode()));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorVO> invalid(Exception error) {return ResponseEntity.badRequest().body(new ErrorVO("请求参数不正确",400));}
}
