package com.mongodb.javabasic.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.javabasic.model.User;

public interface UserRepository extends MongoRepository<User, String> {
}