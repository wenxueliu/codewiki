package com.codebase.service.impl;

import com.codebase.dto.ChangedFile;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GitService {
    public Optional<String> getHeadCommitId(Path projectPath) throws IOException {
        try (Git git = Git.open(projectPath.toFile())) {
            ObjectId head = git.getRepository().resolve("HEAD");
            return Optional.ofNullable(head).map(ObjectId::getName);
        }
    }

    public List<ChangedFile> getChangedFiles(Path projectPath, String oldCommitId, String newCommitId) throws Exception {
        try (Git git = Git.open(projectPath.toFile())) {
            Repository repository = git.getRepository();
            try (ObjectReader reader = repository.newObjectReader()) {
                CanonicalTreeParser newTree = new CanonicalTreeParser();
                newTree.reset(reader, repository.resolve(newCommitId + "^{tree}"));

                CanonicalTreeParser oldTree = new CanonicalTreeParser();
                // 如果 oldCommitId 为 null，说明是第一次全量索引，与一个空树比较
                if (oldCommitId != null) {
                    ObjectId oldCommitObjectId = repository.resolve(oldCommitId);
                    if (oldCommitObjectId != null) {
                        oldTree.reset(reader, repository.resolve(oldCommitId + "^{tree}"));
                    }
                }

                List<DiffEntry> diffs = git.diff()
                        .setNewTree(newTree)
                        .setOldTree(oldTree)
                        .setShowNameAndStatusOnly(true)
                        .call();

                return diffs.stream()
                        .filter(diff -> diff.getNewPath().endsWith(".java") || diff.getOldPath().endsWith(".java"))
                        .map(diff -> new ChangedFile(diff.getChangeType(), diff.getOldPath(), diff.getNewPath()))
                        .collect(Collectors.toList());
            }
        }
    }
}
