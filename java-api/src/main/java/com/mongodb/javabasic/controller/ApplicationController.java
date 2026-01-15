package com.mongodb.javabasic.controller;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.javabasic.model.Stat;
import com.mongodb.javabasic.model.Workload;
@RestController
@RequestMapping(path = "/")
public class ApplicationController {
        private final Logger logger = LoggerFactory.getLogger(getClass());

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
                                                .operationPerSecond(noOfTasks / stopWatch.getTotalTimeSeconds())
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

        // spring vs mongodb driver:
        // spring data repo/spring helper/mongodb driver
        // spring converter/mongodb driver codec

        // Operations:
        // insert/update/delete/replace
        // Option:
        // bulk/distributed/write concern

        // Transaction:
        // Insert+Update/Validate+Update
        // Search:

        // Cache using MongoDB

        // Search
        // Create index

        // Application framework:
        // RBAC, FBAC, DBAC
        // Application Parameters
        // Translations
        // Stream processing
}
