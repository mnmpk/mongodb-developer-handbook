package com.mongodb.javabasic.config;


import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.session.data.mongo.JacksonMongoSessionConverter;
import org.springframework.session.data.mongo.JdkMongoSessionConverter;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;

@Profile("mongo-session")
@Configuration(proxyBeanMethods = false)
@EnableMongoHttpSession
public class MongoHttpSessionConfig {

	/*@Bean
	public JdkMongoSessionConverter jdkMongoSessionConverter() {
		return new JdkMongoSessionConverter(Duration.ofMinutes(30));
	}*/
	/*@Bean
	public JacksonMongoSessionConverter jacksonMongoSessionConverter() {
		return new JacksonMongoSessionConverter();
	}*/
	@Bean
	public MongoSessionConverter mongoSessionConverter() {
		return new MongoSessionConverter();
	}

}