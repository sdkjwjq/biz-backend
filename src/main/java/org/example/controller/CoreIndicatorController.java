package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.vo.ErrorVO;
import org.example.service.CoreIndicatorService;
import org.example.service.TaskManagementException;
import org.example.utils.JWTUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequestMapping("/core-indicators")
public class CoreIndicatorController {
    @org.springframework.web.bind.annotation.ExceptionHandler(org.example.service.AdminManagementException.class)
    public org.springframework.http.ResponseEntity<org.example.entity.vo.ErrorVO> permissionError(org.example.service.AdminManagementException e){return org.springframework.http.ResponseEntity.status(e.getCode()).body(new org.example.entity.vo.ErrorVO(e.getMessage(),e.getCode()));}

 private final CoreIndicatorService core;
 public CoreIndicatorController(CoreIndicatorService core){this.core=core;}
 private Long actor(HttpServletRequest request){return JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));}
 public record Period(Integer year,Integer month){}
 public record Version(Long version){}
 @GetMapping("/capabilities") public Object capabilities(HttpServletRequest request){return core.capabilities(actor(request));}
 @GetMapping public Object list(HttpServletRequest request,@RequestParam(required=false) Integer year,@RequestParam(required=false) Integer month){return core.list(actor(request),year,month);}
 @GetMapping("/{indicatorId}/history") public Object history(@PathVariable Long indicatorId,HttpServletRequest request,@RequestParam(required=false) Integer year,@RequestParam(required=false) Integer month){return core.history(indicatorId,actor(request),year,month);}
 @PostMapping("/{indicatorId}/draft") public Object draft(@PathVariable Long indicatorId,@RequestBody Period body,HttpServletRequest request){if(body.year()==null||body.month()==null)throw new TaskManagementException(400,"请选择年月");return core.draft(indicatorId,actor(request),body.year(),body.month());}
 @GetMapping("/reports/{id}") public Object detail(@PathVariable Long id,HttpServletRequest request){return core.detail(id,actor(request));}
 @PutMapping("/reports/{id}") public Object save(@PathVariable Long id,@RequestBody CoreIndicatorService.Fill body,HttpServletRequest request){return core.save(id,actor(request),body);}
 @PostMapping("/reports/{id}/submit") public Object submit(@PathVariable Long id,@RequestBody Version body,HttpServletRequest request){return core.submit(id,actor(request),body.version());}
 @PostMapping("/reports/{id}/audit") public Object audit(@PathVariable Long id,@RequestBody CoreIndicatorService.Decision body,HttpServletRequest request){return core.audit(id,actor(request),body);}
 @PostMapping("/reports/{id}/withdraw") public Object withdraw(@PathVariable Long id,@RequestBody Version body,HttpServletRequest request){return core.withdraw(id,actor(request),body.version());}
 @GetMapping("/audit/todo") public Object todo(HttpServletRequest request){return core.audits(actor(request),true);}
 @GetMapping("/audit/records") public Object records(HttpServletRequest request){return core.audits(actor(request),false);}
 @ExceptionHandler(TaskManagementException.class) public ResponseEntity<ErrorVO> business(TaskManagementException error){return ResponseEntity.status(error.getCode()).body(new ErrorVO(error.getMessage(),error.getCode()));}
 @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class}) public ResponseEntity<ErrorVO> invalid(Exception error){return ResponseEntity.badRequest().body(new ErrorVO("请求参数不正确",400));}
}
