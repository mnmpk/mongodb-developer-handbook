package com.mongodb.javabasic.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.model.Filters;
import com.mongodb.javabasic.model.PipelineTemplate;
import com.mongodb.javabasic.repositories.CustomPipelineRepository;

public class PipelineRepositoryImpl implements CustomPipelineRepository {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private MongoTemplate mongoTemplate;

    public PipelineTemplate findByName(String name) {
        PipelineTemplate p = mongoTemplate.getCollection(mongoTemplate.getCollectionName(PipelineTemplate.class))
                .withDocumentClass(PipelineTemplate.class).find(Filters.eq("name", name)).first();
        return p;
    }
}
