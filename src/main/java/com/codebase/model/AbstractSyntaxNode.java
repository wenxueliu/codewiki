package com.codebase.model;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;

@Data
public abstract class AbstractSyntaxNode {
    @Id
    private String qualifiedName;
    private String name;
    private String filePath;
    private int startLine;
    private int endLine;
}
