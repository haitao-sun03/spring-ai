package com.haitao.ai.service;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class BookServiceServiceImpl implements BookingService {

    @Tool(description = "退票/取消预定")
    @Override
    public String cancel(@ToolParam(description = "预定号") String no,
                         ToolContext toolContext) {
        return "已完成退订" + no + "<UNK>" + toolContext.getContext().get("userName");
    }
}
