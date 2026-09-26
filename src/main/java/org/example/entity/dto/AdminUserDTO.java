package org.example.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class AdminUserDTO {
    private Long userId;
    private Long deptId;
    private String userName;
    private String nickName;
    private String email;
    private String role;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private Date expectedUpdateTime;
    private String reason;
}
