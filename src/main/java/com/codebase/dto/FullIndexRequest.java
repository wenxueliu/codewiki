package com.codebase.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FullIndexRequest {
    private String repositoryPath;

    private String repositoryId;
}
