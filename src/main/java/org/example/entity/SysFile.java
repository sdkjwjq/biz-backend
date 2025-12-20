package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysFile {
    private Long fileId; // 文件ID
    private String fileName; // 文件名
    private String filePath; // 路径
    private String fileUrl; // URL
    private String fileSuffix; // 后缀
    private Long fileSize; // 大小
    private Long uploadBy; // 上传人ID
    private Integer isDelete; // 0:存在 1:删除
    private Date uploadTime; // 上传时间
}