package com.mongodb.javabasic.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.javabasic.repositories.RouteRepository;
import com.mongodb.javabasic.repositories.StopRepository;
import com.mongodb.javabasic.service.AggregationService;
import com.mongodb.javabasic.model.Stop;
import com.mongodb.javabasic.model.Suggestion;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.mongodb.javabasic.model.Route;

@RestController
@RequestMapping(path = "/ptes")
public class PTESController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
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
                .find(Filters.nearSphere("location", new Point(new Position(lng, lat)), 100d, 0d)).into(new ArrayList<>());
        // return stopRepository.findAll();
    }

    @GetMapping("/routes")
    public List<Route> getRoutes() {
        return routeRepository.findAll();
    }

    @GetMapping("/routes/{start}/{end}")
    public List<Suggestion> getRoutes(@PathVariable String start, @PathVariable String end) {
        List<Suggestion> suggestions = new ArrayList<>();
        suggestions.addAll(this.getDirectRoutes(start, end));
        suggestions.addAll(this.get1TRoutes());
        suggestions.addAll(this.get2TRoutes());
        return suggestions;
    }

    private List<Suggestion> getDirectRoutes(String start, String end) {
        return aggregationService
                .getPipelineResults(mongoTemplate.getCollectionName(Route.class), "direct-route.json", Route.class,
                        Map.of("start", start, "end", end))
                .stream().map(r -> Suggestion.builder().legs(List.of(r)).build()).toList();
    }

    private List<Suggestion> get1TRoutes() {
        mongoTemplate.getCollection(mongoTemplate.getCollectionName(Route.class));
        return List.of();
    }

    private List<Suggestion> get2TRoutes() {
        mongoTemplate.getCollection(mongoTemplate.getCollectionName(Route.class));
        return List.of();
    }
}
