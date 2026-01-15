package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysUserDTO {
    private Long userId; // 用户ID
    private Long deptId; // 所属部门ID
    private String userName; // 账号
    private String nickName; // 姓名
    private String email; // 邮箱
    private String password; // 密码
    private String role; // 角色 0:admin 1:user 2:leader
    private String status; // 状态 0:正常 1:停用
    private Integer isDelete; // 0:存在 1:删除
}
