package com.mongodb.javabasic.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.StopWatch;

import com.mongodb.client.model.Aggregates;
import com.mongodb.javabasic.model.Stat;
import com.mongodb.javabasic.model.Workload;

public abstract class GenericService<T> {
    @Autowired
    private AsyncService<T> asyncService;
    @Autowired
    private MongoTemplate mongoTemplate;

    public abstract Stat<Page<T>> search(String query, Pageable pageable);

    public T sample(Class<T> clazz) {
        return mongoTemplate
                .getCollection(mongoTemplate.getCollectionName(clazz)).withDocumentClass(clazz)
                .aggregate(List.of(Aggregates.sample(1))).first();
    }

    public abstract Stat<Page<T>> list(Workload workload, Pageable pageable);

    public abstract Stat<T> _load(List<T> entities, Workload workload);

    public Stat<T> load(List<T> entities, Workload workload) {
        Stat<T> stat = new Stat<>();
        var ends = new ArrayList<CompletableFuture<Stat<T>>>();
        int pageSize = entities.size() / workload.getNoOfWorkers();
        if (pageSize <= 0) {
            pageSize = 1;
        }
        int accPages = entities.size() / pageSize;
        for (int pageIdx = 0; pageIdx <= accPages; pageIdx++) {
            int fromIdx = pageIdx * pageSize;
            int toIdx = Math.min(entities.size(), (pageIdx + 1) * pageSize);
            var subList = entities.subList(fromIdx, toIdx);

            ends.add(asyncService.load(subList, (List<T> l) -> _load(l, workload)));
            // ends.add(
            // CompletableFuture.supplyAsync(() -> {
            // return _bulk(subList);
            // }));

            if (toIdx == entities.size()) {
                break;
            }
        }

        time(stat, workload, (v) -> {
            CompletableFuture<Void> allFuturesResult = CompletableFuture
                    .allOf(ends.toArray(new CompletableFuture<?>[ends.size()]));
            List<Stat<T>> list = allFuturesResult
                    .thenApply(v2 -> ends.stream().map(CompletableFuture::join).collect(Collectors.toList())).join();
            List<T> data = new ArrayList<>();
            list.stream().forEach(s -> {
                data.addAll(s.getData());
                if (stat.getMaxLatency() == 0)
                    stat.setMaxLatency(s.getMaxLatency());
                stat.setMinLatency(Math.min(stat.getMinLatency(), s.getMinLatency()));
                stat.setMaxLatency(Math.max(stat.getMaxLatency(), s.getMaxLatency()));
                if (stat.getFields() == null)
                    stat.setFields(s.getFields());
            });
            stat.setData(data);
            return null;
        });
        return stat;
    }

    public void time(Stat<?> stat, Workload workload, Function<Void, Void> function) {
        stat.setWorkload(Workload.builder().implementation(workload.getImplementation()).type(workload.getType())
                .converter(workload.getConverter()).bulk(workload.isBulk()).writeConcern(workload.getWriteConcern())
                .operationType(workload.getOperationType())
                .collection(workload.getCollection())
                .noOfWorkers(workload.getType() == Workload.Type.READ ? 1 : workload.getNoOfWorkers())
                .quantity(workload.getQuantity()).build());
        stat.setStartAt(new Date());
        StopWatch sw = new StopWatch();
        sw.start();
        function.apply(null);
        sw.stop();

        if (stat.getMinLatency() == 0) {
            stat.setMinLatency(sw.getTotalTimeMillis());
        }
        stat.setMinLatency(Math.min(stat.getMinLatency(), sw.getTotalTimeMillis()));
        stat.setMaxLatency(Math.max(stat.getMaxLatency(), sw.getTotalTimeMillis()));
        stat.setDuration(stat.getDuration() + sw.getTotalTimeMillis());
        stat.setEndAt(new Date());
    }
}
