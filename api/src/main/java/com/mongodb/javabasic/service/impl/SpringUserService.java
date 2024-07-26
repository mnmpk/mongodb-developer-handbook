package com.mongodb.javabasic.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.mongodb.bulk.BulkWriteInsert;
import com.mongodb.bulk.BulkWriteUpsert;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.javabasic.model.Stat;
import com.mongodb.javabasic.model.User;
import com.mongodb.javabasic.model.Workload;
import com.mongodb.javabasic.repositories.UserRepository;
import com.mongodb.javabasic.service.UserService;

@Service("springUserService")
public class SpringUserService extends UserService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MongoTemplate template;

    @Override
    public Page<User> search(String query, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'search'");
    }

    @Override
    public Page<User> list(Pageable pageable) {
        return null;
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
        Stat<User> stat = new Stat<>();
        stat.setWorkload(Workload.builder().implementation(workload.getImplementation())
                .converter(workload.getConverter()).bulk(workload.isBulk()).writeConcern(workload.getWriteConcern())
                .operationType(workload.getOperationType())
                .collection(workload.getCollection()).noOfWorkers(1)
                .quantity(entities.size()).build());
        stat.setStartAt(new Date());
        long min = 0;
        long max = 0;
        long total = 0;
        if (workload.isBulk()) {
            StopWatch sw = new StopWatch();
            sw.start();
            switch (workload.getOperationType()) {
                case DELETE:
                    BulkOperations bulkRemoves = template.bulkOps(BulkMode.ORDERED, User.class);
                    entities.stream().forEach(e -> {
                        bulkRemoves.remove(new Query(Criteria.where("_id").is(e.getId())));
                    });
                    bulkRemoves.execute();
                    break;
                case INSERT:
                    BulkOperations bulkInserts = template.bulkOps(BulkMode.ORDERED, User.class);
                    entities.stream().forEach(e -> {
                        bulkInserts.insert(e);
                    });
                    List<BulkWriteInsert> inserts = bulkInserts.execute().getInserts();
                    for (int i = 0; i < inserts.size(); i++) {
                        entities.get(i).setId(inserts.get(i).getId().asObjectId().getValue().toHexString());
                    }
                    break;
                case REPLACE:
                    BulkOperations bulkReplaces = template.bulkOps(BulkMode.ORDERED, User.class);
                    entities.stream().forEach(e -> {
                        bulkReplaces.replaceOne(new Query(Criteria.where("_id").is(e.getId())), e);
                    });
                    List<BulkWriteUpsert> newReplaces = bulkReplaces.execute().getUpserts();

                    for (int i = 0; i < newReplaces.size(); i++) {
                        entities.get(i).setId(newReplaces.get(i).getId().asObjectId().getValue().toHexString());
                    }
                    break;
                case UPDATE:

                    BulkOperations bulkUpdates = template.bulkOps(BulkMode.ORDERED, User.class);
                    entities.stream().forEach(e -> {
                        bulkUpdates.updateOne(new Query(Criteria.where("_id").is(e.getId())), new Update().inc("v", 1));
                    });
                    List<BulkWriteUpsert> upserts = bulkUpdates.execute().getUpserts();

                    for (int i = 0; i < upserts.size(); i++) {
                        entities.get(i).setId(upserts.get(i).getId().asObjectId().getValue().toHexString());
                    }
                    break;
            }
            stat.setData(entities);
            sw.stop();
            min = sw.getTotalTimeMillis();
            max = sw.getTotalTimeMillis();
            total = sw.getTotalTimeMillis();
        } else {
            List<User> newEntities = new ArrayList<>();
            for (User e : entities) {
                StopWatch sw = new StopWatch();
                sw.start();
                //TODO
                //e.setId(collection.insertOne(e).getInsertedId().asObjectId().getValue().toHexString());
                sw.stop();
                long time = sw.getTotalTimeMillis();
                total += time;
                if (min == 0) {
                    min = time;
                }
                min = Math.min(min, time);
                max = Math.max(max, time);
            }
            stat.setData(newEntities);
        }
        stat.setMinLatency(min);
        stat.setMaxLatency(max);
        stat.setDuration(total);
        stat.setEndAt(new Date());
        stat.setDuration(total);
        return stat;
    }

}
