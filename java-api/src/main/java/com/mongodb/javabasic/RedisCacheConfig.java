package com.mongodb.javabasic;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import io.lettuce.core.RedisURI;

@Configuration
@EnableCaching
public class RedisCacheConfig {
    RedisURI redisURI = RedisURI.create("rediss://tsp-test-byysaj.serverless.ape1.cache.amazonaws.com:6379");

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.create(connectionFactory);
    }
}
