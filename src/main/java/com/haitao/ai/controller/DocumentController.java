package com.haitao.ai.controller;

import com.haitao.ai.model.ApiResponse;
import com.haitao.ai.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping
    public ApiResponse<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            String documentId = UUID.randomUUID().toString();
            documentService.uploadDocument(file, documentId);
            return ApiResponse.success(documentId);
        } catch (Exception e) {
            return ApiResponse.error("Failed to upload document: " + e.getMessage());
        }
    }

    @DeleteMapping("/{documentId}")
    public ApiResponse<String> deleteDocument(@PathVariable String documentId) {
        try {
            documentService.deleteDocument(documentId);
            return ApiResponse.success("Document deleted successfully");
        } catch (Exception e) {
            return ApiResponse.error("Failed to delete document: " + e.getMessage());
        }
    }
}