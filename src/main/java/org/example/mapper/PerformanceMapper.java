package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.entity.BizPerformance;
import org.example.entity.BizPerformanceYear;
import org.example.entity.RelTaskPerformance;

import java.util.List;

@Mapper
public interface PerformanceMapper {
    @Select("select * from biz_performance ")
    List<BizPerformance> getAllPerformance();

    @Select("select * from biz_performance where perf_id = #{perfId}")
    BizPerformance getPerformanceById(Long perfId);

    @Update(
            "update biz_performance set " +
                    "perf_code = #{perfCode}, " +
                    "perf_name = #{perfName}, " +
                    "target_value = #{targetValue}, " +
                    "current_value = #{currentValue}, " +
                    "data_type = #{dataType}, " +
                    "dept_id = #{deptId}, " +
                    "principal_id = #{principalId}, " +
                    "auditor_id = #{auditorId}, " +
                    "leader_id = #{leaderId}, " +
                    "is_delete = #{isDelete}, " +
                    "update_time = #{updateTime} " +
                    "where perf_id = #{perfId}"
    )
    void updatePerformance(BizPerformance bizPerformance);

    @Select("select * from rel_task_performance where perf_id = #{perfId}")
    List<RelTaskPerformance> getAllTaskPerformanceByPerfId(Long perfId);

    @Select("select * from rel_task_performance where task_id = #{taskId}")
    List<RelTaskPerformance> getAllPerformanceByTaskId(Long taskId);

    // 修复：添加 @Param 注解
    @Select("select * from rel_task_performance where task_id = #{taskId} and perf_id = #{perfId}")
    List<RelTaskPerformance> getRelTaskPerformanceByTaskIdAndPerfId(@Param("taskId") Long taskId, @Param("perfId") Long perfId);

    @Select("select perf_id from rel_task_performance where task_id = #{taskId}")
    List<Long> getPerfIdByTaskId(@Param("taskId") Long taskId);

    @Select("select * from biz_performance where perf_id in (select perf_id from rel_task_performance where task_id = #{taskId})")
    List<BizPerformance> getPerformanceByTaskId(@Param("taskId") Long taskId);

    @Select("select task_id from rel_task_performance where perf_id = #{perfId}")
    List<Long> getTaskIdByPerfId(@Param("perfId") Long perfId);


    // 一次性查询所有关联关系
    @Select("select * from rel_task_performance")
    List<RelTaskPerformance> getAllRelTaskPerformance();



    @Select("select * from biz_performance_year")
    List<BizPerformanceYear> getAllPerformanceYear();

    @Select("select * from biz_performance_year where year_id = #{yearId}")
    BizPerformanceYear getPerformanceYearById(Long yearId);

    @Select("select * from biz_performance_year where year = #{year}")
    List<BizPerformanceYear> getPerformanceYearByYear(@Param("year") Integer year);

    @Select("select * from biz_performance_year where perf_id = #{perfId} and year = #{year}")
    BizPerformanceYear getPerformanceYearByPerfIdAndYear(@Param("perfId") Long perfId, @Param("year") Integer year);

    @Update(
            "UPDATE biz_performance_year SET " +
                    "target_value = #{targetValue}, " +
                    "actual_value = #{actualValue}, " +
                    "data_type = #{dataType}, " +
                    "is_delete = #{isDelete} " +
                    "WHERE year_id = #{yearId}"
    )
    void updatePerformanceYear(BizPerformanceYear bizPerformanceYear);
}