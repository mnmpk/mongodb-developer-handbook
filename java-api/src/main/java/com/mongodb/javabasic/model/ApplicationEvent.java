package com.mongodb.javabasic.model;

import com.mongodb.client.model.changestream.UpdateDescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationEvent<T> {
    private String name;
    private String key;
    private T document;
    private UpdateDescription updateDescription;
}
