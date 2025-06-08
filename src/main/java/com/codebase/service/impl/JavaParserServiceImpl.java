package com.codebase.service.impl;

import com.codebase.dto.CodeParseResult;
import com.codebase.model.*;
import com.codebase.service.CodeParserService;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class JavaParserServiceImpl implements CodeParserService {

    @Override
    public CodeParseResult parseFile(File javaFile, Path projectRoot) {
        // ... Symbol Solver 配置不变 ...

        CodeParseResult result = new CodeParseResult();
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);

            // 遍历文件中的所有类和接口定义
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(typeDecl -> {
                try {
                    ResolvedReferenceTypeDeclaration resolvedType = typeDecl.resolve();

                    // --- 1. 创建 Class 或 Interface 节点 ---
                    if (typeDecl.isInterface()) {
                        InterfaceNode interfaceNode = createInterfaceNode(resolvedType, typeDecl, javaFile.toPath());
                        result.getInterfaceNodes().add(interfaceNode);
                    } else {
                        ClassNode classNode = createClassNode(resolvedType, typeDecl, javaFile.toPath());
                        result.getClassNodes().add(classNode);
                    }

                    // --- 2. 提取继承 (EXTENDS) 和实现 (IMPLEMENTS) 关系 ---
                    // 继承关系
                    resolvedType.getAncestors().stream()
                            .filter(ResolvedReferenceType::isJavaLangObject) // 直接父类
                            .findFirst()
                            .ifPresent(parentClass -> result.getExtendsRelationships().add(
                                    new CodeParseResult.ExtendsRelationship(resolvedType.getQualifiedName(), parentClass.getQualifiedName())
                            ));

                    // 实现关系
                    resolvedType.getAncestors().stream()
                            .filter(ResolvedReferenceType::isInferenceVariable) // 所有实现的接口
                            .forEach(parentInterface -> result.getImplementsRelationships().add(
                                    new CodeParseResult.ImplementsRelationship(resolvedType.getQualifiedName(), parentInterface.getQualifiedName())
                            ));

                    // --- 3. 提取方法 (Method) 和 HAS_METHOD, CALLS 关系 ---
                    typeDecl.findAll(MethodDeclaration.class).forEach(md -> {
                        try {
                            ResolvedMethodDeclaration resolvedMd = md.resolve();
                            String methodFqn = resolvedMd.getQualifiedSignature();

                            // 创建 Method 节点
                            result.getMethodNodes().add(createMethodNode(resolvedMd, md, javaFile.toPath()));
                            // 创建 Code Document (用于 ES)
                            result.getDocuments().add(createCodeDocument(resolvedMd, md, javaFile.toPath()));

                            // 创建 HAS_METHOD 关系
                            result.getHasMethodRelationships().add(
                                    new CodeParseResult.HasMethodRelationship(resolvedType.getQualifiedName(), methodFqn)
                            );

                            // 查找方法调用 (CALLS)
                            md.findAll(MethodCallExpr.class).forEach(mce -> {
                                try {
                                    result.getCallRelationships().add(new CodeParseResult.CallRelationship(
                                            methodFqn, mce.resolve().getQualifiedSignature()
                                    ));
                                } catch (Exception e) {
                                    log.trace("Could not resolve method call '{}' in {}", mce.getNameAsString(), methodFqn);
                                }
                            });
                        } catch (Exception e) {
                            log.warn("Could not resolve method '{}' in type '{}'.", md.getNameAsString(), resolvedType.getQualifiedName());
                        }
                    });

                } catch (Exception e) {
                    log.warn("Could not resolve type '{}' in file {}.", typeDecl.getNameAsString(), javaFile.getName());
                }
            });

        } catch (Exception e) {
            log.error("Failed to parse file: {}", javaFile.getAbsolutePath(), e);
        }

        return result;
    }

    // --- 辅助创建方法 ---

    private ClassNode createClassNode(ResolvedReferenceTypeDeclaration resolvedType, ClassOrInterfaceDeclaration typeDecl, Path filePath) {
        ClassNode node = new ClassNode();
        populateAbstractSyntaxNode(node, resolvedType.getQualifiedName(), typeDecl.getNameAsString(), filePath, typeDecl);
        return node;
    }

    private InterfaceNode createInterfaceNode(ResolvedReferenceTypeDeclaration resolvedType, ClassOrInterfaceDeclaration typeDecl, Path filePath) {
        InterfaceNode node = new InterfaceNode();
        populateAbstractSyntaxNode(node, resolvedType.getQualifiedName(), typeDecl.getNameAsString(), filePath, typeDecl);
        return node;
    }

    private MethodNode createMethodNode(ResolvedMethodDeclaration resolvedMd, MethodDeclaration md, Path filePath) {
        MethodNode node = new MethodNode();
        populateAbstractSyntaxNode(node, resolvedMd.getQualifiedSignature(), md.getNameAsString(), filePath, md);
        return node;
    }

    // 重构出的通用填充方法
    private void populateAbstractSyntaxNode(AbstractSyntaxNode node, String fqn, String name, Path filePath, com.github.javaparser.ast.Node astNode) {
        node.setQualifiedName(fqn);
        node.setName(name);
        node.setFilePath(filePath.toString());
        astNode.getRange().ifPresent(range -> {
            node.setStartLine(range.begin.line);
            node.setEndLine(range.end.line);
        });
    }

    private CodeDocument createCodeDocument(ResolvedMethodDeclaration resolvedMd, MethodDeclaration md, Path filePath) {
        CodeDocument doc = new CodeDocument();
        doc.setQualifiedName(resolvedMd.getQualifiedSignature());
        doc.setName(md.getNameAsString());
        doc.setType("Method");
        doc.setFilePath(filePath.toString());
        md.getRange().ifPresent(range -> {
            doc.setStartLine(range.begin.line);
            doc.setEndLine(range.end.line);
        });

        // 从 AST 节点 md 获取注释
        Optional<String> commentContent = md.getComment().map(comment -> comment.getContent());
        doc.setComments(commentContent.orElse(null));

        // 从 AST 节点 md 获取方法体源码
        String sourceCode = md.getBody().map(Object::toString).orElse("");

        // TODO: 在这里调用一致性分析和向量化服务
        // 传入注释和源码，计算可信度
        doc.setCommentConfidence(calculateCommentConfidence(commentContent.orElse(""), sourceCode));

        // TODO: 从注释或配置中提取别名
        doc.setAlias(extractAliases(commentContent.orElse("")));

        // TODO: 调用向量化服务
        // doc.setCodeVector(vectorize(md.getNameAsString() + " " + commentContent.orElse("")));
        // doc.setCodeVector(...)

        return doc;
    }

    // 伪代码：计算注释可信度
    private double calculateCommentConfidence(String comment, String code) {
        if (comment == null || comment.isBlank()) {
            return 0.0; // 没有注释，可信度为0
        }
        // 示例：一个简单的启发式规则，如果代码中包含 "TODO" 或 "FIXME"，降低可信度
        if (code.contains("TODO") || code.contains("FIXME")) {
            return 0.6;
        }
        // 实际应实现基于向量相似度的计算
        return 0.85; // 默认值
    }

    /**
     * [伪代码] 从注释中提取别名。
     * 例如，解析 "@alias 用户服务" 这样的标签。
     */
    private List<String> extractAliases(String comment) {
        // 实际应使用正则表达式匹配自定义标签
        if (comment.contains("@alias")) {
            // ... 解析逻辑 ...
        }
        return Collections.emptyList();
    }
}
