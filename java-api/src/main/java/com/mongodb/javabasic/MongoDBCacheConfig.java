package com.mongodb.javabasic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.javabasic.cache.MongoCacheConfig;
import com.mongodb.javabasic.cache.MongoCacheManager;


@Configuration
@EnableCaching
public class MongoDBCacheConfig {

    @Autowired
    MongoTemplate mongoTemplate;
    
    @Bean
    public CacheManager mongoCacheManager() {
        return new MongoCacheManager(mongoTemplate, MongoCacheConfig.builder().collectionName("cache").ttl(600).flushOnBoot(false).storeBinaryOnly(false).build());
    }
}
