package com.mongodb.javabasic.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.javabasic.model.Config;

public interface ConfigRepository<T> extends MongoRepository<Config<T>, String>, CustomConfigRepository<T> {

}
