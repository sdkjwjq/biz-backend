package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizTrendData {
    private Long dataId;
    private Integer year;
    private Integer month;
    private Integer day;
    private Date createTime;
    private Integer totalTasks;      // 新增：当年总任务数
    private Integer completionCount; // 完成数量
    private Double completionRate;   // 完成率
    private Integer isDelete;
}