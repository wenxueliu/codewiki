package com.codebase.service.impl;

import com.codebase.dto.CodeParseResult;
import com.codebase.model.CodeDocument;
import com.codebase.model.MethodNode;
import com.codebase.service.CodeParserService;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;

@Slf4j
@Service
public class JavaParserServiceImpl implements CodeParserService {

    @Override
    public CodeParseResult parseFile(File javaFile, Path projectRoot) {
        // --- 1. 配置 Symbol Solver ---
        // 关键：为每次解析提供完整的项目上下文
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver()); // 解析 JDK 类
        typeSolver.add(new JavaParserTypeSolver(projectRoot)); // 解析项目源码
        // 如果有 .jar 依赖，还需添加 JarTypeSolver

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        CodeParseResult result = new CodeParseResult();

        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);

            // --- 2. 遍历 AST，提取方法和调用关系 ---
            cu.findAll(MethodDeclaration.class).forEach(md -> {
                try {
                    ResolvedMethodDeclaration resolvedMd = md.resolve();
                    String methodFqn = resolvedMd.getQualifiedSignature();

                    // a. 创建 MethodNode 和 CodeDocument
                    MethodNode methodNode = createMethodNode(resolvedMd, javaFile.toPath());
                    result.getMethodNodes().add(methodNode);

                    CodeDocument codeDocument = createCodeDocument(resolvedMd, md, javaFile.toPath());
                    result.getDocuments().add(codeDocument);

                    // b. 查找此方法内部的所有方法调用
                    md.findAll(MethodCallExpr.class).forEach(mce -> {
                        try {
                            ResolvedMethodDeclaration callee = mce.resolve();
                            String calleeFqn = callee.getQualifiedSignature();

                            // c. 创建调用关系
                            result.getCallRelationships().add(new CodeParseResult.CallRelationship(methodFqn, calleeFqn));
                        } catch (Exception e) {
                            log.warn("Could not resolve method call '{}' in {}", mce.getNameAsString(), methodFqn);
                        }
                    });
                } catch (Exception e) {
                    log.warn("Could not resolve method declaration '{}' in file {}", md.getNameAsString(), javaFile.getName());
                }
            });

        } catch (Exception e) {
            log.error("Failed to parse file: {}", javaFile.getAbsolutePath(), e);
        }

        return result;
    }

    private MethodNode createMethodNode(ResolvedMethodDeclaration resolvedMd, Path filePath) {
        MethodNode node = new MethodNode();
        node.setQualifiedName(resolvedMd.getQualifiedSignature());
        node.setName(resolvedMd.getName());
        node.setFilePath(filePath.toString());
        // 实际行号需要从 AST 节点获取
        // node.setStartLine(resolvedMd. ...);
        return node;
    }

    private CodeDocument createCodeDocument(ResolvedMethodDeclaration resolvedMd, MethodDeclaration md, Path filePath) {
        CodeDocument doc = new CodeDocument();
        doc.setQualifiedName(resolvedMd.getQualifiedSignature());
        doc.setName(resolvedMd.getName());
        doc.setType("Method");
        doc.setFilePath(filePath.toString());
        md.getRange().ifPresent(range -> {
            doc.setStartLine(range.begin.line);
            doc.setEndLine(range.end.line);
        });
        md.getComment().ifPresent(comment -> doc.setComments(comment.getContent()));

        // TODO: 在这里调用一致性分析和向量化服务
        doc.setCommentConfidence(calculateCommentConfidence(doc.getComments(), md.toString()));
        doc.setAlias(Collections.emptyList()); // TODO: 从注释或配置中提取别名
        // doc.setCodeVector(...)

        return doc;
    }

    // 伪代码：计算注释可信度
    private double calculateCommentConfidence(String comment, String code) {
        if (comment == null || comment.isBlank()) {
            return 0.0; // 没有注释，可信度为0
        }
        // TODO: 实现更复杂的语义相似度计算
        return 0.85; // 假设默认为0.85
    }
}
