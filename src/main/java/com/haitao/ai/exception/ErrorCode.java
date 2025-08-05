package com.haitao.ai.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_WORDS_ERROR(40300, "提示词中存在敏感话题，请重试"),
    AGENT_NOT_IDLE(400500,"智能体目前状态为非空闲，请稍后重试"),
    AGENT_USER_PROMPT_NOT_ALLOW_BLANK(400600,"用户提示词不能为空"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败");


    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
