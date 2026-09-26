package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizSubForthDTO {
    private Long task_id;
    private BigDecimal reported_value;
    private String data_type;
    private String task_name;
    private BigDecimal target_value;
    private BigDecimal actual_progress;
    private BigDecimal counted_progress;

    public BizSubForthDTO(Long taskId, BigDecimal reportedValue, String dataType) {
        this.task_id = taskId;
        this.reported_value = reportedValue;
        this.data_type = dataType;
    }

}
