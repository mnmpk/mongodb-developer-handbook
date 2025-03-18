package com.mongodb.javabasic.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.javabasic.model.Stop;

public interface StopRepository extends MongoRepository<Stop, String> {
}