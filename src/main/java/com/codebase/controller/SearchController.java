package com.codebase.controller;

import com.codebase.dto.ApiResponse;
import com.codebase.entity.NluResult;
import com.codebase.service.NluService;
import com.codebase.service.QueryExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SearchController {

    private final NluService nluService;
    private final QueryExecutor queryExecutor;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> search(@RequestParam("q") String query) {
        // 1. Understand user's intent
        NluResult nluResult = nluService.understand(query);

        // 2. Execute query based on intent
        ApiResponse<?> response = queryExecutor.executeQuery(nluResult);

        return ResponseEntity.ok(response);
    }
}
