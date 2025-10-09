package com.mongodb.javabasic.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.bson.BsonArray;
import org.bson.BsonValue;
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
import org.springframework.stereotype.Service;

import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteInsert;
import com.mongodb.bulk.BulkWriteUpsert;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.Facet;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.search.FieldSearchPath;
import com.mongodb.client.model.search.SearchPath;
import com.mongodb.client.model.search.VectorSearchOptions;
import com.mongodb.javabasic.ai.EmbeddingProvider;
import com.mongodb.javabasic.model.Product;
import com.mongodb.javabasic.model.Stat;
import com.mongodb.javabasic.model.Workload;
import com.mongodb.javabasic.model.Workload.Converter;
import com.mongodb.javabasic.service.ProductService;

@Service("productService")
public class DriverProductService extends ProductService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    MongoConverter mongoConverter;

    @Autowired
    private CodecRegistry pojoCodecRegistry;

    @Override
    public Stat<Page<Product>> search(String query, Pageable pageable) {

        Stat<Page<Product>> stat = new Stat<>(Product.class);
        MongoCollection<Document> collection = mongoTemplate
                .getCollection(mongoTemplate.getCollectionName(Product.class));
        EmbeddingProvider embeddingProvider = new EmbeddingProvider();
        BsonArray embeddingBsonArray = embeddingProvider.getEmbedding(query);

        List<Double> embedding = new ArrayList<>();
        for (BsonValue value : embeddingBsonArray.stream().toList()) {
            embedding.add(value.asDouble().getValue());
        }

        // define $vectorSearch pipeline
        String indexName = "vector_index";
        FieldSearchPath fieldSearchPath = SearchPath.fieldPath("embedding");
        int limit = 5;

        List<Bson> pipeline = Arrays.asList(
                Aggregates.vectorSearch(
                        fieldSearchPath,
                        embedding,
                        indexName,
                        limit,
                        VectorSearchOptions.exactVectorSearchOptions()),
                Aggregates.project(
                        Projections.fields(Projections.exclude("_id"), Projections.include("description"),
                                Projections.metaVectorSearchScore("score"))));

        // run query and print results
        List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());

        if (results.isEmpty()) {
            System.out.println("No results found.");
        } else {
            results.forEach(doc -> {
                System.out.println("Text: " + doc.getString("description"));
                System.out.println("Score: " + doc.getDouble("score"));
            });
        }
        //stat.setData(results);
        return stat;
    }

    @Override
    public Stat<Page<Product>> list(Workload workload, Pageable pageable) {
        Stat<Page<Product>> stat = new Stat<>(Product.class);
        time(stat, workload, (v) -> {
            MongoCollection<Document> collection = mongoTemplate
                    .getCollection(mongoTemplate.getCollectionName(Product.class));
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

            List<Product> list = new ArrayList<Product>();
            Stream<Document> s = result.getList("data", Document.class).stream();
            if (Converter.SPRING == workload.getConverter()) {
                list = s.map(d -> mongoConverter.read(Product.class, d))
                        .toList();
            } else {
                DecoderContext dc = DecoderContext.builder().build();
                list = s.map(d -> pojoCodecRegistry.get(Product.class).decode(d.toBsonDocument().asBsonReader(), dc))
                        .toList();
            }
            if (list.size() > 0)
                stat.setData(List.of(new PageImpl<>(list, pageable,
                        result.getList("meta", Document.class).get(0).getInteger("count"))));
            else
                stat.setData(List.of(new PageImpl<>(List.of(), pageable,
                        0)));
            return null;
        });
        return stat;
    }

    @Override
    public Stat<Product> _load(List<Product> entities, Workload workload) {
        Stat<Product> stat = new Stat<>(Product.class);
        MongoCollection<Product> collection = mongoTemplate
                .getCollection(mongoTemplate.getCollectionName(Product.class))
                .withWriteConcern(WriteConcern.valueOf(workload.getWriteConcern().name()))
                .withDocumentClass(Product.class);
        if (workload.isBulk()) {
            time(stat, workload, (v) -> {
                switch (workload.getOperationType()) {
                    case DELETE:
                        collection.bulkWrite(entities.stream()
                                .map(e -> new DeleteOneModel<Product>(Filters.eq("_id", new ObjectId(e.getId()))))
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
                                                .map(e -> new UpdateOneModel<Product>(
                                                        Filters.eq("_id", new ObjectId(e.getId())),
                                                        Updates.combine(Updates.inc("version", 1)),
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
            List<Product> newEntities = new ArrayList<>();
            for (Product e : entities) {
                time(stat, workload, (v) -> {
                    switch (workload.getOperationType()) {
                        case INSERT:
                            e.setId(collection.insertOne(e).getInsertedId().asObjectId().getValue().toHexString());
                            break;
                        case DELETE:
                            collection.deleteOne(Filters.eq("_id", new ObjectId(e.getId())));
                            break;
                        case REPLACE:
                            collection.replaceOne(Filters.eq("_id", new ObjectId(e.getId())), e);
                            break;
                        case UPDATE:
                            collection.updateOne(Filters.eq("_id", new ObjectId(e.getId())), Updates.inc("version", 1));
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
