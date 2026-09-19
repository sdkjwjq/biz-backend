package org.example.entity;

import lombok.Data;
import java.util.Date;

/** 纪实主体。提交后的正文及关联统计快照只读。 */
@Data
public class BizWorkRecord {
    private Long recordId;
    private Long ownerId;
    private String ownerName;
    private Integer recordYear;
    private Integer recordMonth;
    private Integer status;
    private Long version;
    private String problems;
    private String nextFocus;
    private String otherMatters;
    private Date createTime;
    private Date updateTime;
    private Date submitTime;
}
