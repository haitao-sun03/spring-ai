package com.haitao.ai.controller;

import com.haitao.ai.agent.business.BusinessManus;
import com.haitao.ai.model.ApiResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class BusinessManusController {

    @Autowired
    private BusinessManus manus;

    /**
     * 使用超级智能体完成复杂任务，使用AgentLoop和ReAct实现
     *
     * @param input
     * @return
     */
    @SneakyThrows
    @GetMapping(value = "/chatWithManus", produces = "application/json;charset=utf-8")
    public ApiResponse<String> chatWithManus(@RequestParam("input") String input) {

        String result = manus.run(input);
        return ApiResponse.success(result);

    }
}
