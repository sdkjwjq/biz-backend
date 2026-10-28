package org.example.entity.dto;

import lombok.Data;

import java.util.List;

/** 管理员归属移交请求：批量统一指定目标归口部门、责任人与归口审核人。 */
@Data
public class OwnershipTransferDTO {
    private List<Long> ids;
    private Long deptId;
    private Long leaderId;
    /** Omitted by legacy clients: retain each record's current professional-group reviewer. */
    private Long auditorId;
    private Long principalId;
    private String reason;
    /** Optional for old clients; new clients send exactly what the administrator reviewed. */
    private List<CurrentOwnership> expected;
    public record CurrentOwnership(Long id, Long deptId, Long leaderId, Long auditorId, Long principalId) { }
}
