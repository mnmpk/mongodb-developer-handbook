package com.mongodb.javabasic.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncService<T> {
    
    @Async
    public CompletableFuture<List<T>> bulk(List<T> entities, Function<List<T>, List<T>> callback){
        return CompletableFuture.completedFuture(callback.apply(entities));
    }
}
