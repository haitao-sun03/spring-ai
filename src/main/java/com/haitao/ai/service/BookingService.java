package com.haitao.ai.service;

import org.springframework.ai.chat.model.ToolContext;

public interface BookingService {
    public String cancel(String no, ToolContext toolContext);
}
