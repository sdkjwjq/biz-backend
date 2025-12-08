package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysDept {
    private Long deptId;
    private String deptName;
    private Long leaderId;
    private String status;
    private Integer isDelete;
    private Date createTime;
    private Date updateTime;
}