package com.mongodb.javabasic.model;


import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.client.model.geojson.Point;

import lombok.Data;

@Data
@Document("td_stops")
public class Stop {
    @Id
    private String id;
    @BsonProperty("name_en")
    private String nameEn;
    @BsonProperty("name_tc")
    private String nameTc;
    @BsonProperty("name_sc")
    private String nameSc;
    private Point location;
}
