package org.example.controller;



import org.example.entity.BizTrendData;
import org.example.entity.vo.ErrorVO;
import org.example.service.TrendDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard/trend")
public class TrendDataController {

    @Autowired
    private TrendDataService trendDataService;

    /**
     * 获取趋势数据
     * @param year 年份（可选，默认当前年份）
     */
    @GetMapping("/{year}")
    public Object getTrendData(@PathVariable(value = "year",required = false) Integer year) {
        try {
            List<BizTrendData> trendData = trendDataService.getTrendDataByYear(year);
            return trendData;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取当前年份的趋势数据
     */
    @GetMapping("/")
    public Object getCurrentYearTrendData() {
        try {
            List<BizTrendData> trendData = trendDataService.getTrendDataByYear(null);
            return trendData;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 手动记录（测试用）
     */
    @PostMapping("/record")
    public Object manualRecord() {
        try {
            return trendDataService.triggerRecord();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }
}