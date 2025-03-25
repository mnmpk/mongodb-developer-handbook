package com.mongodb.javabasic.model;


import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.client.model.geojson.Point;

import lombok.Data;

@Data
@Document("td_stops")
public class Stop {
    private Integer stopId;
    private Integer stopSeq;
    @BsonProperty("stopNamee")
    private String nameEn;
    @BsonProperty("stopNamec")
    private String nameTc;
    @BsonProperty("stopNames")
    private String nameSc;
    private Point location;
}
