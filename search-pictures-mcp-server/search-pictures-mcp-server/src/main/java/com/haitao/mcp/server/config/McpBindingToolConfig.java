package com.haitao.mcp.server.config;

import com.haitao.mcp.server.tools.PictureSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpBindingToolConfig {

    @Bean
    public ToolCallbackProvider imageSearchTools(PictureSearchTool pictureSearchTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(pictureSearchTool)
                .build();
    }
}
