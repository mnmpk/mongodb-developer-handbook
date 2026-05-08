package com.mongodb.javabasic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

@Configuration
public class ChatModelConfiguration {

        @Bean
        public ChatModel ollamaModel() {
                return OllamaChatModel.builder()
                                .modelName("llama3.2")
                                .baseUrl("http://localhost:11434")
                                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                                .logRequests(true)
                                .logResponses(true)
                                .maxRetries(2)
                                .temperature(0.1)
                                .build();
        }

}