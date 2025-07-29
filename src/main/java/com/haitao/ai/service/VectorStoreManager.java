package com.haitao.ai.service;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class VectorStoreManager {

    private final VectorStore vectorStore;
    private final Parser tikaParser;

    public VectorStoreManager(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.tikaParser = new AutoDetectParser();
    }

    /**
     * 添加文档到向量数据库
     * @param file 上传的文件
     * @param documentId 文档唯一ID
     * @return 处理后的文档块数量
     */
    public int addDocument(MultipartFile file, String documentId) throws IOException, TikaException, SAXException {
        String content = extractTextFromFile(file);
        
        Document document = new Document(content);
        document.getMetadata().put("document_id", documentId);
        document.getMetadata().put("file_name", file.getOriginalFilename());
        document.getMetadata().put("file_type", file.getContentType());
        document.getMetadata().put("file_size", String.valueOf(file.getSize()));
        
        TextSplitter textSplitter = new TokenTextSplitter();
        List<Document> splitDocuments = textSplitter.split(List.of(document));
        
        vectorStore.accept(splitDocuments);
        return splitDocuments.size();
    }

    /**
     * 从文件中提取文本内容
     */
    private String extractTextFromFile(MultipartFile file) throws IOException, TikaException, SAXException {
        Metadata metadata = new Metadata();
        metadata.set("resourceName", file.getOriginalFilename());
        
        try (InputStream inputStream = file.getInputStream()) {
            BodyContentHandler handler = new BodyContentHandler(-1); // -1表示不限制输出长度
            ParseContext context = new ParseContext();
            
            tikaParser.parse(inputStream, handler, metadata, context);
            return handler.toString();
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