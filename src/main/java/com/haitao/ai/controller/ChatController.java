package com.haitao.ai.controller;

import com.haitao.ai.service.ImageUnderstandService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class ChatController {

    private final ChatClient chatClient;

    private final ChatMemory chatMemory;

    private final ImageUnderstandService imageUnderstandService;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public String chat(String sessionId, String input) {
        return chatClient.prompt()
                .user(input)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();
    }

    @PostMapping(value = "/image-chat", produces = "text/html;charset=utf-8")
    public String imageChat(@RequestParam("sessionId") String sessionId,
                            @RequestParam("input") String input,
                            @RequestParam("image") MultipartFile image) {
        // 1. 使用 Qwen-VL-Max 处理图像并生成描述
        String imageDescription = imageUnderstandService.processImage(image, input);
        System.out.println("===:" + imageDescription);

        // 2. 将图像描述作为用户输入，结合 RAG 流程
        return chatClient.prompt()
                .user("图像描述：" + imageDescription + "\n用户查询：" + input)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();
    }
}
