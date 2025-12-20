package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizTask {
    private Long taskId; // 任务ID
    private Long projectId; // 项目ID
    private Long parentId; // 父任务节点ID
    private String ancestors; // 祖先节点ID集合
    private Integer phase; // 所属年份
    private String taskCode; // 任务编号
    private String taskName; // 任务名称
    private Integer level; // 任务层级

    // 组织归属相关
    private Long deptId; // 归口部门ID
    private Long principalId; // 归口负责人ID
    private Long leaderId; // 任务负责人ID

    // 数据需求配置相关
    private String expTarget; // 预期达成情况
    private String expLevel; // 预期成果级别（国/省/市）
    private String expEffect; // 预期效果
    private String expMaterialDesc; // 预期过程（佐证）材料清单(文本描述)
    /**
     * 数据类型枚举：
     * 0:对指标没有影响
     * 1:数值(累加)
     * 2:百分比(取大)
     */
    private String dataType;
    private BigDecimal targetValue; // 目标值
    private BigDecimal currentValue; // 当前完成值(缓存统计)
    private BigDecimal weight; // 权重(冗余)
    private Integer progress; // 任务进度(冗余)
    /**
     * 任务状态枚举：
     * 0:未开始 1:进行中
     * 2:审核中 3:已完成
     */
    private String status;
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}