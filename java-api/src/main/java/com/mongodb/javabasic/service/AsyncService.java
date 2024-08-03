package com.mongodb.javabasic.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mongodb.javabasic.model.Stat;

@Service
public class AsyncService<T> {
    
    @Async
    public CompletableFuture<Stat<T>> load(List<T> entities, Function<List<T>, Stat<T>> callback){
        return CompletableFuture.completedFuture(callback.apply(entities));
    }
}
