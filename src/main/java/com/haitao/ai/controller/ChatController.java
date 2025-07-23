package com.haitao.ai.controller;

import com.haitao.ai.model.ApiResponse;
import com.haitao.ai.repository.ChatMessageRepository;
import com.haitao.ai.service.ImageUnderstandService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class ChatController {

    private final ChatClient chatClient;

    private final ChatMemory chatMemory;

    private final ImageUnderstandService imageUnderstandService;

    private final ChatMessageRepository chatMessageRepository;

    @PostMapping(value = "/chat", produces = "application/json;charset=utf-8")
    public ApiResponse<String> chat(@RequestParam("sessionId") String sessionId,
                                    @RequestParam("input") String input,
                                    @RequestParam(value = "image",required = false) MultipartFile image) {
        if (image != null) {
            // 1. 使用 Qwen-VL-Max 处理图像并生成描述
            String imageDescription = imageUnderstandService.processImage(image, input);
            System.out.println("===:" + imageDescription);
            // 2. 将图像描述作为用户输入，结合 RAG 流程
            String result = chatClient.prompt()
                    .user("图像描述：" + imageDescription + "\n用户查询：" + input)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                    .call()
                    .content();
            return ApiResponse.success(result);
        } else {
            String result = chatClient.prompt()
                    .user(input)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                    .call()
                    .content();
            return ApiResponse.success(result);
        }
    }

    @DeleteMapping(value = "/deleteChat", produces = "application/json;charset=utf-8")
    public ApiResponse<String> deleteChat(@RequestParam("sessionId") String sessionId) {
        try {
            chatMessageRepository.deleteByConversationId(sessionId);
            return ApiResponse.success("Conversation deleted successfully");
        } catch (Exception ex) {
            return ApiResponse.error("Failed to delete conversation: " + ex.getMessage());
        }
    }
}
