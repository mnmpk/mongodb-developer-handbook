package com.mongodb.javabasic.ai.langgraph;

import org.bsc.langgraph4j.action.NodeAction;
import java.util.Map;

// Node that adds a greeting
public class GreeterNode implements NodeAction<SimpleState> {
    @Override
    public Map<String, Object> apply(SimpleState state) {
        System.out.println("GreeterNode executing. Current messages: " + state.messages());
        return Map.of(SimpleState.MESSAGES_KEY, "Hello from GreeterNode!");
    }
}

