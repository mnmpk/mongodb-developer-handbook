package com.mongodb.javabasic.controller;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

@RestController
@RequestMapping(path = "/change-stream")
public class ChangeStreamController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MongoTemplate mongoTemplate;

    @RequestMapping("/list")
    public List<Document> list() {
        return mongoTemplate.getCollection("changeStreams").find().into(new ArrayList<>());
    }

    @RequestMapping("/watch/{database}/{collection}")
    public void watch(
            @PathVariable(required = false) String collection,
            @RequestParam String pipeline,
            @RequestParam String mode,
            @RequestParam(required = false, defaultValue = "10") int batchSize,
            @RequestParam(required = false, defaultValue = "10") int maxAwaitTime,
            @RequestParam(required = false) boolean resume,
            @RequestParam(required = false) long startAt,
            @RequestParam(required = false) long lastEventTime,
            @RequestParam(required = false, defaultValue = "1") int noOfChangeStream,
            @RequestParam(required = false, defaultValue = "false") boolean fullDocument) throws Exception {
        mongoTemplate.getCollection("changeStreams").updateOne(Filters.and(
                Filters.eq("coll", collection), Filters.eq("pipeline", pipeline)),
                Updates.combine(Updates.set("coll", collection), Updates.set("pipeline", pipeline),
                        Updates.set("mode", mode)),
                new UpdateOptions().upsert(true));

    }

    @RequestMapping("/unwatch/{database}/{collection}")
    public void unwatch(
            @PathVariable(required = false) String collection,
            @RequestParam String pipeline,
            @RequestParam String mode) throws Exception {
        mongoTemplate.getCollection("changeStreams").deleteOne(Filters.and(
                Filters.eq("coll", collection), Filters.eq("pipeline", pipeline)));
    }
}
