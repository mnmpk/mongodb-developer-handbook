package com.mongodb.javabasic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
import com.mongodb.client.model.changestream.OperationType;
import com.mongodb.javabasic.model.ChangeStream;
import com.mongodb.javabasic.model.ChangeStreamProcess;
import com.mongodb.javabasic.model.ChangeStreamProcessConfig;

import jakarta.annotation.PostConstruct;

@Configuration
public class ChangeStreamConfig {

        @Value("${app.changeStream.batchSize}")
        private int batchSize;

        @Value("${app.changeStream.maxAwaitTime}")
        private int maxAwaitTime;

        @Value("${app.changeStream.watchColls}")
        private String[] watchColls;

        private Logger logger = LoggerFactory.getLogger(getClass());

        @Autowired
        private MongoTemplate mongoTemplate;

        @PostConstruct
        public void startChangeStream() throws Exception {
                logger.info("Start watching:" + watchColls);
                ChangeStream<Document> changeStream = new ChangeStream<>();
                changeStream.run((ChangeStreamProcessConfig<Document> config) -> {
                        List<Bson> pipeline = (List.of(Aggregates.match(
                                        Filters.in("ns.coll", watchColls))));
                        return new ChangeStreamProcess<Document>(config,
                                        (e) -> {
                                                // logger.info("Body:" + e.getFullDocument());
                                                Document doc = e.getFullDocument();
                                                switch (e.getNamespace().getCollectionName()) {
                                                        case "tTableRating":
                                                                MongoCollection<Document> tTableRating = mongoTemplate
                                                                                .getDb()
                                                                                .getCollection("tTableRating");
                                                                logger.info("tranID:" + doc.getLong("tranID"));
                                                                Document d = tTableRating.aggregate(Arrays.asList(
                                                                                new Document("$match",
                                                                                                new Document("tranID",
                                                                                                                doc.getLong("tranID"))),
                                                                                new Document("$lookup",
                                                                                                new Document("from",
                                                                                                                "tLocn")
                                                                                                                .append("localField",
                                                                                                                                "locnID")
                                                                                                                .append("foreignField",
                                                                                                                                "locnId")
                                                                                                                .append("as", "locns")),
                                                                                new Document("$lookup",
                                                                                                new Document("from",
                                                                                                                "tCasino")
                                                                                                                .append("localField",
                                                                                                                                "casinoID")
                                                                                                                .append("foreignField",
                                                                                                                                "casinoId")
                                                                                                                .append("as", "casinos")),
                                                                                new Document("$lookup",
                                                                                                new Document("from",
                                                                                                                "tDept")
                                                                                                                .append("localField",
                                                                                                                                "deptID")
                                                                                                                .append("foreignField",
                                                                                                                                "deptId")
                                                                                                                .append("as", "deptss")),
                                                                                new Document("$lookup",
                                                                                                new Document("from",
                                                                                                                "tArea")
                                                                                                                .append("localField",
                                                                                                                                "areaID")
                                                                                                                .append("foreignField",
                                                                                                                                "areaId")
                                                                                                                .append("as", "areas")),
                                                                                new Document("$lookup",
                                                                                                new Document("from",
                                                                                                                "tGame")
                                                                                                                .append("localField",
                                                                                                                                "gameID")
                                                                                                                .append("foreignField",
                                                                                                                                "gameId")
                                                                                                                .append("as", "games")),
                                                                                new Document("$lookup",
                                                                                                new Document("from",
                                                                                                                "tPlayerCard")
                                                                                                                .append("localField",
                                                                                                                                "playerId")
                                                                                                                .append("foreignField",
                                                                                                                                "playerId")
                                                                                                                .append("as", "playerCards")),
                                                                                new Document("$lookup",
                                                                                                new Document("from",
                                                                                                                "tAwards")
                                                                                                                .append("localField",
                                                                                                                                "tranID")
                                                                                                                .append("foreignField",
                                                                                                                                "relatedTranId")
                                                                                                                .append("pipeline",
                                                                                                                                Arrays.asList(new Document(
                                                                                                                                                "$lookup",
                                                                                                                                                new Document("from",
                                                                                                                                                                "tPlayerPoints")
                                                                                                                                                                .append("localField",
                                                                                                                                                                                "tranId")
                                                                                                                                                                .append("foreignField",
                                                                                                                                                                                "tranId")
                                                                                                                                                                .append("as", "playerPoints")),
                                                                                                                                                new Document("$lookup",
                                                                                                                                                                new Document("from",
                                                                                                                                                                                "tPlayerComps")
                                                                                                                                                                                .append("localField",
                                                                                                                                                                                                "tranId")
                                                                                                                                                                                .append("foreignField",
                                                                                                                                                                                                "tranId")
                                                                                                                                                                                .append("as", "playerComps"))))
                                                                                                                .append("as", "awards")),
                                                                                new Document("$match",
                                                                                                new Document("awards",
                                                                                                                new Document("$ne",
                                                                                                                                Arrays.asList()))
                                                                                                                .append("awards.playerPoints",
                                                                                                                                new Document("$ne",
                                                                                                                                                Arrays.asList()))
                                                                                                                .append("awards.playerComps",
                                                                                                                                new Document("$ne",
                                                                                                                                                Arrays.asList()))),
                                                                                new Document("$project",
                                                                                                new Document("tranID",
                                                                                                                "$tranID")
                                                                                                                .append("gamingDt",
                                                                                                                                "$gamingDt")
                                                                                                                .append("bucketDt3mins",
                                                                                                                                new Document("$dateTrunc",
                                                                                                                                                new Document("date",
                                                                                                                                                                "$postDtm")
                                                                                                                                                                .append("binSize",
                                                                                                                                                                                3)
                                                                                                                                                                .append("unit", "minute")))
                                                                                                                .append("bucketDt1day",
                                                                                                                                new Document("$dateTrunc",
                                                                                                                                                new Document("date",
                                                                                                                                                                "$postDtm")
                                                                                                                                                                .append("binSize",
                                                                                                                                                                                1)
                                                                                                                                                                .append("unit", "day")))
                                                                                                                .append("bucketDt15days",
                                                                                                                                new Document("$dateTrunc",
                                                                                                                                                new Document("date",
                                                                                                                                                                "$postDtm")
                                                                                                                                                                .append("binSize",
                                                                                                                                                                                15)
                                                                                                                                                                .append("unit", "day")))
                                                                                                                .append("postDtm",
                                                                                                                                "$postDtm")
                                                                                                                .append("ratingCategory",
                                                                                                                                "$ratingCategory")
                                                                                                                .append("theorWin",
                                                                                                                                "$theorWin")
                                                                                                                .append("casinoWin",
                                                                                                                                "$casinoWin")
                                                                                                                .append("bet", "$bet")
                                                                                                                .append("acct",
                                                                                                                                new Document("$first",
                                                                                                                                                "$playerCards.acct"))
                                                                                                                .append("locnCode",
                                                                                                                                new Document("$first",
                                                                                                                                                "$locns.locnCode"))
                                                                                                                .append("areaCode",
                                                                                                                                new Document("$first",
                                                                                                                                                "$areas.areaCode"))
                                                                                                                .append("casinoCode",
                                                                                                                                new Document("$first",
                                                                                                                                                "$casinos.casinoCode"))
                                                                                                                .append("gameCode",
                                                                                                                                new Document("$first",
                                                                                                                                                "$games.gameCode"))
                                                                                                                .append("locnInfo3",
                                                                                                                                new Document("$first",
                                                                                                                                                "$locns.locnInfo3"))
                                                                                                                .append("locnInfo4",
                                                                                                                                new Document("$first",
                                                                                                                                                "$locns.locnInfo4"))
                                                                                                                .append("deptCode",
                                                                                                                                new Document("$first",
                                                                                                                                                "$deptss.deptCode")))

                                        ))
                                                                                .first();
                                                                if (d != null) {
                                                                        // logger.info(d.toJson());
                                                                        MongoCollection<Document> player = mongoTemplate
                                                                                        .getDb()
                                                                                        .getCollection("tRatingBucket");
                                                                        logger.info("acct:" + d.getInteger("acct")
                                                                                        + " bucketDt3mins:"
                                                                                        + d.getDate("bucketDt3mins")
                                                                                        + " bucketDt1day:"
                                                                                        + d.getDate("bucketDt1day")
                                                                                        + " bucketDt15days:"
                                                                                        + d.getDate("bucketDt15days"));
                                                                        BulkWriteResult result = player.bulkWrite(
                                                                                        List.of(
                                                                                                        this
                                                                                                                        .createPlayerBucketUpdateModel(
                                                                                                                                        d,
                                                                                                                                        "15-days",
                                                                                                                                        d.getDate("bucketDt15days"),
                                                                                                                                        "acct",
                                                                                                                                        "areaCode"),
                                                                                                        this
                                                                                                                        .createPlayerBucketUpdateModel(
                                                                                                                                        d,
                                                                                                                                        "3-mins",
                                                                                                                                        d.getDate("bucketDt3mins"),
                                                                                                                                        "acct",
                                                                                                                                        "areaCode"),
                                                                                                        this
                                                                                                                        .createPlayerBucketUpdateModel(
                                                                                                                                        d,
                                                                                                                                        "1-day",
                                                                                                                                        d.getDate("bucketDt1day"),
                                                                                                                                        "acct",
                                                                                                                                        "casinoCode",
                                                                                                                                        "areaCode"),
                                                                                                        this
                                                                                                                        .createPlayerBucketUpdateModel(
                                                                                                                                        d,
                                                                                                                                        "3-mins",
                                                                                                                                        d.getDate("bucketDt3mins"),
                                                                                                                                        "acct",
                                                                                                                                        "casinoCode",
                                                                                                                                        "areaCode"),
                                                                                                        this
                                                                                                                        .createPlayerBucketUpdateModel(
                                                                                                                                        d,
                                                                                                                                        "1-day",
                                                                                                                                        d.getDate("bucketDt1day"),
                                                                                                                                        "acct",
                                                                                                                                        "casinoCode")),
                                                                                        new BulkWriteOptions().ordered(
                                                                                                        false));
                                                                        logger.info(result.toString());
                                                                }
                                                                break;

                                                        case "tArea":
                                                                break;
                                                        case "tLocn":
                                                                break;
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
                                                if (OperationType.DELETE != e.getOperationType()) {
                                                        Document doc = e.getFullDocument();
                                                        // logger.info(doc.toString());

                                                        /*
                                                         * 1. Rule1: Sum the bet amount by the acct and areaCode in the
                                                         * past 15 days
                                                         * select Acct, GamingDt, CasinoCode, DeptCode, GameCode,
                                                         * RatingCategory, LocnInfo3, LocnInfo4, AreaCode, sum(bet) as
                                                         * sum_bet
                                                         * from Rating // new rating model
                                                         * where PostDtm > current_date - interval '15' day
                                                         * group by Acct, GamingDt, CasinoCode, DeptCode, GameCode,
                                                         * RatingCategory, LocnInfo3, LocnInfo4, AreaCode
                                                         * 
                                                         * 
                                                         * 2. Rule2: Sum the bet amount by the acct and areaCode in the
                                                         * past 3 minutes
                                                         * select Acct, GamingDt, CasinoCode, DeptCode, GameCode,
                                                         * RatingCategory, LocnInfo3, LocnInfo4, AreaCode, sum(bet) as
                                                         * sum_bet
                                                         * from Rating
                                                         * where PostDtm > current_date - interval '3' minutes
                                                         * group by Acct, GamingDt, CasinoCode, DeptCode, GameCode,
                                                         * RatingCategory, LocnInfo3, LocnInfo4, AreaCode
                                                         * 
                                                         * 
                                                         * 3. Rule3: Sum the CasinoWin by the acct, casinoCode and
                                                         * areaCode in every day
                                                         * select Acct, GamingDt, CasinoCode, AreaCode, sum(CasinoWin)
                                                         * as sum_casinoWin
                                                         * from Rating
                                                         * group by Acct, GamingDt, CasinoCode, AreaCode
                                                         * 
                                                         * 
                                                         * 4. Rule4: Sum the CasinoWin by the acct, casinoCode and
                                                         * areaCode in the past 3 minutes
                                                         * select Acct, GamingDt, CasinoCode, AreaCode, sum(CasinoWin)
                                                         * as sum_casinoWin
                                                         * from Rating
                                                         * where PostDtm > current_date - interval '3' minutes
                                                         * group by Acct, GamingDt, CasinoCode, AreaCode
                                                         * 
                                                         * 
                                                         * 5. Rule5:
                                                         * Sum the TheorWin by the acct and casinoCode in every day
                                                         * select Acct, GamingDt, CasinoCode, sum(TheorWin) as
                                                         * sum_casinoWin
                                                         * from Rating
                                                         * group by Acct, GamingDt, CasinoCode
                                                         */
                                                }
                                        }) {
                                @Override
                                public ChangeStreamIterable<Document> initChangeStream(List<Bson> p) {
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
                filters.add(Filters.eq("bucketSize", bucketSize));
                filters.add(Filters.eq("bucketDt", bucketDt));
                return new UpdateOneModel<Document>(
                                Filters.and(filters),
                                Updates.combine(
                                                Updates.combine(updates),
                                                Updates.set("bucketDt",
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