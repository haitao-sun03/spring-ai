package com.haitao.ai.advisor;

import com.haitao.ai.exception.ErrorCode;
import com.haitao.ai.utils.ThrowUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "forbidden")
@Component
@Setter
@Slf4j
public class ForbiddenWordsAdvisor implements BaseAdvisor {

    private String version;
    private List<String> words = new ArrayList<>();

    private int order = -10;

    @PostConstruct
    public void init() {
        log.info("ForbiddenWordsAdvisor initialized with version: {}, words: {}", version, words);
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        if (words == null || words.isEmpty()) {
            log.warn("No forbidden words configured, skipping check");
            return chatClientRequest.mutate().build();
        }
        String input = chatClientRequest.prompt().getUserMessage().getText();
        String find = words.stream()
            .filter(item -> input.contains(item))
            .findFirst()
            .orElse(null);
        ThrowUtils.throwIf(StringUtils.isNotBlank(find), ErrorCode.FORBIDDEN_WORDS_ERROR,
            "Input contains forbidden word: " + find);
        return chatClientRequest.mutate().build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public ForbiddenWordsAdvisor withOrder(int order) {
        this.order = order;
        return this;
    }
}