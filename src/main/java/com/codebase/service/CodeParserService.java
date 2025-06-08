package com.codebase.service;

import com.codebase.dto.CodeParseResult;

import java.io.File;
import java.nio.file.Path;

public interface CodeParserService {
    /**
     * 解析单个 Java 文件。
     *
     * @param javaFile    要解析的文件
     * @param projectRoot 项目根目录，用于符号解析的上下文
     * @return 解析结果，包含待入库的实体和关系
     */
    CodeParseResult parseFile(File javaFile, Path projectRoot);
}