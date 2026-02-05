package com.mongodb.javabasic.config;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

@Profile("scheduler")
@Configuration
@EnableScheduling
public class ScheduleConfig {
    private static final String INDEX_KEY = "at";
    private static final String INDEX_NAME = "ttl";

    @Value("${settings.instance-group.collection}")
    private String collection;
    @Value("${settings.instance-group.maxTimeout}")
    private long maxTimeout;

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    private void init() {
        mongoTemplate.getCollection(collection).createIndex(Indexes.descending(INDEX_KEY),
                new IndexOptions().expireAfter(maxTimeout, TimeUnit.MILLISECONDS).name(INDEX_NAME));

    }

    @Scheduled(fixedRateString = "${settings.instance-group.heartbeat.interval}")
    private void heartbeat() {
        String podName = System.getenv("HOSTNAME");
        mongoTemplate.getCollection(collection).updateOne(Filters.eq("_id", podName),
                Updates.combine(Updates.set("_id", podName), Updates.set(INDEX_KEY, new Date())),
                new UpdateOptions().upsert(true));
    }
}
