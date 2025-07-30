package com.haitao.ai.advisor;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

public class RagVectorStoreAdvisorFactory {

    public static Advisor create(VectorStore vectorStore, QueryAugmenter augmenter, String status) {
        Filter.Expression filterExpression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();

        DocumentRetriever retriever = VectorStoreDocumentRetriever
                .builder()
                .vectorStore(vectorStore)
                .filterExpression(filterExpression)
                .similarityThreshold(0.5)
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
