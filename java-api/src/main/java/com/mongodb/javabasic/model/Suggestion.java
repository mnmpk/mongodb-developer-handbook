package com.mongodb.javabasic.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Suggestion {
    private List<Route> legs;
}
