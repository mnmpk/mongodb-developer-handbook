package com.mongodb.javabasic.model;

import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {
	@Id
	@BsonRepresentation(BsonType.OBJECT_ID)
	private String id;
    
    private String sku;

	@Field("uPrice")
	@BsonProperty("uPrice")
	@JsonProperty("unitPrice")
    private double unitPrice;

	@Field("soh")
	@BsonProperty("soh")
	@JsonProperty("stockOnHand")
    private int stockOnHand;

	private int version;
}
