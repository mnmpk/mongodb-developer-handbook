package com.mongodb.javabasic.model;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "ori_country_info")
public class TspCountryInfo implements Serializable{
    private String country_code;
    private String country_name;
    private String country_official_name;
    private String local_currency;
    private String nationality;
    private String global_region;

    private List<TspPortInfo> ports;
}
