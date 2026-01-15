package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAuditDTO {
    private Long sub_id;
    private Boolean is_pass;
    private String title;
    private String content;
//    private String file_id;
}
