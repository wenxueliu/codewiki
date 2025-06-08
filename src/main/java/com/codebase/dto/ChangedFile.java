package com.codebase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.jgit.diff.DiffEntry;

@Data
@AllArgsConstructor
public class ChangedFile {
    private DiffEntry.ChangeType changeType;
    private String oldPath;
    private String newPath;
}
