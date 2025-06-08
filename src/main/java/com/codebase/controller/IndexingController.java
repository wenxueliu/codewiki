package com.codebase.controller;

import com.codebase.dto.ChangedFile;
import com.codebase.service.IndexingService;
import com.codebase.service.ProjectStateService;
import com.codebase.service.impl.GitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1/indexing")
@RequiredArgsConstructor
public class IndexingController {

    private final IndexingService indexingService;
    private final GitService gitService;
    private final ProjectStateService projectStateService;


    @PostMapping("/full")
    public ResponseEntity<String> startIndexing(@RequestParam("path") String projectPathStr) {
        Path projectPath = Paths.get(projectPathStr);
        // 全量索引
        indexingService.startIndexing(projectPath);
        // 索引完成后，更新 commit 状态
        try {
            gitService.getHeadCommitId(projectPath).ifPresent(commitId -> {
                projectStateService.setLastCommitId(projectPath.toString(), commitId);
                // 注意：全量索引的 IndexingService 实现现在不负责更新状态，所以在这里做
            });
        } catch (Exception e) {
            // log error
        }
        return ResponseEntity.accepted().body("Full indexing process started for path: " + projectPathStr);
    }

    @PostMapping("/incremental")
    public ResponseEntity<String> startIncrementalIndexing(@RequestParam("path") String projectPathStr) {
        Path projectPath = Paths.get(projectPathStr);
        // 异步执行
        new Thread(() -> {
            try {
                String newCommitId = gitService.getHeadCommitId(projectPath)
                        .orElseThrow(() -> new IllegalStateException("HEAD commit not found."));

                String oldCommitId = projectStateService.getLastCommitId(projectPath.toString()).orElse(null);

                if (newCommitId.equals(oldCommitId)) {
                    // log info "No new commits"
                    return;
                }

                // 1. 发现变更
                List<ChangedFile> changedFiles = gitService.getChangedFiles(projectPath, oldCommitId, newCommitId);

                if (changedFiles.isEmpty()) {
                    // log info "No relevant file changes"
                } else {
                    // 2. 调用服务处理变更
                    indexingService.startIncrementalIndexing(projectPath, changedFiles);
                }

                // 3. 更新状态
                projectStateService.setLastCommitId(projectPath.toString(), newCommitId);

            } catch (Exception e) {
                // log error
            }
        }).start();

        return ResponseEntity.accepted().body("Incremental indexing process started for path: " + projectPathStr);
    }
}
