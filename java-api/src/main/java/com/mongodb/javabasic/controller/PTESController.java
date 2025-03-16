package com.mongodb.javabasic.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.geotools.measure.Measure;
import org.geotools.referencing.GeodeticCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.javabasic.repositories.RouteRepository;
import com.mongodb.javabasic.repositories.StopRepository;
import com.mongodb.javabasic.service.AggregationService;

import si.uom.SI;

import com.mongodb.javabasic.model.Stop;
import com.mongodb.javabasic.model.Suggestion;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.geojson.MultiPoint;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.mongodb.javabasic.KMeans;
import com.mongodb.javabasic.model.Route;

@RestController
@RequestMapping(path = "/ptes")
public class PTESController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final int SEARCH_THRESHOLD = 500;
    private static final int TRANSFER_WALK_THRESHOLD = 100;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AggregationService aggregationService;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StopRepository stopRepository;

    @GetMapping("/stops")
    public List<Stop> getStops(@RequestParam("lat") double lat, @RequestParam("lng") double lng) {
        return mongoTemplate.getCollection(mongoTemplate.getCollectionName(Stop.class)).withDocumentClass(Stop.class)
                .find(Filters.nearSphere("location", new Point(new Position(lng, lat)), 500d, 0d))
                .into(new ArrayList<>());
        // return stopRepository.findAll();
    }

    /*
     * @GetMapping("/routes")
     * public List<Route> getRoutes() {
     * return routeRepository.findAll();
     * }
     */

    @GetMapping("/routes")
    public List<Route> getRoutes(@RequestParam("lat") double lat, @RequestParam("lng") double lng) {
        return aggregationService.getPipelineResults(mongoTemplate.getCollectionName(Route.class), "direct-route.json",
                Route.class, Map.of("lat", lat, "lng", lng, "maxDistance", SEARCH_THRESHOLD));
        // return
        // mongoTemplate.getCollection(mongoTemplate.getCollectionName(Route.class)).withDocumentClass(Route.class)
        // .find(Filters.nearSphere("stops.location", new Point(new Position(lng, lat)),
        // 200d, 0d)).into(new ArrayList<>());
    }

    @PostMapping("/routes")
    public List<Suggestion> getRoutes(double[] start, double[] end) {
        List<Route> startRoutes = this.getRoutes(start[1], start[0]);
        List<Route> endRoutes = this.getRoutes(end[1], end[0]);
        List<Suggestion> suggestions = new ArrayList<>();

        suggestions.addAll(this.getDirectRoutesAnd1T(startRoutes, endRoutes));

        // suggestions.addAll(this.get1TRoutes(startRoutes, endRoutes));
        // suggestions.addAll(this.get2TRoutes());
        return suggestions;
    }

    private List<Suggestion> getDirectRoutesAnd1T(List<Route> startRoutes, List<Route> endRoutes) {
        List<Suggestion> suggestions = new ArrayList<>();

        // For 2T
        final Map<Position, List<Route>> transfer1StopMap = new ConcurrentHashMap<>();
        startRoutes.forEach(r -> {
            for (int i = r.getStartIndex(); i < r.getStops().size(); i++) {
                Stop s = r.getStops().get(i);
                if (!transfer1StopMap.containsKey(s.getLocation().getCoordinates()))
                    transfer1StopMap.put(s.getLocation().getCoordinates(), new ArrayList<>());
                transfer1StopMap.get(s.getLocation().getCoordinates()).add(
                        Route.builder().route(r.getRoute()).bound(r.getBound())
                                .serviceType(r.getServiceType()).stops(r.getStops()).endIndex(i).build());
            }
        });
        logger.info("startRoute stop size:" + transfer1StopMap.size());
        final Map<Position, List<Route>> transfer2StopMap = new ConcurrentHashMap<>();
        endRoutes.forEach(r -> {
            for (int i = 0; i <= r.getStartIndex(); i++) {
                Stop s = r.getStops().get(i);
                if (!transfer2StopMap.containsKey(s.getLocation().getCoordinates()))
                    transfer2StopMap.put(s.getLocation().getCoordinates(), new ArrayList<>());
                transfer2StopMap.get(s.getLocation().getCoordinates()).add(
                        Route.builder().route(r.getRoute()).bound(r.getBound())
                                .serviceType(r.getServiceType()).stops(r.getStops()).startIndex(i)
                                .endIndex(r.getStartIndex()).build());
            }
        });
        logger.info("endRoute stop size:" + transfer2StopMap.size());

        Map<String, Suggestion> map = new HashMap<>();

        startRoutes.forEach(r -> {
            List<Position> rStopList = new ArrayList<>();
            r.getStops().stream().forEach(s -> rStopList.add(s.getLocation().getCoordinates()));

            endRoutes.forEach(r2 -> {
                // Direct route
                if (r.getRoute().equalsIgnoreCase(r2.getRoute()) &&
                        r.getBound().equalsIgnoreCase(r2.getBound()) &&
                        r.getServiceType().equalsIgnoreCase(r2.getServiceType()) &&
                        r.getStartIndex() < r2.getStartIndex()) {
                    r.setEndIndex(r2.getStartIndex());
                    logger.info("adding direct route:"+r.getRoute()+"-"+r.getServiceType());
                    suggestions.add(Suggestion.builder().transferStops(List.of()).legs(List.of(r)).build());
                }

                // 1T
                for (int i = 0; i < r2.getStops().size(); i++) {
                    Stop s = r2.getStops().get(i);
                    if (rStopList.stream().anyMatch(ss -> getDistance(ss, s.getLocation().getCoordinates()) < 500)) {
                        Route tr1 = Route.builder().route(r.getRoute()).bound(r.getBound())
                                .serviceType(r.getServiceType())
                                .stops(r.getStops()).startIndex(r.getStartIndex())
                                .endIndex(rStopList.indexOf(s.getLocation().getCoordinates())).build();
                        Route tr2 = Route.builder().route(r2.getRoute()).bound(r2.getBound())
                                .serviceType(r2.getServiceType()).stops(r2.getStops()).startIndex(i)
                                .endIndex(r2.getStartIndex()).build();
                        String key = tr1.getRoute() + "-" + tr1.getServiceType() + ">" + tr2.getRoute()
                                + "-" + tr2.getServiceType();
                        if (!(tr1.getRoute().equalsIgnoreCase(tr2.getRoute()) &&
                                !tr1.getBound().equalsIgnoreCase(tr2.getBound()) &&
                                !tr1.getServiceType().equalsIgnoreCase(tr2.getServiceType())) &&
                                tr1.getStartIndex() < tr1.getEndIndex() && tr2.getStartIndex() < tr2.getEndIndex() &&
                                !map.containsKey(key)) {

                            map.put(key,
                                    Suggestion.builder().transferStops(List.of(s)).legs(List.of(tr1,
                                            tr2)).build());
                            logger.info("adding 1T route " + key);

                        }
                        // For 2T
                        transfer1StopMap.remove(s.getLocation().getCoordinates());
                        transfer2StopMap.remove(s.getLocation().getCoordinates());
                    }
                }
            });
        });
        suggestions.addAll(map.values());

        logger.info("startRoute stop size:" + transfer1StopMap.size());
        logger.info("endRoute stop size:" + transfer2StopMap.size());

        // 2T
        if (suggestions.size() < 10)
            this.get2T(suggestions, transfer1StopMap, transfer2StopMap);

        return suggestions;
    }

    private void get2T(List<Suggestion> suggestions, Map<Position, List<Route>> transfer1Stops,
            Map<Position, List<Route>> transfer2Stops) {
        List<Route> intermediateRoutes = mongoTemplate.getCollection(mongoTemplate.getCollectionName(Route.class))
                .withDocumentClass(Route.class)
                .find(Filters.geoIntersects("stops.location",
                        new MultiPoint(transfer1Stops.keySet().stream().collect(Collectors.toList()))))
                .into(new ArrayList<>());
        logger.info("transfer1Stops:" + transfer1Stops.size() + " transfer2Stops:" + transfer2Stops.size()
                + " intermediateRoutes:" + intermediateRoutes.size());
        Map<String, Suggestion> map = new HashMap<>();
        intermediateRoutes.stream()
                .forEach(r -> {
                    Map<Position, Stop> startRouteConnectedStops = new LinkedHashMap<>();
                    Map<Position, Stop> endRouteConnectedStops = new LinkedHashMap<>();
                    for (Stop s : r.getStops()) {
                        for (Position p : transfer1Stops.keySet()) {
                            if (getDistance(p, s.getLocation().getCoordinates()) < TRANSFER_WALK_THRESHOLD) {
                                startRouteConnectedStops.put(p, s);
                            }
                        }
                        for (Position p : transfer2Stops.keySet()) {
                            if (getDistance(p, s.getLocation().getCoordinates()) < TRANSFER_WALK_THRESHOLD) {
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
                                            String key = sr.getRoute() + "-" + sr.getServiceType() + ">" + r.getRoute()
                                                    + "-" + r.getServiceType() + ">" + er.getRoute() + "-"
                                                    + er.getServiceType();
                                            if (!map.containsKey(key)) {

                                                Route r2 = Route.builder().route(r.getRoute()).bound(r.getBound())
                                                        .serviceType(r.getServiceType()).stops(r.getStops())
                                                        .startIndex(startIndex)
                                                        .endIndex(endIndex).build();

                                                map.put(key,
                                                        Suggestion.builder()
                                                                .transferStops(List.of(startRouteConnectedStops.get(ps),
                                                                        endRouteConnectedStops.get(pe)))
                                                                .legs(List.of(sr, r2, er)).build());

                                                logger.info("adding 2T route " + key);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                });

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

    private List<Suggestion> get2TRoutes() {
        mongoTemplate.getCollection(mongoTemplate.getCollectionName(Route.class));
        return List.of();
    }
}
