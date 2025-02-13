package com.mongodb.javabasic.service;

import com.mongodb.client.MongoCollection;

import freemarker.template.Configuration;
import freemarker.template.Template;

import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.codecs.BsonArrayCodec;
import org.bson.codecs.DecoderContext;
import org.bson.json.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AggregationService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    Configuration freemarkerConfig;

    public <T> List<T> getPipelineResults(String collectionName, String pipelineName, Class<T> clazz) throws Exception {
        return getPipelineResults(collectionName, pipelineName, clazz, null);
    }

    public <T> List<T> getPipelineResults(String collectionName, String pipelineName, Class<T> clazz,
            Map<String, Object> variables) {
        MongoCollection<T> collection = mongoTemplate.getCollection(collectionName).withDocumentClass(clazz);
        List<BsonDocument> optionalPipeline = loadPipeline(pipelineName, Optional.ofNullable(variables));
        if (optionalPipeline != null) {
            return collection.aggregate(optionalPipeline, clazz).into(new ArrayList<>());
        } else {
            throw new RuntimeException("Pipeline not found");
        }
    }

    private List<BsonDocument> loadPipeline(String pipelineName, Optional<Map<String, Object>> variables) {
        try {
            return new BsonArrayCodec()
                    .decode(new JsonReader(
                            FreeMarkerTemplateUtils.processTemplateIntoString(
                                    new Template("pipeline", new InputStreamReader(
                                            new ClassPathResource("/pipelines/" + pipelineName)
                                                    .getInputStream(),
                                            "UTF-8"), freemarkerConfig),
                                    variables.orElse(new HashMap<>()))),
                            DecoderContext.builder().build())
                    .stream().map(BsonValue::asDocument)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Load pipeline failed");
        }
    }
}
