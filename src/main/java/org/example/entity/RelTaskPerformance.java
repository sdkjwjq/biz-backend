package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelTaskPerformance {
    private Long id;
    private Long taskId;
    private Long perfId;
    private Long yearId;
    private BigDecimal weight;
    private BigDecimal contributionValue;
    private String dataType;
}