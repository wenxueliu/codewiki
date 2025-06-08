package com.codebase.service;

import com.codebase.dto.ApiResponse;
import com.codebase.entity.NluResult;

public interface QueryStrategy {
    String getIntent();

    /**
     * 执行查询。
     *
     * @param nluResult NLU模块的解析结果
     * @return 标准化的查询结果对象，可以直接序列化为 JSON 返回给前端
     */
    ApiResponse<?> execute(NluResult nluResult);
}
