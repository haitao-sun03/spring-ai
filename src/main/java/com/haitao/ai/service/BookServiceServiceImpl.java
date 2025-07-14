package com.haitao.ai.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class BookServiceServiceImpl implements BookingService {

    @Tool(description = "退票/取消预定")
    @Override
    public String cancel(@ToolParam(description = "预定号") String no
            , @ToolParam(description = "姓名") String name) {
        return "已完成退订" + no + "<UNK>" + name;
    }
}
