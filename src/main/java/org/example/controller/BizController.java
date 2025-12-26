package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.dto.AuditDTO;
import org.example.entity.dto.BizSubDTO;
import org.example.entity.dto.ReSubDTO;
import org.example.entity.vo.ErrorVO;
import org.example.utils.FileUploadUtil;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.example.service.BizService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/biz")
public class BizController {
    @Autowired
    private BizService bizService;

//    获取全量任务数据
    @GetMapping("/tasks")
    public Object getAllTasks(HttpServletRequest request){
        try{
            return bizService.getTasksByUserRole(JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

//    根据id获取任务
    @GetMapping("/tasks/{taskId}")
    public Object getTaskById(@PathVariable("taskId") Long taskId){
        try{
            return bizService.getTaskById(taskId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

//  获取当前任务的所有子任务
    @GetMapping("/tasks/children")
    public Object getAllChildrenTasks(@RequestParam("task_id") Long taskId){
        try{
            return bizService.getAllChildrenTasks(taskId);
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }
//  获取当前任务的父任务
    @GetMapping("/tasks/parent")
    public Object getParentTask(@RequestParam("task_id") Long taskId){
        try{
            return bizService.getParentTask(taskId);
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }

//    根据部门id获取任务
    @GetMapping("/tasks/dept")
    public Object getTasksByDeptId(@RequestParam("dept_id") Long deptId){
        try{
            return bizService.getTasksByDeptId(deptId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

//    提交审批材料
    @PostMapping("/submit")
    public Object submitMaterial(@RequestBody BizSubDTO bizSubDTO, HttpServletRequest request){
        try{
            return bizService.submitMaterial(bizSubDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")) );
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

//    获取审批单
    @GetMapping("/audit/{taskId}")
    public Object getAudit(@PathVariable("taskId") Long taskId,HttpServletRequest request){
        try{
            return bizService.getAudit(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }

    }

    @PostMapping("/audit")
    public Object audit( @RequestBody AuditDTO auditDTO, HttpServletRequest request){
        try{
            return bizService.audit(auditDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @PostMapping("/resub")
    public Object reSubmitMaterial(@RequestBody ReSubDTO reSubDTO, HttpServletRequest request){
        try{
            return bizService.reSubmitMaterial(reSubDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }
}
