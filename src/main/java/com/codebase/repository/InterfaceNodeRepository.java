package com.codebase.repository;

import com.codebase.model.InterfaceNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterfaceNodeRepository extends Neo4jRepository<InterfaceNode, String> {

        /**
         * 根据文件路径查找所有接口节点。
         * 用于增量更新中的删除操作。
         *
         * @param filePath 文件的路径
         * @return 匹配的 InterfaceNode 列表
         */
        List<InterfaceNode> findAllByFilePath(String filePath);

        /**
         * 创建接口与方法之间的 HAS_METHOD 关系。
         *
         * @param interfaceFqn 接口的完全限定名
         * @param methodFqn    方法的完全限定签名
         */
        @Query("MATCH (i:Interface {qualifiedName: $interfaceFqn}), (m:Method {qualifiedName: $methodFqn}) " +
                        "MERGE (i)-[:HAS_METHOD]->(m)")
        void createHasMethodRelationship(@Param("interfaceFqn") String interfaceFqn,
                        @Param("methodFqn") String methodFqn);

        /**
         * 创建接口与父接口之间的 EXTENDS 关系。
         *
         * @param childFqn  子接口的完全限定名
         * @param parentFqn 父接口的完全限定名
         */
        @Query("MATCH (child:Interface {qualifiedName: $childFqn}), (parent:Interface {qualifiedName: $parentFqn}) " +
                        "MERGE (child)-[:EXTENDS]->(parent)")
        void createExtendsRelationship(@Param("childFqn") String childFqn, @Param("parentFqn") String parentFqn);

        void deleteByRepositoryId(String repositoryId);
}
