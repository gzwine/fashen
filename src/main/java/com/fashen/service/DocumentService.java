package com.fashen.service;

import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;

@Service
public class DocumentService {
    
    @Value("${ark.api.key}")
    private String apiKey;
    
    private final ArkService arkService;
    
    public DocumentService(@Value("${ark.api.key}") String apiKey) {
        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();
        this.arkService = ArkService.builder()
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3")
                .apiKey(apiKey)
                .build();
    }

    public String extractContent(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename().toLowerCase();
        if (fileName.endsWith(".pdf")) {
            return extractPdfContent(file);
        } else if (fileName.endsWith(".docx")) {
            return extractDocxContent(file);
        } else {
            // 对于txt等纯文本文件
            return new String(file.getBytes());
        }
    }

    private String extractPdfContent(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractDocxContent(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }

    public String analyzeContent(String content) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM)
                .content("你是一个专业的法律文件分析助手，请帮助提炼和总结文件的主要内容。")
                .build());
        
        messages.add(ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content("请分析以下文件内容，提供主要观点和关键信息的总结：\n" + content)
                .build());

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("ep-20241018173751-267j5")
                .messages(messages)
                .build();

        StringBuilder response = new StringBuilder();
        arkService.createChatCompletion(request)
                .getChoices()
                .forEach(choice -> response.append(choice.getMessage().getContent()));

        return response.toString();
    }
} 