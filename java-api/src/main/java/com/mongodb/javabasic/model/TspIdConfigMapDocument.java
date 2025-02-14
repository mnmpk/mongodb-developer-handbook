package com.mongodb.javabasic.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Document(collection = "ori_tsp_id_config")
public class TspIdConfigMapDocument {
    @Id
    private String id;
    private String channel;
    @Field("tsp_id")
    private String tspId;
    private boolean used;
    private Date createTime;
    private Date updateTime;
}
