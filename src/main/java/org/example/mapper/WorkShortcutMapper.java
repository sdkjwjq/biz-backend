package org.example.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

/** 一次读取当前有效审核摘要，避免列表逐条查询审核记录。 */
@Mapper
public interface WorkShortcutMapper {
    // 任务退回会同时设置 is_delete=1；按最新提交判定，避免把已重提或撤回的历史退回重新计入。
    // 现有批量四级填报挂在三级任务上，同时兼容直接挂在四级任务上的历史审核单。
    @Select("<script>SELECT DISTINCT t.task_id AS taskId, s.flow_status AS flowStatus, s.current_handler_id AS handlerId, "
            + "d.dept_id AS handlerDeptId, d.dept_name AS handlerDeptName "
            + "FROM biz_task t JOIN biz_material_submission s ON ("
            + "s.task_id=t.task_id "
            + "OR s.task_id IN (SELECT c.task_id FROM biz_level4_task c WHERE c.parent_id=t.task_id AND COALESCE(c.is_delete,0)=0)) "
            + "LEFT JOIN sys_user u ON u.user_id=s.current_handler_id AND u.is_delete=0 "
            + "LEFT JOIN sys_dept d ON d.dept_id=u.dept_id AND d.is_delete=0 "
            + "WHERE t.level=3 AND COALESCE(t.is_delete,0)=0 AND (s.is_delete=0 OR s.flow_status &lt; 0) "
            + "AND s.sub_id=(SELECT MAX(n.sub_id) FROM biz_material_submission n WHERE n.task_id=s.task_id) "
            + "AND t.task_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<Map<String, Object>> taskStates(@Param("ids") List<Long> ids);

    @Select("<script>SELECT perf_id AS perfId, year, flow_status AS flowStatus, current_handler_id AS handlerId FROM ("
            + "SELECT s.*, ROW_NUMBER() OVER (PARTITION BY perf_id, year ORDER BY submit_time DESC, sub_id DESC) AS rn "
            + "FROM biz_performance_submission s WHERE perf_id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + ") latest WHERE rn=1 AND is_delete=0</script>")
    List<Map<String, Object>> performanceStates(@Param("ids") List<Long> ids);
}
