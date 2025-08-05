package com.haitao.ai.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.haitao.ai.agent.enums.AgentStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallingAgent extends ReActAgent {

    private ToolCallback[] availableTools;

    private ChatOptions chatOptions;

    private ToolCallingManager toolCallingManager;

    private ChatResponse toolCallResponse;


    public ToolCallingAgent(ToolCallback[] availableTools) {
        this.availableTools = availableTools;
        this.chatOptions = DashScopeChatOptions
                .builder()
                .withInternalToolExecutionEnabled(false)
                .build();

        this.toolCallingManager = DefaultToolCallingManager
                .builder()
                .build();
    }

    @Override
    public boolean think() {
        List<Message> messages = getMessages();
        Prompt prompt = new Prompt(messages, chatOptions);
        ChatResponse chatResponse = getChatClient()
                .prompt(prompt)
                .system(getSystemPrompt())
                .toolCallbacks(availableTools)
                .call()
                .chatResponse();
        this.toolCallResponse = chatResponse;

        AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
        String messageText = assistantMessage.getText();
        List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
        log.info("思考结果：{}", messageText);
        log.info("思考选择的工具数量为：{}", toolCalls.size());
        String thinkResult = toolCalls
                .stream()
                .map(toolCall -> String.format("工具名称为：%s,调用参数为：%s", toolCall.name(), toolCall.arguments()))
                .collect(Collectors.joining("\n"));

        if (assistantMessage.hasToolCalls()) {
            return true;
        } else {
            messages.add(assistantMessage);
            return false;
        }
    }

    @Override
    public String act() {

        List<Message> messages = getMessages();
        Prompt prompt = new Prompt(messages, chatOptions);
//        使用toolCallingManager进行工具调用
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, this.toolCallResponse);
//        toolExecutionResult.conversationHistory()包含之前的消息，返回的助手消息，以及工具调用后的消息
        List<Message> allMessage = toolExecutionResult.conversationHistory();
        setMessages(allMessage);

        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(allMessage);
        String terminateMethodName = "doTerminate";
        boolean isTerminate = toolResponseMessage.getResponses()
                .stream()
                .anyMatch(toolResponse -> terminateMethodName.equals(toolResponse.name()));
        if(isTerminate) {
            setStatus(AgentStatusEnum.FINISHED);
        }
        String toolCallResult = toolResponseMessage.getResponses()
                .stream()
                .map(toolCallResponse -> String.format("工具 %s,调用结果为：%s", toolCallResponse.name(), toolCallResponse.responseData()))
                .collect(Collectors.joining("\n"));

        log.info(toolCallResult);
        return toolCallResult;
    }
}
