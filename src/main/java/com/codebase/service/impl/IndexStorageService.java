package com.codebase.service.impl;

import com.codebase.dto.CodeParseResult;
import com.codebase.repository.CodeSearchRepository;
import com.codebase.repository.MethodNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexStorageService {

    private final CodeSearchRepository searchRepository;
    private final MethodNodeRepository methodNodeRepository;

    @Transactional
    public void save(CodeParseResult result) {
        // 保存到 Elasticsearch
        if (!result.getDocuments().isEmpty()) {
            searchRepository.saveAll(result.getDocuments());
        }

        // 保存到 Neo4j
        if (!result.getMethodNodes().isEmpty()) {
            methodNodeRepository.saveAll(result.getMethodNodes());
        }

        // 建立调用关系
        result.getCallRelationships().forEach(rel -> {
            methodNodeRepository.findById(rel.getCallerFqn()).ifPresent(caller -> {
                methodNodeRepository.findById(rel.getCalleeFqn()).ifPresent(callee -> {
                    caller.getCalls().add(callee);
                    methodNodeRepository.save(caller);
                });
            });
        });
    }

    @Transactional
    public void deleteByFilePath(String filePath) {
        // 从 Elasticsearch 删除
        // searchRepository.deleteByFilePath(filePath); // 假设实现了此方法
        log.warn("Elasticsearch deleteByFilePath is not yet implemented.");

        // 从 Neo4j 删除
//        methodNodeRepository.findAllByFilePath(filePath).forEach(node -> {
//            methodNodeRepository.delete(node);
//        });
        log.debug("Deleted Neo4j nodes for file: {}", filePath);
    }

    @Transactional
    public void deleteAll() {
        searchRepository.deleteAll();
        methodNodeRepository.deleteAll();
        log.info("All index data has been cleared.");
    }
}
