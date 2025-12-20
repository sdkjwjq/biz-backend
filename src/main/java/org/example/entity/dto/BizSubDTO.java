package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
//reported_value(可空 value_type) task_id
public class BizSubDTO {
    private Long task_id;
    private BigDecimal reported_value;
    private String data_type;
    private String filename;
}
