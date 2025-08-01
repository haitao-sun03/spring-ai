package com.haitao.ai.configuration;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.haitao.ai.advisor.ForbiddenWordsAdvisor;
import com.haitao.ai.advisor.ReReadingAdvisor;
import com.haitao.ai.service.BookingService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

@Configuration
public class CommonConfiguration {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.dashscope.vision.options.model}")
    private String visionModel;

    @Value("${spring.ai.client.default-system}")
    private String defaultSystem;
    @Autowired
    private DBChatMemoryRepository dbChatMemoryRepository;

    @Autowired
    private ForbiddenWordsAdvisor forbiddenWordsAdvisor;

    @Resource
    private Advisor ragDashScopeCloudAdvisor;


    //图片理解
    @Bean
    public MultiModalConversation multiModalConversation() {
        return new MultiModalConversation();
    }

    @Bean
    public MultiModalConversationParam multiModalConversationParam() {
        return MultiModalConversationParam.builder()
                .apiKey(apiKey)
                .model(visionModel)
                .incrementalOutput(true)
                .build();
    }


    //LLM对话记忆
//    @Bean
//    public ChatMemoryRepository chatMemoryRepository() {
//        return new InMemoryChatMemoryRepository();
//    }

    //LLM对话记忆
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory
                .builder()
                .chatMemoryRepository(dbChatMemoryRepository)
                .maxMessages(10)
                .build();

    }

    //embedding存储
    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(1536) // 需确认 text-embedding-v4 的嵌入维度
                .initializeSchema(true) // 初始化数据库表结构
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .removeExistingVectorStoreTable(false) // 是否删除已存在的表
                .schemaName("public") // 模式名
                .vectorTableName("airline_vector_store") // 自定义表名
                .build();
    }


    //对话客户端初始化
    @Bean
    public ChatClient chatClient(DashScopeChatModel model,
                                 BookingService bookingService,
                                 ChatMemory chatMemory,
                                 VectorStore vectorStore) {
        return ChatClient
                .builder(model)
                .defaultSystem(defaultSystem)
                .defaultAdvisors(new SimpleLoggerAdvisor(5),
                        new ReReadingAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
//                        new QuestionAnswerAdvisor(vectorStore),
//                        ragDashScopeCloudAdvisor,
                        forbiddenWordsAdvisor
                )
                .defaultTools(bookingService)
                .defaultToolContext(Map.of("userName","张三"))
                .build();
    }

}
