package com.codebase.service;


import com.codebase.dto.ChangedFile;

import java.nio.file.Path;
import java.util.List;

public interface IndexingService {
    /**
     * 对整个代码库进行全量索引。
     *
     * @param projectRoot 项目根目录
     */
    void fullIndexing(Path projectRoot);

    /**
     * 根据 Git 的变更进行增量索引。
     *
     * @param projectPath  项目根目录
     * @param changedFiles 修改的文件列表
     */
    void startIncrementalIndexing(Path projectPath, List<ChangedFile> changedFiles);
}
