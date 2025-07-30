package com.mongodb.javabasic.controller;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.mongodb.ClientSessionOptions;
import com.mongodb.ReadConcern;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.TransactionBody;
import com.mongodb.client.model.Filters;
import com.mongodb.javabasic.service.AggregationService;

@RestController
@RequestMapping(path = "/bets")
public class BetController {
        private final Logger logger = LoggerFactory.getLogger(getClass());
        @Autowired
        private MongoClient client;
        @Autowired
        private MongoTemplate mongoTemplate;
        @Autowired
        private AggregationService aggregationService;

        @GetMapping("/place")
        public String placeBets(@RequestParam(required = false, defaultValue = "100000") double noOfBets) {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                ExecutorService es = Executors.newFixedThreadPool(500);
                for (int i = 0; i < noOfBets; i++) {
                        final String betId = i + "-" + Math.random() * 1000;
                        es.execute(new Runnable() {
                                @Override
                                public void run() {
                                        final ClientSession clientSession = client.startSession(ClientSessionOptions
                                                        .builder().causallyConsistent(true).build());
                                        try {
                                                clientSession.withTransaction(new TransactionBody<String>() {
                                                        public String execute() {
                                                                Document doc = new Document("betId", betId)
                                                                                .append("cAt",
                                                                                                new Date());
                                                                mongoTemplate.getCollection("bets")
                                                                                .insertOne(clientSession, doc);
                                                                mongoTemplate.getCollection("outstandingBets").insertOne(clientSession,
                                                                 doc);
                                                                return null;
                                                        }
                                                }, TransactionOptions.builder()
                                                                .readConcern(ReadConcern.MAJORITY)
                                                                .writeConcern(WriteConcern.MAJORITY)
                                                                .build());
                                        } catch (RuntimeException e) {
                                                e.printStackTrace();
                                        } finally {
                                                clientSession.close();
                                        }

                                }
                        });
                }
                es.shutdown();
                try {
                        boolean finished = es.awaitTermination(1, TimeUnit.HOURS);
                } catch (InterruptedException e) {
                        e.printStackTrace();
                } finally {
                        if (!es.isTerminated()) {
                                es.shutdownNow();
                        }
                }
                stopWatch.stop();
                logger.info("Total time taken for " + noOfBets + " bets: "
                                + stopWatch.getTotalTimeMillis() + " ms");
                logger.info("Average time per bet: " + (stopWatch.getTotalTimeMillis() / noOfBets)
                                + " ms");
                logger.info("TPS: " + (noOfBets / (stopWatch.getTotalTimeMillis() / 1000.0))
                                + " bets/sec");

                // Wait for outstanding bets to be processed
                try {
                        while (true) {
                                Thread.sleep(20000);
                                long count = mongoTemplate.getCollection("outstandingBets").countDocuments();
                                if (count == 0) {
                                        break;
                                }

                        }
                        aggregationService.getPipelineResults("collations", "collation_final.json", Document.class);
                } catch (Exception e) {
                        e.printStackTrace();
                }

                return new Document("noOfBets", noOfBets)
                                .append("totalTimeMillis", stopWatch.getTotalTimeMillis())
                                .append("averageTimePerBet", (stopWatch.getTotalTimeMillis() / noOfBets))
                                .append("tps", (noOfBets / (stopWatch.getTotalTimeMillis() / 1000.0)))
                                .toJson();
        }

        @GetMapping("/live-watch")
        public ResponseEntity<StreamingResponseBody> liveWatchBets() {
                StreamingResponseBody responseBody = outputStream -> {
                        outputStream.write(""
                                        .getBytes());
                        outputStream.flush();
                };
                return ResponseEntity.status(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(responseBody);
        }

        @GetMapping("/watch")
        public void watchBets() {
                mongoTemplate.getCollection("bets").deleteMany(Filters.empty());
                mongoTemplate.getCollection("collations").deleteMany(Filters.empty());
                mongoTemplate.getCollection("outstandingBets").deleteMany(Filters.empty());
                ExecutorService es = Executors.newFixedThreadPool(500);
                CompletableFuture.supplyAsync(() -> {
                        mongoTemplate.getCollection("bets").watch()
                                        .batchSize(10000)
                                        .maxAwaitTime(2000, TimeUnit.MILLISECONDS)
                                        .forEach(changeStreamDocument -> {
                                                es.execute(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                                final ClientSession clientSession = client
                                                                                .startSession(ClientSessionOptions
                                                                                                .builder()
                                                                                                .causallyConsistent(
                                                                                                                true)
                                                                                                .build());

                                                                //mongoTemplate.getCollection("outstandingBets").withReadConcern(ReadConcern.MAJORITY).withWriteConcern(WriteConcern.MAJORITY)
                                                                //                .insertOne(clientSession,
                                                                //                                changeStreamDocument
                                                                //                                                .getFullDocument());
                                                                // Call PMU.COL

                                                                try {
                                                                        clientSession.withTransaction(
                                                                                        new TransactionBody<String>() {
                                                                                                public String execute() {
                                                                                                        Date pAt = new Date();
                                                                                                        mongoTemplate.getCollection(
                                                                                                                        "outstandingBets")
                                                                                                                        .deleteOne(clientSession,
                                                                                                                                        Filters.eq("betId",
                                                                                                                                                        changeStreamDocument
                                                                                                                                                                        .getFullDocument()
                                                                                                                                                                        .getString("betId")));
                                                                                                        mongoTemplate.getCollection(
                                                                                                                        "collations")
                                                                                                                        .insertOne(clientSession,
                                                                                                                                        changeStreamDocument
                                                                                                                                                        .getFullDocument()
                                                                                                                                                        .append("collatedAt",
                                                                                                                                                                        pAt)
                                                                                                                                                        .append("diff",
                                                                                                                                                                        pAt.getTime()
                                                                                                                                                                                        - changeStreamDocument
                                                                                                                                                                                                        .getFullDocument()
                                                                                                                                                                                                        .getDate("cAt")
                                                                                                                                                                                                        .getTime()));
                                                                                                        return null;
                                                                                                }
                                                                                        }, TransactionOptions.builder()
                                                                                                        .readConcern(ReadConcern.MAJORITY)
                                                                                                        .writeConcern(WriteConcern.MAJORITY)
                                                                                                        .build());
                                                                } catch (RuntimeException e) {
                                                                        e.printStackTrace();
                                                                } finally {
                                                                        clientSession.close();
                                                                }
                                                        }
                                                });
                                        });
                        return null;
                });
        }
}
