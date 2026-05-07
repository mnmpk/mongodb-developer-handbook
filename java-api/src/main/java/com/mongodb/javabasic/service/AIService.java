package com.mongodb.javabasic.service;

import java.util.Map;

import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Service;

import com.mongodb.javabasic.ai.langgraph.AgentExecutor;
import com.mongodb.javabasic.ai.langgraph.GreeterNode;
import com.mongodb.javabasic.ai.langgraph.ResponderNode;
import com.mongodb.javabasic.ai.langgraph.SimpleState;
import com.mongodb.javabasic.ai.langgraph.TestTool;

import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.ollama.OllamaChatModel;

import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.action.NodeAction;

@Service
public class AIService {
    public void generateResponse(String prompt) throws GraphStateException {
        // Initialize nodes
        GreeterNode greeterNode = new GreeterNode();
        ResponderNode responderNode = new ResponderNode();

        // Define the graph structure
        var stateGraph = new StateGraph<>(SimpleState.SCHEMA, initData -> new SimpleState(initData))
                .addNode("greeter", AsyncNodeAction.node_async(greeterNode))
                .addNode("responder", AsyncNodeAction.node_async(responderNode))
                // Define edges
                .addEdge(StateGraph.START, "greeter") // Start with the greeter node
                .addEdge("greeter", "responder")
                .addEdge("responder", StateGraph.END) // End after the responder node
        ;
        // Compile the graph
        var compiledGraph = stateGraph.compile();

        // Run the graph
        // The `stream` method returns an AsyncGenerator.
        // For simplicity, we'll collect results. In a real app, you might process them
        // as they arrive.
        // Here, the final state after execution is the item of interest.

        for (var item : compiledGraph.stream(Map.of(SimpleState.MESSAGES_KEY, prompt))) {

            System.out.println(item);
        }

    }

    public void runAgent(String prompt) throws GraphStateException {

        var model = OllamaChatModel.builder()
                .modelName("llama3")
                .baseUrl("http://localhost:11434")
                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                .logRequests(true)
                .logResponses(true)
                .maxRetries(2)
                .temperature(0.0)
                .build();

        var agent = AgentExecutor.builder()
                .chatModel(model)
                .toolsFromObject(new TestTool())
                .build()
                .compile();

        for (var item : agent
                .stream(Map.of("messages", prompt))) {

            System.out.println(item);
        }

    }
}
