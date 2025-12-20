package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//上传文件 post
//input token reported_value(可空 value_type) task_id文件
//新生成file数据
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadDTO {
    private String filename;
    private String filepath;
}
