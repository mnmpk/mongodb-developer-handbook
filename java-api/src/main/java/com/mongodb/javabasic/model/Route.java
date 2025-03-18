package com.mongodb.javabasic.model;

import java.util.List;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.client.model.geojson.Point;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document("td_routes")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    private String route;
    private String bound;
    @BsonProperty("service_type")
    private String serviceType;
    private List<Stop> stops;


    private Point nearestStop;
    private Integer startIndex;
    private Integer endIndex;
    private Double distance;
}
