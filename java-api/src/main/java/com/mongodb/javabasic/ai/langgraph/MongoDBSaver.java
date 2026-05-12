package com.mongodb.javabasic.ai.langgraph;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.checkpoint.AbstractCheckpointSaver;
import org.bsc.langgraph4j.checkpoint.Checkpoint;
import org.bson.Document;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.JacksonChatMessageJsonCodec;

@Component
public class MongoDBSaver extends AbstractCheckpointSaver {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MongoTemplate mongoTemplate;

    private byte[] encodeState(Map<String, Object> data) throws IOException {
        return JacksonChatMessageJsonCodec.chatMessageJsonMapperBuilder().build()
                .writeValueAsBytes(data.get("messages"));
    }

    /**
     * Deserialize state bytes back to a {@code Map<String,Object>}.
     */
    private Map<String, Object> decodeState(byte[] payload)
            throws IOException, ClassNotFoundException {

        Map<String, Object> map = new HashMap<>();
        map.put("messages", JacksonChatMessageJsonCodec.chatMessageJsonMapperBuilder().build().readValue(payload,
                new TypeReference<List<ChatMessage>>() {
                }));
        return map;
    }

    @Override
    protected LinkedList<Checkpoint> loadCheckpoints(RunnableConfig config) throws Exception {
        final var checkpoints = new LinkedList<Checkpoint>();
        final var threadId = threadId(config);
        mongoTemplate.getCollection("checkpoints")
                .find(Filters.and(Filters.eq("tId", threadId), Filters.eq("isRelease", false))).forEach(doc -> {

                        try {
                            checkpoints.add(Checkpoint.builder().id(doc.getString("_id")).nodeId(doc.getString("nId"))
                                    .nextNodeId(doc.getString("nnId"))
                                    .state(decodeState(doc.get("state", Binary.class).getData()))
                                    // .state(decodeState(doc.getString("state")))
                                    .build());
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                });
        return checkpoints;
    }

    @Override
    protected void insertedCheckpoint(RunnableConfig config, LinkedList<Checkpoint> checkpoints, Checkpoint checkpoint)
            throws Exception {
        final var threadId = threadId(config);
        mongoTemplate.getCollection("checkpoints")
                .insertOne(new Document("tId", threadId).append("isRelease", false)
                        .append("_id", checkpoint.getId())
                        .append("nId", checkpoint.getNodeId())
                        .append("nnId", checkpoint.getNextNodeId())
                        .append("state", encodeState(checkpoint.getState()))
                        .append("cAt", new Date()));

    }

    @Override
    protected void updatedCheckpoint(RunnableConfig config, LinkedList<Checkpoint> checkpoints, Checkpoint checkpoint)
            throws Exception {
        // TODO: ???
    }

    @Override
    protected Tag releaseCheckpoints(RunnableConfig config, LinkedList<Checkpoint> checkpoints) throws Exception {
        final var threadId = threadId(config);
        mongoTemplate.getCollection("checkpoints").updateMany(Filters.eq("tId", threadId),
                Updates.set("isRelease", true));
        return new Tag(threadId, checkpoints);
    }
}
