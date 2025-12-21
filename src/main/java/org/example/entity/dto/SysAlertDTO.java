package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//站内信息 预警
//input token to_user_nick_name title/content source_id
public class SysAlertDTO {
    private String to_user_nick_name;
    private String title;
    private String content;
    private Long source_id;
}
