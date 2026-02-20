package com.mongodb.javabasic.service;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Facet;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Variable;
import com.mongodb.javabasic.model.Aggregation;

import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AggregationService {

        private Logger logger = LoggerFactory.getLogger(getClass());

        @Autowired
        private MongoTemplate mongoTemplate;

        @Autowired
        private CodecRegistry pojoCodecRegistry;

        public <T> List<T> execute(Aggregation<T> agg) {
                return execute(agg, (Map<String, Object>) null);
        }

        public <T> List<T> execute(Aggregation<T> agg,
                        Map<String, Object> variables) {
                return mongoTemplate.getCollection(agg.getCollectionName()).withDocumentClass(agg.getDocumentClass())
                                .aggregate(loadPipeline(agg, Optional.ofNullable(variables), null))
                                .into(new ArrayList<>());
        }

        public <T> Page<T> execute(Aggregation<T> agg, Pageable pageable) {
                return execute(agg, pageable, null);
        }

        public <T> Page<T> execute(Aggregation<T> agg, Pageable pageable,
                        Map<String, Object> variables) {
                List<BsonDocument> pipeline = loadPipeline(agg, Optional.ofNullable(variables),
                                pageable == null ? Pageable.unpaged() : pageable);
                Document result = mongoTemplate.getCollection(agg.getCollectionName()).aggregate(pipeline).first();
                return PageableExecutionUtils.getPage(
                                result.getList("results", Document.class).stream()
                                                .map(d -> pojoCodecRegistry.get(agg.getDocumentClass()).decode(
                                                                d.toBsonDocument().asBsonReader(),
                                                                DecoderContext.builder().build()))
                                                .collect(Collectors.toList()),
                                pageable,
                                () -> result.getList("total", Document.class).get(0).getInteger("v"));
        }

        private List<BsonDocument> loadPipeline(Aggregation<?> agg, Optional<Map<String, Object>> variables,
                        Pageable pageable) {

                List<BsonDocument> pipeline = agg.merge(variables).asArray()
                                .stream().map(BsonValue::asDocument).collect(Collectors.toList());

                if (agg.getPermission() != null)
                        permissionCheck(pipeline, agg.getPermission());
                if (pageable != null)
                        paginate(pipeline, pageable);
                return pipeline;
        }


        private void permissionCheck(List<BsonDocument> pipeline, long permission) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                ObjectId userId = new ObjectId(authentication.getName());
                pipeline.add(Aggregates.lookup(
                                "permission", List.of(new Variable<String>("id", "$_id")), List.of(Aggregates.match(
                                                Filters.and(
                                                                Filters.eq("eId", "$$id"),
                                                                Filters.eq("uId", userId)))),
                                "ps").toBsonDocument());
                pipeline.add(Aggregates.match(
                                Filters.or(
                                                Filters.bitsAnySet("p", permission),
                                                Filters.bitsAnySet("ps.p", permission)))
                                .toBsonDocument());
        }

        private void paginate(List<BsonDocument> pipeline, Pageable pageable) {
                pipeline.add(
                                pageable.isPaged() ? Aggregates.facet(List.of(new Facet("results", List.of(
                                                Aggregates.skip(pageable.getPageNumber() * pageable.getPageSize()),
                                                Aggregates.limit(pageable.getPageSize()))),
                                                new Facet("total", Aggregates.count("v"))))
                                                .toBsonDocument()
                                                : Aggregates.facet(List.of(new Facet("results", List.of()),
                                                                new Facet("total", Aggregates.count("v"))))
                                                                .toBsonDocument());
        }
}
