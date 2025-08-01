package com.haitao.ai.advisor;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RetrievalAugmentationAdvisorConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.dashscope.knowledge-index}")
    private String dashScopeKnowledgeIndex;

    /**
     * spring-ai-alibaba的DashScopeDocumentRetriever实现了DocumentRetriever接口
     * 用于从阿里百炼灵基知识库进行切片查询
     * 使用RetrievalAugmentationAdvisor能够自定义documentRetriever，从而从外部知识库进行切片搜索
     */
    @Bean
    public Advisor ragDashScopeCloudAdvisor() {
        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(apiKey).build();
        DocumentRetriever retriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName(dashScopeKnowledgeIndex)
                        .withDenseSimilarityTopK(3)
                        .build());

        return RetrievalAugmentationAdvisor
                .builder()
                .documentRetriever(retriever)
                .build();
    }
}
