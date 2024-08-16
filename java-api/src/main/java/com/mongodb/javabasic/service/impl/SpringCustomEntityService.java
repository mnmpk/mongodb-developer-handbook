package com.mongodb.javabasic.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.bulk.BulkWriteInsert;
import com.mongodb.bulk.BulkWriteUpsert;
import com.mongodb.javabasic.model.Stat;
import com.mongodb.javabasic.model.CustomEntity;
import com.mongodb.javabasic.model.Workload;
import com.mongodb.javabasic.service.CustomEntityService;

@Service("springCustomEntityService")
public class SpringCustomEntityService extends CustomEntityService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MongoTemplate template;

    @Override
    public Stat<Page<CustomEntity>> search(String query, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'search'");
    }

    @Override
    public Stat<Page<CustomEntity>> list(Workload workload, Pageable pageable) {
        Stat<Page<CustomEntity>> stat = new Stat<>(CustomEntity.class);
        time(stat, workload, (v) -> {
            var list = template.find(
                    new Query().with(pageable.getSort()).skip(pageable.getPageNumber()).limit(pageable.getPageSize()),
                    CustomEntity.class);
            stat.setData(List.of(new PageImpl<>(list, pageable, template.count(new Query(), CustomEntity.class))));
            return null;
        });
        return stat;
    }

    @Override
    public Stat<CustomEntity> _load(List<CustomEntity> entities, Workload workload) {
        Stat<CustomEntity> stat = new Stat<>(CustomEntity.class);

        if (workload.isBulk()) {
            time(stat, workload, (v) -> {
                switch (workload.getOperationType()) {
                    case DELETE:
                        BulkOperations bulkRemoves = template.bulkOps(BulkMode.ORDERED, CustomEntity.class);
                        entities.stream().forEach(e -> {
                            bulkRemoves.remove(new Query(Criteria.where("_id").is(e.getId())));
                        });
                        bulkRemoves.execute();
                        break;
                    case INSERT:
                        BulkOperations bulkInserts = template.bulkOps(BulkMode.ORDERED, CustomEntity.class);
                        entities.stream().forEach(e -> {
                            bulkInserts.insert(e);
                        });
                        List<BulkWriteInsert> inserts = bulkInserts.execute().getInserts();
                        for (int i = 0; i < inserts.size(); i++) {
                            entities.get(i).setId(inserts.get(i).getId().asObjectId().getValue().toHexString());
                        }
                        break;
                    case REPLACE:
                        BulkOperations bulkReplaces = template.bulkOps(BulkMode.ORDERED, CustomEntity.class);
                        entities.stream().forEach(e -> {
                            bulkReplaces.replaceOne(new Query(Criteria.where("_id").is(e.getId())), e);
                        });
                        List<BulkWriteUpsert> newReplaces = bulkReplaces.execute().getUpserts();
                        for (int i = 0; i < newReplaces.size(); i++) {
                            entities.get(i).setId(newReplaces.get(i).getId().asObjectId().getValue().toHexString());
                        }
                        break;
                    case UPDATE:
                        BulkOperations bulkUpdates = template.bulkOps(BulkMode.ORDERED, CustomEntity.class);
                        entities.stream().forEach(e -> {
                            bulkUpdates.updateOne(new Query(Criteria.where("_id").is(e.getId())),
                                    new Update().inc("version", 1));
                        });
                        List<BulkWriteUpsert> upserts = bulkUpdates.execute().getUpserts();
                        for (int i = 0; i < upserts.size(); i++) {
                            entities.get(i).setId(upserts.get(i).getId().asObjectId().getValue().toHexString());
                        }
                        break;
                }
                stat.setData(entities);
                return null;
            });
        } else {
            List<CustomEntity> newEntities = new ArrayList<>();
            for (CustomEntity e : entities) {
                time(stat, workload, (v) -> {
                    switch (workload.getOperationType()) {
                        case INSERT:
                            template.insert(e);
                            break;
                        case DELETE:
                            template.remove(e);
                            break;
                        case REPLACE:
                            template.update(CustomEntity.class).matching(new Query(Criteria.where("_id").is(e.getId())))
                                    .replaceWith(e);
                            break;
                        case UPDATE:
                            Update update = new Update();
                            update.inc("version");
                            template.update(CustomEntity.class).matching(new Query(Criteria.where("_id").is(e.getId())))
                                    .apply(update);
                            break;
                    }
                    newEntities.add(e);
                    return null;
                });
            }
            stat.setData(newEntities);
        }
        return stat;
    }

}
