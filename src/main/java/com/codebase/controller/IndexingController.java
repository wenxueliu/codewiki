package com.codebase.controller;

import lombok.extern.slf4j.Slf4j;
import com.codebase.dto.ChangedFile;
import com.codebase.dto.FullIndexRequest;
import com.codebase.service.IndexingService;
import com.codebase.service.ProjectStateService;
import com.codebase.service.impl.GitService;
import com.codebase.service.impl.IndexStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1/indexing")
@RequiredArgsConstructor
@Slf4j
public class IndexingController {

    private final IndexingService indexingService;
    private final GitService gitService;
    private final ProjectStateService projectStateService;
    private final IndexStorageService indexStorageService;

    @PostMapping("/full")
    public ResponseEntity<String> fullIndexing(@RequestBody FullIndexRequest fullIndexRequest) {
        Path projectPath = Paths.get(fullIndexRequest.getRepositoryPath());
        // 全量索引
        indexingService.fullIndexing(projectPath, fullIndexRequest.getRepositoryId());
        // 索引完成后，更新 commit 状态
        try {
            gitService.getHeadCommitId(projectPath).ifPresent(commitId -> {
                projectStateService.setLastCommitId(projectPath.toString(), commitId);
            });
        } catch (Exception e) {
            log.error("Error getting head commit id for project path: {}", projectPath, e);
        }
        return ResponseEntity.accepted().body("Full indexing process started for path: " + projectPath);
    }

    @PostMapping("/incremental")
    public ResponseEntity<String> incrementalIndexing(@RequestParam("repositoryId") String repositoryId,
            @RequestParam("path") String projectPathStr) {
        Path projectPath = Paths.get(projectPathStr);
        try {
            String newCommitId = gitService.getHeadCommitId(projectPath)
                    .orElseThrow(() -> new IllegalStateException("HEAD commit not found."));

            String oldCommitId = projectStateService.getLastCommitId(projectPath.toString()).orElse(null);

            if (newCommitId.equals(oldCommitId)) {
                // log info "No new commits"
                return ResponseEntity.accepted().body("no change, doesn't need index for " + projectPathStr);
            }

            // 1. 发现变更
            List<ChangedFile> changedFiles = gitService.getChangedFiles(projectPath, oldCommitId, newCommitId);

            if (changedFiles.isEmpty()) {
                // log info "No relevant file changes"
            } else {
                // 2. 调用服务处理变更
                indexingService.startIncrementalIndexing(repositoryId, Paths.get(projectPathStr), changedFiles);
            }

            // 3. 更新状态
            projectStateService.setLastCommitId(projectPath.toString(), newCommitId);

        } catch (Exception e) {
            log.error("Error getting head commit id for project path: {}", projectPath, e);
        }
        return ResponseEntity.accepted().body("Incremental indexing process started for path: " + projectPathStr);
    }

    /**
     * 删除指定仓库的所有索引数据
     *
     * @param repositoryId 仓库ID
     * @return 删除操作的响应
     */
    @DeleteMapping("/{repositoryId}")
    public ResponseEntity<String> deleteIndexing(@PathVariable("repositoryId") String repositoryId) {
        log.info("Deleting all index data for repository: {}", repositoryId);
        try {
            // 删除索引数据
            indexStorageService.deleteByRepositoryId(repositoryId);
            // 删除项目状态
            projectStateService.setLastCommitId(repositoryId, null);
            return ResponseEntity.ok().body("Successfully deleted all index data for repository: " + repositoryId);
        } catch (Exception e) {
            log.error("Error deleting index data for repository: {}", repositoryId, e);
            return ResponseEntity.internalServerError().body("Failed to delete index data: " + e.getMessage());
        }
    }
}
