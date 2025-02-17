package com.mongodb.javabasic.model;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
public class TspAirortInfo {
    /*private String country_code;
    private String country_name;
    private String country_official_name;
    private String local_currency;
    private String nationality;
    private String global_region;

    private String port_code;
    private String port_name;
    private String country_code;
    private Integer utc_local_time_variation;*/

    private String iata_airport_code;
    private String icao_airport_code;
    private String airport_name;
    private String port_code;
}
