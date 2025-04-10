package com.mongodb.javabasic.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.geotools.referencing.GeodeticCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.geojson.MultiPoint;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.mongodb.javabasic.model.Route;
import com.mongodb.javabasic.model.Stop;
import com.mongodb.javabasic.model.Suggestion;

@Service
public class GeoSpatialService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AggregationService aggregationService;
    @Autowired
    private ConfigService<Integer> configService;
    @Autowired
    private ConfigService<Boolean> bConfigService;

    public List<Stop> getStops(double lat, double lng) {
        Integer searchThreshold = configService.getConfig("SEARCH_THRESHOLD", Integer.class);
        return mongoTemplate.getCollection(mongoTemplate.getCollectionName(Stop.class)).withDocumentClass(Stop.class)
                .find(Filters.nearSphere("location", new Point(new Position(lng, lat)), (double) searchThreshold, 0d))
                .into(new ArrayList<>());
    }

    public List<Route> getRoutes(double lat, double lng) {
        Integer searchThreshold = configService.getConfig("SEARCH_THRESHOLD", Integer.class);

        return aggregationService.getPipelineResults(mongoTemplate.getCollectionName(Route.class), "direct-route.json",
                Route.class, Map.of("lat", lat, "lng", lng, "maxDistance", searchThreshold));
    }

    public List<Suggestion> getRouteSuggestions(List<Route> startRoutes, List<Route> endRoutes) {
        Integer searchThreshold = configService.getConfig("SEARCH_THRESHOLD", Integer.class);
        Integer maxSuggestions = configService.getConfig("MAX_SUGGESTIONS", Integer.class);
        List<Suggestion> suggestions = new ArrayList<>();

        StopWatch sw = new StopWatch();

        sw.start("Direct route");
        Map<String, Suggestion> map = new ConcurrentHashMap<>();
        Set<Position> excludePositions = ConcurrentHashMap.newKeySet();

        var cfs = new ArrayList<CompletableFuture<Void>>();
        for (Route r : startRoutes) {
            cfs.add(CompletableFuture.runAsync(() -> {
                for (Route r2 : endRoutes) {
                    // Direct route
                    if (r.getRouteId().equals(r2.getRouteId()) &&
                            r.getRouteSeq().equals(r2.getRouteSeq()) &&
                            r.getStartIndex() < r2.getStartIndex()) {
                        r.setEndIndex(r2.getStartIndex());
                        logger.info("adding direct route:" + r.getRouteId());
                        suggestions.add(Suggestion.builder().transferStops(List.of()).legs(List.of(r)).build());
                    }
                }
            }));
        }
        CompletableFuture.allOf(cfs.toArray(new CompletableFuture<?>[cfs.size()])).join();
        logger.info("Direct route:" + suggestions.size());
        sw.stop();

        if (suggestions.size() < maxSuggestions) {
            sw.start("1T");
            for (Route r : startRoutes) {
                cfs = new ArrayList<CompletableFuture<Void>>();
                for (Route r2 : endRoutes) {
                    cfs.add(CompletableFuture.runAsync(() -> {
                        // 1T
                        this.get1T(map, excludePositions, r, r2, searchThreshold, maxSuggestions);
                    }));
                }
                CompletableFuture.allOf(cfs.toArray(new CompletableFuture<?>[cfs.size()])).join();
                if (suggestions.size() + map.size() >= maxSuggestions) {
                    break;
                }
            }
            suggestions.addAll(new ArrayList<>(map.values()).subList(0,
                    Math.min(map.size(), maxSuggestions - suggestions.size())));
            sw.stop();
        }
        Boolean is2TEnable = bConfigService.getConfig("2T_ENABLE", Boolean.class);
        // 2T
        if (suggestions.size() < maxSuggestions && (is2TEnable != null && is2TEnable)) {
            sw.start("2T");
            this.get2T(suggestions, startRoutes, endRoutes, maxSuggestions);
            sw.stop();
        }

        logger.info(sw.prettyPrint());
        return suggestions;
    }

    private void get1T(Map<String, Suggestion> map, Set<Position> excludePositions, Route r, Route r2,
            double searchThreshold, double maxSuggestions) {
        for (int i = 0; i < r2.getStops().size(); i++) {
            Stop s = r2.getStops().get(i);
            double distance = searchThreshold;
            int matchedIndex = -1;
            for (int j = 0; j < r.getStops().size(); j++) {
                Stop s2 = r.getStops().get(j);
                double d = getDistance(s2.getLocation().getCoordinates(), s.getLocation().getCoordinates());
                if (d < distance) {
                    matchedIndex = j;
                    distance = d;
                }
            }
            if (matchedIndex >= 0) {
                Route tr1 = Route.builder().routeId(r.getRouteId()).routeSeq(r.getRouteSeq())
                        .routeType(r.getRouteType())
                        .serviceMode(r.getServiceMode()).serviceType(r.getServiceType())
                        .nameEn(r.getNameEn()).nameSc(r.getNameSc())
                        .nameTc(r.getNameTc()).locStartNamec(r.getLocStartNamec())
                        .locStartNames(r.getLocStartNames())
                        .locStartNamee(r.getLocStartNamee()).locEndNamec(r.getLocEndNamec())
                        .locEndNames(r.getLocEndNames())
                        .locEndNamee(r.getLocEndNamee()).companyCode(r.getCompanyCode())
                        .isCircular(r.isCircular())
                        .journeyTime(r.getJourneyTime()).operationMode(r.getOperationMode())
                        .stops(r.getStops()).startIndex(r.getStartIndex())
                        .endIndex(matchedIndex).build();
                Route tr2 = Route.builder().routeId(r2.getRouteId()).routeSeq(r2.getRouteSeq())
                        .routeType(r2.getRouteType())
                        .serviceMode(r2.getServiceMode()).serviceType(r2.getServiceType())
                        .nameEn(r2.getNameEn()).nameSc(r2.getNameSc())
                        .nameTc(r2.getNameTc()).locStartNamec(r2.getLocStartNamec())
                        .locStartNames(r2.getLocStartNames())
                        .locStartNamee(r2.getLocStartNamee()).locEndNamec(r2.getLocEndNamec())
                        .locEndNames(r2.getLocEndNames())
                        .locEndNamee(r2.getLocEndNamee()).companyCode(r2.getCompanyCode())
                        .isCircular(r2.isCircular())
                        .journeyTime(r2.getJourneyTime()).operationMode(r2.getOperationMode())
                        .stops(r2.getStops()).startIndex(i)
                        .endIndex(r2.getStartIndex()).build();
                String key = tr1.getRouteId() + ">" + tr2.getRouteId();
                if (!(tr1.getRouteId().equals(tr2.getRouteId()) &&
                        tr1.getRouteSeq().equals(tr2.getRouteSeq())) &&
                        tr1.getStartIndex() <= tr1.getEndIndex() && tr2.getStartIndex() <= tr2.getEndIndex()
                        && (!map.containsKey(key) || distance < map.get(key).getTransferDistance())) {
                    map.put(key,
                            Suggestion.builder().transferDistance(distance).transferStops(List.of(s))
                                    .legs(List.of(tr1,
                                            tr2))
                                    .build());
                    logger.info("adding 1T route " + key);
                }
                // For 2T
                excludePositions.add(s.getLocation().getCoordinates());
            }
        }
    }

    private void get2T(List<Suggestion> suggestions, List<Route> startRoutes,
            List<Route> endRoutes, int maxSuggestions) {
        StopWatch sw = new StopWatch();
        sw.start("getWalkThreshold");
        int walkThreshold = configService.getConfig("TRANSFER_WALK_THRESHOLD", Integer.class);
        sw.stop();
        // For 2T
        sw.start("transfer1StopMap");
        final Map<Position, List<Route>> transfer1StopMap = new ConcurrentHashMap<>();
        startRoutes.forEach(r -> {
            for (int i = r.getStartIndex(); i < r.getStops().size(); i++) {
                Stop s = r.getStops().get(i);
                if (!transfer1StopMap.containsKey(s.getLocation().getCoordinates()))
                    transfer1StopMap.put(s.getLocation().getCoordinates(), new ArrayList<>());
                transfer1StopMap.get(s.getLocation().getCoordinates()).add(
                        Route.builder().routeId(r.getRouteId()).routeSeq(r.getRouteSeq()).routeType(r.getRouteType())
                                .serviceMode(r.getServiceMode()).serviceType(r.getServiceType())
                                .nameEn(r.getNameEn()).nameSc(r.getNameSc())
                                .nameTc(r.getNameTc()).locStartNamec(r.getLocStartNamec())
                                .locStartNames(r.getLocStartNames())
                                .locStartNamee(r.getLocStartNamee()).locEndNamec(r.getLocEndNamec())
                                .locEndNames(r.getLocEndNames())
                                .locEndNamee(r.getLocEndNamee()).companyCode(r.getCompanyCode())
                                .isCircular(r.isCircular())
                                .journeyTime(r.getJourneyTime()).operationMode(r.getOperationMode())
                                .stops(r.getStops())
                                .startIndex(r.getStartIndex())
                                .endIndex(i).build());
            }
        });
        sw.stop();
        sw.start("transfer2StopMap");
        final Map<Position, List<Route>> transfer2StopMap = new ConcurrentHashMap<>();
        endRoutes.forEach(r -> {
            for (int i = 0; i <= r.getStartIndex(); i++) {
                Stop s = r.getStops().get(i);
                if (!transfer2StopMap.containsKey(s.getLocation().getCoordinates()))
                    transfer2StopMap.put(s.getLocation().getCoordinates(), new ArrayList<>());
                transfer2StopMap.get(s.getLocation().getCoordinates()).add(
                        Route.builder().routeId(r.getRouteId()).routeSeq(r.getRouteSeq()).routeType(r.getRouteType())
                                .serviceMode(r.getServiceMode()).serviceType(r.getServiceType())
                                .nameEn(r.getNameEn()).nameSc(r.getNameSc())
                                .nameTc(r.getNameTc()).locStartNamec(r.getLocStartNamec())
                                .locStartNames(r.getLocStartNames())
                                .locStartNamee(r.getLocStartNamee()).locEndNamec(r.getLocEndNamec())
                                .locEndNames(r.getLocEndNames())
                                .locEndNamee(r.getLocEndNamee()).companyCode(r.getCompanyCode())
                                .isCircular(r.isCircular())
                                .journeyTime(r.getJourneyTime()).operationMode(r.getOperationMode()).stops(r.getStops())
                                .startIndex(i)
                                .endIndex(r.getStartIndex()).build());
            }
        });
        sw.stop();
        sw.start("intermediateRoutes");
        List<Route> intermediateRoutes = mongoTemplate.getCollection(mongoTemplate.getCollectionName(Route.class))
                .withDocumentClass(Route.class)
                .find(Filters.geoIntersects("stops.location",
                        new MultiPoint(transfer1StopMap.keySet().stream().collect(Collectors.toList()))))
                .into(new ArrayList<>());
        logger.info("transfer1Stops:" + transfer1StopMap.size() + " transfer2Stops:" + transfer2StopMap.size()
                + " intermediateRoutes:" + intermediateRoutes.size());
        sw.stop();
        Map<String, Suggestion> map = new ConcurrentHashMap<>();
        var cfs = new ArrayList<CompletableFuture<Void>>();
        sw.start("process intermediateRoutes");
        for (Route r : intermediateRoutes) {
            cfs.add(CompletableFuture.runAsync(() -> {
                Map<Position, Pair<Double, Stop>> startRouteConnectedStops = new LinkedHashMap<>();
                Map<Position, Pair<Double, Stop>> endRouteConnectedStops = new LinkedHashMap<>();
                for (Stop s : r.getStops()) {
                    for (Position p : transfer1StopMap.keySet()) {
                        double d = getDistance(p, s.getLocation().getCoordinates());
                        if (d < walkThreshold) {
                            startRouteConnectedStops.put(p, Pair.of(d, s));
                        }
                    }
                    for (Position p : transfer2StopMap.keySet()) {
                        double d = getDistance(p, s.getLocation().getCoordinates());
                        if (d < walkThreshold) {
                            endRouteConnectedStops.put(p, Pair.of(d, s));
                        }
                    }
                }
                if (startRouteConnectedStops.size() > 0 && endRouteConnectedStops.size() > 0) {
                    for (Position ps : startRouteConnectedStops.keySet()) {
                        for (Position pe : endRouteConnectedStops.keySet()) {
                            Pair<Double, Stop> startStop = startRouteConnectedStops.get(ps);
                            Pair<Double, Stop> endStop = endRouteConnectedStops.get(pe);
                            int startIndex = r.getStops().indexOf(startStop.getRight());
                            int endIndex = r.getStops().indexOf(endStop.getRight());
                            if (startIndex < endIndex) {
                                for (Route sr : transfer1StopMap.get(ps)) {
                                    for (Route er : transfer2StopMap.get(pe)) {
                                        String key = sr.getRouteId() + ">" + r.getRouteId()
                                                + ">" + er.getRouteId();
                                        double totalWalkDistance = startStop.getLeft() + endStop.getLeft();
                                        if (!map.containsKey(key)
                                                || totalWalkDistance < map.get(key).getTransferDistance()) {

                                            Route r2 = Route.builder().routeId(r.getRouteId()).routeSeq(r.getRouteSeq())
                                                    .routeType(r.getRouteType())
                                                    .serviceMode(r.getServiceMode()).serviceType(r.getServiceType())
                                                    .nameEn(r.getNameEn()).nameSc(r.getNameSc())
                                                    .nameTc(r.getNameTc()).locStartNamec(r.getLocStartNamec())
                                                    .locStartNames(r.getLocStartNames())
                                                    .locStartNamee(r.getLocStartNamee()).locEndNamec(r.getLocEndNamec())
                                                    .locEndNames(r.getLocEndNames())
                                                    .locEndNamee(r.getLocEndNamee()).companyCode(r.getCompanyCode())
                                                    .isCircular(r.isCircular())
                                                    .journeyTime(r.getJourneyTime()).operationMode(r.getOperationMode())
                                                    .stops(r.getStops())
                                                    .startIndex(startIndex)
                                                    .endIndex(endIndex).build();

                                            map.put(key,
                                                    Suggestion.builder().transferDistance(totalWalkDistance)
                                                            .transferStops(List.of(startStop.getRight(),
                                                                    endStop.getRight()))
                                                            .legs(List.of(sr, r2, er)).build());

                                            logger.info("adding 2T route " + key);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }));
            CompletableFuture.allOf(cfs.toArray(new CompletableFuture<?>[cfs.size()])).join();
            if (suggestions.size() + map.size() >= maxSuggestions) {
                break;
            }
        }
        sw.stop();
        logger.info(sw.prettyPrint());
        suggestions.addAll(
                new ArrayList<>(map.values()).subList(0, Math.min(map.size(), maxSuggestions - suggestions.size())));
    }

    private double getDistance(Position from, Position to) {
        double distance = 0.0;

        GeodeticCalculator calc = new GeodeticCalculator();
        calc.setStartingGeographicPoint(from.getValues().get(0), from.getValues().get(1));
        calc.setDestinationGeographicPoint(to.getValues().get(0), to.getValues().get(1));

        distance = calc.getOrthodromicDistance();
        double bearing = calc.getAzimuth();
        return distance;
    }
}
