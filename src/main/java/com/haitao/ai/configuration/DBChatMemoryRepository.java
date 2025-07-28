package com.haitao.ai.configuration;

import com.haitao.ai.entity.ChatMessageEntity;
import com.haitao.ai.repository.ChatMessageRepository;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DBChatMemoryRepository implements ChatMemoryRepository {
    private final ChatMessageRepository chatMessageRepository;

    public DBChatMemoryRepository(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public List<String> findConversationIds() {
        return chatMessageRepository.findAll()
                .stream()
                .map(ChatMessageEntity::getConversationId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        return chatMessageRepository.findByConversationId(conversationId)
                .stream()
                .map(this::toMessage)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveAll(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");

        List<ChatMessageEntity> entities = messages.stream()
                .map(message -> toEntity(conversationId, message))
                .collect(Collectors.toList());
        chatMessageRepository.deleteByConversationId(conversationId);
        chatMessageRepository.saveAll(entities);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        chatMessageRepository.deleteByConversationId(conversationId);
    }

    private ChatMessageEntity toEntity(String conversationId, Message message) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setConversationId(conversationId);
        entity.setMessageType(message.getMessageType().getValue());
        entity.setContent(message.getText());
        entity.setCreatedAt(System.currentTimeMillis());
        return entity;
    }

    private Message toMessage(ChatMessageEntity entity) {
        String messageType = entity.getMessageType();
        String content = entity.getContent();

        return switch (MessageType.fromValue(messageType)) {
            case USER -> new UserMessage(content);
            case ASSISTANT -> new AssistantMessage(content);
            case SYSTEM -> new SystemMessage(content);
            default -> throw new IllegalArgumentException("Unsupported message type: " + messageType);
        };
    }
}