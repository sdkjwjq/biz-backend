package org.example.controller;

import org.example.entity.vo.ErrorVO;
import org.example.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/expert-reviews")
public class ExpertReviewController {
    private final ExpertReviewService reviews;private final PermissionService access;
    public ExpertReviewController(ExpertReviewService reviews,PermissionService access){this.reviews=reviews;this.access=access;}
    @GetMapping public Object objects(@RequestParam Integer year,@RequestParam(required=false) Long rootId,@RequestParam(required=false) Long deptId,@RequestParam(required=false) String keyword,@RequestParam(required=false) String status,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int pageSize,@RequestParam(required=false) Long expertId){return reviews.objects(access.actor(),year,rootId,deptId,keyword,status,page,pageSize,expertId);}
    @GetMapping("/tasks/{taskId}")public Object detail(@PathVariable Long taskId,@RequestParam Integer year,@RequestParam(required=false) Long expertId){return reviews.detail(access.actor(),taskId,year,expertId);}
    @GetMapping("/tasks/{taskId}/history")public Object history(@PathVariable Long taskId,@RequestParam Integer year,@RequestParam(required=false) Long expertId){return reviews.history(access.actor(),taskId,year,expertId);}
    @PostMapping("/draft")public Object draft(@RequestBody ExpertReviewService.Input input){return reviews.save(access.actor(),input,false);}
    @PostMapping("/submit")public Object submit(@RequestBody ExpertReviewService.Input input){return reviews.save(access.actor(),input,true);}
    @GetMapping("/admin/experts")public Object experts(){return reviews.experts(access.actor());}
    @GetMapping("/admin")public Object admin(@RequestParam(required=false) Integer year,@RequestParam(required=false) Long expertId,@RequestParam(required=false) Long taskId,@RequestParam(required=false) String keyword,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="10") int pageSize){return reviews.adminList(access.actor(),year,expertId,taskId,keyword,page,pageSize);}
    @GetMapping("/admin/{reviewId}/history")public Object adminHistory(@PathVariable Long reviewId,@RequestParam(required=false) Integer year){return reviews.adminHistory(access.actor(),reviewId,year);}
    @PostMapping("/admin/export")public ResponseEntity<byte[]> export(@RequestBody ExpertReviewService.Export input)throws java.io.IOException{return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=expert-reviews.xlsx").body(reviews.export(access.actor(),input));}
    @ExceptionHandler(AdminManagementException.class)public ResponseEntity<ErrorVO> error(AdminManagementException e){return ResponseEntity.status(e.getCode()).body(new ErrorVO(e.getMessage(),e.getCode()));}
    @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,org.springframework.web.bind.MissingServletRequestParameterException.class})public ResponseEntity<ErrorVO> invalid(Exception e){return ResponseEntity.badRequest().body(new ErrorVO("请求参数不正确",400));}
}
