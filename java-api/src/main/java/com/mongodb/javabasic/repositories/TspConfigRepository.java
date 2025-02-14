package com.mongodb.javabasic.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.javabasic.model.TspConfig;

public interface TspConfigRepository extends MongoRepository<TspConfig, String>, CustomTspConfigRepository {

}
