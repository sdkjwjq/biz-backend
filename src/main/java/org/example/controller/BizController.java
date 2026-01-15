package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.dto.BizAuditDTO;
import org.example.entity.dto.BizSubDTO;
import org.example.entity.dto.BizReSubDTO;
import org.example.entity.dto.BizTaskDTO;
import org.example.entity.vo.ErrorVO;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.example.service.BizService;

@RestController
@RequestMapping("/biz")
public class BizController {
    @Autowired
    private BizService bizService;

//    获取全量任务数据
    @GetMapping("/tasks")
    public Object getTasks(HttpServletRequest request){
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

//    添加任务
    @PostMapping("/tasks/manage/add")
    public Object addTask(@RequestBody BizTaskDTO task, HttpServletRequest request){
        try{
            bizService.addTask(task);
            return "任务"+task.getTaskName()+"添加成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

//    更新任务
    @PostMapping("/tasks/manage/update")
    public Object updateTask(@RequestBody BizTaskDTO task, HttpServletRequest request){
        try{
            bizService.updateTask(task);
            return "任务"+task.getTaskName()+"更新成功";
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

    // 获取“待我审批”的审批单列表（按 current_handler_id 查询）
    @GetMapping("/audit/todo")
    public Object getTodoAudits(HttpServletRequest request) {
        try {
            return bizService.getTodoAudits(JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    // 获取某任务的全部审批单（用于任务详情抽屉展示完整流程）
    @GetMapping("/audit/task/{taskId}")
    public Object getAuditByTaskId(@PathVariable("taskId") Long taskId, HttpServletRequest request) {
        try {
            return bizService.getAuditByTaskId(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

//    根据任务id获取上次周期上传的文件
    @GetMapping("/audit/file/{taskId}")
    public Object getLastCycleFiles(@PathVariable("taskId") Long taskId, HttpServletResponse  response) {
        try {
            bizService.downloadTaskFile(taskId, response);
            return "下载成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    // 获取审批操作日志（biz_audit_log）
    @GetMapping("/audit/logs/{subId}")
    public Object getAuditLogs(@PathVariable("subId") Long subId, HttpServletRequest request) {
        try {
            return bizService.getAuditLogs(subId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @PostMapping("/audit")
    public Object audit(@RequestBody BizAuditDTO bizAuditDTO, HttpServletRequest request){
        try{
            return bizService.audit(bizAuditDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @PostMapping("/resub")
    public Object reSubmitMaterial(@RequestBody BizReSubDTO bizReSubDTO, HttpServletRequest request){
        try{
            return bizService.reSubmitMaterial(bizReSubDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }
}
