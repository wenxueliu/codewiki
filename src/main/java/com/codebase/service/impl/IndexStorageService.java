package com.codebase.service.impl;

import com.codebase.dto.CodeParseResult;
import com.codebase.repository.ClassNodeRepository;
import com.codebase.repository.InterfaceNodeRepository;
import com.codebase.repository.MethodNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexStorageService {

    // private final CodeSearchRepository searchRepository;
    private final MethodNodeRepository methodNodeRepository;
    private final ClassNodeRepository classNodeRepository;
    private final InterfaceNodeRepository interfaceNodeRepository;

    @Transactional
    public void save(CodeParseResult result, String repositoryId) {
        // 1. 设置 repositoryId
        result.getClassNodes().forEach(n -> n.setRepositoryId(repositoryId));
        result.getInterfaceNodes().forEach(n -> n.setRepositoryId(repositoryId));
        result.getMethodNodes().forEach(n -> n.setRepositoryId(repositoryId));
        result.getDocuments().forEach(d -> d.setRepositoryId(repositoryId));

        // 2. 保存节点
        if (!result.getClassNodes().isEmpty()) {
            classNodeRepository.saveAll(result.getClassNodes());
        }
        if (!result.getInterfaceNodes().isEmpty()) {
            interfaceNodeRepository.saveAll(result.getInterfaceNodes());
        }
        if (!result.getMethodNodes().isEmpty()) {
            methodNodeRepository.saveAll(result.getMethodNodes());
        }
        // 保存到 Elasticsearch
        if (!result.getDocuments().isEmpty()) {
            // searchRepository.saveAll(result.getDocuments());
        }

        // 3. 创建关系
        // 3.1 类与方法的关系
        result.getHasMethodRelationships()
                .forEach(rel -> classNodeRepository.createHasMethodRelationship(rel.getOwnerFqn(), rel.getMethodFqn()));

        // 3.2 类的继承关系
        result.getExtendsRelationships()
                .forEach(rel -> classNodeRepository.createExtendsRelationship(rel.getChildFqn(), rel.getParentFqn()));

        // 3.3 类实现接口的关系
        result.getImplementsRelationships().forEach(
                rel -> classNodeRepository.createImplementsRelationship(rel.getClassFqn(), rel.getInterfaceFqn()));

        // 3.4 方法调用关系
        result.getCallRelationships()
                .forEach(rel -> methodNodeRepository.createCallsRelationship(rel.getCallerFqn(), rel.getCalleeFqn()));

        log.debug("Saved all nodes and relationships for repository: {}", repositoryId);
    }

    @Transactional
    public void deleteByFilePath(String filePath) {
        // 从 Elasticsearch 删除
        log.warn("Elasticsearch deleteByFilePath is not yet implemented.");
        // searchRepository.deleteByFilePath(filePath);

        // 从 Neo4j 删除
        methodNodeRepository.findAllByFilePath(filePath).forEach(methodNodeRepository::delete);
        classNodeRepository.findAllByFilePath(filePath).forEach(classNodeRepository::delete);
        interfaceNodeRepository.findAllByFilePath(filePath).forEach(interfaceNodeRepository::delete);
        log.debug("Deleted Neo4j nodes for file: {}", filePath);
    }

    @Transactional
    public void deleteByRepositoryId(String repositoryId) {
        // 从 Elasticsearch 删除
        // searchRepository.deleteByRepositoryId(repositoryId);

        // 从 Neo4j 删除
        classNodeRepository.deleteByRepositoryId(repositoryId);
        methodNodeRepository.deleteByRepositoryId(repositoryId);
        interfaceNodeRepository.deleteByRepositoryId(repositoryId);
        log.info("All index data for repository '{}' has been cleared.", repositoryId);
    }

    @Transactional
    public void deleteAll() {
        // searchRepository.deleteAll();
        classNodeRepository.deleteAll();
        interfaceNodeRepository.deleteAll();
        methodNodeRepository.deleteAll();
        log.info("All index data has been cleared.");
    }
}
