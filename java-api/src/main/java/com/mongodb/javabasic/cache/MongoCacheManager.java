package com.mongodb.javabasic.cache;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.lang.NonNull;

public class MongoCacheManager extends AbstractCacheManager {
    private MongoTemplate mongoTemplate;
    private String collectionName;
    private long ttl;
    private boolean flushOnBoot;

    public MongoCacheManager(MongoTemplate mongoTemplate, String collectionName, long ttl, boolean flushOnBoot) {
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
        this.ttl = ttl;
        this.flushOnBoot = flushOnBoot;
    }

    @Override
    protected @NonNull Collection<? extends Cache> loadCaches() {
        final Collection<Cache> caches = new LinkedHashSet<>(16);
        caches.add(new MongoCache(mongoTemplate, collectionName, ttl, flushOnBoot));
        return caches;
    }

}
