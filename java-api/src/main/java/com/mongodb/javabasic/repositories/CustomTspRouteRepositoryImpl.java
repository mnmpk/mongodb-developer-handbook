package com.mongodb.javabasic.repositories;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.javabasic.model.TspRoute;

public class CustomTspRouteRepositoryImpl implements CustomTspRouteRepository {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<TspRoute> getRoutes(String dep, String arr) {
        List<Bson> stages = new ArrayList<>();
        if (StringUtils.isNotBlank(dep))
            stages.add(Aggregates.match(Filters.eq("_id", dep)));
        if (StringUtils.isNotBlank(arr)) {
            stages.add(new Document("$set",
                    new Document("destinations",
                            new Document("$filter",
                                    new Document("input", "$destinations")
                                            .append("as", "port")
                                            .append("cond",
                                                    new Document("$eq", Arrays.asList("$$port._id", arr)))))));

            logger.info("stages" + stages);
        }
        MongoCollection<TspRoute> coll = mongoTemplate.getDb()
                .getCollection(mongoTemplate.getCollectionName(TspRoute.class), TspRoute.class);
        return coll.aggregate(stages, TspRoute.class).into(new ArrayList<>());
    }

    @Override
    public List<TspRoute> getRoutes(String dep) {
        return this.getRoutes(dep, null);
    }
}
