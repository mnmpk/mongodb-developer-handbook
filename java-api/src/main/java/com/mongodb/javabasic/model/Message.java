package com.mongodb.javabasic.model;

import java.util.Date;

import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    public enum Type {
        REQ, ACK, RES
    }
    private Type type;

    @BsonProperty("t")
    private String target;

    @BsonProperty("c")
    private Document content;

    @BsonProperty("cAt")
    private Date createdAt;
}
