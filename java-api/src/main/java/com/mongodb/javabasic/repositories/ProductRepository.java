package com.mongodb.javabasic.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.javabasic.model.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
}