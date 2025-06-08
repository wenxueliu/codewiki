package com.codebase.entity;

import lombok.Data;

@Data
public class Entity {
    private String type; // e.g., "Class", "Method", "Concept"
    private String name;
}
