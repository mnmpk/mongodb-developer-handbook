package com.mongodb.javabasic.service;

import java.util.List;
import java.util.Map;

import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.RunnableConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mongodb.javabasic.ai.langgraph.AgentExecutor;
import com.mongodb.javabasic.ai.langgraph.AgentTool;
import com.mongodb.javabasic.ai.langgraph.MongoDBSaver;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.model.chat.ChatModel;

@Service
public class AIService {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Value("${spring.mongodb.uri}")
        private String uri;
        @Value("${spring.mongodb.database}")
        private String dbName;
        @Autowired
        private ChatModel chatModel;
        @Autowired
        private AgentTool tools;

        @Autowired
        private MongoDBSaver mongoDBSaver;

        public String runAgent(String tId, String prompt) throws GraphStateException {

                StdioMcpTransport transport = new StdioMcpTransport.Builder()
                                .command(List.of("npx", "-y",
                                                "mongodb-mcp-server@latest",
                                                "--readOnly"))
                                .logEvents(true).environment(Map.of(
                                                // "MDB_MCP_API_CLIENT_ID", "<client-id>",
                                                // "MDB_MCP_API_CLIENT_SECRET", "<client-secret>",
                                                "MDB_MCP_CONNECTION_STRING", uri+dbName))
                                .build();

                // 2. Create the MCP Client
                McpClient mcpClient = new DefaultMcpClient.Builder()
                                .transport(transport)
                                .build();

                // 3. Optional: Perform a health check
                mcpClient.checkHealth();
                var agent = AgentExecutor.builder().systemMessage(SystemMessage.from(
                                "You are an expert data analyst assistant. Your task is to analyze user-provided data and generate insights. Instructions: Always summarize findings first, followed by detailed analysis. Constraints: Only use the data provided by the user. If the data is insufficient, ask for clarification. Output Format: Output analysis in HTML format with bullet points for key takeaways."))
                                .chatModel(chatModel)
                                .toolsFromObject(tools)
                                .tool(mcpClient)
                                .build()
                                .compile(
                                                CompileConfig.builder()
                                                                .checkpointSaver(mongoDBSaver).releaseThread(false)
                                                                .build());

                var result = agent.stream(Map.of("messages", UserMessage.from(prompt)),
                                RunnableConfig.builder().threadId(tId).build());

                var state = result.stream()
                                .peek(s -> logger.debug(s.node()))
                                .reduce((a, b) -> b)
                                .map(NodeOutput::state)
                                .orElseThrow();
                /*
                 * if (state.isEND()) {
                 * return state.state().finalResponse().orElseThrow();
                 * }
                 * return null;
                 */

                return state.lastMessage().map(AiMessage.class::cast)
                                .map(AiMessage::text)
                                .orElseThrow();

        }
}
