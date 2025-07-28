package com.haitao.ai.controller;

import com.haitao.ai.exception.BusinessException;
import com.haitao.ai.exception.ErrorCode;
import com.haitao.ai.model.ApiResponse;
import com.haitao.ai.service.DocumentService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @SneakyThrows
    @PostMapping
    public ApiResponse<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        String documentId = UUID.randomUUID().toString();
        documentService.uploadDocument(file, documentId);
        return ApiResponse.success(documentId);
    }

    @DeleteMapping("/{documentId}")
    public ApiResponse<String> deleteDocument(@PathVariable String documentId) {
        documentService.deleteDocument(documentId);
        return ApiResponse.success("Document deleted successfully");
    }
}