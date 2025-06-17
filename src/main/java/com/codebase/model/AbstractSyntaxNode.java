package com.codebase.model;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;

@Data
public abstract class AbstractSyntaxNode {
    @Id
    private String globalFqn;
    private String name;
    private String filePath;
    private int startLine;
    private int endLine;

    @Property("embedding") // 明确指定属性名
    private float[] embedding;

    private String repositoryId;
}
