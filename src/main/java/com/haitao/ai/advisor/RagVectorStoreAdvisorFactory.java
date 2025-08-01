package com.haitao.ai.advisor;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

import java.util.Map;

public class RagVectorStoreAdvisorFactory {

    public static Advisor create(VectorStore vectorStore, QueryAugmenter augmenter, Map<String, String> metadata) {

        Filter.Expression filterExpression = null;

        // 动态构建 metadata 的精确匹配条件
        if (metadata != null && !metadata.isEmpty()) {
            FilterExpressionBuilder builder = new FilterExpressionBuilder();
            FilterExpressionBuilder.Op current = null;

            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                if (current == null) {
                    current = builder.eq(entry.getKey(), entry.getValue());
                } else {
                    current = builder.and(current, builder.eq(entry.getKey(), entry.getValue()));
                }
            }
            filterExpression = current.build();
        }

        DocumentRetriever retriever = VectorStoreDocumentRetriever
                .builder()
                .vectorStore(vectorStore)
                .filterExpression(filterExpression)
                .similarityThreshold(0.1)
                .topK(3)
                .build();

//        检索增强顾问，可以更加灵活的进行检索，封装documentRetriever，queryAugmenter等
        return RetrievalAugmentationAdvisor
                .builder()
                .documentRetriever(retriever)
                .queryAugmenter(augmenter)
                .build();
    }
}
