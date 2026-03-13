package org.example.service;

import org.example.entity.BizPerformance;
import org.example.entity.BizTask;
import org.example.entity.RelTaskPerformance;
import org.example.mapper.BizMapper;
import org.example.mapper.PerformanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PerformanceService {

    @Autowired
    private PerformanceMapper performanceMapper;

    @Autowired
    private BizMapper taskMapper;

    public Object calcuateAllPerformance(){
        List<BizTask> allTasks = taskMapper.getAllTasks();
        for(BizTask task : allTasks){
            List<Long> prefIds= performanceMapper.getPerfIdByTaskId(task.getTaskId());
            for(Long prefId : prefIds){
                BizPerformance pref = performanceMapper.getPerformanceById(prefId);

                if(pref.getDataType().equals("0")){
                    continue;
                }else if(pref.getDataType().equals("1")){
                    pref.setCurrentValue(pref.getCurrentValue().add(task.getCurrentValue()));
                    pref.setUpdateTime(new Date());
                    performanceMapper.updatePerformance(pref);

                }else if(pref.getDataType().equals("2")){
                    if(task.getCurrentValue().compareTo(pref.getCurrentValue())>0){
                        pref.setCurrentValue(task.getCurrentValue());
                        pref.setUpdateTime(new Date());
                        performanceMapper.updatePerformance(pref);
                    }

                }
            }
        }
//        返回已更新了多少个绩效
        return "绩效数据已更新";

    }

//    根据任务id更新绩效
    public Object updatePerformanceByTaskId(Long taskId){
        if(taskId==null){
            throw new RuntimeException("请输入有效的任务ID");
        }
        if(taskMapper.getTaskById(taskId)==null){
            throw new RuntimeException("没有找到该任务");
        }
        List<Long> prefIds = performanceMapper.getPerfIdByTaskId(taskId);
        for(Long prefId : prefIds){
            BizPerformance pref = performanceMapper.getPerformanceById(prefId);
            if(pref.getDataType().equals("0")){
                continue;
            }else if(pref.getDataType().equals("1")){
                pref.setCurrentValue(pref.getCurrentValue().add(taskMapper.getTaskById(taskId).getCurrentValue()));
                pref.setUpdateTime(new Date());
                performanceMapper.updatePerformance(pref);

            }else if(pref.getDataType().equals("2")){
                if(taskMapper.getTaskById(taskId).getCurrentValue().compareTo(pref.getCurrentValue())>0){
                    pref.setCurrentValue(taskMapper.getTaskById(taskId).getCurrentValue());
                    pref.setUpdateTime(new Date());
                    performanceMapper.updatePerformance(pref);
                }
            }
        }
        return "绩效数据已更新";
    }



    public Object getAllPerformance(){
        return performanceMapper.getAllPerformance();
    }

    public Object getPerformanceById(Long perfId){
        if(perfId==null){
            throw new RuntimeException("请输入有效的绩效ID");
        }
        if(performanceMapper.getPerformanceById(perfId)==null){
            throw new RuntimeException("没有找到该绩效");
        }
        if(performanceMapper.getPerformanceById(perfId).getIsDelete().equals("1")){
            throw new RuntimeException("该绩效已被删除");
        }
        return performanceMapper.getPerformanceById(perfId);
    }

    public Object getPerfByTaskId(Long taskId){
        if(taskId==null){
            throw new RuntimeException("请输入有效的任务ID");
        }
        if(taskMapper.getTaskById(taskId)==null){
            throw new RuntimeException("没有找到该任务");
        }
        if(taskMapper.getTaskById(taskId).getIsDelete().equals("1")){
            throw new RuntimeException("该任务已被删除");
        }
        List<Long> prefIds = performanceMapper.getPerfIdByTaskId(taskId);
        List<BizPerformance> prefList = new ArrayList<>();
        for(Long prefId : prefIds){
            prefList.add(performanceMapper.getPerformanceById(prefId));
        }
        return prefList;
    }

    public Object getTaskByPerfId(Long perfId){
        if(perfId==null){
            throw new RuntimeException("请输入有效的绩效ID");
        }
        if(performanceMapper.getPerformanceById(perfId)==null){
            throw new RuntimeException("没有找到该绩效");
        }
        if(performanceMapper.getPerformanceById(perfId).getIsDelete().equals("1")){
            throw new RuntimeException("该绩效已被删除");
        }
        List<Long> taskIds = performanceMapper.getPerfIdByTaskId(perfId);
        List<BizTask> taskList = new ArrayList<>();
        for(Long taskId : taskIds){
            taskList.add(taskMapper.getTaskById(taskId));
        }
        return taskList;
    }
}
