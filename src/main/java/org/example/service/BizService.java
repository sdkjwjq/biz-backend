package org.example.service;

import org.example.entity.BizTask;
import org.example.mapper.BizMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BizService {

    @Autowired
    private BizMapper bizMapper;
//    获取全部任务
    public List<BizTask> getAllTasks(){
        return bizMapper.getAllTasks();
    }

//根据id获取任务
    public BizTask getTaskById(Long taskId){
        try{
            if (taskId == null){
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null){
                throw new RuntimeException("该任务不存在");
            }
            return bizMapper.getTaskById(taskId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
//    获取当前任务的所有子任务
    public List<BizTask> getAllChildrenTasks(Long taskId){
        try{
            if (taskId == null){
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null){
                throw new RuntimeException("该任务不存在");
            }
            return bizMapper.getAllChildrenTasks(taskId);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

//  获取当前任务的父任务
    public BizTask getParentTask(Long taskId){
        try{
            if (taskId == null){
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null){
                throw new RuntimeException("该任务不存在");
            }
            Long parentId = bizMapper.getTaskById(taskId).getParentId();
            return bizMapper.getTaskById(parentId);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public List<BizTask> getTasksByDeptId(Long deptId){
        try{
            return bizMapper.getTasksByDeptId(deptId);
        }catch (Exception e){
            throw new RuntimeException("获取任务失败,请检查部门id是否正确");
        }

    }


    public List<BizTask> getThirdLevelTasksByParentId(Long parentId){
        try {
            return bizMapper.getThirdLevelTasksByParentId(parentId);
        } catch (RuntimeException e) {
            throw new RuntimeException("获取任务失败,请检查任务id是否正确");
        }
    }
}
