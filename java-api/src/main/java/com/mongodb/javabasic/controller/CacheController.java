package com.mongodb.javabasic.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping(path = "/cache")
public class CacheController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/data")
    @Cacheable(value = "data")
    public Document cache(@RequestParam int size) {
        return mongoTemplate.insert(new Document("value", RandomStringUtils.randomAscii(size)), "cacheObject");
    }

    @GetMapping("/clear")
    @CacheEvict(cacheNames = "data", key="#size")
    public Document noCache(@RequestParam int size) {
        return mongoTemplate.insert(new Document("value", RandomStringUtils.randomAscii(size)), "cacheObject");
    }

}
