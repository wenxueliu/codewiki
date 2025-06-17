package com.codebase.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VectorIndexRepository {

    private final Neo4jClient neo4jClient;
    private static final String INDEX_NAME = "codebase_embeddings";

    /**
     * 检查并创建向量索引（如果不存在）。
     * 应该在应用启动时调用一次。
     * @param dimensions 向量维度
     * @param similarityFunction 相似度函数 ('cosine' 或 'euclidean')
     */
    public void createVectorIndexIfNotExists(int dimensions, String similarityFunction) {
        String checkIndexQuery = "SHOW INDEXES YIELD name WHERE name = $indexName RETURN count(*) > 0";

        boolean indexExists = neo4jClient.query(checkIndexQuery)
                .bind(INDEX_NAME).to("indexName")
                .fetchAs(Boolean.class)
                .one()
                .orElse(false);

        if (!indexExists) {
            String createIndexQuery = String.format(
                    "CREATE VECTOR INDEX %s " +
                            "FOR (n:Class|Method|Interface) ON (n.embedding) " +
                            "OPTIONS {indexProvider: 'vector-2.0', dimensions: %d, similarityFunction: '%s'}",
                    INDEX_NAME, dimensions, similarityFunction
            );
            neo4jClient.query(createIndexQuery).run();
            // log.info("Created Neo4j vector index: {}", INDEX_NAME);
        } else {
            // log.info("Neo4j vector index '{}' already exists.", INDEX_NAME);
        }
    }
}
