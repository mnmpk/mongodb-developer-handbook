package com.mongodb.javabasic.config;

import liquibase.database.DatabaseFactory;
import liquibase.exception.DatabaseException;
import liquibase.ext.mongodb.database.MongoLiquibaseDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoLiquibaseRunnerConfig {

    @Value("${spring.data.mongodb.database}")
    public String MONGODB_DATABASE;

    @Value("${spring.data.mongodb.uri}")
    public String MONGODB_URL;

    @Bean
    public MongoLiquibaseRunner liquibaseRunner(final MongoLiquibaseDatabase database) {
        return new MongoLiquibaseRunner(database);
    }

    /**
     * @return Database with connection
     * @throws DatabaseException when cannot connect
     */
    @Bean
    public MongoLiquibaseDatabase database() throws DatabaseException {
        String url = MONGODB_URL + MONGODB_DATABASE;
        return (MongoLiquibaseDatabase) DatabaseFactory.getInstance().openDatabase(url, null, null, null, null);
    }

}
