package com.haitao.ai.configuration;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
class CommandRunners {

    @Bean
    CommandLineRunner documentLoader(VectorStore vectorStore, @Value("classpath:documents/sample.txt") Resource pdfResource) {
        return args -> {
            TextReader textReader = new TextReader(pdfResource);
            List<Document> documents = textReader.read();

            // 分割文档为小块
            TextSplitter textSplitter = new TokenTextSplitter();
            List<Document> splitDocuments = textSplitter.split(documents);

            // 使用 text-embedding-v4 生成嵌入并存储
            vectorStore.accept(splitDocuments);
            System.out.println("文档已加载到向量存储中");
        };
    }

}