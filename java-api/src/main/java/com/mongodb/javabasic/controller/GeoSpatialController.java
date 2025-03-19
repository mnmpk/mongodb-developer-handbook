package com.mongodb.javabasic.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.javabasic.service.GeoSpatialService;
import com.mongodb.javabasic.model.Stop;
import com.mongodb.javabasic.model.Suggestion;
import com.mongodb.javabasic.model.Route;

@RestController
@RequestMapping(path = "/geo-spatial")
public class GeoSpatialController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private GeoSpatialService geoSpatialService;

    @GetMapping("/stops")
    public List<Stop> getStops(@RequestParam("lat") double lat, @RequestParam("lng") double lng) {
        return geoSpatialService.getStops(lat, lng);
    }

    @GetMapping("/routes")
    public List<Route> getRoutes(@RequestParam("lat") double lat, @RequestParam("lng") double lng) {
        return geoSpatialService.getRoutes(lat, lng);
    }

    @PostMapping("/routes")
    public List<Suggestion> getRoutes(@RequestBody Map<String,double[]> map) {
        List<Route> startRoutes = this.getRoutes(map.get("start")[1], map.get("start")[0]);
        List<Route> endRoutes = this.getRoutes(map.get("end")[1], map.get("end")[0]);
        List<Suggestion> suggestions = new ArrayList<>();
        suggestions.addAll(geoSpatialService.getRouteSuggestions(startRoutes, endRoutes));
        return suggestions;
    }

}
