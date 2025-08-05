package com.haitao.ai.agent;

import cn.hutool.core.util.StrUtil;
import com.haitao.ai.agent.enums.AgentStatusEnum;
import com.haitao.ai.exception.BusinessException;
import com.haitao.ai.exception.ErrorCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public abstract class BaseAgent {

    private String name;

    private String systemPrompt;

    private Integer currentStep = 1;
    private Integer maxStep = 10;

    private AgentStatusEnum status = AgentStatusEnum.IDEL;

    private ChatClient chatClient;

    private List<Message> messages = new ArrayList<>();


    public String run(String userPrompt) {
        if (status != AgentStatusEnum.IDEL) {
            throw new BusinessException(ErrorCode.AGENT_NOT_IDLE);
        }

        if (StrUtil.isBlank(userPrompt)) {
            throw new BusinessException(ErrorCode.AGENT_USER_PROMPT_NOT_ALLOW_BLANK);
        }
        messages.add(new UserMessage(userPrompt));

        List<String> results = new ArrayList<>();
        try {
            while (status != AgentStatusEnum.FINISHED && currentStep <= maxStep) {
                currentStep++;
                String res = step();
                log.info("第{}/{}步，该步结果为: {}", currentStep, maxStep, res);
                results.add(res);
            }

            if (currentStep >= maxStep) {
                status = AgentStatusEnum.FINISHED;
                results.add("已达到maxStep：" + maxStep + ", agent stop");
            }
//            重置为IDLE
            status = AgentStatusEnum.IDEL;
            return String.join("\n", results);
        } catch (Exception e) {
            log.error("run fail,the exception is: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }


    public abstract String step();


}
