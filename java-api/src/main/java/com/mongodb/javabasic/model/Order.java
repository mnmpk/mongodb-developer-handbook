package com.mongodb.javabasic.model;

import java.util.Date;
import java.util.List;

import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
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
@Document(collection = "orders")
public class Order {
	@Id
	@BsonRepresentation(BsonType.OBJECT_ID)
	private String id;
    
	@Field("oAt")
	@JsonProperty("orderAt")
	@BsonProperty("oAt")
    private Date orderAt;

	@Field("total")
	@JsonProperty("total")
	@BsonProperty("total")
    private double total;
    
	@Field("items")
	@DocumentReference
	@BsonIgnore
	@JsonProperty("items")
	private List<Product> items;
	@Transient
	@BsonProperty("items")
	@JsonProperty("itemIds")
	private List<ObjectId> itemIds;

	@Field("oBy")
	@DocumentReference
	@BsonIgnore
	@JsonProperty("orderBy")
	private User orderBy;
	@Transient
	@BsonProperty("oBy")
	@JsonProperty("orderById")
	private ObjectId orderById;

	private int version;
}
