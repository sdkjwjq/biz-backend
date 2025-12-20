package org.example.utils;

import org.example.entity.dto.FileUploadDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUploadUtil {
    // 使用绝对路径，确保文件保存在项目根目录下的uploads文件夹
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    public static FileUploadDTO upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 创建上传目录
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String fileExtension = originalName.substring(originalName.lastIndexOf("."));
        String newName = UUID.randomUUID().toString() + fileExtension;

        // 保存文件
        Path filePath = uploadPath.resolve(newName);
        Files.copy(file.getInputStream(), filePath);
        filePath.toFile().setReadable(true, false);
        // 返回访问路径（注意这里返回的路径应该与静态资源映射一致）
//        return "/uploads/" + newName;
        return new FileUploadDTO(newName,"/uploads/" + newName);
    }
}
