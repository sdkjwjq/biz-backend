package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAchievement {
    private Long achId; // 成果ID
    private Long projectId; // 项目ID
    private String achName; // 成果名称
    private String level; // 成果级别
    private String sourceType; // 来源类型
    private Long sourceId; // 来源ID
    private Date obtainDate; // 获得日期
    private Long createBy; // 创建人ID
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
}