package com.codebase.dto;

import com.codebase.model.CodeDocument;
import com.codebase.model.MethodNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class CodeParseResult {
    // 用于存入 Elasticsearch 的文档
    private List<CodeDocument> documents = new ArrayList<>();

    // 用于存入 Neo4j 的节点
    private Set<MethodNode> methodNodes = new HashSet<>();
    // 可以在此添加 ClassNode, InterfaceNode 等

    // 用于描述调用关系，方便后续在 Neo4j 中建立关系
    private Set<CallRelationship> callRelationships = new HashSet<>();

    @Data
    public static class CallRelationship {
        private final String callerFqn; // 调用方方法的完全限定名
        private final String calleeFqn; // 被调用方方法的完全限定名
    }
}
