package com.mongodb.javabasic.bootstrap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.changestream.FullDocument;
import com.mongodb.javabasic.config.AppConfig;
import com.mongodb.javabasic.model.Aggregation;
import com.mongodb.javabasic.model.ChangeStream;
import com.mongodb.javabasic.model.ChangeStream.Mode;
import com.mongodb.javabasic.model.ChangeStream.ResumeStrategy;
import com.mongodb.javabasic.model.ChangeStreamRegistry;
import com.mongodb.javabasic.model.PipelineTemplate;
import com.mongodb.javabasic.repositories.PipelineRepository;
import com.mongodb.javabasic.service.AggregationService;
import com.mongodb.javabasic.service.ChangeStreamService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Profile("change-stream")
@Component
public class ChangeStreamRunner {

    private final AppConfig appConfig;
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ChangeStreamService<Document> changeStreamService;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AggregationService aggregationService;
    @Value("${settings.changeStream.watchColls}")
    private String[] watchColls;

    @Value("${settings.changeStream.batchSize}")
    private int batchSize;

    @Value("${settings.changeStream.maxAwaitTime}")
    private long maxAwaitTime;

    @Autowired
    private PipelineRepository pipelineRepository;

    ChangeStream<Document> cs;
    ChangeStream<Document> cs2;
    ChangeStream<Document> cs3;

    ChangeStreamRunner(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @PostConstruct
    private void watch() {
        cs = ChangeStream.of("bucket-data", Mode.AUTO_RECOVER,
                List.of(Aggregates.match(
                        Filters.in("ns.coll", watchColls))))
                .resumeStrategy(ResumeStrategy.TIME, 60000).batchSize(batchSize).maxAwaitTime(maxAwaitTime)
                .fullDocument(FullDocument.UPDATE_LOOKUP);
        changeStreamService.run(ChangeStreamRegistry.<Document>builder().body(e -> {
            try {
                // logger.debug("Body:" + e.getFullDocument());
                Document doc = e.getFullDocument();
                if (doc != null) {
                    switch (e.getNamespace().getCollectionName()) {
                        case "tTableRating":
                            logger.info("tranID:" + doc
                                    .getLong("tranID"));
                            PipelineTemplate p = this.pipelineRepository.findByName("bucket");
                            if (p == null || p.getContent() == null)
                                throw new RuntimeException("Pipeline not found");
                            List<Document> docs = aggregationService
                                    .execute(Aggregation.of(e.getNamespace().getCollectionName(),
                                            p.getContent()),
                                            Map.of("tranID", doc
                                                    .getLong("tranID")));
                            if (docs != null && !docs.isEmpty()) {
                                Document d = docs.get(0);
                                // logger.debug(d.toJson());
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
                                logger.debug(result.toString());
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
        }).changeStream(cs).build());

        cs2 = ChangeStream.of("final-data", Mode.AUTO_RECOVER,
                List.of(
                        Aggregates.addFields(new Field<>("fullDocument.locnIndex",
                                new Document("$abs",
                                        new Document("$mod", List.of(
                                                new Document("$toHashedIndexKey", "$fullDocument.locnCode"), 100))))),
                        Aggregates.match(Filters.and(
                                Filters.eq("fullDocument.type",
                                        "acct-casinoCode-areaCode-locnCode"),
                                Filters.eq("fullDocument.bucketSize", "1day")))))
                .resumeStrategy(ResumeStrategy.TIME, 60000).batchSize(batchSize).maxAwaitTime(maxAwaitTime)
                .fullDocument(FullDocument.UPDATE_LOOKUP);
        changeStreamService.run(ChangeStreamRegistry.<Document>builder().collectionName("tRatingBucket").body(e -> {
            try {
                PipelineTemplate p = this.pipelineRepository.findByName("final");
                if (p == null || p.getContent() == null)
                    throw new RuntimeException("Pipeline not found");
                aggregationService
                        .execute(Aggregation.of(e.getNamespace().getCollectionName(),
                                p.getContent()),
                                Map.of("locnCode", e
                                        .getFullDocument()
                                        .getString("locnCode")));

                // TODO: replace graphQL with Change stream Websocket

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).changeStream(cs2).build());

        cs3 = ChangeStream.of("dashboard", Mode.BOARDCAST,
                List.of(
                        Aggregates.addFields(new Field<>("fullDocument.noOfTxn",
                                new Document("$size",
                                        new Document("$ifNull",List.of("$fullDocument.trans", List.of())))),
                                new Field<>("fullDocument.avgBet",
                                        new Document("$avg", "$fullDocument.trans.bet")),
                                new Field<>("fullDocument.avgCasinoWin",
                                        new Document("$avg", "$fullDocument.trans.casinoWin")),
                                new Field<>("fullDocument.avgTheorWin",
                                        new Document("$avg", "$fullDocument.trans.theorWin")))))
                .fullDocument(FullDocument.UPDATE_LOOKUP);
        changeStreamService.run(ChangeStreamRegistry.<Document>builder().collectionName("tRatingBucket").body(e -> {

            // TODO: replace graphQL with Change stream Websocket
            /*switch (e.getFullDocument().getString("type")) {
                case "acct-areaCode":
                    logger.info("Dashboard change stream - areaCode:" + e.getFullDocument().getString("areaCode"));
                    switch (e.getFullDocument().getString("bucketSize")) {
                        case "15days":
                            logger.info("Dashboard change stream - areaCode:"
                                    + e.getFullDocument().getString("areaCode") + " bucketSize:15days");

                            break;
                        case "1day":
                            logger.info("Dashboard change stream - areaCode:"
                                    + e.getFullDocument().getString("areaCode") + " bucketSize:1day");

                            break;
                        case "3mins":
                            logger.info("Dashboard change stream - areaCode:"
                                    + e.getFullDocument().getString("areaCode") + " bucketSize:3mins");

                    }
                    break;
                case "acct-casinoCode":
                    switch (e.getFullDocument().getString("bucketSize")) {
                        case "15days":
                            logger.info("Dashboard change stream - casinoCode:"
                                    + e.getFullDocument().getString("casinoCode") + " bucketSize:15days");

                            break;
                        case "1day":
                            logger.info("Dashboard change stream - casinoCode:"
                                    + e.getFullDocument().getString("casinoCode") + " bucketSize:1day");

                            break;
                        case "3mins":
                            logger.info("Dashboard change stream - casinoCode:"
                                    + e.getFullDocument().getString("casinoCode") + " bucketSize:3mins");

                            break;
                    }
                    break;
                case "acct-casinoCode-areaCode":
                    logger.info("Dashboard change stream - casinoCode:" + e.getFullDocument().getString("casinoCode")
                            + " areaCode:" + e.getFullDocument().getString("areaCode"));
                    switch (e.getFullDocument().getString("bucketSize")) {
                        case "15days":
                            logger.info("Dashboard change stream - casinoCode:"
                                    + e.getFullDocument().getString("casinoCode") + " bucketSize:15days");

                            break;
                        case "1day":
                            logger.info("Dashboard change stream - casinoCode:"
                                    + e.getFullDocument().getString("casinoCode") + " bucketSize:1day");

                            break;
                        case "3mins":
                            logger.info("Dashboard change stream - casinoCode:"
                                    + e.getFullDocument().getString("casinoCode") + " bucketSize:3mins");

                            break;
                    }
                    break;
                default:
                    break;
            }*/

        }).changeStream(cs3).build());
    }

    @PreDestroy
    private void clear() {
        if (cs != null)
            cs.setRunning(false);
        if (cs2 != null)
            cs2.setRunning(false);
        if (cs3 != null)
            cs3.setRunning(false);
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
        logger.debug(filters.toString());
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
