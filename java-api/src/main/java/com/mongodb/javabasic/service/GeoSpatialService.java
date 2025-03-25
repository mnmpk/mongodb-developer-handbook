package com.mongodb.javabasic.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.geotools.referencing.GeodeticCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

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

    public List<Stop> getStops(double lat, double lng) {
        return mongoTemplate.getCollection(mongoTemplate.getCollectionName(Stop.class)).withDocumentClass(Stop.class)
                .find(Filters.nearSphere("location", new Point(new Position(lng, lat)), 500d, 0d))
                .into(new ArrayList<>());
    }

    public List<Route> getRoutes(double lat, double lng) {
        int searchThreshold = configService.getConfig("SEARCH_THRESHOLD", Integer.class).get(0);

        return aggregationService.getPipelineResults(mongoTemplate.getCollectionName(Route.class), "direct-route.json",
                Route.class, Map.of("lat", lat, "lng", lng, "maxDistance", searchThreshold));
    }

    public List<Suggestion> getRouteSuggestions(List<Route> startRoutes, List<Route> endRoutes) {
        int searchThreshold = configService.getConfig("SEARCH_THRESHOLD", Integer.class).get(0);
        int maxSuggestions = configService.getConfig("MAX_SUGGESTIONS", Integer.class).get(0);
        List<Suggestion> suggestions = new ArrayList<>();

        // For 2T
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

        Map<String, Suggestion> map = new HashMap<>();

        for (Route r : startRoutes) {
            List<Position> rStopList = new ArrayList<>();
            r.getStops().stream().forEach(s -> rStopList.add(s.getLocation().getCoordinates()));

            for (Route r2 : endRoutes) {
                // Direct route
                if (r.getRouteId().equals(r2.getRouteId()) &&
                        r.getRouteSeq().equals(r2.getRouteSeq()) &&
                        r.getStartIndex() < r2.getStartIndex()) {
                    r.setEndIndex(r2.getStartIndex());
                    logger.info("adding direct route:" + r.getRouteId());
                    suggestions.add(Suggestion.builder().transferStops(List.of()).legs(List.of(r)).build());
                }

                if (suggestions.size() < maxSuggestions) {
                    // 1T
                    for (int i = 0; i < r2.getStops().size(); i++) {
                        Stop s = r2.getStops().get(i);
                        Position nearestMatch = null;
                        double distance = searchThreshold;
                        for (int j = 0; j < rStopList.size(); j++) {
                            double d = getDistance(rStopList.get(j), s.getLocation().getCoordinates());
                            if (d < distance) {
                                nearestMatch = rStopList.get(j);
                                distance = d;
                            }
                        }
                        if (nearestMatch != null) {
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
                                    .endIndex(rStopList.indexOf(nearestMatch)).build();
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
                                    &&
                                    !map.containsKey(key)) {
                                map.put(key,
                                        Suggestion.builder().transferStops(List.of(s)).legs(List.of(tr1,
                                                tr2)).build());
                                logger.info("adding 1T route " + key);

                                if (map.size() >= maxSuggestions) {
                                    break;
                                }
                            }
                            // For 2T
                            transfer1StopMap.remove(s.getLocation().getCoordinates());
                            transfer2StopMap.remove(s.getLocation().getCoordinates());
                        }
                    }
                } else {
                    break;
                }
            }
            if (map.size() >= maxSuggestions) {
                break;
            }
        }
        suggestions.addAll(map.values());

        // 2T
        if (suggestions.size() < maxSuggestions)
            this.get2T(suggestions, transfer1StopMap, transfer2StopMap);

        return suggestions;
    }

    private void get2T(List<Suggestion> suggestions, Map<Position, List<Route>> transfer1Stops,
            Map<Position, List<Route>> transfer2Stops) {
        int maxSuggestions = configService.getConfig("MAX_SUGGESTIONS", Integer.class).get(0);
        int walkThreshold = configService.getConfig("TRANSFER_WALK_THRESHOLD", Integer.class).get(0);

        List<Route> intermediateRoutes = mongoTemplate.getCollection(mongoTemplate.getCollectionName(Route.class))
                .withDocumentClass(Route.class)
                .find(Filters.geoIntersects("stops.location",
                        new MultiPoint(transfer1Stops.keySet().stream().collect(Collectors.toList()))))
                .into(new ArrayList<>());
        logger.info("transfer1Stops:" + transfer1Stops.size() + " transfer2Stops:" + transfer2Stops.size()
                + " intermediateRoutes:" + intermediateRoutes.size());
        Map<String, Suggestion> map = new HashMap<>();
        for (Route r : intermediateRoutes) {
            Map<Position, Stop> startRouteConnectedStops = new LinkedHashMap<>();
            Map<Position, Stop> endRouteConnectedStops = new LinkedHashMap<>();
            for (Stop s : r.getStops()) {
                for (Position p : transfer1Stops.keySet()) {
                    if (getDistance(p, s.getLocation().getCoordinates()) < walkThreshold) {
                        startRouteConnectedStops.put(p, s);
                    }
                }
                for (Position p : transfer2Stops.keySet()) {
                    if (getDistance(p, s.getLocation().getCoordinates()) < walkThreshold) {
                        endRouteConnectedStops.put(p, s);
                    }
                }
            }
            if (startRouteConnectedStops.size() > 0 && endRouteConnectedStops.size() > 0) {
                for (Position ps : startRouteConnectedStops.keySet()) {
                    for (Position pe : endRouteConnectedStops.keySet()) {
                        int startIndex = r.getStops().indexOf(startRouteConnectedStops.get(ps));
                        int endIndex = r.getStops().indexOf(endRouteConnectedStops.get(pe));
                        if (startIndex < endIndex) {
                            for (Route sr : transfer1Stops.get(ps)) {
                                for (Route er : transfer2Stops.get(pe)) {
                                    String key = sr.getRouteId() + ">" + r.getRouteId()
                                            + ">" + er.getRouteId();
                                    if (!map.containsKey(key)) {

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
                                                Suggestion.builder()
                                                        .transferStops(List.of(startRouteConnectedStops.get(ps),
                                                                endRouteConnectedStops.get(pe)))
                                                        .legs(List.of(sr, r2, er)).build());

                                        logger.info("adding 2T route " + key);
                                        if (map.size() >= maxSuggestions) {
                                            break;
                                        }
                                    }
                                }
                                if (map.size() >= maxSuggestions) {
                                    break;
                                }
                            }
                        }
                        if (map.size() >= maxSuggestions) {
                            break;
                        }
                    }
                    if (map.size() >= maxSuggestions) {
                        break;
                    }
                }
                if (map.size() >= maxSuggestions) {
                    break;
                }
            }
        }

        suggestions.addAll(map.values());
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
