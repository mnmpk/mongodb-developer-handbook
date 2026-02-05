package com.mongodb.javabasic.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!redis-cache | !redis-session")
@Configuration
@EnableAutoConfiguration(exclude = {RedisAutoConfiguration.class})
public class NonRedisConfig {
}
