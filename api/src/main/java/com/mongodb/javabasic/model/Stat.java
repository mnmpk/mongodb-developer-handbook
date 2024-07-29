package com.mongodb.javabasic.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Stat<T> {
    private List<String> fields;

    public Stat(Class<T> clazz) {
        fields = new ArrayList<>();
        for (Field f : clazz.getDeclaredFields()) {
            this.fields.add(f.getName());
        }
    }

    private Date startAt;
    private Date endAt;
    private Workload workload;
    private long duration;
    @JsonProperty("ops")
    private double operationPerSecond;
    @JsonProperty("min")
    private long minLatency;
    @JsonProperty("max")
    private long maxLatency;
    @JsonProperty("avg")
    private double avgLatency;
    private List<T> data;

    public void setDuration(long duration) {
        this.duration = duration;
        if (workload != null && workload.getQuantity() > 0) {
            this.operationPerSecond = ((double) workload.getQuantity() / this.duration) * 1000;
            this.avgLatency = ((double) this.duration / workload.getQuantity());
        }
    }
}
