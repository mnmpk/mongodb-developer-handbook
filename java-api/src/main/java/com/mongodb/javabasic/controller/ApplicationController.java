package com.mongodb.javabasic.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.javabasic.model.CustomEntity;
import com.mongodb.javabasic.model.Stat;
import com.mongodb.javabasic.model.Workload;
import com.mongodb.javabasic.repositories.CustomEntityRepository;

@RestController
@RequestMapping(path = "/")
public class ApplicationController {
        private final Logger logger = LoggerFactory.getLogger(getClass());
        @Autowired
        private MongoTemplate mongoTemplate;
        @Autowired
        private CustomEntityRepository repository;

        @GetMapping
        public String health() {
                return "OK";
        }

        // Simulate CPU bound tasks: smaller wait time, TPS result should larger with
        // less threads
        // Simulate IO bound tasks: larger wait time, TPS result should larger with more
        // threads
        @GetMapping("/threads")
        public Stat<Object> threads(@RequestParam(required = false, defaultValue = "100") int noOfThreads,
                        @RequestParam(required = false, defaultValue = "10000") int noOfTasks,
                        @RequestParam(required = false, defaultValue = "10") int serviceTime,
                        @RequestParam(required = false, defaultValue = "20") int waitTime) {
                ExecutorService es = Executors.newFixedThreadPool(noOfThreads);
                StopWatch stopWatch = new StopWatch();
                Date startAt = new Date();
                stopWatch.start();
                for (int i = 0; i < noOfTasks; i++) {
                        es.execute(new Runnable() {
                                @Override
                                public void run() {
                                        try {
                                                // Simulate service time
                                                long start = new Date().getTime();
                                                while (new Date().getTime() - start < serviceTime) {
                                                        double result = Math.sqrt(Math.pow(Math.random(), 2)
                                                                        + Math.pow(Math.random(), 2));
                                                }
                                                // Simulate wait time
                                                Thread.sleep(waitTime);
                                        } catch (InterruptedException e) {
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                        }
                                }
                        });
                }
                es.shutdown();
                try {
                        boolean finished = es.awaitTermination(1, TimeUnit.HOURS);
                        stopWatch.stop();
                        if (finished) {
                                logger.info("Total time taken for " + noOfTasks + " tasks: "
                                                + stopWatch.getTotalTimeMillis() + " ms");
                                logger.info("Average time per task: " + (stopWatch.getTotalTimeMillis() / noOfTasks)
                                                + " ms");
                                logger.info("TPS: " + (noOfTasks / (stopWatch.getTotalTimeMillis() / 1000.0))
                                                + " tasks/sec");
                                return Stat.builder()
                                                .startAt(startAt)
                                                .endAt(new Date())
                                                .workload(Workload.builder().noOfWorkers(noOfThreads)
                                                                .quantity(noOfTasks).build())
                                                .duration(stopWatch.getTotalTimeMillis())
                                                .avgLatency(stopWatch.getTotalTimeMillis() / noOfTasks)
                                                .operationPerSecond(noOfTasks/ stopWatch.getTotalTimeSeconds())
                                                .build();
                        } else {
                                logger.warn("Not all tasks finished within the timeout.");
                        }
                } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                } finally {
                        if (!es.isTerminated()) {
                                es.shutdownNow();
                        }
                }
                return null;
        }

        @GetMapping("/test")
        public String test() {
this.processDelayMsg(123456789, 10, 2);
                /*logger.info(mongoTemplate.getDb().getReadConcern().toString());
                logger.info(mongoTemplate.getDb().getReadPreference().toString());
                logger.info(mongoTemplate.getDb().getWriteConcern().toString());
                for (int i = 0; i < 1000; i++) {
                        t();
                }*/
                return "OK";
        }
        public void processDelayMsg(long matchId, int currentSeq, int delay) {
                MongoCollection<Document> collection = mongoTemplate.getCollection("matchDelay");

                collection.updateOne(
                        Filters.eq("_id", new Document("matchId", matchId).append("v", 1)),
                        Arrays.asList(new Document("$set", new Document()
                        .append("delay", new Document("$cond", new Document()
                                .append("if", new Document("$lt", Arrays.asList("$dSeq", currentSeq)))
                                .append("then", delay)
                                .append("else", "$delay")))
                        .append("dSeq", new Document("$cond", new Document()
                                .append("if", new Document("$lt", Arrays.asList("$dSeq", currentSeq)))
                                .append("then", currentSeq)
                                .append("else", "$dSeq")))
                        .append("uAt", new Document("$cond", new Document()
                                .append("if", new Document("$lt", Arrays.asList("$dSeq", currentSeq)))
                                .append("then", new Date())
                                .append("else", "$uAt"))))),
                        new UpdateOptions().upsert(true));
        }
        public void processStatusMsg(long matchId, int currentSeq, String status, int[] poolsId) {
                MongoCollection<Document> collection = mongoTemplate.getCollection("matchDelay");

                collection.updateOne(
                        Filters.eq("_id", new Document("matchId", matchId).append("v", 1)),
                        Arrays.asList(new Document("$set", new Document()
                        .append("status", new Document("$cond", new Document()
                                .append("if", new Document("$lt", Arrays.asList("$sSeq", currentSeq)))
                                .append("then", status)
                                .append("else", "$status")))
			    .append("pools", new Document("$cond", new Document()
                                .append("if", new Document("$lt", Arrays.asList("$sSeq", currentSeq)))
                                .append("then", poolsId)
                                .append("else", "$pools")))

                        .append("sSeq", new Document("$cond", new Document()
                                .append("if", new Document("$lt", Arrays.asList("$sSeq", currentSeq)))
                                .append("then", currentSeq)
                                .append("else", "$sSeq")))
                        .append("uAt", new Document("$cond", new Document()
                                .append("if", new Document("$lt", Arrays.asList("$sSeq", currentSeq)))
                                .append("then", new Date())
                                .append("else", "$uAt"))))),
                        new UpdateOptions().upsert(true));
        }
public void processDefaultDelayMsg(long matchId, int currentSeq, int delay, int allUpDelay) {
                MongoCollection<Document> collection = mongoTemplate.getCollection("defaultDelay");

                collection.updateOne(
                        Filters.eq("_id", new Document("matchId", matchId).append("v", 1)),
                        Arrays.asList(new Document("$set", new Document()
                        .append("delay", new Document("$cond", new Document()
                                .append("if", new Document("$lt", Arrays.asList("$seq", currentSeq)))
                                .append("then", delay)
                                .append("else", "$delay")))
			    .append("allUpDelay", new Document("$cond", new Document()
                                .append("if", new Document("$lt", Arrays.asList("$seq", currentSeq)))
                                .append("then", allUpDelay)
                                .append("else", "$allUpDelay")))

                        .append("seq", new Document("$cond", new Document()
                                .append("if", new Document("$lt", Arrays.asList("$seq", currentSeq)))
                                .append("then", currentSeq)
                                .append("else", "$seq")))
                        .append("uAt", new Document("$cond", new Document()
                                .append("if", new Document("$lt", Arrays.asList("$seq", currentSeq)))
                                .append("then", new Date())
                                .append("else", "$uAt"))))),
                        new UpdateOptions().upsert(true));
        }
        public void processStatusMsg(long matchId, int currentSeq, int delay) {
                MongoCollection<Document> collection = mongoTemplate.getCollection("matchDelay");

                collection.updateOne(
                        Filters.eq("_id", new Document("matchId", matchId).append("v", 1)),
                        Arrays.asList(new Document("$set", new Document()
                        .append("delay", new Document("$cond", new Document()
                                .append("if", new Document("$lt", Arrays.asList("$dSeq", currentSeq)))
                                .append("then", delay)
                                .append("else", "$delay")))
                        .append("dSeq", new Document("$cond", new Document()
                                .append("if", new Document("$lt", Arrays.asList("$dSeq", currentSeq)))
                                .append("then", currentSeq)
                                .append("else", "$dSeq")))
                        .append("uAt", new Document("$cond", new Document()
                                .append("if", new Document("$lt", Arrays.asList("$dSeq", currentSeq)))
                                .append("then", new Date())
                                .append("else", "$uAt"))))),
                        new UpdateOptions().upsert(true));
        }

        private void t() {
                List<CustomEntity> l = repository.findAll();

                CustomEntity ce = new CustomEntity();
                if (l.size() > 0)
                        ce = l.get(0);

                logger.info("***Original entity***:" + ce.toString());
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                map.put("test", "test" + Math.random());
                ce.setData(map);
                CustomEntity newCE = repository.save(ce);
                logger.info("***entity returned by save***:" + newCE.toString());

                CustomEntity queryCE = repository.findById(newCE.getId()).get();
                logger.info("***find same entity by id***:" + queryCE.toString());
                logger.info(
                                "***Compare value***: old - " + ce.getData().get("test") + " new - "
                                                + queryCE.getData().get("test"));
                if (ce.getData().containsKey("test") && queryCE.getData().containsKey("test"))
                        logger.info("equal? " + ce.getData().get("test").equals(queryCE.getData().get("test")));
                else
                        logger.info("************************** Missing value");
        }

        // spring vs mongodb driver:
        // spring data repo/spring helper/mongodb driver
        // spring converter/mongodb driver codec

        // Operations:
        // insert/update/delete/replace
        // Option:
        // bulk/distributed/write concern

        // Transaction:
        // Insert+Update/Validate+Update
        // Seach:

        // Cache using MongoDB

        // Search
        // Create index

        // Application framework:
        // RBAC, FBAC, DBAC
        // Application Parameters
        // Translations
        // Stream processing
}
