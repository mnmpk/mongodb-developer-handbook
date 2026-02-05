package com.mongodb.javabasic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;


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

}
