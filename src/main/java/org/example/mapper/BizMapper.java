package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.entity.BizTask;

import java.util.List;

@Mapper
public interface BizMapper {
//    获取全部任务
    @Select("SELECT * FROM biz_task")
    public List<BizTask> getAllTasks();
//    根据id获取任务
    @Select("SELECT * FROM biz_task WHERE task_id = #{taskId}")
    public BizTask getTaskById(Long taskId);

//    根据部门id获取任务
    @Select("SELECT * FROM biz_task WHERE dept_id = #{deptId}")
    public List<BizTask> getTasksByDeptId(Long deptId);

//    根据归口负责人获取任务
    @Select("SELECT * FROM biz_task WHERE principal_id = #{principalId}")
    public List<BizTask> getTasksByPrincipalId(Long principalId);

//  获取所有一级任务
    @Select("SELECT * FROM biz_task WHERE level=1")
    public List<BizTask> getAllFirstLevelTasks();

//    获取当前任务的所有子任务
    @Select("SELECT * FROM biz_task WHERE parent_id = #{taskId}")
    public List<BizTask> getAllChildrenTasks(Long taskId);

//    根据一级任务id获取其二级子任务
    @Select("SELECT * FROM biz_task WHERE parent_id = #{parentId} AND level=2")
    public List<BizTask> getSecondLevelTasksByParentId(Long parentId);

//    根据二级任务id获取其三级子任务
    @Select("SELECT * FROM biz_task WHERE parent_id = #{parentId} AND level=3")
    public List<BizTask> getThirdLevelTasksByParentId(Long parentId);


}
