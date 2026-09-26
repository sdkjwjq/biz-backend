package org.example.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class AdminDepartmentDTO {
    private Long leaderId;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private Date expectedUpdateTime;
    private String reason;
}
