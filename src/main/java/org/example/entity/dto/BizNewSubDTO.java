package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizNewSubDTO {
    private Long third_task_id;
    private Long task_id;
    private Long file_id;
    private BigDecimal reported_value;
    private String data_type;
    private String comment;
    private List<BizSubForthDTO> sub_list;
}
