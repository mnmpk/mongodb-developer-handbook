package com.mongodb.javabasic;

import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.ext.mongodb.database.MongoLiquibaseDatabase;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;


@Component
public class MongoLiquibaseRunner implements CommandLineRunner, ResourceLoaderAware {

    @Value("${spring.data.mongodb.database}")
    public String MONGODB_DATABASE;

    @Value("${spring.data.mongodb.uri}")
    public String MONGODB_URL;

    @Value("${spring.liquibase.change-log}")
    public String LIQUIBASE_CHANGE_LOG;

    @Value("${spring.liquibase.enabled}")
    public boolean LIQUIBASE_ENABLED;

    public MongoLiquibaseDatabase database;

    protected ResourceLoader resourceLoader;

    public MongoLiquibaseRunner(MongoLiquibaseDatabase database) {
        this.database = database;
    }

    public void setResourceLoader(final ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void run(final String... args) throws Exception {
        if (!LIQUIBASE_ENABLED) {
            return;
        }
        String url = MONGODB_URL + MONGODB_DATABASE;
        MongoLiquibaseDatabase database = (MongoLiquibaseDatabase) DatabaseFactory.getInstance().openDatabase(url, null, null, null, new ClassLoaderResourceAccessor());
        Liquibase liquiBase = new Liquibase(LIQUIBASE_CHANGE_LOG, new ClassLoaderResourceAccessor(), database);
        liquiBase.update("");
    }

}
