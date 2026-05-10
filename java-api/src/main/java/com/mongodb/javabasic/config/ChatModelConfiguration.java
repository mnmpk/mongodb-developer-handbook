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
                String modelName = "gemma4";
                //String modelName = "llama3.2";
                //String modelName = "llama3-groq-tool-use";
                return OllamaChatModel.builder()
                                .modelName(modelName)
                                .baseUrl("http://localhost:11434")
                                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                                .logRequests(true)
                                .logResponses(true)
                                .maxRetries(2)
                                .temperature(0.1)
                                .build();
        }

}