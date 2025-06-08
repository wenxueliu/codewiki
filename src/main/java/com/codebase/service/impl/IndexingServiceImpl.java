package com.codebase.service.impl;

import com.codebase.dto.ChangedFile;
import com.codebase.dto.CodeParseResult;
import com.codebase.service.CodeParserService;
import com.codebase.service.IndexingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final CodeParserService codeParserService;

    private final IndexStorageService indexStorageService;

    @Override
    @Transactional // 使用事务保证数据一致性
    public void startIndexing(Path projectRoot) {
        log.info("Starting full codebase indexing for project: {}", projectRoot);

        // 1. 清理旧数据 (可选，全量索引时通常需要)
        log.info("Clearing old index data...");
        indexStorageService.deleteAll();

        // 2. 遍历所有 .java 文件
        try (Stream<Path> paths = Files.walk(projectRoot)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                    .forEach(javaFile -> {
                        log.info("Parsing file: {}", javaFile);
                        processFile(javaFile, projectRoot);
                    });
        } catch (IOException e) {
            log.error("Error walking through project directory", e);
        }

        // 5. 在所有节点都入库后，建立关系
        // (这一步也可以在解析每个文件后立即执行，但批量处理性能更好)
        // 简单的示例是再遍历一次所有文件，或者维护一个全局的待处理关系列表
        log.info("Indexing finished.");
    }

    @Override
    public void startIncrementalIndexing(Path projectRoot, List<ChangedFile> changedFiles) {
        log.info("Starting incremental indexing for {} changed files.", changedFiles.size());

        for (ChangedFile file : changedFiles) {
            // 现在直接处理传入的变更列表
            processChange(file, projectRoot);
        }

        log.info("Incremental indexing finished.");
    }

    private void processChange(ChangedFile changedFile, Path projectRoot) {
        log.info("Processing change: {} on file {}", changedFile.getChangeType(), changedFile.getNewPath());

        // 先删除旧的，再处理新的。对于 RENAME，这正好是正确的操作。
        if (changedFile.getOldPath() != null && !changedFile.getOldPath().equals("/dev/null")) {
            // 注意：传入的 path 可能是相对路径，需要与 projectRoot 结合
            indexStorageService.deleteByFilePath(projectRoot.resolve(changedFile.getOldPath()).toString());
        }

        switch (changedFile.getChangeType()) {
            case ADD:
            case MODIFY:
            case RENAME:
                if (changedFile.getNewPath() != null && !changedFile.getNewPath().equals("/dev/null")) {
                    processFile(projectRoot.resolve(changedFile.getNewPath()), projectRoot);
                }
                break;
            case DELETE:
                // 删除操作已在前面完成
                log.info("File {} deleted.", changedFile.getOldPath());
                break;
            default:
                log.warn("Unhandled change type: {}", changedFile.getChangeType());
        }
    }

    private void processFile(Path filePath, Path projectRoot) {
        log.debug("Parsing file: {}", filePath);
        CodeParseResult result = codeParserService.parseFile(filePath.toFile(), projectRoot);
        if (result != null) {
            indexStorageService.save(result);
        }
    }
}