package com.mongodb.javabasic.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
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
import com.mongodb.javabasic.model.Stat;

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
    public List<Suggestion> getRoutes(@RequestBody Map<String, double[]> map) {
        var cfs = new ArrayList<CompletableFuture<List<Route>>>();
        cfs.add(CompletableFuture.supplyAsync(() -> {
            StopWatch sw = new StopWatch();
            sw.start("start route");
            List<Route> r = this.getRoutes(map.get("start")[1], map.get("start")[0]);
            sw.stop();
            logger.info(sw.prettyPrint());
            return r;
        }));
        cfs.add(CompletableFuture.supplyAsync(() -> {
            StopWatch sw = new StopWatch();
            sw.start("end route");
            List<Route> r = this.getRoutes(map.get("end")[1], map.get("end")[0]);
            sw.stop();
            logger.info(sw.prettyPrint());
            return r;
        }));
        return CompletableFuture
                .allOf(cfs.toArray(new CompletableFuture<?>[cfs.size()]))
                .thenApply(v -> {
                    try {
                        return geoSpatialService.getRouteSuggestions(cfs.get(0).get(), cfs.get(1).get());
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return null;
                }).join();
    }

}
