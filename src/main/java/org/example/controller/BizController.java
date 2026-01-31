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

/**
 * 业务控制器：处理所有业务相关请求
 * 包含任务管理、材料提交、审批流程等功能
 */
@RestController
@RequestMapping("/biz")
public class BizController {
    @Autowired
    private BizService bizService;

    /**
     * 获取全量任务数据
     * @param request HTTP请求
     * @return 任务列表或错误信息
     */
    @GetMapping("/tasks")
    public Object getTasks(HttpServletRequest request){
        try{
            return bizService.getTasksByUserRole(JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据id获取任务
     * @param taskId 任务ID
     * @return 任务详情或错误信息
     */
    @GetMapping("/tasks/{taskId}")
    public Object getTaskById(@PathVariable("taskId") Long taskId){
        try{
            return bizService.getTaskById(taskId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取当前任务的所有子任务
     * @param taskId 任务ID
     * @return 子任务列表或错误信息
     */
    @GetMapping("/tasks/children")
    public Object getAllChildrenTasks(@RequestParam("task_id") Long taskId){
        try{
            return bizService.getAllChildrenTasks(taskId);
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取当前任务的父任务
     * @param taskId 任务ID
     * @return 父任务信息或错误信息
     */
    @GetMapping("/tasks/parent")
    public Object getParentTask(@RequestParam("task_id") Long taskId){
        try{
            return bizService.getParentTask(taskId);
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据部门id获取任务
     * @param deptId 部门ID
     * @return 任务列表或错误信息
     */
    @GetMapping("/tasks/dept")
    public Object getTasksByDeptId(@RequestParam("dept_id") Long deptId){
        try{
            return bizService.getTasksByDeptId(deptId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 添加任务
     * @param task 任务数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/tasks/manage/add")
    public Object addTask(@RequestBody BizTaskDTO task, HttpServletRequest request){
        try{
            bizService.addTask(task);
            return "任务"+task.getTaskName()+"添加成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 更新任务
     * @param task 任务数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/tasks/manage/update")
    public Object updateTask(@RequestBody BizTaskDTO task, HttpServletRequest request){
        try{
            bizService.updateTask(task);
            return "任务"+task.getTaskName()+"更新成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 完成任务
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/tasks/manage/finish/{taskId}")
    public Object finishTask(@PathVariable("taskId") Long taskId, HttpServletRequest request){
        try{
            return bizService.finishTask(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 提交审批材料
     * @param bizSubDTO 提交数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/submit")
    public Object submitMaterial(@RequestBody BizSubDTO bizSubDTO, HttpServletRequest request){
        try{
            return bizService.submitMaterial(bizSubDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")) );
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取审批单
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 审批单信息或错误信息
     */
    @GetMapping("/audit/{taskId}")
    public Object getAudit(@PathVariable("taskId") Long taskId,HttpServletRequest request){
        try{
            return bizService.getAudit(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取"待我审批"的审批单列表（按 current_handler_id 查询）
     * @param request HTTP请求
     * @return 待审批列表或错误信息
     */
    @GetMapping("/audit/todo")
    public Object getTodoAudits(HttpServletRequest request) {
        try {
            return bizService.getTodoAudits(JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取某任务的全部审批单（用于任务详情抽屉展示完整流程）
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 审批单列表或错误信息
     */
    @GetMapping("/audit/task/{taskId}")
    public Object getAuditByTaskId(@PathVariable("taskId") Long taskId, HttpServletRequest request) {
        try {
            return bizService.getAuditByTaskId(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据任务id获取上次周期上传的文件
     * @param taskId 任务ID
     * @param response HTTP响应
     * @return 文件信息或错误信息
     */
    @GetMapping("/audit/file/{taskId}")
    public Object getLastCycleFiles(@PathVariable("taskId") Long taskId, HttpServletResponse  response) {
        try {
            return bizService.getLastCycleFiles(taskId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取审批操作日志（biz_audit_log）
     * @param subId 提交ID
     * @param request HTTP请求
     * @return 操作日志或错误信息
     */
    @GetMapping("/audit/logs/{subId}")
    public Object getAuditLogs(@PathVariable("subId") Long subId, HttpServletRequest request) {
        try {
            return bizService.getAuditLogs(subId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 审批操作
     * @param bizAuditDTO 审批数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/audit")
    public Object audit(@RequestBody BizAuditDTO bizAuditDTO, HttpServletRequest request){
        try{
            return bizService.audit(bizAuditDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 撤回提交申请
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/drawback/{taskId}")
    public Object drawbackSubmit(@PathVariable("taskId") Long taskId, HttpServletRequest request){
        try{
            return bizService.drawbackSubmit(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 重新提交材料
     * @param bizReSubDTO 重新提交数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/resub")
    public Object reSubmitMaterial(@RequestBody BizReSubDTO bizReSubDTO, HttpServletRequest request){
        try{
            return bizService.reSubmitMaterial(bizReSubDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }
}