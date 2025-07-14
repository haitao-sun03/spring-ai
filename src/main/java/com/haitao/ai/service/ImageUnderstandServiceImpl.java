package com.haitao.ai.service;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.haitao.ai.constants.ImageConstant;
import com.haitao.ai.utils.ImageUtils;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class ImageUnderstandServiceImpl implements ImageUnderstandService {

    @Autowired
    private MultiModalConversation multiModalConversation;

    @Autowired
    private MultiModalConversationParam conversationParam;


    @Override
    public String processImage(MultipartFile image, String userQuery) {
        try {
            // 将图像文件转换为 Base64 或 URL（假设阿里云支持 URL 或 Base64 输入）
            String imageBase64 = ImageUtils.convertImageToBase64(image); // 实现图像转 Base64

            MultiModalMessage userMsg = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(Arrays.asList(Collections.singletonMap("image", ImageConstant.BASE64_PREFIX + imageBase64),
                            Collections.singletonMap("text", userQuery)))
                    .build();

            conversationParam.setMessages(Arrays.asList(userMsg));

            // 2. 调用 Qwen-VL-Max 进行图片理解
            Flowable<MultiModalConversationResult> results = multiModalConversation.streamCall(conversationParam);
            StringBuilder reasoningContent = new StringBuilder();
            StringBuilder finalContent = new StringBuilder();

            results.blockingForEach(message -> {
                handleGenerationResult(message,reasoningContent,finalContent);
            });

            // 3. 将图像描述存入向量数据库（可选，增强 RAG）
//            float[] embedding = embeddingModel.embed(imageDescription).getEmbedding();
//            vectorStore.add(List.of(new org.springframework.ai.document.Document(imageDescription, embedding)));

            // 4. 返回图像描述（可用于后续 RAG 查询）
            return finalContent.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to process image with qvq-max: " + e.getMessage(), e);
        }
    }


    private void handleGenerationResult(MultiModalConversationResult message,
                                               StringBuilder reasoningContent,
                                               StringBuilder finalContent) {
        String re = message.getOutput().getChoices().get(0).getMessage().getReasoningContent();
        String reasoning = Objects.isNull(re) ? "" : re; // 默认值

        List<Map<String, Object>> content = message.getOutput().getChoices().get(0).getMessage().getContent();
        if (!reasoning.isEmpty()) {
            reasoningContent.append(reasoning);
        }

        if (Objects.nonNull(content) && !content.isEmpty()) {
            Object text = content.get(0).get("text");
            finalContent.append(text);
        }
    }
}
