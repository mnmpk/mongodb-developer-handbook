package com.mongodb.javabasic;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;


@Profile("redis")
@Configuration(proxyBeanMethods = false)
@EnableRedisIndexedHttpSession
public class RedisHttpSessionConfig {


}