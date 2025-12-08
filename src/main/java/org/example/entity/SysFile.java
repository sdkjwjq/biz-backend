package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysFile {
    private Long fileId;
    private String fileName;
    private String filePath;
    private String fileUrl;
    private String fileSuffix;
    private Long fileSize;
    private Long uploadBy;
    private Integer isDelete;
    private Date uploadTime;
}