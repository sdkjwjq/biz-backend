package org.example.service;

import org.example.mapper.SysMapper;
import org.example.utils.BusinessLogUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

/** 本人主动发送的消息；系统审核通知和平台必读通知不属于可撤回范围。 */
@Service
public class SentMessageService {
    private final PermissionService access;
    private final SysMapper notices;

    public SentMessageService(PermissionService access, SysMapper notices) {
        this.access = access;
        this.notices = notices;
    }

    public Map<String, Object> sent(Long actor, int requestedPage) {
        access.require(actor, "messages.read");
        int page = Math.max(1, requestedPage);
        int pageSize = 10;
        long total = notices.countSentMessages(actor);
        long offset = Math.min((long) (page - 1) * pageSize, total);
        return Map.of("items", notices.sentMessages(actor, pageSize, offset),
                "total", total, "page", page, "pageSize", pageSize);
    }

    @Transactional
    public void recall(Long actor, Long noticeId) {
        access.require(actor, "messages.read");
        if (noticeId == null || noticeId <= 0) throw new AdminManagementException(400, "消息编号不正确");
        Map<String, Object> notice = notices.lockSentMessage(noticeId);
        if (notice == null || !Objects.equals(actor, number(notice.get("fromUserId"))) || !manual(notice))
            throw new AdminManagementException(404, "未找到本人发出的消息");
        if ("1".equals(String.valueOf(notice.get("isDelete"))))
            throw new AdminManagementException(409, "消息已撤回，请刷新列表");
        if (!"0".equals(String.valueOf(notice.get("isRead"))))
            throw new AdminManagementException(409, "对方已阅读，不能撤回");
        if (notices.recallSentMessage(noticeId, actor) != 1)
            throw new AdminManagementException(409, "消息状态已变化，请刷新列表");
        BusinessLogUtil.info("撤回消息", "operatorId", actor, "noticeId", noticeId, "result", "成功");
    }

    private boolean manual(Map<String, Object> notice) {
        String source = String.valueOf(notice.get("sourceType"));
        return "1".equals(source) || ("0".equals(source)
                && "3".equals(String.valueOf(notice.get("type")))
                && "TASK".equals(String.valueOf(notice.get("triggerEvent"))));
    }

    private Long number(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }
}
