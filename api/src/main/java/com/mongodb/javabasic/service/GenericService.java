package com.mongodb.javabasic.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public abstract class GenericService<T> {
    @Autowired
    private AsyncService<T> asyncService;

    public abstract Page<T> search(String query, Pageable pageable);

    public abstract Page<T> list(Pageable pageable);

    public abstract T get(String id);

    public abstract T create(T entity);

    public abstract void delete(String id);

    public abstract T update(T entity);
    
    public abstract List<T> _bulk(List<T> entities);

    public List<T> bulk(List<T> entities, int noOfThreads) {

        var ends = new ArrayList<CompletableFuture<List<T>>>();
        int pageSize = entities.size() / noOfThreads;
        if (pageSize <= 0) {
            pageSize = 1;
        }
        int accPages = entities.size() / pageSize;
        for (int pageIdx = 0; pageIdx <= accPages; pageIdx++) {
            int fromIdx = pageIdx * pageSize;
            int toIdx = Math.min(entities.size(), (pageIdx + 1) * pageSize);
            var subList = entities.subList(fromIdx, toIdx);

            ends.add(asyncService.bulk(subList, (List<T> l)-> _bulk(l)));
            //ends.add(
            //    CompletableFuture.supplyAsync(() -> {
            //        return _bulk(subList);
            //    }));

            if (toIdx == entities.size()) {
                break;
            }
        }
        CompletableFuture<Void> allFuturesResult = CompletableFuture
                .allOf(ends.toArray(new CompletableFuture<?>[ends.size()]));

        List<List<T>> list = allFuturesResult
                .thenApply(v -> ends.stream().map(CompletableFuture::join).collect(Collectors.toList())).join();
        return list.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

}
