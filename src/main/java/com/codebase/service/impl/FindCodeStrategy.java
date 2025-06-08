package com.codebase.service.impl;

import com.codebase.dto.ApiResponse;
import com.codebase.entity.NluResult;
import com.codebase.model.CodeDocument;
import com.codebase.repository.CodeSearchRepository;
import com.codebase.service.QueryStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FindCodeStrategy implements QueryStrategy {

    private final CodeSearchRepository searchRepository;

    @Override
    public String getIntent() {
        return "FIND_CODE";
    }

    @Override
    public ApiResponse<?> execute(NluResult nluResult) {
        String queryText = nluResult.getOriginalQuery();

        // 此处应使用更复杂的ES查询，这里用findAll作为示例
        // 实际应查询 name, alias, comments 等字段
        List<CodeDocument> documents = (List<CodeDocument>) searchRepository.findAll();

        return ApiResponse.builder()
                .intent(getIntent())
                .result(documents)
                .build();
    }
}
