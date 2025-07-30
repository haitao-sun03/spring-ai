package com.haitao.ai.service;

import org.apache.tika.exception.TikaException;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class VectorStoreManager {

    private final VectorStore vectorStore;


    public VectorStoreManager(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 添加文档到向量数据库
     * @param file 上传的文件
     * @param documentId 文档唯一ID
     * @return 处理后的文档块数量
     */
    public int addDocument(MultipartFile file, String documentId) throws IOException, TikaException, SAXException {
        List<Document> documents = extractTextFromFile(file);
//        处理metadata
        for (Document document : documents) {
            document.getMetadata().put("document_id", documentId);
            document.getMetadata().put("resource_name", file.getOriginalFilename());
        }

        TextSplitter textSplitter = new TokenTextSplitter();
        List<Document> splitDocuments = textSplitter.split(documents);
        
        vectorStore.accept(splitDocuments);
        return splitDocuments.size();
    }

    /**
     * 从文件中提取文本内容
     */
    private List<Document> extractTextFromFile(MultipartFile file) throws IOException, TikaException, SAXException {

        try (InputStream inputStream = file.getInputStream()) {
            // 将 MultipartFile 的 InputStream 转换为 Resource
            InputStreamResource resource = new InputStreamResource(inputStream);
            TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
            return tikaDocumentReader.read();
        }
    }

    /**
     * 根据文档ID删除所有相关文档块
     */
    public void deleteDocumentsByDocumentId(String documentId) {
        // 精确匹配 document_id 元数据字段
        vectorStore.delete(
                vectorStore.similaritySearch("") // 空搜索获取全部
                        .stream()
                        .filter(doc -> documentId.equals(doc.getMetadata().get("document_id")))
                        .map(Document::getId)
                        .toList()
        );
    }

    /**
     * 清空向量存储
     */
    public void clearVectorStore() {
        vectorStore.delete(vectorStore.similaritySearch("").stream()
            .map(Document::getId)
            .toList());
    }
}