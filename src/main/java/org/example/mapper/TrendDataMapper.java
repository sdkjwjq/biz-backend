package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.entity.BizTrendData;
import java.util.List;

@Mapper
public interface TrendDataMapper {

    /**
     * 插入趋势数据
     */
    @Insert("INSERT INTO biz_trend_data (year, month, day, total_tasks, completion_count, completion_rate) " +
            "VALUES (#{year}, #{month}, #{day}, #{totalTasks}, #{completionCount}, #{completionRate})")
    void insertTrendData(BizTrendData trendData);

    /**
     * 根据年份获取所有数据
     */
    @Select("SELECT * FROM biz_trend_data WHERE year = #{year} AND is_delete = 0 " +
            "ORDER BY year DESC, month DESC, day DESC")
    List<BizTrendData> getTrendDataByYear(@Param("year") Integer year);

    /**
     * 获取最近一年的数据
     */
    @Select("SELECT * FROM biz_trend_data WHERE year = #{year} AND is_delete = 0 " +
            "ORDER BY month, day LIMIT 365")
    List<BizTrendData> getLatestTrendData(@Param("year") Integer year);

    /**
     * 检查今天是否已记录
     */
    @Select("SELECT COUNT(*) FROM biz_trend_data " +
            "WHERE year = YEAR(CURDATE()) " +
            "AND month = MONTH(CURDATE()) " +
            "AND day = DAY(CURDATE()) " +
            "AND is_delete = 0")
    Integer checkTodayRecorded();
}