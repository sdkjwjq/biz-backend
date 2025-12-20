package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysDept {
    private Long deptId; // 部门ID
    private String deptName; // 部门名称
    private Long leaderId; // 部门负责人ID
    private String status; // 状态 0:正常 1:停用
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}