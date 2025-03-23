package com.mongodb.javabasic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.javabasic.cache.MongoCacheManager;
import com.mongodb.javabasic.model.CacheConfig;

@Profile("mongo")
@Configuration
@EnableCaching
public class MongoCacheConfig {

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
