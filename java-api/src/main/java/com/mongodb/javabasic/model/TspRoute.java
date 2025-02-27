package com.mongodb.javabasic.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "tsp_routes")
public class TspRoute extends TspPortInfo{

    @Id
    private String id;
    private List<TspRoute> destinations;
    
}
