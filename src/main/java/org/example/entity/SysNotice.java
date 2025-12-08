package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysNotice {
    private Long noticeId;
    private Long fromUserId;
    private Long toUserId;
    private String type;
    private String triggerEvent;
    private String title;
    private String content;
    private String sourceType;
    private Long sourceId;
    private String isRead;
    private Integer isDelete;
    private Date createTime;
}