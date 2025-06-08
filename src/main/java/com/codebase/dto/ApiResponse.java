package com.codebase.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {
    private String intent;
    private T result;
    private String message; // 用于需要用户交互的消息

    // 内部类，用于表示需要用户选择的选项
    @Data
    @Builder
    public static class DisambiguationOption {
        private String label;
        private String value;
    }
}