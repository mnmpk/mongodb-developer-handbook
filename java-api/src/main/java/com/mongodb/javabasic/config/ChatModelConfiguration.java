package com.mongodb.javabasic.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

@Configuration
public class ChatModelConfiguration {

        @Value("${settings.ai.url}")
        private String url;

        @Value("${settings.ai.model}")
        private String model;

        @Bean
        public ChatModel ollamaModel() {
                return OllamaChatModel.builder()
                                .modelName(model)
                                .baseUrl(url)
                                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                                .logRequests(true)
                                .logResponses(true)
                                .timeout(Duration.ofSeconds(600))
                                .maxRetries(2)
                                .temperature(0.1)
                                .build();
        }

}