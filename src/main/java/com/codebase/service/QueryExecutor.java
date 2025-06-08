package com.codebase.service;

import com.codebase.dto.ApiResponse;
import com.codebase.entity.NluResult;
import com.codebase.service.QueryStrategy;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QueryExecutor {
    private final Map<String, QueryStrategy> strategyMap;

    // Spring会自动注入所有QueryStrategy的实现
    public QueryExecutor(List<QueryStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(QueryStrategy::getIntent, Function.identity()));
    }

    public ApiResponse<?> executeQuery(NluResult nluResult) {
        QueryStrategy strategy = strategyMap.get(nluResult.getIntent());
        if (strategy == null) {
            return ApiResponse.builder()
                    .intent("UNKNOWN_INTENT")
                    .message("Sorry, I don't understand what you mean.")
                    .build();
        }
        return strategy.execute(nluResult);
    }
}
