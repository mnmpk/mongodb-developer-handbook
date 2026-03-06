package com.mongodb.javabasic.repositories;

import com.mongodb.javabasic.model.PipelineTemplate;

public interface CustomPipelineRepository {

    public PipelineTemplate findByName(String name);
}
