package com.mongodb.javabasic.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.javabasic.model.TspConfig;

public interface TspRouteRepository extends MongoRepository<TspConfig, String>, CustomTspRouteRepository {

}
