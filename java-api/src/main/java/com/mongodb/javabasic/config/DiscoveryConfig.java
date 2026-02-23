package com.mongodb.javabasic.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.changestream.FullDocumentBeforeChange;
import com.mongodb.javabasic.model.ApplicationEvent;
import com.mongodb.javabasic.model.ChangeStream;
import com.mongodb.javabasic.model.ChangeStream.Mode;
import com.mongodb.javabasic.model.ChangeStreamRegistry;
import com.mongodb.javabasic.service.ApplicationEventService;
import com.mongodb.javabasic.service.ChangeStreamService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Profile("discovery")
@Configuration
@EnableScheduling
public class DiscoveryConfig {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String INDEX_KEY = "at";
    private static final String INDEX_NAME = "ttl";

    @Value("${settings.instances.collection}")
    private String collection;
    @Value("${settings.instances.maxTimeout}")
    private long maxTimeout;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private Set<String> instances;

    @Autowired
    private ChangeStreamService<Document> changeStreamService;

    @Autowired
    private ApplicationEventService<Document> applicationEventService;
    
	@Autowired
	private String podName;

    private void createIndex(MongoCollection<Document> coll) {
        coll.createIndex(Indexes.descending(INDEX_KEY),
                new IndexOptions().expireAfter(maxTimeout, TimeUnit.MILLISECONDS).name(INDEX_NAME));
    }

    ChangeStream<Document> cs;

    @PostConstruct
    private void init() {
        MongoCollection<Document> coll = mongoTemplate.getCollection(collection);
        try {
            createIndex(coll);
        } catch (MongoCommandException e) {
            if (e.getErrorCode() == 85 || e.getErrorCode() == 86) {
                coll.dropIndex(INDEX_NAME);
                createIndex(coll);
            }
        }
        this.instances.addAll(coll.find().projection(Projections.include("_id")).map(d->d.getString("_id")).into(new ArrayList<>()));
        cs = ChangeStream.of("discovery", Mode.BOARDCAST,
                List.of(Aggregates.match(Filters.in("operationType", List.of("insert", "update", "delete"))))).fullDocumentBeforeChange(FullDocumentBeforeChange.REQUIRED);
        changeStreamService.run(ChangeStreamRegistry.<Document>builder().collectionName(collection).body(e -> {
            ApplicationEvent<Document> event = ApplicationEvent.<Document>builder().name(e.getOperationType().name()).key(e.getDocumentKey().getString("_id").getValue()).updateDescription(e.getUpdateDescription()).build();
            switch (e.getOperationType()) {
                case INSERT:
                    instances.add(event.getKey());
                    event.setDocument(e.getFullDocument());
                    break;
                case UPDATE:
                    break;
                case DELETE:
                    instances.remove(event.getKey());
                    event.setDocument(e.getFullDocumentBeforeChange());
                    break;
                default:
            }
            applicationEventService.publish(event);
        }).changeStream(cs).build());
    }

    @PreDestroy
    private void clear() {
        if (cs != null)
            cs.setRunning(false);
    }

    @Scheduled(fixedRateString = "${settings.instances.heartbeat.interval}")
    private void heartbeat() {
        mongoTemplate.getCollection(collection).updateOne(Filters.eq("_id", podName),
                Updates.combine(Updates.set("_id", podName), Updates.set(INDEX_KEY, new Date())),
                new UpdateOptions().upsert(true));
    }
}