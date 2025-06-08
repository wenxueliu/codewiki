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

    @Query("MATCH (caller:Method)-[:CALLS]->(callee:Method {qualifiedName: $fqn}) RETURN caller")
    List<MethodNode> findCallersByFqn(@Param("fqn") String fullyQualifiedName);

    // 查询调用链，限定深度为5
    @Query("MATCH path = (startNode:Method)-[:CALLS*1..5]->(endNode:Method {qualifiedName: $fqn}) " +
            "WHERE NOT EXISTS((:Method)-[:CALLS]->(startNode)) " + // 确保是调用链起点
            "RETURN nodes(path), relationships(path)")
    Set<Object> findCallChainByFqn(@Param("fqn") String fullyQualifiedName);
}
