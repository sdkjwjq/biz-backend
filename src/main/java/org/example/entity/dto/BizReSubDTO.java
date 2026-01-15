package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizReSubDTO {
    private Long sub_id;
    private BigDecimal reported_value;
    private String data_type;
    private Long file_id;
}
