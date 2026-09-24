package org.example.entity.dto;

import lombok.Data;

import java.util.List;

/** 管理员归属移交请求：批量统一指定目标归口部门、责任人与归口审核人。 */
@Data
public class OwnershipTransferDTO {
    private List<Long> ids;
    private Long deptId;
    private Long leaderId;
    private Long principalId;
    private String reason;
}
