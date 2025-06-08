package com.codebase.service.impl;

import com.codebase.entity.NluResult;
import com.codebase.entity.Entity;
import com.codebase.service.NluService;
import org.springframework.stereotype.Service;

import java.util.List;

// 这是一个伪代码实现，你需要集成具体的LLM SDK
@Service
public class LlmNluServiceImpl implements NluService {
    @Override
    public NluResult understand(String query) {
        // 1. 构建 Prompt (如之前讨论的)
        String prompt = "System: ... \nUser: " + query;

        // 2. 调用 LLM API
        // String llmResponseJson = llmApiClient.complete(prompt);

        // 3. 解析 JSON 响应到 NluResult 对象
        // ObjectMapper mapper = new ObjectMapper();
        // NluResult result = mapper.readValue(llmResponseJson, NluResult.class);

        // --- MOCK IMPLEMENTATION FOR TESTING ---
        NluResult mockResult = new NluResult();
        if (query.contains("调用链")) {
            mockResult.setIntent("FIND_CALL_CHAIN");
        } else if (query.contains("被谁调用") || query.contains("哪里用")) {
            mockResult.setIntent("FIND_USAGES");
        } else {
            mockResult.setIntent("FIND_CODE");
        }
        Entity entity = new Entity();
        entity.setName(extractEntity(query)); // 简单的实体提取
        entity.setType("Method"); // 假设是方法
        mockResult.setEntities(List.of(entity));
        mockResult.setOriginalQuery(query);
        return mockResult;
    }

    // 简陋的实体提取，实际应由LLM完成
    private String extractEntity(String query) {
        if (query.contains("updateUser"))
            return "updateUser";
        if (query.contains("UserService"))
            return "UserService";
        return "unknown";
    }
}