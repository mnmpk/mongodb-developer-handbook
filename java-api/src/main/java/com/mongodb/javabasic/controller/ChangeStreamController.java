package com.mongodb.javabasic.controller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.FullDocument;
import com.mongodb.javabasic.model.ChangeStreamProcess;
import com.mongodb.javabasic.model.ChangeStreamProcessConfig;
import com.mongodb.javabasic.model.ChangeStream;

@RestController
@RequestMapping(path = "/watch")
public class ChangeStreamController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MongoTemplate mongoTemplate;

    @RequestMapping("/{collection}")
    public void watch(@PathVariable("collection") String collection,
            @RequestParam(required = false, defaultValue = "10") int batchSize,
            @RequestParam(required = false, defaultValue = "10") int maxAwaitTime,
            @RequestParam(required = false) boolean resume,
            @RequestParam(required = false) long startAt,
            @RequestParam(required = false) long lastEventTime,
            @RequestParam(required = false, defaultValue = "1") int noOfChangeStream,
            @RequestParam(required = false, defaultValue = "false") boolean fullDocument) throws Exception {
        new ChangeStream<Document>().run(noOfChangeStream, (ChangeStreamProcessConfig<Document> config) -> {
            List<Bson> pipeline = (List.of(Aggregates.match(
                    Filters.in("ns.coll", List.of(collection)))));
            if (startAt > 0)
                config.setStartAt(new BsonTimestamp(startAt));
            if (lastEventTime > 0)
                config.setEndAt(new BsonTimestamp(lastEventTime));
            return new ChangeStreamProcess<Document>(config,
                    (e) -> {
                        logger.info("Body:" + e.getFullDocument());
                    }) {
                @Override
                public ChangeStreamIterable<Document> initChangeStream(List<Bson> p) {
                    if (pipeline != null && pipeline.size() > 0)
                        p.addAll(pipeline);
                    ChangeStreamIterable<Document> changeStream = mongoTemplate.getDb().watch(p, Document.class)
                            .batchSize(batchSize)
                            .maxAwaitTime(maxAwaitTime, TimeUnit.MILLISECONDS);
                    if (fullDocument) {
                        changeStream = changeStream.fullDocument(FullDocument.UPDATE_LOOKUP);
                    }
                    return changeStream;
                }

            };
        }, resume);
    }
}
