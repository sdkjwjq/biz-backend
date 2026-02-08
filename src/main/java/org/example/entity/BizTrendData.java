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
    private Double completionRate; // 用Double简单处理
    private Integer isDelete;
}