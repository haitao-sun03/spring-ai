package com.haitao.ai.service;

import com.haitao.ai.entity.DocumentMetadata;
import com.haitao.ai.repository.DocumentMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentMetadataRepository metadataRepo;
    private final VectorStoreManager vectorStoreManager;

    @Transactional
    public void uploadDocument(MultipartFile file, String documentId) throws IOException {

        // 保存元数据
        DocumentMetadata metadata = DocumentMetadata.builder()
            .documentId(documentId)
            .fileName(file.getOriginalFilename())
            .fileType(file.getContentType())
            .fileSize(file.getSize())
            .status(DocumentMetadata.DocumentStatus.PROCESSING)
            .build();
        
        metadataRepo.save(metadata);
        
        try {
            // 处理文档
            int chunkCount = vectorStoreManager.addDocument(file, documentId);
            
            // 更新状态
            metadata.setStatus(DocumentMetadata.DocumentStatus.COMPLETED);
            metadata.setChunkCount(chunkCount);
            metadataRepo.save(metadata);
            
        } catch (Exception e) {
            log.error("文档处理失败: {}", documentId, e);
            metadata.setStatus(DocumentMetadata.DocumentStatus.FAILED);
            metadataRepo.save(metadata);
        }
    }

    @Transactional
    public void deleteDocument(String documentId) {
        DocumentMetadata metadata = metadataRepo.findActiveByDocumentId(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId));
        
        // 从向量库删除
        vectorStoreManager.deleteDocumentsByDocumentId(documentId);
        
        // 软删除
        metadataRepo.softDeleteByDocumentId(documentId);
    }
    
    // 自定义异常
    public static class DocumentNotFoundException extends RuntimeException {
        public DocumentNotFoundException(String documentId) {
            super("Document not found with ID: " + documentId);
        }
    }
}