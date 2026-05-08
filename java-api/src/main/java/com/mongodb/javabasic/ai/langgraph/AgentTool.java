package com.mongodb.javabasic.ai.langgraph;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

@Service
public class AgentTool {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    MongoTemplate mongoTemplate;

    @Tool("tool for getting all MongoDB collections' name")
    List<String> getDBCollections() {
        List<String> collections = mongoTemplate.getDb().listCollectionNames().into(new ArrayList<>());
        logger.info("Retrieved collections: {}", collections);
        return collections;
    }

    @Tool("tool for test AI agent executor")
    String execTest(@P("test message") String message) {
        return String.format("test tool ('%s') executed with result 'OK'", message);
    }

    @Tool("return current number of system thread allocated by application")
    int threadCount() {
        return Thread.getAllStackTraces().size();
    }
}
