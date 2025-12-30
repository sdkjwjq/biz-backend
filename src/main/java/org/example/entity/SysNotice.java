package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysNotice {
    private Long noticeId; // 通知ID
    private Long fromUserId; // 发起人ID
    private Long toUserId; // 接收人ID
    private String type; // 类型
    private String triggerEvent; // 触发事件
    private String title; // 标题
    private String content; // 内容
    private String sourceType; // 关联来源类型
    private Long sourceId; // 关联业务ID
    private String isRead; // 阅读状态 0:未读 1:已读
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
}