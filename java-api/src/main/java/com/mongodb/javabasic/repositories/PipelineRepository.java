package com.mongodb.javabasic.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.javabasic.model.PipelineTemplate;


public interface PipelineRepository extends MongoRepository<PipelineTemplate, String>, CustomPipelineRepository {

    public PipelineTemplate findByName(String name);

}
