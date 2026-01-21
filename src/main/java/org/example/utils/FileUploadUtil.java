package org.example.utils;

import org.example.entity.dto.FileUploadDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUploadUtil {
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    private static final Pattern INVALID_CHARS = Pattern.compile("[\\\\/:*?\"<>|]");
    private static final Pattern DUPLICATE_PATTERN = Pattern.compile("(.+?)(?:\\((\\d+)\\))?(\\.[^.]*)?$");

    public static FileUploadDTO upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 创建上传目录
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 获取并处理原始文件名
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }

        // 安全处理
        String safeFileName = Paths.get(originalName).getFileName().toString();
        safeFileName = INVALID_CHARS.matcher(safeFileName).replaceAll("");

        if (safeFileName.isEmpty()) {
            safeFileName = "uploaded_file";
        }

        // 生成唯一文件名
        String finalFileName = getUniqueFileName(uploadPath, safeFileName);

        // 保存文件
        Path filePath = uploadPath.resolve(finalFileName);
        Files.copy(file.getInputStream(), filePath);
        filePath.toFile().setReadable(true, false);

        return new FileUploadDTO(finalFileName, "/uploads/" + finalFileName);
    }

    /**
     * 智能生成唯一文件名
     * 示例：
     * file.txt -> file.txt (不存在时)
     * file.txt -> file(1).txt (file.txt已存在)
     * file(1).txt -> file(2).txt (file(1).txt已存在)
     * file(2).txt -> file(3).txt (file(2).txt已存在)
     */
    private static String getUniqueFileName(Path uploadPath, String fileName) {
        // 解析文件名，提取基础名称、数字后缀和扩展名
        Matcher matcher = DUPLICATE_PATTERN.matcher(fileName);
        if (!matcher.matches()) {
            throw new RuntimeException("文件名格式不正确");
        }

        String baseName = matcher.group(1);      // 基础名称
        String numberStr = matcher.group(2);     // 数字后缀，可能为null
        String extension = matcher.group(3) != null ? matcher.group(3) : ""; // 扩展名

        int startNumber = 1;
        if (numberStr != null) {
            // 如果文件名已经有数字后缀，则从该数字+1开始
            try {
                startNumber = Integer.parseInt(numberStr) + 1;
                // 如果有数字后缀，baseName就是原始基础名
                // 例如：file(1).txt -> baseName = "file", numberStr = "1"
            } catch (NumberFormatException e) {
                // 如果数字格式错误，从1开始
                startNumber = 1;
            }
        }

        // 先检查原始文件名（没有后缀的情况）
        if (numberStr == null && !Files.exists(uploadPath.resolve(fileName))) {
            return fileName;
        }

        // 寻找下一个可用的数字后缀
        for (int i = startNumber; i <= 1000; i++) {
            String newFileName;
            if (i == 1) {
                // 第一次添加后缀
                newFileName = String.format("%s(%d)%s", baseName, i, extension);
            } else {
                // 更新数字后缀
                newFileName = String.format("%s(%d)%s", baseName, i, extension);
            }

            if (!Files.exists(uploadPath.resolve(newFileName))) {
                return newFileName;
            }
        }

        throw new RuntimeException("文件名冲突过多，请重命名文件后上传");
    }
}