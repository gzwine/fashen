package com.fashen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fashen.service.DocumentService;

@RestController
@RequestMapping("/api/file")
@Tag(name = "文件上传接口", description = "处理文件上传相关的接口")
@CrossOrigin
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    @Value("${file.upload.path}")
    private String uploadPath;
    
    @Autowired
    private DocumentService documentService;

    // 用于存储历史记录
    private List<Map<String, String>> analysisHistory = Collections.synchronizedList(new ArrayList<>());

    @PostMapping("/upload")
    @Operation(summary = "上传法审文件", description = "上传文件并返回文件存储路径")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "文件不能为空"));
        }

        try {
            // 提取文件内容
            String content = documentService.extractContent(file);
            
            // 使用大模型分析内容
            String analysis = documentService.analyzeContent(content);
            
            // 保存文件
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            File uploadDir = new File(uploadPath + File.separator + datePath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + extension;
            
            File destFile = new File(uploadDir.getAbsolutePath() + File.separator + newFileName);
            file.transferTo(destFile);

            Map<String, String> result = new HashMap<>();
            result.put("fileName", originalFilename);
            result.put("filePath", datePath + "/" + newFileName);
            result.put("analysis", analysis);
            
            // 添加到历史记录
            analysisHistory.add(0, result);
            
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("文件处理失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "文件处理失败：" + e.getMessage()));
        }
    }

    @GetMapping("/history")
    @Operation(summary = "获取历史记录", description = "获取所有文件处理的历史记录")
    public ResponseEntity<List<Map<String, String>>> getHistory() {
        return ResponseEntity.ok(analysisHistory);
    }

    // 添加配置，允许访问上传的文件
    @Configuration
    public static class WebConfig implements WebMvcConfigurer {
        @Value("${file.upload.path}")
        private String uploadPath;

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/uploads/**")
                   .addResourceLocations("file:" + uploadPath + "/");
        }
    }
} 