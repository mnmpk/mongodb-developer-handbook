package com.mongodb.javabasic.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.javabasic.model.Config;

public class CustomConfigRepositoryImpl<T> implements CustomConfigRepository<T> {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Autowired
        private MongoTemplate mongoTemplate;
        @Autowired
        private CodecRegistry pojoCodecRegistry;

        @Override
        public List<T> getConfig(Class<T> clazz) {
                MongoCollection<T> coll = mongoTemplate.getDb()
                                .getCollection(mongoTemplate.getCollectionName(Config.class), clazz);
                return coll.aggregate(
                                List.of(Aggregates.project(Projections.fields(Projections.excludeId(),
                                                Projections.include("value")))))
                                .into(new ArrayList<>());
        }

        @SuppressWarnings({ "unchecked" })
        @Override
        public List<T> getConfig(List<Entry<String, List<String>>> entries, Class<T> clazz) {
                List<Bson> filters = new ArrayList<>();
                for (Entry<String, List<String>> entry : entries) {
                        filters.add(Filters.and(
                                        Filters.eq("params.key", entry.getKey()),
                                        Filters.in("params.value", entry.getValue())));
                }
                MongoCollection<BsonDocument> coll = mongoTemplate.getDb()
                                .getCollection(mongoTemplate.getCollectionName(Config.class), BsonDocument.class);
                return coll.aggregate(
                                List.of(Aggregates.match(Filters.and(filters)),
                                                Aggregates.project(Projections.fields(Projections.excludeId(),
                                                                Projections.include("value")))))
                                .map(d -> {
                                        if (clazz == Integer.class) {
                                                return (T) Integer.valueOf(d.getInt32("value").getValue());
                                        } else if (clazz == Double.class) {
                                                return (T) Double.valueOf(d.getDouble("value").getValue());
                                        } else if (clazz == String.class) {
                                                return (T) d.getString("value").getValue();
                                        }
                                        return pojoCodecRegistry.get(clazz).decode(
                                                        d.getDocument("value").asBsonReader(),
                                                        DecoderContext.builder().build());
                                }).into(new ArrayList<>());

        }
}
