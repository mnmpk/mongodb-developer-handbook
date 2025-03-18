package com.mongodb.javabasic.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.javabasic.model.Route;

public interface RouteRepository extends MongoRepository<Route, String> {
}