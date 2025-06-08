package com.codebase.entity;

import lombok.Data;

import java.util.List;

@Data
public class NluResult {
    private String intent;
    private List<Entity> entities;
    private String originalQuery;
}