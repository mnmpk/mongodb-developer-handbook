package com.mongodb.javabasic.service.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteInsert;
import com.mongodb.bulk.BulkWriteUpsert;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.Facet;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.javabasic.model.Stat;
import com.mongodb.javabasic.model.User;
import com.mongodb.javabasic.model.Workload;
import com.mongodb.javabasic.model.Workload.Converter;
import com.mongodb.javabasic.service.UserService;

@Service("userService")
public class DriverUserService extends UserService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    MongoConverter mongoConverter;

    @Autowired
    private CodecRegistry pojoCodecRegistry;

    @Override
    public Page<User> search(String query, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'search'");
    }

    @Override
    public Stat<Page<User>> list(Workload workload, Pageable pageable) {
        Stat<Page<User>> stat = new Stat<>(User.class);
        time(stat, workload, (v) -> {
            MongoCollection<Document> collection = mongoTemplate
                    .getCollection(mongoTemplate.getCollectionName(User.class));
            List<Bson> pipeline = new ArrayList<>();

            Sort sort = pageable.getSort();
            if (sort != null && sort.isSorted()) {
                List<Bson> orders = new ArrayList<>();
                sort.forEach(o -> {
                    if (o.isAscending()) {
                        orders.add(Sorts.ascending(o.getProperty()));
                    } else {
                        orders.add(Sorts.descending(o.getProperty()));
                    }
                });
                if (orders.size() > 0)
                    pipeline.add(Aggregates.sort(Sorts.orderBy(orders)));
            }
            pipeline.add(Aggregates.facet(new Facet("meta", List.of(Aggregates.count("count"))),
                    new Facet("data", List.of(Aggregates.skip(pageable.getPageNumber() * pageable.getPageSize()),
                            Aggregates.limit(pageable.getPageSize())))));
            Document result = collection.withDocumentClass(Document.class)
                    .aggregate(pipeline).first();

            List<User> list = new ArrayList<User>();
            Stream<Document> s = result.getList("data", Document.class).stream();
            if (Converter.SPRING == workload.getConverter()) {
                list = s.map(d -> mongoConverter.read(User.class, d))
                        .toList();
            } else {
                DecoderContext dc = DecoderContext.builder().build();
                list = s.map(d -> pojoCodecRegistry.get(User.class).decode(d.toBsonDocument().asBsonReader(), dc))
                        .toList();
            }

            stat.setData(List.of(new PageImpl<>(list, pageable,
                    result.getList("meta", Document.class).get(0).getInteger("count"))));

            return null;
        });
        return stat;
    }

    @Override
    public User get(String id) {
        return null;
    }

    @Override
    public User create(User entity) {
        return null;
    }

    @Override
    public void delete(String id) {
    }

    @Override
    public User update(User entity) {
        return null;
    }

    @Override
    public Stat<User> _load(List<User> entities, Workload workload) {
        Stat<User> stat = new Stat<>(User.class);
        MongoCollection<User> collection = mongoTemplate.getCollection(mongoTemplate.getCollectionName(User.class))
                .withWriteConcern(WriteConcern.valueOf(workload.getWriteConcern().name()))
                .withDocumentClass(User.class);
        if (workload.isBulk()) {

            time(stat, workload, (v) -> {
                switch (workload.getOperationType()) {
                    case DELETE:
                        collection.bulkWrite(entities.stream()
                                .map(e -> new DeleteOneModel<User>(Filters.eq("_id", new ObjectId(e.getId()))))
                                .toList());
                        break;
                    case INSERT:
                        List<BulkWriteInsert> inserts = collection
                                .bulkWrite(entities.stream().map(e -> new InsertOneModel<>(e)).toList()).getInserts();
                        for (int i = 0; i < inserts.size(); i++) {
                            entities.get(i).setId(inserts.get(i).getId().asObjectId().getValue().toHexString());
                        }
                        break;
                    case REPLACE:
                        List<BulkWriteUpsert> newReplaces = collection
                                .bulkWrite(
                                        entities.stream()
                                                .map(e -> new ReplaceOneModel<>(
                                                        Filters.eq("_id", new ObjectId(e.getId())),
                                                        e, new ReplaceOptions().upsert(true)))
                                                .toList())
                                .getUpserts();
                        for (int i = 0; i < newReplaces.size(); i++) {
                            entities.get(i).setId(newReplaces.get(i).getId().asObjectId().getValue().toHexString());
                        }
                        break;
                    case UPDATE:
                        List<BulkWriteUpsert> upserts = collection
                                .bulkWrite(
                                        entities.stream()
                                                .map(e -> new UpdateOneModel<User>(
                                                        Filters.eq("_id", new ObjectId(e.getId())),
                                                        Updates.combine(Updates.inc("v", 1)),
                                                        new UpdateOptions().upsert(true)))
                                                .toList())
                                .getUpserts();
                        for (int i = 0; i < upserts.size(); i++) {
                            entities.get(i).setId(upserts.get(i).getId().asObjectId().getValue().toHexString());
                        }
                        break;
                }
                stat.setData(entities);
                return null;
            });
        } else {
            List<User> newEntities = new ArrayList<>();
            for (User e : entities) {
                time(stat, workload, (v) -> {
                    // TODO:update, replace, delete
                    e.setId(collection.insertOne(e).getInsertedId().asObjectId().getValue().toHexString());
                    newEntities.add(e);
                    return null;
                });
            }
            stat.setData(newEntities);
        }
        return stat;
    }

}
