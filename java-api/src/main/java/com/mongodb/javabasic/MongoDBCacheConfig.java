package com.mongodb.javabasic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.javabasic.cache.MongoCacheManager;
import com.mongodb.javabasic.model.CacheConfig;

@Configuration
@EnableCaching
public class MongoDBCacheConfig {

    @Autowired
    MongoTemplate mongoTemplate;

    @Bean
    public CacheManager mongoCacheManager() {
        return new MongoCacheManager(mongoTemplate, CacheConfig.builder()
        .cacheName("data")
        .ttl(600)
        .flushOnBoot(false)
        .storeBinaryOnly(false).build(),CacheConfig.builder()
        .cacheName("config")
        .ttl(60*60*24*7)
        .flushOnBoot(false)
        .storeBinaryOnly(true).build());
    }
}
