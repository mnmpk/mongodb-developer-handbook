package com.mongodb.javabasic.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;
import java.time.Instant;
import java.util.Date;

@Data
@Document(collection = "sessions")
public class SessionsEntity {
    @Id
    private String id;
    private Map<String, Object> data;
    private Instant creationTime;
    private Date expireAt;
    private Instant lastAccessedTime;
    private long expireAfterSeconds;
}