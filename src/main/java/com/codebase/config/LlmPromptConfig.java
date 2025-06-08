package com.codebase.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "llm.nlu.prompt")
public class LlmPromptConfig {
    // 将 System 和 User 的模板分开，更符合 Spring AI 1.x 的设计
    private String systemMessage;
    private String userMessageTemplate;
}
