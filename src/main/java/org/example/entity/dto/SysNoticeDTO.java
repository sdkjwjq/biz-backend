package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysNoticeDTO {
    private Long to_user_id;
    private String type;
    private String trigger_event;
    private String title;
    private String content;
    private String source_type;
    private Long source_id;
    private String is_read;
}
