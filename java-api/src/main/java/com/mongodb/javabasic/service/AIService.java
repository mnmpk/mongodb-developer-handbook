package com.mongodb.javabasic.service;

import java.util.Map;

import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.javabasic.ai.langgraph.AgentExecutor;
import com.mongodb.javabasic.ai.langgraph.AgentExecutorEx;
import com.mongodb.javabasic.ai.langgraph.AgentTool;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;

@Service
public class AIService {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Autowired
        private ChatModel chatModel;
        @Autowired
        private AgentTool tools;

        @Autowired
        private MongoClient mongoClient;

        public String runAgent(String prompt) throws GraphStateException {
                var agent = AgentExecutorEx.builder()
                                .chatModel(chatModel)
                                .toolsFromObject(tools)
                                .build()
                                .compile(/*CompileConfig.builder()
                                                .checkpointSaver(MongoDBSaver.builder()
                                                                .mongoClient(mongoClient)
                                                                .database("agent_logs")
                                                                .collection("logs")
                                                .build()*/);

                var result = agent.stream(Map.of("messages", UserMessage.from(prompt)));

                var state = result.stream()
                                .peek(s -> logger.debug(s.node()))
                                .reduce((a, b) -> b)
                                .map(NodeOutput::state)
                                .orElseThrow();

        return state.lastMessage().map(AiMessage.class::cast)
                .map(AiMessage::text)
                                .orElseThrow();
        }
}
