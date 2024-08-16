package com.mongodb.javabasic.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.javabasic.model.CustomEntity;

public interface CustomEntityRepository extends MongoRepository<CustomEntity, String> {
}