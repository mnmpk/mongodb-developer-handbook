package com.mongodb.javabasic.config;

import java.util.concurrent.TimeUnit;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;


@Configuration
@EnableMongoRepositories(basePackages = { "com.mongodb.javabasic.repositories" })
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Bean
    public MongoClient mongoClient() {
        ConnectionString connString = new ConnectionString(uri);
        ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).strict(false).deprecationErrors(true)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applicationName("developer-handbook-java")
                .readPreference(ReadPreference.primaryPreferred())
                .readConcern(ReadConcern.MAJORITY)
                .writeConcern(WriteConcern.MAJORITY)
                .applyConnectionString(connString)
                .applyToConnectionPoolSettings(
                        builder -> builder.minSize(2).maxSize(10).maxConnectionLifeTime(5400, TimeUnit.SECONDS))
                .timeout(30, TimeUnit.SECONDS)
                // .applyToSocketSettings(builder -> builder.readTimeout(30, TimeUnit.SECONDS))
                .serverApi(serverApi)
                .codecRegistry(pojoCodecRegistry())
                .build();

        return MongoClients.create(
                settings);
    }

    @Bean
    public CodecRegistry pojoCodecRegistry() {
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        return CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(pojoCodecProvider));
    }
}
