package com.codebase.repository;

import com.codebase.model.CodeDocument;

import java.util.List;

//@Repository
public interface CodeSearchRepository {

    // Spring Data Elasticsearch 会自动根据方法名生成查询
    // 这里会进行多字段匹配
    List<CodeDocument> findByNameOrAliasOrComments(String name, String alias, String comments);

    List<CodeDocument> findByRepositoryId(String repositoryId);

    void deleteByRepositoryId(String repositoryId);

    void deleteByFilePath(String filePath);

    // ... 其他文本搜索查询
}