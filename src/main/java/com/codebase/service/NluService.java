package com.codebase.service;

import com.codebase.entity.NluResult;

public interface NluService {
    /**
     * 解析自然语言查询。
     *
     * @param query 用户输入的字符串
     * @return 结构化的 NLU 结果
     */
    NluResult understand(String query);
}