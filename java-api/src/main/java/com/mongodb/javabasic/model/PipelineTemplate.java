package com.mongodb.javabasic.model;

import java.util.LinkedHashMap;
import java.util.List;

import org.bson.BsonDocument;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "pipelines")
public class PipelineTemplate {
    @Id
    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)
    private String id;

    @Field("name")
    @BsonProperty("name")
    private String name;

    @Field("v")
    @BsonIgnore
    private List<LinkedHashMap<String, Object>> aggs;
    
    @BsonProperty("v")
    @Transient
    private List<BsonDocument> content;

}
