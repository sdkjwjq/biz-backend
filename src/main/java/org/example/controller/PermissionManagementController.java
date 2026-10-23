package org.example.controller;

import org.example.service.*;
import org.example.entity.vo.ErrorVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/admin/permissions")
public class PermissionManagementController {
    private final PermissionManagementService management;private final PermissionService access;
    public PermissionManagementController(PermissionManagementService management,PermissionService access){this.management=management;this.access=access;}
    @GetMapping("/catalog")public Object catalog(){return management.catalog(access.actor());}
    @GetMapping("/options")public Object options(){return management.options(access.actor());}
    @GetMapping("/users/{id}")public Object user(@PathVariable Long id){return management.configuration(id,access.actor());}
    @PostMapping("/users/{id}/preview")public Object previewUser(@PathVariable Long id,@RequestBody PermissionManagementService.UserChange body){return management.previewUser(id,body,access.actor());}
    @PutMapping("/users/{id}")public Object saveUser(@PathVariable Long id,@RequestBody PermissionManagementService.UserChange body){return management.saveUser(id,body,access.actor());}
    @PostMapping("/roles/preview")public Object previewRole(@RequestBody PermissionManagementService.RoleChange body){return management.previewRole(body,access.actor());}
    @PostMapping("/roles")public Object saveRole(@RequestBody PermissionManagementService.RoleChange body){return management.saveRole(body,access.actor());}
    @DeleteMapping("/roles/{code}")public Object deleteRole(@PathVariable String code,@RequestParam Long version,@RequestParam Long revision,@RequestParam String reason){return management.deleteRole(code,version,revision,reason,access.actor());}
    @PostMapping("/experts")public Object expert(@RequestBody PermissionManagementService.ExpertAccount body){return management.createExpert(body,access.actor());}
    @ExceptionHandler(AdminManagementException.class)public ResponseEntity<ErrorVO> error(AdminManagementException e){return ResponseEntity.status(e.getCode()).body(new ErrorVO(e.getMessage(),e.getCode()));}
    @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})public ResponseEntity<ErrorVO> invalid(Exception e){return ResponseEntity.badRequest().body(new ErrorVO("请求参数不正确",400));}
}
