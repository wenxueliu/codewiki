package com.codebase.model;


import java.util.List;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "codebase_index")
public class CodeDocument {
    @Id
    private String qualifiedName;

    private String name;

    @Field(type = FieldType.Text, analyzer = "ik_smart")
    private List<String> alias; // 中文别名、同义词

    private String type; // "Class", "Method", "Interface"

    private String filePath;

    private int startLine;

    private int endLine;

    @Field(type = FieldType.Text, analyzer = "ik_smart")
    private String comments;

    // 0.0 to 1.0, 表示注释与代码的匹配度
    private double commentConfidence;

    // 用于语义搜索的向量
    @Field(type = FieldType.Dense_Vector, dims = 768) // 维度取决于你用的模型
    private float[] codeVector;
}
