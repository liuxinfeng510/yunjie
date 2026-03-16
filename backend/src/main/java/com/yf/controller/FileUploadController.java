package com.yf.controller;

import com.yf.vo.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileUploadController {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "application/pdf"
    );

    @PostMapping("/upload")
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return ApiResponse.error("仅支持 JPG/PNG/GIF/PDF 格式");
        }

        try {
            String subDir = "first-marketing";
            File dir = new File(uploadDir + File.separator + subDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

            File dest = new File(dir, fileName);
            file.transferTo(dest);

            String url = "/uploads/" + subDir + "/" + fileName;
            log.info("文件上传成功：{}", url);
            return ApiResponse.success(url);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ApiResponse.error("文件上传失败");
        }
    }
}
