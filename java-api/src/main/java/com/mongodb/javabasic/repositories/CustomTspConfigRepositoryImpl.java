package com.mongodb.javabasic.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.javabasic.model.TspConfig;

public class CustomTspConfigRepositoryImpl implements CustomTspConfigRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<TspConfig> getConfig(List<Entry<String, List<String>>> entries) {
        List<Bson> filters = new ArrayList<>();
        for (Entry<String, List<String>> entry : entries) {
            filters.add(Filters.and(
                    Filters.eq("params.key", entry.getKey()),
                    Filters.in("params.value", entry.getValue())));
        }
        MongoCollection<TspConfig> coll = mongoTemplate.getDb()
                .getCollection(mongoTemplate.getCollectionName(TspConfig.class), TspConfig.class);
        return coll.aggregate(
                List.of(Aggregates.match(Filters.and(filters)), Aggregates.project(Projections.excludeId())))
                .into(new ArrayList<>());
    }
}
