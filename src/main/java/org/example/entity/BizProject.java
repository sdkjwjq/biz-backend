package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizProject {
    private Long projectId; // 项目ID
    private String projectName; // 项目名称
    private String projectCode; // 项目编码
    private Long leaderId; // 项目负责人ID
    private Date startDate; // 开始日期
    private Date endDate; // 结束日期
    /**
     * 项目状态枚举：
     * 0:未开始 1:进行中 2:已完成
     * 3:暂停 4:逾期
     */
    private String status;
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}