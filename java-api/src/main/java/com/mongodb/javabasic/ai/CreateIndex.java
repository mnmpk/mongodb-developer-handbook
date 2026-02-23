package com.mongodb.javabasic.ai;

import com.mongodb.client.ListSearchIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.SearchIndexModel;
import com.mongodb.client.model.SearchIndexType;
import com.mongodb.javabasic.model.Product;

import jakarta.annotation.PostConstruct;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CreateIndex {
	private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    MongoTemplate mongoTemplate;

    @PostConstruct
    public void init() {

        MongoCollection<Document> collection = mongoTemplate
                .getCollection(mongoTemplate.getCollectionName(Product.class));

        // define the index details
        String indexName = "vector_index";
        int dimensionsHuggingFaceModel = 1024;
        int dimensionsOpenAiModel = 1536;

        Bson definition = new Document(
                "fields",
                Collections.singletonList(
                        new Document("type", "vector")
                                .append("path", "embedding")
                                .append("numDimensions", 1024/*"dimensionsVoyageAiModel:1024"*/) // replace with var for the
                                                                                         // model used
                                .append("similarity", "dotProduct")));

        // define the index model using the specified details
        SearchIndexModel indexModel = new SearchIndexModel(
                indexName,
                definition,
                SearchIndexType.vectorSearch());

        // Create the index using the model
        try {
            List<String> result = collection.createSearchIndexes(Collections.singletonList(indexModel));
            logger.info("Successfully created a vector index named: " + result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // wait for index to build and become queryable
        logger.info("Polling to confirm the index has completed building.");
        logger.info("It may take up to a minute for the index to build before you can query using it.");

        ListSearchIndexesIterable<Document> searchIndexes = collection.listSearchIndexes();
        Document doc = null;
        while (doc == null) {
            try (MongoCursor<Document> cursor = searchIndexes.iterator()) {
                if (!cursor.hasNext()) {
                    break;
                }
                Document current = cursor.next();
                String name = current.getString("name");
                boolean queryable = current.getBoolean("queryable");
                if (name.equals(indexName) && queryable) {
                    doc = current;
                } else {
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        logger.info(indexName + " index is ready to query");

    }
}
