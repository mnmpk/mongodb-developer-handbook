package com.mongodb.javabasic.model;

import java.util.Map;

import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "custom")
public class CustomEntity {
	@Id
	@BsonRepresentation(BsonType.OBJECT_ID)
	private String id;
    
    private Map<String, Object> data;
	
	private int version;
}
