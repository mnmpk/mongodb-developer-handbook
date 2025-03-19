package com.mongodb.javabasic.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;

import com.mongodb.DuplicateKeyException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Updates;

import lombok.Data;

@Data
public class MongoCache implements Cache {
    private static final long DEFAULT_TTL = TimeUnit.DAYS.toSeconds(30);
    private static final String COLL_NAME_SUFFIX = "_cache";
    private static final String FIELD_CREATED = "created";
    private static final String FIELD_ACCESSED = "accessed";
    private static final String FIELD_EXPIRED_AT = "expireAt";
    private static final String FIELD_HIT = "hit";
    private static final String FIELD_VALUE = "v";
    private static final String FIELD_DOC = "doc";
    private static final String INDEX_KEY = FIELD_EXPIRED_AT;
    private static final String INDEX_NAME = "expireAt";

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final boolean flushOnBoot;
    private final boolean storeBinaryOnly;
    private final String cacheName;
    private final long ttl;
    private final MongoTemplate mongoTemplate;

    private MongoCollection<Document> collection;

    public MongoCache(MongoTemplate mongoTemplate, String cacheName) {
        this(mongoTemplate, cacheName, DEFAULT_TTL);
    }

    public MongoCache(MongoTemplate mongoTemplate, String cacheName, long ttl) {
        this(mongoTemplate, cacheName, DEFAULT_TTL, false, true);
    }

    public MongoCache(MongoTemplate mongoTemplate, String cacheName, long ttl,
            boolean flushOnBoot, boolean storeBinaryOnly) {
        this.mongoTemplate = mongoTemplate;

        this.flushOnBoot = flushOnBoot;
        this.storeBinaryOnly = storeBinaryOnly;
        this.cacheName = cacheName;
        this.ttl = ttl;
        collection = this.mongoTemplate.getCollection(cacheName+COLL_NAME_SUFFIX);

        if (isFlushOnBoot()) {
            clear();
        }
        collection.createIndex(Indexes.ascending(INDEX_KEY),
                new IndexOptions().expireAfter(0l, TimeUnit.SECONDS).name(INDEX_NAME));
    }

    @Override
    public @NonNull String getName() {
        return this.cacheName;
    }

    @Override
    public @NonNull Object getNativeCache() {
        return mongoTemplate;
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        StopWatch watch = new StopWatch();
        watch.start("read cache");
        final Document doc = this.collection.findOneAndUpdate(Filters.eq("_id", key.toString()),
                Updates.combine(Updates.set(FIELD_ACCESSED, Date.from(Instant.now())),
                Updates.inc(FIELD_HIT, 1),
                        Updates.set(FIELD_EXPIRED_AT, Date.from(Instant.now().plusSeconds(this.ttl)))),
                new FindOneAndUpdateOptions()
                        .projection(Projections.fields(Projections.excludeId(), Projections.include(FIELD_VALUE))));
        watch.stop();
        if (doc != null) {
            try {
                watch.start("deserialize");
                SimpleValueWrapper v = new SimpleValueWrapper(deserialize(doc.get(FIELD_VALUE, Binary.class).getData()));
                watch.stop();
                logger.info(watch.prettyPrint());
                return v;
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.info(watch.prettyPrint());
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(@NonNull Object key, @Nullable Class<T> type) {
        ValueWrapper wrapper = get(key);
        if (wrapper == null) {
            return null;
        }
        return (T) wrapper.get();
    }

    @Override
    public void put(@NonNull Object key, @Nullable Object value) {
        try {
            StopWatch watch = new StopWatch();
            watch.start("cache");
            Date created = Date.from(Instant.now());
            Date expireAt = Date.from(Instant.now().plusSeconds(this.ttl));
            Document doc = new Document("_id", key.toString()).append(FIELD_VALUE, serialize(value)).append(FIELD_CREATED, created)
                    .append(FIELD_ACCESSED, created).append(FIELD_HIT, 1)
                    .append(FIELD_EXPIRED_AT, expireAt);
            if (!storeBinaryOnly)
                doc.append(FIELD_DOC, value);
            this.collection.replaceOne(Filters.eq("_id", key.toString()),
                    doc,
                    new ReplaceOptions().upsert(true));
            watch.stop();
            logger.info(watch.prettyPrint());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public ValueWrapper putIfAbsent(@NonNull Object key, @Nullable Object value) {
        try {
            Date created = Date.from(Instant.now());
            Date expireAt = Date.from(Instant.now().plusSeconds(this.ttl));
            Document doc = new Document("_id", key.toString()).append(FIELD_VALUE, serialize(value)).append(FIELD_CREATED, created)
                    .append(FIELD_ACCESSED, created).append(FIELD_HIT, 1)
                    .append(FIELD_EXPIRED_AT, expireAt);
            if (!storeBinaryOnly)
                doc.append(FIELD_DOC, value);
            StopWatch watch = new StopWatch();
            watch.start("cache");
            this.collection.insertOne(doc);
            watch.stop();
            logger.info(watch.prettyPrint());
            return new SimpleValueWrapper(value);
        } catch (DuplicateKeyException e) {
            logger.info(String.format("Key: %s already exists in the cache. Element will not be replaced.", key), e);
            return get(key);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void evict(@NonNull Object key) {
        StopWatch watch = new StopWatch();
        watch.start("evict cache");
        this.collection.deleteOne(Filters.eq("_id", key.toString()));
        watch.stop();
        logger.info(watch.prettyPrint());
    }

    @Override
    public void clear() {
        StopWatch watch = new StopWatch();
        watch.start("clear cache");
        this.collection.drop();
        watch.stop();
        logger.info(watch.prettyPrint());
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        Assert.isTrue(key instanceof String, "'key' must be an instance of 'java.lang.String'.");
        Assert.notNull(valueLoader, "'valueLoader' must not be null");

        Object cached = get(key);
        if (cached != null) {
            return (T) cached;
        }

        final Object dynamicLock = ((String) key).intern();

        synchronized (dynamicLock) {
            cached = get(key);
            if (cached != null) {
                return (T) cached;
            }

            T value;
            try {
                value = valueLoader.call();
            } catch (Throwable ex) {
                throw new ValueRetrievalException(key, valueLoader, ex);
            }

            ValueWrapper newCachedValue = putIfAbsent(key, value);
            if (newCachedValue != null) {
                return (T) newCachedValue.get();
            } else {
                return value;
            }
        }
    }

    private byte[] serialize(Object value) throws IOException {
        try (final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                final ObjectOutputStream output = new ObjectOutputStream(buffer)) {
            output.writeObject(value);
            return buffer.toByteArray();
        }
    }

    private Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (final ByteArrayInputStream buffer = new ByteArrayInputStream(data);
                final ObjectInputStream output = new ObjectInputStream(buffer)) {
            return output.readObject();
        }
    }
}
