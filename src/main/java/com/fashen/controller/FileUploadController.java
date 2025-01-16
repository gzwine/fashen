package com.fashen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/file")
@Tag(name = "文件上传接口", description = "处理文件上传相关的接口")
@CrossOrigin
public class FileUploadController {

    @Value("${file.upload.path}")
    private String uploadPath;

    @PostMapping("/upload")
    @Operation(summary = "上传法审文件", description = "上传文件并返回文件存储路径")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "文件不能为空"));
        }

        // 创建上传目录
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        File uploadDir = new File(uploadPath + File.separator + datePath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成新的文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + extension;

        // 保存文件
        try {
            File destFile = new File(uploadDir.getAbsolutePath() + File.separator + newFileName);
            file.transferTo(destFile);

            Map<String, String> response = new HashMap<>();
            response.put("fileName", originalFilename);
            response.put("filePath", datePath + "/" + newFileName);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "文件上传失败：" + e.getMessage()));
        }
    }
} 