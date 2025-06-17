package com.codebase.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProjectStateService {
    // 使用 Map 模拟持久化存储，key 为项目路径，value 为最后的 commitId
    private final Map<String, String> projectLastCommit = new ConcurrentHashMap<>();

    public Optional<String> getLastCommitId(String projectPath) {
        return Optional.ofNullable(projectLastCommit.get(projectPath));
    }

    public void setLastCommitId(String projectPath, String commitId) {
        if (commitId == null) {
            if (!projectLastCommit.containsKey(projectPath)) {
                return;
            }
            projectLastCommit.remove(projectPath);
            return;
        }
        if (projectLastCommit.containsKey(projectPath)) {
            projectLastCommit.replace(projectPath, commitId);
            return;
        }
        projectLastCommit.put(projectPath, commitId);
    }
}
