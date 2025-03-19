package com.mongodb.javabasic.cache;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.lang.NonNull;

import com.mongodb.javabasic.model.CacheConfig;

public class MongoCacheManager extends AbstractCacheManager {
    private MongoTemplate mongoTemplate;
    private CacheConfig[] configs;

    public MongoCacheManager(MongoTemplate mongoTemplate, CacheConfig... configs) {
        this.mongoTemplate = mongoTemplate;
        this.configs = configs;
    }

    @Override
    protected @NonNull Collection<? extends Cache> loadCaches() {
        final Collection<Cache> caches = new LinkedHashSet<>(16);
        for (CacheConfig config : configs) {
            caches.add(new MongoCache(mongoTemplate, config.getCacheName(), config.getTtl(), config.isFlushOnBoot(), config.isStoreBinaryOnly()));
        }
        return caches;
    }

}
