package com.haitao.ai.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private String status; // SUCCESS, ERROR
    private String message; // 描述信息
    private T data; // 实际数据
    private String timestamp; // 响应时间
    private String requestId; // 请求ID（可选）

    // 构造函数
    public ApiResponse(String status, String message, T data, String timestamp, String requestId) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
        this.requestId = requestId;
    }

    // 静态工厂方法，方便构造成功响应
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "Request processed successfully", data, 
            java.time.ZonedDateTime.now().toString(), generateRequestId());
    }

    // 静态工厂方法，构造错误响应
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", message, null, 
            java.time.ZonedDateTime.now().toString(), generateRequestId());
    }

    private static String generateRequestId() {
        return java.util.UUID.randomUUID().toString(); // 生成唯一请求ID
    }
}