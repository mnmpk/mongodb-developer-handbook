package com.mongodb.javabasic.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.javabasic.model.Order;

public interface OrderRepository extends MongoRepository<Order, String> {
}