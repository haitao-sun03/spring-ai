package com.haitao.ai.model;

import com.haitao.ai.exception.ErrorCode;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private int code; // SUCCESS, ERROR
    private String message; // 描述信息
    private T data; // 实际数据
    private String timestamp; // 响应时间
    private String requestId; // 请求ID（可选）

    // 构造函数
    public ApiResponse(int  code, String message, T data, String timestamp, String requestId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
        this.requestId = requestId;
    }

    // 静态工厂方法，方便构造成功响应
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data,
            java.time.ZonedDateTime.now().toString(), generateRequestId());
    }

    // 静态工厂方法，构造错误响应
    public static <T> ApiResponse<T> error(int code,String message) {
        return new ApiResponse<>(code, message, null,
            java.time.ZonedDateTime.now().toString(), generateRequestId());
    }

    private static String generateRequestId() {
        return java.util.UUID.randomUUID().toString(); // 生成唯一请求ID
    }
}