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
    private Integer routeId;
    private String companyCode;
    private Integer routeType;
    private boolean isCircular;
    private Double journeyTime;
    private String serviceMode;
    private String operationMode;
    private String serviceType;
    private String locStartNamec;
    private String locStartNames;
    private String locStartNamee;
    private String locEndNamec;
    private String locEndNames;
    private String locEndNamee;
    private Integer routeSeq;
    @BsonProperty("routeNamee")
    private String nameEn;
    @BsonProperty("routeNamec")
    private String nameTc;
    @BsonProperty("routeNames")
    private String nameSc;


    private List<Stop> stops;

    private Point nearestStop;
    private Integer startIndex;
    private Integer endIndex;
    private Double distance;
}
