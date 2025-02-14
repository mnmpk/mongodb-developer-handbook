package com.mongodb.javabasic.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Entry implements Map.Entry<String, String> {
    private String key;
    private String value;

    @Override
    public String setValue(String value) {
        this.value = value;
        return this.value;
    }
}
