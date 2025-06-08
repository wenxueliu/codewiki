package com.codebase.model;

import java.util.Set;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Data
@Node("Method")
public class MethodNode {
    @Id
    private String qualifiedName;

    private String name;

    private String filePath;

    private int startLine;

    @Relationship(type = "CALLS", direction = Relationship.Direction.OUTGOING)
    private Set<MethodNode> calls;

    @Relationship(type = "CALLED_BY", direction = Relationship.Direction.INCOMING)
    private Set<MethodNode> calledBy;
}
