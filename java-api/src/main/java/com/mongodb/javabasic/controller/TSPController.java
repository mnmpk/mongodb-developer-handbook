package com.mongodb.javabasic.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bson.Document;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.javabasic.model.TspConfig;
import com.mongodb.javabasic.model.TspIdConfigMapDocumentSuggested;
import com.mongodb.javabasic.model.TspRoute;
import com.mongodb.javabasic.repositories.CustomEntityRepository;
import com.mongodb.javabasic.repositories.TspConfigRepository;
import com.mongodb.javabasic.repositories.TspCountryInfoRepository;
import com.mongodb.javabasic.repositories.TspIdConfigMapDocumentRepository;
import com.mongodb.javabasic.repositories.TspIdConfigMapDocumentSuggestedRepository;
import com.mongodb.javabasic.repositories.TspPortInfoRepository;
import com.mongodb.javabasic.repositories.TspRouteRepository;
import com.mongodb.javabasic.service.AggregationService;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.server.PathParam;

@RestController
@RequestMapping(path = "/")
public class TSPController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private CustomEntityRepository repository;
    @Autowired
    private AggregationService aggregationService;

    @Autowired
    private TspConfigRepository configRepository;
    @Autowired
    private TspRouteRepository routeRepository;

    @GetMapping("/session")
    public Object consessionfig(HttpSession session) {
        Object obj = session.getAttribute("test");
        session.setAttribute("test", "test"+Math.random());
        return obj+" . "+session.getAttribute("test");
    }
    @GetMapping("/config")
    public List<TspConfig> config(HttpSession session) {
        //Combine config & office_id_config & fare_family_mapping
        return configRepository.getConfig(List.of(Map.entry("office_id", "MNLCX08DM"),Map.entry("channel", "MOB")));
    }

    // secure_flight??

    @Autowired 
    private TspCountryInfoRepository countryInfoRepository;
    @Autowired
    private TspPortInfoRepository portInfoRepository;

    @GetMapping("/country/init")
    public String countryInit() {
        MongoCollection<Document> coll = mongoTemplate.getCollection("ori_country_info");
        coll.createIndex(Indexes.ascending("ports.port_code"));
        coll.createIndex(Indexes.ascending("ports.airports.iata_airport_code"));
        try {
            aggregationService.getPipelineResults("ori_country_info", "country_to_port.json", Document.class);
            coll = mongoTemplate.getCollection("port_info");
        coll.createIndex(Indexes.ascending("port_code"));
        coll.createIndex(Indexes.ascending("airports.iata_airport_code"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }
    @GetMapping("/country")
    public String country() {
        StopWatch watch = new StopWatch();
        watch.start("country");
        countryInfoRepository.findCountriesByAirportCodes(Set.of("ALV","GRG"));
        watch.stop();
        watch.start("port");
        portInfoRepository.findPortsByAirportCodes(Set.of("ALV","GRG"));
        watch.stop();
        return watch.prettyPrint();
    }


    @Autowired
    private TspIdConfigMapDocumentRepository idConfigRepository;
    @Autowired
    private TspIdConfigMapDocumentSuggestedRepository idConfigSuggestedRepository;
    @GetMapping("/id_config/init")
    public List<TspIdConfigMapDocumentSuggested> idConfigsInit() {
        /*
        mongoTemplate.getCollection("tsp_id_config").createIndex(Indexes.ascending("channel", "tsp_id", "used"));
        String[] channels={"MOB","WCMP"};
        EasyRandom generator = new EasyRandom(new EasyRandomParameters().seed(new Date().getTime()).stringLengthRange(5, 5));
        List<TspIdConfigMapDocument> l = generator.objects(TspIdConfigMapDocument.class, 30000).map(e -> {
					e.setId(null);
                    e.setCreateTime(new Date());
                    e.setUpdateTime(new Date());
                    e.setUsed(false);
                    e.setChannel(channels[Math.random() > 0.5 ? 0 : 1]);
					return e;
				}).collect(Collectors.toList());
        return idConfigRepository.saveAll(l);*/
        MongoCollection<Document> coll = mongoTemplate.getCollection("tsp_id_config_new");
        coll.createIndex(Indexes.ascending("channel", "tsp_id", "used"));
        coll.createIndex(Indexes.ascending( "used"), new IndexOptions().expireAfter(10000L, TimeUnit.HOURS));
        String[] channels={"MOB","WCMP"};
        EasyRandom generator = new EasyRandom(new EasyRandomParameters().seed(new Date().getTime()).stringLengthRange(5, 5));
        List<TspIdConfigMapDocumentSuggested> l = generator.objects(TspIdConfigMapDocumentSuggested.class, 30000).map(e -> {
					e.setId(null);
                    e.setCreateTime(new Date());
                    e.setUpdateTime(new Date());
                    if(Math.random() > 0.5)
                        e.setUsed(null);
                    e.setChannel(channels[Math.random() > 0.5 ? 0 : 1]);
					return e;
				}).collect(Collectors.toList());
        return idConfigSuggestedRepository.saveAll(l);
    }
    @Cacheable("cache")
    @GetMapping("/id_config/{channel}/all")
    public List<TspIdConfigMapDocumentSuggested> idConfigsAll(@PathVariable String channel) {
        StopWatch watch = new StopWatch();
        watch.start("old");
        idConfigRepository.findByChannel(channel);
        watch.stop();
        watch.start("new");
        List<TspIdConfigMapDocumentSuggested> list = idConfigSuggestedRepository.findByChannel(channel);
        watch.stop();
        logger.info(watch.prettyPrint());
        return list;
    }
    @Cacheable("cache")
    @GetMapping("/id_config/{channel}")
    public List<TspIdConfigMapDocumentSuggested> idConfigs(@PathVariable String channel, @RequestParam("used") boolean used) {
        StopWatch watch = new StopWatch();
        watch.start("old");
        idConfigRepository.findByChannelAndUsed(channel, used);
        watch.stop();
        watch.start("new");
        List<TspIdConfigMapDocumentSuggested> list = idConfigSuggestedRepository.findByChannelAndUsedExists(channel, used);
        watch.stop();
        logger.info(watch.prettyPrint());
        return list;
    }
    @Cacheable("cache")
    @GetMapping("/id_config/{channel}/{tspId}")
    public TspIdConfigMapDocumentSuggested idConfig(@PathVariable String channel, @PathVariable String tspId) {
        StopWatch watch = new StopWatch();
        watch.start("old");
        idConfigRepository.findByChannelAndTspId(channel, tspId);
        watch.stop();
        watch.start("new");
        TspIdConfigMapDocumentSuggested doc = idConfigSuggestedRepository.findByChannelAndTspId(channel, tspId);
        watch.stop();
        logger.info(watch.prettyPrint());
        return doc;
    }

    @Cacheable("cache")
    @GetMapping("/id_config/{channel}/count")
    public Long idConfigCount(@PathVariable String channel, @PathParam("used") boolean used) {
        StopWatch watch = new StopWatch();
        watch.start("old");
        idConfigRepository.countByChannelAndUsed(channel, used);
        watch.stop();
        watch.start("new");
        Long count = idConfigSuggestedRepository.countByChannelAndUsedExists(channel, used);
        watch.stop();
        logger.info(watch.prettyPrint());
        return count;
    }




    @GetMapping(value={"/route/{dep}", "/route/{dep}/{arr}"})
    public List<TspRoute> route(@PathVariable String dep, @PathVariable(required = false) String arr) {
        return routeRepository.getRoutes(dep, arr);
    }
}
