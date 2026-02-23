package com.mongodb.javabasic.config;


import org.mongodb.spring.session.config.annotation.web.http.EnableMongoHttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("mongo-session")
@Configuration //(proxyBeanMethods = false)
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