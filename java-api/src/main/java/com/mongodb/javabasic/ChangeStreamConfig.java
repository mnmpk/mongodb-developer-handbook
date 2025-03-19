package com.mongodb.javabasic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.changestream.FullDocument;
import com.mongodb.javabasic.model.ChangeStream;
import com.mongodb.javabasic.model.ChangeStreamProcess;
import com.mongodb.javabasic.model.ChangeStreamProcessConfig;
import com.mongodb.javabasic.service.AggregationService;

import jakarta.annotation.PostConstruct;

@Configuration
public class ChangeStreamConfig {

        @Value("${settings.changeStream.batchSize}")
        private int batchSize;

        @Value("${settings.changeStream.maxAwaitTime}")
        private int maxAwaitTime;

        @Value("${settings.changeStream.watchColls}")
        private String[] watchColls;

        private Logger logger = LoggerFactory.getLogger(getClass());

        @Autowired
        private MongoTemplate mongoTemplate;

        @Autowired
        private AggregationService aggregationService;

        @PostConstruct
        public void startChangeStream() throws Exception {
                logger.info("Start watching:" + watchColls);
                ChangeStream<Document> changeStream = new ChangeStream<>();
                changeStream.run((ChangeStreamProcessConfig<Document> config) -> {
                        List<Bson> pipeline = (List.of(Aggregates.match(
                                        Filters.in("ns.coll", watchColls))));
                        return new ChangeStreamProcess<Document>(config,
                                        (e) -> {
                                                try {
                                                        // logger.info("Body:" + e.getFullDocument());
                                                        Document doc = e.getFullDocument();
                                                        if (doc != null) {
                                                                switch (e.getNamespace().getCollectionName()) {
                                                                        case "tTableRating":
                                                                                logger.info("tranID:" + doc
                                                                                                .getLong("tranID"));
                                                                                List<Document> docs = aggregationService
                                                                                                .getPipelineResults(
                                                                                                                e.getNamespace().getCollectionName(),
                                                                                                                "bucket.json",
                                                                                                                Document.class,
                                                                                                                Map.of("tranID", doc
                                                                                                                                .getLong("tranID")));
                                                                                if (docs != null && !docs.isEmpty()) {
                                                                                        Document d = docs.get(0);
                                                                                        // logger.info(d.toJson());
                                                                                        MongoCollection<Document> tRatingBucket = mongoTemplate
                                                                                                        .getDb()
                                                                                                        .getCollection("tRatingBucket");
                                                                                        logger.info("*******************acct:"
                                                                                                        + d.getInteger("acct")
                                                                                                        + " bucketDt3mins:"
                                                                                                        + d.getDate("bucketDt3mins")
                                                                                                        + " bucketDt1day:"
                                                                                                        + d.getDate("bucketDt1day")
                                                                                                        + " bucketDt15days:"
                                                                                                        + d.getDate("bucketDt15days")
                                                                                                        + " casinoCode:"
                                                                                                        + d.getString("casinoCode")
                                                                                                        + " areaCode:"
                                                                                                        + d.getString("areaCode")
                                                                                                        + " locnCode:"
                                                                                                        + d.getString("locnCode"));
                                                                                        BulkWriteResult result = tRatingBucket
                                                                                                        .bulkWrite(
                                                                                                                        List.of(
                                                                                                                                        this
                                                                                                                                                        .createPlayerBucketUpdateModel(
                                                                                                                                                                        d,
                                                                                                                                                                        "15days",
                                                                                                                                                                        d.getDate("bucketDt15days"),
                                                                                                                                                                        "acct",
                                                                                                                                                                        "areaCode"),
                                                                                                                                        this
                                                                                                                                                        .createPlayerBucketUpdateModel(
                                                                                                                                                                        d,
                                                                                                                                                                        "1day",
                                                                                                                                                                        d.getDate("bucketDt1day"),
                                                                                                                                                                        "acct",
                                                                                                                                                                        "areaCode"),
                                                                                                                                        this
                                                                                                                                                        .createPlayerBucketUpdateModel(
                                                                                                                                                                        d,
                                                                                                                                                                        "3mins",
                                                                                                                                                                        d.getDate("bucketDt3mins"),
                                                                                                                                                                        "acct",
                                                                                                                                                                        "areaCode"),
                                                                                                                                        this
                                                                                                                                                        .createPlayerBucketUpdateModel(
                                                                                                                                                                        d,
                                                                                                                                                                        "15days",
                                                                                                                                                                        d.getDate("bucketDt15days"),
                                                                                                                                                                        "acct",
                                                                                                                                                                        "casinoCode",
                                                                                                                                                                        "areaCode"),
                                                                                                                                        this
                                                                                                                                                        .createPlayerBucketUpdateModel(
                                                                                                                                                                        d,
                                                                                                                                                                        "1day",
                                                                                                                                                                        d.getDate("bucketDt1day"),
                                                                                                                                                                        "acct",
                                                                                                                                                                        "casinoCode",
                                                                                                                                                                        "areaCode"),
                                                                                                                                        this
                                                                                                                                                        .createPlayerBucketUpdateModel(
                                                                                                                                                                        d,
                                                                                                                                                                        "3mins",
                                                                                                                                                                        d.getDate("bucketDt3mins"),
                                                                                                                                                                        "acct",
                                                                                                                                                                        "casinoCode",
                                                                                                                                                                        "areaCode"),
                                                                                                                                        this
                                                                                                                                                        .createPlayerBucketUpdateModel(
                                                                                                                                                                        d,
                                                                                                                                                                        "15days",
                                                                                                                                                                        d.getDate("bucketDt15days"),
                                                                                                                                                                        "acct",
                                                                                                                                                                        "casinoCode"),
                                                                                                                                        this
                                                                                                                                                        .createPlayerBucketUpdateModel(
                                                                                                                                                                        d,
                                                                                                                                                                        "1day",
                                                                                                                                                                        d.getDate("bucketDt1day"),
                                                                                                                                                                        "acct",
                                                                                                                                                                        "casinoCode"),
                                                                                                                                        this
                                                                                                                                                        .createPlayerBucketUpdateModel(
                                                                                                                                                                        d,
                                                                                                                                                                        "3mins",
                                                                                                                                                                        d.getDate("bucketDt3mins"),
                                                                                                                                                                        "acct",
                                                                                                                                                                        "casinoCode"),
                                                                                                                                        this
                                                                                                                                                        .createPlayerBucketUpdateModel(
                                                                                                                                                                        d,
                                                                                                                                                                        "1day",
                                                                                                                                                                        d.getDate("bucketDt1day"),
                                                                                                                                                                        "acct",
                                                                                                                                                                        "casinoCode",
                                                                                                                                                                        "areaCode",
                                                                                                                                                                        "locnCode")),
                                                                                                                        new BulkWriteOptions()
                                                                                                                                        .ordered(
                                                                                                                                                        false));
                                                                                        logger.info(result.toString());
                                                                                }
                                                                                break;

                                                                        case "tArea":
                                                                                break;
                                                                        case "tLocn":
                                                                                break;
                                                                }

                                                        }
                                                } catch (Exception ex) {
                                                        ex.printStackTrace();
                                                }
                                        }) {
                                @Override
                                public ChangeStreamIterable<Document> initChangeStream(List<Bson> p) {
                                        if (pipeline != null && pipeline.size() > 0)
                                                p.addAll(pipeline);
                                        ChangeStreamIterable<Document> cs = mongoTemplate.getDb()
                                                        .watch(p, Document.class)
                                                        .batchSize(batchSize)
                                                        .maxAwaitTime(maxAwaitTime, TimeUnit.MILLISECONDS)
                                                        .fullDocument(FullDocument.UPDATE_LOOKUP);

                                        return cs;
                                }

                        };
                }, true);

                ChangeStream<Document> changeStream2 = new ChangeStream<>();
                changeStream2.run((ChangeStreamProcessConfig<Document> config) -> {
                        return new ChangeStreamProcess<Document>(config,
                                        (e) -> {
                                                try {
                                                        aggregationService
                                                                        .getPipelineResults(
                                                                                        e.getNamespace().getCollectionName(),
                                                                                        "final.json",
                                                                                        Document.class,
                                                                                        Map.of("locnCode", e
                                                                                                        .getFullDocument()
                                                                                                        .getString("locnCode")));
                                                } catch (Exception ex) {
                                                        ex.printStackTrace();
                                                }
                                        }) {

                                @Override
                                public ChangeStreamIterable<Document> initChangeStream(List<Bson> p) {
                                        p.add(Aggregates.match(Filters.and(
                                                        Filters.eq("fullDocument.type",
                                                                        "acct-casinoCode-areaCode-locnCode"),
                                                        Filters.eq("fullDocument.bucketSize", "1day"))));
                                        ChangeStreamIterable<Document> cs = mongoTemplate.getDb()
                                                        .getCollection("tRatingBucket")
                                                        .watch(p, Document.class)
                                                        .batchSize(batchSize)
                                                        .maxAwaitTime(maxAwaitTime, TimeUnit.MILLISECONDS)
                                                        .fullDocument(FullDocument.UPDATE_LOOKUP);
                                        return cs;
                                }

                        };
                }, true);

        }

        private UpdateOneModel<Document> createPlayerBucketUpdateModel(Document d, String bucketSize, Date bucketDt,
                        String... groupBys) {
                List<Bson> filters = new ArrayList<>();
                List<Bson> updates = new ArrayList<>();
                for (String groupBy : groupBys) {
                        filters.add(Filters.eq(groupBy, d.get(groupBy)));
                        updates.add(Updates.set(groupBy, d.get(groupBy)));
                }
                filters.add(Filters.eq("type", String.join("-", groupBys)));
                filters.add(Filters.eq("bucketSize", bucketSize));
                filters.add(Filters.eq("bucketDt" + bucketSize, bucketDt));
                logger.info(filters.toString());
                return new UpdateOneModel<Document>(
                                Filters.and(filters),
                                Updates.combine(
                                                Updates.combine(updates),
                                                Updates.set("type", String.join("-", groupBys)),
                                                Updates.set("bucketDt" + bucketSize,
                                                                bucketDt),
                                                Updates.set("bucketSize",
                                                                bucketSize),
                                                Updates.inc("sumBet",
                                                                d.getDouble("bet")),
                                                Updates.inc("sumCasinoWin",
                                                                d.getDouble("casinoWin")),
                                                Updates.inc("sumTheorWin",
                                                                d.getDouble("theorWin")),
                                                Updates.addToSet(
                                                                "trans",
                                                                new Document("tranID",
                                                                                d.getLong("tranID"))
                                                                                .append("gameCode",
                                                                                                d.getString("gameCode"))
                                                                                .append("gamingDt",
                                                                                                d.getDate("gamingDt"))
                                                                                .append("postDtm",
                                                                                                d.getDate("postDtm"))
                                                                                .append("bet",
                                                                                                d.getDouble("bet"))
                                                                                .append("theorWin",
                                                                                                d.getDouble("theorWin"))
                                                                                .append("ratingCategory",
                                                                                                d.getString("ratingCategory"))
                                                                                .append("casinoWin",
                                                                                                d.getDouble("casinoWin"))
                                                                                .append("casinoCode",
                                                                                                d.getString("casinoCode"))
                                                                                .append("deptCode",
                                                                                                d.getString("deptCode"))
                                                                                .append("locnCode",
                                                                                                d.getString("locnCode"))
                                                                                .append("locnInfo3",
                                                                                                d.getInteger("locnInfo3"))
                                                                                .append("locnInfo4",
                                                                                                d.getInteger("locnInfo4"))
                                                                                .append("areaCode",
                                                                                                d.getString("areaCode")))),

                                new UpdateOptions()
                                                .upsert(true));
        }
}
