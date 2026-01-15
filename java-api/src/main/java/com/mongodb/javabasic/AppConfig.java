package com.mongodb.javabasic;

import java.util.concurrent.Executor;
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
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import freemarker.template.TemplateExceptionHandler;

@Configuration
@EnableMongoRepositories()
@EnableRetry
@EnableAsync
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class AppConfig {
    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Bean
    public MongoClient mongoClient() {
        ConnectionString connString = new ConnectionString(uri);
        ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).strict(true).deprecationErrors(true)
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
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200", "https://mongodb-handbook.mzinx.com/",
                                "https://mongodb-japi.mzinx.com/")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    @Bean
    public freemarker.template.Configuration freemarkerConfig() {
        freemarker.template.Configuration freemarkerConfig = new freemarker.template.Configuration(
                freemarker.template.Configuration.VERSION_2_3_29);
        freemarkerConfig.setDefaultEncoding("UTF-8");
        freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        freemarkerConfig.setLogTemplateExceptions(true);
        freemarkerConfig.setWrapUncheckedExceptions(true);
        freemarkerConfig.setFallbackOnNullLoopVariable(true);
        return freemarkerConfig;
    }

}
