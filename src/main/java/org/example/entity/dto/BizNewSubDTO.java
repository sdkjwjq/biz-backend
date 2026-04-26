package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizNewSubDTO {
    private Long third_task_id;
    private List<BizSubDTO> sub_list;
}
