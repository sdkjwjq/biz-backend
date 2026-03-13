package org.example.controller;

import org.example.entity.vo.ErrorVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.example.service.PerformanceService;

@RestController
@RequestMapping("/performance")
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    @GetMapping
    public Object getAllPerformance() {
        try {
            return performanceService.getAllPerformance();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @PostMapping("/refresh")
    public Object refreshPerformance() {
        try {
            return performanceService.calcuateAllPerformance();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @GetMapping("/{perfId}")
    public Object getPerformanceById(@PathVariable("perfId") Long perfId) {
        try {
            return performanceService.getPerformanceById(perfId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

//    @GetMapping("/task/{taskId}")
//    public Object getPerformanceByTaskId(@PathVariable("taskId") Long taskId) {
//        try {
//            return performanceService.getPerfByTaskId(taskId);
//        } catch (Exception e) {
//            return new ErrorVO(e.getMessage(), 500);
//        }
//    }

    @GetMapping("/task/{prefId}")
    public Object getTaskByPerfId(@PathVariable("prefId") Long prefId) {
        try {
            return performanceService.getTaskByPerfId(prefId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }





}
