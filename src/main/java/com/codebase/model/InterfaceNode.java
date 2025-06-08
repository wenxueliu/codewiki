package com.codebase.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Node("Interface")
public class InterfaceNode extends AbstractSyntaxNode {

    @Relationship(type = "HAS_METHOD", direction = Relationship.Direction.OUTGOING)
    private Set<MethodNode> methods;

    @Relationship(type = "EXTENDS", direction = Relationship.Direction.OUTGOING)
    private Set<InterfaceNode> extendedInterfaces; // 接口可以多继承
}
