package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.entity.BizPerformance;
import org.example.entity.RelTaskPerformance;

import java.util.List;

//-- 4.1 绩效指标表
//DROP TABLE IF EXISTS `biz_performance`;
//CREATE TABLE `biz_performance` (
//        `perf_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '指标ID',
//        `project_id` bigint(20) NOT NULL,
//    `parent_id` bigint(20) DEFAULT 0,
//        `ancestors` varchar(500) DEFAULT '',
//        `perf_code` varchar(50) DEFAULT NULL COMMENT '编码',
//        `perf_name` varchar(255) NOT NULL COMMENT '名称',
//
//        `target_value` decimal(20,4) DEFAULT 0.00 COMMENT '总目标值',
//        `current_value` decimal(20,4) DEFAULT 0.00 COMMENT '当前完成值(计算得出)',
//        `data_type` char(1) DEFAULT '1' COMMENT '数据类型 0:对指标没有影响 1:数值(累加) 2:百分比(取大)',
//
//        `dept_id` bigint(20) NOT NULL COMMENT '归口部门ID',
//        `principal_id` bigint(20) NOT NULL COMMENT '归口负责人ID',
//        `auditor_id` bigint(20) NOT NULL COMMENT '专业群审核人ID',
//        `leader_id` bigint(20) DEFAULT NULL COMMENT '任务负责人ID',
//
//        `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',
//        `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
//        `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
//PRIMARY KEY (`perf_id`),
//CONSTRAINT `fk_perf_dept` FOREIGN KEY (`dept_id`) REFERENCES `sys_dept` (`dept_id`) ON DELETE RESTRICT,
//CONSTRAINT `fk_perf_principal` FOREIGN KEY (`principal_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT
//) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效指标库';
//
//        -- 4.2 绩效年度分解
//DROP TABLE IF EXISTS `biz_performance_year`;
//CREATE TABLE `biz_performance_year` (
//        `year_id` bigint(20) NOT NULL AUTO_INCREMENT,
//    `perf_id` bigint(20) NOT NULL COMMENT '指标ID',
//        `year` int(4) NOT NULL,
//    `target_value` decimal(20,4) DEFAULT 0.00,
//        `actual_value` decimal(20,4) DEFAULT 0.00 COMMENT '年度实际完成',
//        `data_type` char(1) DEFAULT '1' COMMENT '数据类型 0:对指标没有影响 1:数值(累加) 2:百分比(取大)',
//        `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',
//PRIMARY KEY (`year_id`),
//CONSTRAINT `fk_year_perf` FOREIGN KEY (`perf_id`) REFERENCES `biz_performance` (`perf_id`) ON DELETE CASCADE
//) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效年度表';
//
//        -- 4.3 任务-绩效关联
//DROP TABLE IF EXISTS `rel_task_performance`;
//CREATE TABLE `rel_task_performance` (
//        `id` bigint(20) NOT NULL AUTO_INCREMENT,
//    `task_id` bigint(20) NOT NULL,
//    `perf_id` bigint(20) NOT NULL,
//    `year_id` bigint(20) NOT NULL,
//    `weight` decimal(5,2) DEFAULT 1.00 ,
//        `contribution_value` decimal(20,4) DEFAULT 0.00 COMMENT '该任务为KPI贡献的数值',
//        `data_type` char(1) DEFAULT '1' COMMENT '数据类型 0:对指标没有影响 1:数值(累加) 2:百分比(取大)',
//PRIMARY KEY (`id`),
//UNIQUE KEY `uk_tp` (`task_id`, `perf_id`, `year_id`),
//CONSTRAINT `fk_rel_task` FOREIGN KEY (`task_id`) REFERENCES `biz_task` (`task_id`) ON DELETE CASCADE,
//CONSTRAINT `fk_rel_perf` FOREIGN KEY (`perf_id`) REFERENCES `biz_performance` (`perf_id`) ON DELETE CASCADE,
//CONSTRAINT `fk_rel_year` FOREIGN KEY (`year_id`) REFERENCES `biz_performance_year` (`year_id`) ON DELETE CASCADE
//) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务绩效关联表';
@Mapper
public interface PerformanceMapper {
//    getAllPerformance
    @Select("select * from biz_performance ")
    List<BizPerformance> getAllPerformance();

    @Select("select * from biz_performance where perf_id = #{perfId}")
    BizPerformance getPerformanceById(Long perfId);


//    updatePerformance
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

//    获取该绩效相关的所有任务
    @Select("select * from rel_task_performance where perf_id = #{perfId}")
    List<RelTaskPerformance> getAllTaskPerformanceByPerfId(Long perfId);

//   根据任务id获取所有的相关绩效
    @Select("select * from rel_task_performance where task_id = #{taskId}")
    List<RelTaskPerformance> getAllPerformanceByTaskId(Long taskId);

//    根据taskId找到prefId
    @Select("select perf_id from rel_task_performance where task_id = #{taskId}")
    List<Long> getPerfIdByTaskId(Long taskId);

//    根据perfId找到taskId
    @Select("select task_id from rel_task_performance where perf_id = #{perfId}")
    List<Long> getTaskIdByPerfId(Long perfId);

}
