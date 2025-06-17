package com.codebase.repository;

import com.codebase.model.MethodNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface MethodNodeRepository extends Neo4jRepository<MethodNode, String> {

        List<MethodNode> findAllByFilePath(String filePath);

        @Query("MATCH (caller:Method)-[:CALLS]->(callee:Method {qualifiedName: $fqn}) RETURN caller")
        List<MethodNode> findCallersByFqn(@Param("fqn") String fullyQualifiedName);

        @Query("MATCH path = (startNode:Method)-[:CALLS*1..5]->(endNode:Method {qualifiedName: $fqn}) " +
                        "WHERE NOT EXISTS((:Method)-[:CALLS]->(startNode)) " +
                        "RETURN nodes(path), relationships(path)")
        Set<Object> findCallChainByFqn(@Param("fqn") String fullyQualifiedName);

        /**
         * 创建方法之间的 CALLS 关系。
         *
         * @param callerFqn 调用方方法的完全限定签名
         * @param calleeFqn 被调用方方法的完全限定签名
         */
        @Query("MATCH (caller:Method {qualifiedName: $callerFqn}), (callee:Method {qualifiedName: $calleeFqn}) " +
                        "MERGE (caller)-[:CALLS]->(callee)")
        void createCallsRelationship(@Param("callerFqn") String callerFqn, @Param("calleeFqn") String calleeFqn);

        void deleteByRepositoryId(String repositoryId);
}
