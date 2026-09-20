package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.entity.*;
import java.util.List;

@Mapper
public interface TaskManagementMapper {
    /** 同项目新增串行化；先获取锁，再读取编号，避免并发重复创建。 */
    @Select("SELECT project_id FROM biz_project WHERE project_id=#{id} FOR UPDATE")
    Long lockProject(Long id);

    @Select("SELECT task_id FROM biz_task WHERE project_id=#{project} AND phase=#{year} "
            + "AND TRIM(task_code)=#{code} AND (is_delete=0 OR is_delete IS NULL) FOR UPDATE")
    List<Long> matchingCodes(@Param("project") Long project, @Param("year") Integer year, @Param("code") String code);

    @Select("SELECT * FROM sys_dept WHERE is_delete=0 OR is_delete IS NULL ORDER BY dept_id")
    List<SysDept> departments();

    @Select("SELECT * FROM biz_task WHERE level IN (1,2) AND (is_delete=0 OR is_delete IS NULL) ORDER BY task_id")
    List<BizTask> directories();
}
