package com.haitao.ai.controller;

import com.haitao.ai.advisor.ContextualQueryAugmenterFactory;
import com.haitao.ai.advisor.RagVectorStoreAdvisorFactory;
import com.haitao.ai.entity.ChatMessageEntity;
import com.haitao.ai.exception.BusinessException;
import com.haitao.ai.exception.ErrorCode;
import com.haitao.ai.model.ApiResponse;
import com.haitao.ai.model.ChatRecord;
import com.haitao.ai.repository.ChatMessageRepository;
import com.haitao.ai.service.ImageUnderstandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
@Slf4j
public class ChatController {

    private final ChatClient chatClient;

    private final ChatMemory chatMemory;

    private final ImageUnderstandService imageUnderstandService;

    private final ChatMessageRepository chatMessageRepository;

    private final VectorStore vectorStore;

    /**
     * 使用spring ai结构化输出，使用的LLM大模型需要能够支持理解提示词
     * spring ai的结构化输出的StructedOutputConverter：
     * 通过FormatProvider将结构化的指令拼接到提示词中
     * 通过Converter<String,T>将结果结构化为对应格式
     *
     * @param sessionId
     * @param input
     * @param image
     * @return
     */
    @PostMapping(value = "/chat", produces = "application/json;charset=utf-8")
    public ApiResponse<ChatRecord> chat(@RequestParam("sessionId") String sessionId,
                                        @RequestParam("input") String input,
                                        @RequestParam("status") String status,
                                        @RequestParam(value = "image", required = false) MultipartFile image) {
        if (image != null) {
            // 1. 使用 Qwen-VL-Max 处理图像并生成描述
            String imageDescription = imageUnderstandService.processImage(image, input);
            log.info("chat image description: {}", imageDescription);
            // 2. 将图像描述作为用户输入，结合 RAG 流程
            ChatRecord result = chatClient.prompt()
                    .user("图像描述：" + imageDescription + "\n用户查询：" + input)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                    .call()
                    .entity(ChatRecord.class);
            return ApiResponse.success(result);
        } else {
            ChatRecord result = chatClient.prompt()
                    .user(input)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                    .advisors(RagVectorStoreAdvisorFactory.create(vectorStore, ContextualQueryAugmenterFactory.create(false), status))
                    .call()
                    .entity(ChatRecord.class);
            return ApiResponse.success(result);
        }
    }


    @GetMapping(value = "/getChat", produces = "application/json;charset=utf-8")
    public ApiResponse<List<ChatMessageEntity>> getChat(@RequestParam("sessionId") String sessionId) {
        List<ChatMessageEntity> chatMemorys = chatMessageRepository.findByConversationId(sessionId);
        return ApiResponse.success(chatMemorys);
    }

    @DeleteMapping(value = "/deleteChat", produces = "application/json;charset=utf-8")
    public ApiResponse<String> deleteChat(@RequestParam("sessionId") String sessionId) {
        chatMessageRepository.deleteByConversationId(sessionId);
        return ApiResponse.success("Conversation deleted successfully");
    }
}
