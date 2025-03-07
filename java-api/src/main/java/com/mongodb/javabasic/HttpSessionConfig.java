package com.mongodb.javabasic;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.mongo.JacksonMongoSessionConverter;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;

@Configuration(proxyBeanMethods = false)
@EnableMongoHttpSession
public class HttpSessionConfig {

	/*@Bean
	public JdkMongoSessionConverter jdkMongoSessionConverter() {
		return new JdkMongoSessionConverter(Duration.ofMinutes(30));
	}*/
	@Bean
	public JacksonMongoSessionConverter jacksonMongoSessionConverter() {
		return new JacksonMongoSessionConverter();
	}

}