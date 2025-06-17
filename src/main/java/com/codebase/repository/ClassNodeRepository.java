package com.codebase.repository;

import com.codebase.model.ClassNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassNodeRepository extends Neo4jRepository<ClassNode, String> {

        /**
         * 根据文件路径查找所有类节点。
         * 这对于增量更新中的删除操作至关重要。
         *
         * @param filePath 文件的路径
         * @return 匹配的 ClassNode 列表
         */
        List<ClassNode> findAllByFilePath(String filePath);

        /**
         * 创建类与方法之间的 HAS_METHOD 关系。
         * 使用 MERGE 可以确保关系的唯一性，避免重复创建。
         *
         * @param classFqn  类的完全限定名
         * @param methodFqn 方法的完全限定签名
         */
        @Query("MATCH (c:Class {qualifiedName: $classFqn}), (m:Method {qualifiedName: $methodFqn}) " +
                        "MERGE (c)-[:HAS_METHOD]->(m)")
        void createHasMethodRelationship(@Param("classFqn") String classFqn, @Param("methodFqn") String methodFqn);

        /**
         * 创建类与父类之间的 EXTENDS 关系。
         *
         * @param childFqn  子类的完全限定名
         * @param parentFqn 父类的完全限定名
         */
        @Query("MATCH (child:Class {qualifiedName: $childFqn}), (parent:Class {qualifiedName: $parentFqn}) " +
                        "MERGE (child)-[:EXTENDS]->(parent)")
        void createExtendsRelationship(@Param("childFqn") String childFqn, @Param("parentFqn") String parentFqn);

        /**
         * 创建类与接口之间的 IMPLEMENTS 关系。
         *
         * @param classFqn     类的完全限定名
         * @param interfaceFqn 接口的完全限定名
         */
        @Query("MATCH (c:Class {qualifiedName: $classFqn}), (i:Interface {qualifiedName: $interfaceFqn}) " +
                        "MERGE (c)-[:IMPLEMENTS]->(i)")
        void createImplementsRelationship(@Param("classFqn") String classFqn,
                        @Param("interfaceFqn") String interfaceFqn);

        /**
         * 创建类与另一个类型（类或接口）之间的 DEPENDS_ON 关系。
         * 这个查询能够处理依赖目标是类或接口两种情况。
         *
         * @param sourceFqn 依赖发起方的完全限定名
         * @param targetFqn 依赖目标的完全限定名
         */
        @Query("MATCH (source:Class {qualifiedName: $sourceFqn}), (target) " +
                        "WHERE target.qualifiedName = $targetFqn AND (target:Class OR target:Interface) " +
                        "MERGE (source)-[:DEPENDS_ON]->(target)")
        void createDependsOnRelationship(@Param("sourceFqn") String sourceFqn, @Param("targetFqn") String targetFqn);

        @Query("MATCH (n:Class {repositoryId: $repositoryId}) DETACH DELETE n")
        void deleteByRepositoryId(@Param("repositoryId") String repositoryId);
}
