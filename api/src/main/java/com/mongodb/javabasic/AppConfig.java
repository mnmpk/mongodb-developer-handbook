package com.mongodb.javabasic;

import java.util.concurrent.Executor;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
@EnableRetry
@EnableAsync
public class AppConfig {
    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(
                MongoClientSettings.builder().codecRegistry(pojoCodecRegistry())
                        .applyConnectionString(new ConnectionString(uri)).build());
    }

    @Bean
    public CodecRegistry pojoCodecRegistry() {
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();

        return CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(pojoCodecProvider));
    }

    @Value("${settings.corePoolsize}")
    private int corePoolsize;

    @Value("${settings.maxPoolSize}")
    private int maxPoolSize;

    @Value("${settings.queueSize}")
    private int queueSize;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolsize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueSize);
        executor.setThreadNamePrefix("th-");
        executor.initialize();
        return executor;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*")
                        .allowedMethods("*")
                        .allowedHeaders("*");
            }
        };
    }
}
