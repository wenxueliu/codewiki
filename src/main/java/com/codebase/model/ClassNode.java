package com.codebase.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Node("Class")
public class ClassNode extends AbstractSyntaxNode {

    @Relationship(type = "HAS_METHOD", direction = Relationship.Direction.OUTGOING)
    private Set<MethodNode> methods;

    @Relationship(type = "IMPLEMENTS", direction = Relationship.Direction.OUTGOING)
    private Set<InterfaceNode> implementedInterfaces;

    @Relationship(type = "EXTENDS", direction = Relationship.Direction.OUTGOING)
    private ClassNode extendedClass; // Java 单继承

    @Relationship(type = "DEPENDS_ON", direction = Relationship.Direction.OUTGOING)
    private Set<AbstractSyntaxNode> dependencies; // 可以依赖类、接口等
}