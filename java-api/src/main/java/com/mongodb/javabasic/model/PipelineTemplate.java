package com.mongodb.javabasic.model;

import java.util.List;
import java.util.Map;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.conversions.Bson;
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
    private String name;

    @Field("v")
    @BsonIgnore
    private List<Map<String, Object>> aggs;
    
    @BsonProperty("v")
    @Transient
    private List<Bson> content;

}
