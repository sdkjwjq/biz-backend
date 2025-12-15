package org.example.controller;

import org.example.entity.BizTask;
import org.example.entity.vo.ErrorVO;
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
    public Object getAllTasks(){
        try{
            return bizService.getAllTasks();
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

    
}
