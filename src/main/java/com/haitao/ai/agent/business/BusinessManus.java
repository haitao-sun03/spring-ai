package com.haitao.ai.agent.business;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.haitao.ai.agent.ToolCallingAgent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BusinessManus extends ToolCallingAgent {

    private final String SYSTEM_PROMPT = """
                You are BusinessManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                After using each tool, clearly explain the execution results and suggest the next steps.
                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                """;


    public BusinessManus(ToolCallback[] allTools, DashScopeChatModel model) {
        super(allTools);
        setName("BusinessManus");
        setSystemPrompt(SYSTEM_PROMPT);
        setMaxStep(15);
        ChatClient chatClient = ChatClient
                .builder(model)
                .defaultAdvisors(new SimpleLoggerAdvisor(5))
                .build();
        setChatClient(chatClient);
    }
}
