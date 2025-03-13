package com.mongodb.javabasic.model;

import java.util.List;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document("td_routes")
public class Route {
    private String route;
    private String bound;
    @BsonProperty("service_type")
    private String serviceType;
    private List<Stop> stops;
}
