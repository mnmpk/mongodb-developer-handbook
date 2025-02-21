package com.mongodb.javabasic.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

import com.mongodb.DuplicateKeyException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReplaceOptions;

import lombok.Data;

@Data
public class MongoCache implements Cache {
    private static final long DEFAULT_TTL = TimeUnit.DAYS.toSeconds(30);
    private static final String INDEX_KEY_NAME = "cAt";
    private static final String INDEX_NAME = "_ttl";

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final boolean flushOnBoot;
    private final boolean storeBinaryOnly;
    private final String collectionName;
    private final MongoTemplate mongoTemplate;
    private final long ttl;

    private MongoCollection<Document> collection;

    public MongoCache(MongoTemplate mongoTemplate, String collectionName) {
        this(mongoTemplate, collectionName, DEFAULT_TTL);
    }

    public MongoCache(MongoTemplate mongoTemplate, String collectionName, long ttl) {
        this(mongoTemplate, collectionName, DEFAULT_TTL, false, true);
    }

    public MongoCache(MongoTemplate mongoTemplate, String collectionName, long ttl,
            boolean flushOnBoot, boolean storeBinaryOnly) {
        this.mongoTemplate = mongoTemplate;

        this.flushOnBoot = flushOnBoot;
        this.storeBinaryOnly = storeBinaryOnly;
        this.collectionName = collectionName;
        collection = this.mongoTemplate.getCollection(collectionName);
        this.ttl = ttl;

        if (isFlushOnBoot()) {
            clear();
        }
        collection.createIndex(Indexes.ascending(INDEX_KEY_NAME),
                new IndexOptions().expireAfter(ttl, TimeUnit.SECONDS).name(INDEX_NAME));
        collection.listIndexes().forEach(index -> {
            if (index.getString("name").equals(collectionName) && index.getLong("expireAfterSeconds") != ttl) {
                collection.dropIndex(INDEX_NAME);
            }
        });
    }

    @Override
    public @NonNull String getName() {
        return this.collectionName;
    }

    @Override
    public @NonNull Object getNativeCache() {
        return mongoTemplate;
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        final Document doc = this.collection.find(Filters.eq("_id", key.hashCode()))
                .projection(Projections.fields(Projections.excludeId(), Projections.include("v"))).first();
        if (doc != null) {
            try {
                return new SimpleValueWrapper(deserialize(doc.get("v", Binary.class).getData()));
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
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
            Document doc = new Document("_id", key.hashCode()).append("v", serialize(value)).append("cAt", new Date());
            if (!storeBinaryOnly)
                doc.append("doc", value);
            this.collection.replaceOne(Filters.eq("_id", key.hashCode()),
                    doc,
                    new ReplaceOptions().upsert(true));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public ValueWrapper putIfAbsent(@NonNull Object key, @Nullable Object value) {
        try {
            Document doc = new Document("_id", key.hashCode()).append("v", serialize(value)).append("cAt", new Date());
            if (!storeBinaryOnly)
                doc.append("doc", value);
            this.collection.insertOne(doc);
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
        this.collection.deleteOne(Filters.eq("_id", key.hashCode()));
    }

    @Override
    public void clear() {
        this.collection.drop();
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
