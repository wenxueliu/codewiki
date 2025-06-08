package com.codebase.dto;

import com.codebase.model.ClassNode;
import com.codebase.model.CodeDocument;
import com.codebase.model.InterfaceNode;
import com.codebase.model.MethodNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import lombok.Value; // 使用 @Value 创建不可变的关系对象

@Data
public class CodeParseResult {
    private List<CodeDocument> documents = new ArrayList<>();

    // 节点
    private Set<ClassNode> classNodes = new HashSet<>();
    private Set<InterfaceNode> interfaceNodes = new HashSet<>();
    private Set<MethodNode> methodNodes = new HashSet<>();

    // 关系 (使用不可变对象更安全)
    @Value
    public static class CallRelationship {
        String callerFqn;
        String calleeFqn;
    }

    @Value
    public static class HasMethodRelationship {
        String ownerFqn;
        String methodFqn;
    }

    @Value
    public static class ExtendsRelationship {
        String childFqn;
        String parentFqn;
    }

    @Value
    public static class ImplementsRelationship {
        String classFqn;
        String interfaceFqn;
    }

    private Set<CallRelationship> callRelationships = new HashSet<>();
    private Set<HasMethodRelationship> hasMethodRelationships = new HashSet<>();
    private Set<ExtendsRelationship> extendsRelationships = new HashSet<>();
    private Set<ImplementsRelationship> implementsRelationships = new HashSet<>();
}
