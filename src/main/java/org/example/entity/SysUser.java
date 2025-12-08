package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysUser {
    private Long userId;
    private Long deptId;
    private String userName;
    private String nickName;
    private String email;
    private String password;
    private String role;
    private String status;
    private Integer isDelete;
    private Date createTime;
    private Date updateTime;
}