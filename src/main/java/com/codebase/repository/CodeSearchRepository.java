package com.codebase.repository;

import com.codebase.model.CodeDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeSearchRepository extends ElasticsearchRepository<CodeDocument, String> {

    // Spring Data Elasticsearch 会自动根据方法名生成查询
    // 这里会进行多字段匹配
    List<CodeDocument> findByNameOrAliasOrComments(String name, String alias, String comments);

    // ... 其他文本搜索查询
}