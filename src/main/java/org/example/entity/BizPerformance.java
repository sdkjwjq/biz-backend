package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizPerformance {
    private Long perfId; // 指标ID
    private Long projectId; // 项目ID
    private Long parentId; // 父指标ID
    private String ancestors; // 祖先指标ID集合
    private String perfCode; // 编码
    private String perfName; // 名称

    private BigDecimal targetValue; // 总目标值
    private BigDecimal currentValue; // 当前完成值(计算得出)
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大

    private Long deptId; // 归口部门ID
    private Long principalId; // 归口负责人ID
    private Long leaderId; // 任务负责人ID

    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}