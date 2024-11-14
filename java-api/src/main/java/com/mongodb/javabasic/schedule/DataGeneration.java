package com.mongodb.javabasic.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.TransactionBody;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.javabasic.model.Area;
import com.mongodb.javabasic.model.Award;
import com.mongodb.javabasic.model.Casino;
import com.mongodb.javabasic.model.Dept;
import com.mongodb.javabasic.model.Game;
import com.mongodb.javabasic.model.Location;
import com.mongodb.javabasic.model.PlayerCard;
import com.mongodb.javabasic.model.PlayerComp;
import com.mongodb.javabasic.model.PlayerPoint;
import com.mongodb.javabasic.model.TableRating;
import com.mongodb.javabasic.service.GenerativeService;
import com.mongodb.client.ClientSession;

import jakarta.annotation.PostConstruct;

@Component
public class DataGeneration {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    GenerativeService<PlayerCard> playerCardGenService;
    @Autowired
    GenerativeService<Location> locationGenService;
    @Autowired
    GenerativeService<Casino> casinoGenService;
    @Autowired
    GenerativeService<Dept> deptGenService;
    @Autowired
    GenerativeService<Area> areaGenService;

    @Autowired
    GenerativeService<Game> gameGenService;
    @Autowired
    GenerativeService<TableRating> tableRatingGenService;
    @Autowired
    GenerativeService<Award> awardGenService;
    @Autowired
    GenerativeService<PlayerPoint> playerPointGenService;
    @Autowired
    GenerativeService<PlayerComp> playerCompGenService;

    @Autowired
    MongoClient mongoClient;

    @Autowired
    MongoTemplate mongoTemplate;

    @PostConstruct
    public void init() throws Exception {
        long MAX_PLAYER = 10;
        long MAX_CASINO = 2;
        long AREA_PER_CASINO = 4;
        long DEPT_PER_CASINO = 5;
        long LOCN_PER_CASINO = 100;

        long playerCount = mongoTemplate.getCollection("tPlayerCard").countDocuments();
        long casinoCount = mongoTemplate.getCollection("tCasino").countDocuments();
        if (playerCount <= 10) {
            for (long i = playerCount; i < MAX_PLAYER; i++) {
                PlayerCard player = playerCardGenService.generateRandom(PlayerCard.class);
                mongoTemplate.getCollection("tPlayerCard").withDocumentClass(PlayerCard.class).insertOne(player);
            }
        }
        if (mongoTemplate.getCollection("tCasino").countDocuments() <= MAX_CASINO) {
            for (long i = casinoCount; i < MAX_CASINO; i++) {
                Casino casino = casinoGenService.generateRandom(Casino.class);
                mongoTemplate.getCollection("tCasino").withDocumentClass(Casino.class).insertOne(casino);

                long deptCount = mongoTemplate.getCollection("tDept")
                        .countDocuments(Filters.eq("casinoId", casino.getCasinoId()));
                long areaCount = mongoTemplate.getCollection("tArea")
                        .countDocuments(Filters.eq("casinoId", casino.getCasinoId()));

                List<Integer> deptList = new ArrayList<>();
                List<Integer> areaList = new ArrayList<>();
                if (deptCount <= DEPT_PER_CASINO) {
                    for (long j = deptCount; j < DEPT_PER_CASINO; j++) {
                        Dept dept = deptGenService.generateRandom(Dept.class);
                        mongoTemplate.getCollection("tDept").withDocumentClass(Dept.class).insertOne(dept);
                        deptList.add(dept.getDeptId());
                    }
                }
                if (areaCount <= AREA_PER_CASINO) {
                    for (long j = areaCount; j < AREA_PER_CASINO; j++) {
                        Area area = areaGenService.generateRandom(Area.class);
                        mongoTemplate.getCollection("tArea").withDocumentClass(Area.class).insertOne(area);
                        areaList.add(area.getAreaId());
                    }
                }

                long locationCount = mongoTemplate.getCollection("tLocn")
                        .countDocuments(Filters.eq("casinoId", casino.getCasinoId()));
                if (locationCount <= LOCN_PER_CASINO) {
                    for (long j = locationCount; j < LOCN_PER_CASINO; j++) {
                        Location location = locationGenService.generateRandom(Location.class);
                        location.setAreaId(areaList.get(new Random().nextInt(areaList.size())));
                        location.setDeptId(deptList.get(new Random().nextInt(deptList.size())));
                        mongoTemplate.getCollection("tLocn").withDocumentClass(Location.class).insertOne(location);
                    }
                }
            }
        }
    }

    @Scheduled(fixedRate = 10000)
    public void game() {
        logger.info("data generated at {}", LocalDateTime.now());
        ClientSession clientSession = mongoClient.startSession();
        try (clientSession) {
            TransactionOptions txnOptions = TransactionOptions.builder()
                    .readPreference(ReadPreference.primary())
                    .readConcern(ReadConcern.MAJORITY)
                    .writeConcern(WriteConcern.MAJORITY)
                    .build();
            TransactionBody<Void> txnBody = () -> {

                PlayerCard player = mongoTemplate.getCollection("tPlayerCard").withDocumentClass(PlayerCard.class)
                        .aggregate(List.of(Aggregates.sample(1))).first();
                Casino casino = mongoTemplate.getCollection("tCasino").withDocumentClass(Casino.class)
                        .aggregate(List.of(Aggregates.sample(1))).first();
                Dept dept = mongoTemplate.getCollection("tDept").withDocumentClass(Dept.class)
                        .aggregate(List.of(Aggregates.sample(1))).first();
                Area area = mongoTemplate.getCollection("tArea").withDocumentClass(Area.class)
                        .aggregate(List.of(Aggregates.sample(1))).first();
                Location location = mongoTemplate.getCollection("tLocn").withDocumentClass(Location.class)
                        .aggregate(List.of(Aggregates.sample(1))).first();

                TableRating tr = tableRatingGenService.generateRandom(TableRating.class);
                tr.setCasinoID(casino.getCasinoId());
                tr.setDeptID(dept.getDeptId());
                tr.setAreaID(area.getAreaId());
                tr.setLocnID(location.getLocnId());
                tr.setPlayerId(player.getPlayerId());
                tr.setGamingDt(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                tr.setPostDtm(new Date());
                mongoTemplate.getCollection("tTableRating").withDocumentClass(TableRating.class)
                        .insertOne(clientSession, tr);

                Game g = gameGenService.generateRandom(Game.class);
                g.setGameId(tr.getGameID());
                mongoTemplate.getCollection("tGame").withDocumentClass(Game.class).insertOne(clientSession, g);
                Award a = awardGenService.generateRandom(Award.class);
                a.setRelatedTranId(tr.getTranID());
                a.setPlayerId(tr.getPlayerId());
                mongoTemplate.getCollection("tAwards").withDocumentClass(Award.class).insertOne(clientSession, a);
                PlayerPoint point = playerPointGenService.generateRandom(PlayerPoint.class);
                point.setTranId(a.getTranId());
                point.setPlayerId(tr.getPlayerId());
                mongoTemplate.getCollection("tPlayerPoints").withDocumentClass(PlayerPoint.class)
                        .insertOne(clientSession, point);
                PlayerComp comp = playerCompGenService.generateRandom(PlayerComp.class);
                comp.setTranId(a.getTranId());
                comp.setPlayerId(tr.getPlayerId());
                mongoTemplate.getCollection("tPlayerComps").withDocumentClass(PlayerComp.class).insertOne(clientSession,
                        comp);
                return null;
            };
            clientSession.withTransaction(txnBody, txnOptions);
        } catch (RuntimeException e) {
            logger.error("Error during transfer, errorMsg={}, errorCause={}, errorStackTrace={}", e.getMessage(),
                    e.getCause(), e.getStackTrace(), e);
        }

    }
}
