package com.codebase.service.impl;

import com.codebase.config.LlmPromptConfig;
import com.codebase.entity.NluResult;
import com.codebase.service.ContextHintService;
import com.codebase.service.NluService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class LlmNluServiceImpl implements NluService {

    private final ChatClient chatClient;
    private final ContextHintService contextHintService; // 用于动态上下文

    // 在构造函数中初始化所有需要的组件
    public LlmNluServiceImpl(ChatClient.Builder chatClientBuilder, ContextHintService contextHintService, LlmPromptConfig promptConfig) {
        this.contextHintService = contextHintService;

        // 使用 BeanOutputParser 并让它从 NluResult.class 的注解中学习格式

        this.chatClient = chatClientBuilder
                .defaultSystem(promptConfig.getSystemMessage()) // 设置默认的系统消息
                .defaultUser(promptConfig.getUserMessageTemplate()) // 设置用户消息模板
                .build();
    }

    @Override
    public NluResult understand(String query) {
        log.debug("Understanding query: '{}'", query);
        try {
            // 1. 获取动态上下文提示
            String hints = contextHintService.getHints(query);

            // 2. 调用 ChatClient
            NluResult nluResult = chatClient.prompt()
                    .user(p -> p.param("context_hints", hints) // 填充模板中的占位符
                            .param("query", query))
                    .call()
                    .entity(NluResult.class);

            nluResult.setOriginalQuery(query);
            log.info("Successfully parsed NLU result for query '{}': {}", query, nluResult);
            return nluResult;

        } catch (Exception e) {
            log.error("Failed to process NLU with Spring AI for query: '{}'. Triggering fallback.", query, e);
            // 这里可以重新抛出一个特定异常，以确保 @Retryable 能捕获到
            // 或者直接在这里处理，然后返回 fallback
            return fallbackNlu(query);
        }
    }

    private NluResult fallbackNlu(String query) {
        log.warn("Falling back to rule-based NLU for query: {}", query);
        NluResult fallbackResult = new NluResult();
        fallbackResult.setOriginalQuery(query);
        if (query.contains("调用链")) {
            fallbackResult.setIntent("FIND_CALL_CHAIN");
        } else if (query.contains("被谁调用") || query.contains("哪里用")) {
            fallbackResult.setIntent("FIND_USAGES");
        } else {
            fallbackResult.setIntent("FIND_CODE");
        }
        fallbackResult.setEntities(Collections.emptyList());
        return fallbackResult;
    }
}