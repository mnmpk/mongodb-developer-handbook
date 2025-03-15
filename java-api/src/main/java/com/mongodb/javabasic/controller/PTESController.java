package com.mongodb.javabasic.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private static final int CLUSTERING_FACTOR = 5;
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
                Route.class, Map.of("lat", lat, "lng", lng));
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
        suggestions.addAll(this.get2TRoutes());
        return suggestions;
    }

    private List<Suggestion> getDirectRoutesAnd1T(List<Route> startRoutes, List<Route> endRoutes) {
        List<Suggestion> suggestions = new ArrayList<>();

        Set<Position> intersectSet = new HashSet<>();
        startRoutes.forEach(r -> {
            List<Position> rStopList = new ArrayList<>();
            r.getStops().stream().forEach(s -> rStopList.add(s.getLocation().getPosition()));
            endRoutes.forEach(r2 -> {
                // Direct route
                if (r.getRoute().equalsIgnoreCase(r2.getRoute()) &&
                        r.getBound().equalsIgnoreCase(r2.getBound()) &&
                        r.getServiceType().equalsIgnoreCase(r2.getServiceType()) &&
                        r.getStartIndex() < r2.getStartIndex()) {
                    r.setEndIndex(r2.getStartIndex());
                    suggestions.add(Suggestion.builder().transferStops(List.of()).legs(List.of(r)).build());
                }

                // 1T with exact matching stops
                for (int i = 0; i < r2.getStops().size(); i++) {
                    Stop s = r2.getStops().get(i);
                    if (rStopList.contains(s.getLocation().getPosition())) {
                        Route tr1 = Route.builder().route(r.getRoute()).bound(r.getBound()).serviceType(r.getServiceType())
                        .stops(r.getStops()).startIndex(r.getStartIndex())
                        .endIndex(rStopList.indexOf(s.getLocation().getPosition())).build();
                        Route tr2 = Route.builder().route(r2.getRoute()).bound(r2.getBound())
                        .serviceType(r2.getServiceType()).stops(r2.getStops()).startIndex(i)
                        .endIndex(r2.getStartIndex()).build();
                        if(tr1.getStartIndex()<tr1.getEndIndex() && tr2.getStartIndex()<tr2.getEndIndex()){
                        suggestions.add(Suggestion.builder().transferStops(List.of(s)).legs(List.of(
                                tr1,
                                tr2)).build());
                        logger.debug(tr1.getRoute()+">"+tr2.getRoute()+" r1 start:"+tr1.getStartIndex()+", transfer: r1-"+rStopList.indexOf(s.getLocation().getPosition())+"|"+tr1.getEndIndex()+" r2-"+i+"|"+tr2.getStartIndex()+ " r2 end:"+tr2.getEndIndex()  );
                        
                        // For Kmeans
                        intersectSet.add(s.getLocation().getPosition());
                        }
                    }
                }
            });
        });

        // logger.info(""+intersectSet);
        double[][] d = intersectSet.stream().map(p -> {
            return p.getValues().stream().mapToDouble(Double::doubleValue).toArray();
        }).toArray(double[][]::new);
        if (intersectSet.size() > CLUSTERING_FACTOR) {
            int k = intersectSet.size() / CLUSTERING_FACTOR;
            KMeans clustering = new KMeans.Builder(k, d)
                    .iterations(10)
                    .pp(true)
                    .epsilon(.001)
                    .useEpsilon(true)
                    .build();
            double[][] centroids = clustering.getCentroids();
            System.out.println("intersect:" + intersectSet.size() + " k:" + k);
            List<Stop> stops = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                Stop stop = new Stop();
                stop.setId("S" + i);
                stop.setLocation(new Point(new Position(centroids[i][0], centroids[i][1])));
                stops.add(stop);
            }
            suggestions.add(Suggestion.builder().transferStops(stops).legs(List.of()).build());
        }

        return suggestions;
    }

    private List<Suggestion> get1TRoutes(List<Route> startRoutes, List<Route> endRoutes) {
        Set<Position> transferStops = new HashSet<>();
        startRoutes.stream().forEach(r -> {
            r.getStops().forEach(s -> {
                if (!s.getLocation().equals(r.getNearestStop())) {
                    transferStops.add(s.getLocation().getPosition());
                }
            });
        });
        List<Route> intermediateRoute = mongoTemplate.getCollection(mongoTemplate.getCollectionName(Route.class))
                .withDocumentClass(Route.class)
                .find(Filters.geoIntersects("stops.location",
                        new MultiPoint(transferStops.stream().collect(Collectors.toList()))))
                .into(new ArrayList<>());

        List<Route> list = new ArrayList<>();
        intermediateRoute.forEach(r -> {
            endRoutes.forEach(r2 -> {
                if (r.getRoute().equalsIgnoreCase(r2.getRoute()) &&
                        r.getBound().equalsIgnoreCase(r2.getBound()) &&
                        r.getServiceType().equalsIgnoreCase(r2.getServiceType())
                // && r.getStopIndex() < r2.getStopIndex() // Need to find alternative way
                ) {
                    list.add(r);
                }
            });
        });
        return list.stream().map(r -> Suggestion.builder().legs(List.of(r)).build()).toList();
    }

    private List<Suggestion> get2TRoutes() {
        mongoTemplate.getCollection(mongoTemplate.getCollectionName(Route.class));
        return List.of();
    }
}
