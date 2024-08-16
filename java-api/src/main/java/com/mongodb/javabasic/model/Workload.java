package com.mongodb.javabasic.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workload {
    public enum Implementation {
        REPO, SPRING, DRIVER
    }

    public enum Type {
        READ, WRITE
    }

    public enum OperationType {
        INSERT, UPDATE, DELETE, REPLACE
    }

    public enum WriteConcern {
        ACKNOWLEDGED, W1, W2, W3, UNACKNOWLEDGED, JOURNALED, MAJORITY
    }

    public enum Converter {
        SPRING,
        MONGODB
    }

    @JsonProperty("impl")
    private Implementation implementation;
    private Type type;
    @JsonProperty("docSize")
    private int documentSize;
    private String schema;
    private Converter converter;
    // TODO:Read options?

    // Write Option
    @JsonProperty("opType")
    private OperationType operationType;
    @JsonProperty("numWorkers")
    private int noOfWorkers;
    @JsonProperty("qty")
    private int quantity;
    @JsonProperty("w")
    private WriteConcern writeConcern;
    private boolean bulk;
    private String query;
    private List<String> ids;
}
