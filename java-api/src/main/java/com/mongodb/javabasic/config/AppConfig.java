package com.mongodb.javabasic.config;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import com.mongodb.javabasic.model.ChangeStreamRegistry;

import freemarker.template.TemplateExceptionHandler;

@Configuration
@EnableRetry
public class AppConfig {

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

    @Bean
    public Set<String> instances() {
        return Collections.synchronizedSet(new LinkedHashSet<>());

    }

    @Bean
    public Map<String, ChangeStreamRegistry<?>> changeStreams() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public String podName() {
        return System.getenv("HOSTNAME");
    }
}
