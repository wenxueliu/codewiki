package com.codebase.model;

import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"calls", "calledBy", "owner"}) // 避免循环引用导致栈溢出
@Node("Method")
public class MethodNode extends AbstractSyntaxNode {

    // 我们使用 `BELONGS_TO` 反向关系，在 Class/Interface 中定义 HAS_METHOD
    @Relationship(type = "HAS_METHOD", direction = Relationship.Direction.INCOMING)
    private AbstractSyntaxNode owner; // 方法属于哪个类或接口

    @Relationship(type = "CALLS", direction = Relationship.Direction.OUTGOING)
    private Set<MethodNode> calls;

    @Relationship(type = "CALLS", direction = Relationship.Direction.INCOMING)
    private Set<MethodNode> calledBy;
}
