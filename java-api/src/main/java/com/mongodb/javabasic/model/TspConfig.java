package com.mongodb.javabasic.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "config")
public class TspConfig {

    @Id
    private String id;
    private List<Entry> params;
    private List<org.bson.Document> values;
}
