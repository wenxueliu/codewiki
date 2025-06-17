package com.codebase.service.impl;

import com.codebase.service.ContextHintService;
import org.springframework.stereotype.Service;

@Service
public class ContextHintServiceImpl implements ContextHintService {
    @Override
    public String getHints(String query) {
        return query;
    }
}
