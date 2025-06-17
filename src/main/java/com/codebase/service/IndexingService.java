package com.codebase.service;

import com.codebase.dto.ChangedFile;

import java.nio.file.Path;
import java.util.List;

public interface IndexingService {
    /**
     * 对整个代码库进行全量索引。
     *
     * @param projectRoot  项目根目录
     * @param repositoryId 仓库ID
     */
    void fullIndexing(Path projectRoot, String repositoryId);

    /**
     * 根据 Git 的变更进行增量索引。
     *
     * @param repositoryId 仓库ID
     * @param projectPath  项目根目录
     * @param changedFiles 修改的文件列表
     */
    void startIncrementalIndexing(String repositoryId, Path projectPath, List<ChangedFile> changedFiles);
}
