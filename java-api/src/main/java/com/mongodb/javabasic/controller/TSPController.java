package com.mongodb.javabasic.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bson.Document;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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
import com.mongodb.javabasic.repositories.CustomEntityRepository;
import com.mongodb.javabasic.repositories.TspConfigRepository;
import com.mongodb.javabasic.repositories.TspIdConfigMapDocumentRepository;
import com.mongodb.javabasic.repositories.TspIdConfigMapDocumentSuggestedRepository;

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
    private TspConfigRepository configRepository;


    @GetMapping("/config")
    public List<TspConfig> config() {
        //Combine config & office_id_config & fare_family_mapping
        return configRepository.getConfig(List.of(Map.entry("office_id", "MNLCX08DM"),Map.entry("channel", "MOB")));
    }

// secure_flight??

    @GetMapping("/country")
    public String country() {
        //unwind contry_info to port level
        return "";
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
    @GetMapping("/id_config/{channel}/all")
    public List<TspIdConfigMapDocumentSuggested> idConfigsAll(@PathVariable String channel) {
        //return idConfigRepository.findByChannel(channel);
        return idConfigSuggestedRepository.findByChannel(channel);
    }
    @GetMapping("/id_config/{channel}")
    public List<TspIdConfigMapDocumentSuggested> idConfigs(@PathVariable String channel, @RequestParam("used") boolean used) {
        //return idConfigRepository.findByChannelAndUsed(channel, used);
        return idConfigSuggestedRepository.findByChannelAndUsedExists(channel, used);
    }
    @GetMapping("/id_config/{channel}/{tspId}")
    public TspIdConfigMapDocumentSuggested idConfig(@PathVariable String channel, @PathVariable String tspId) {
        //return idConfigRepository.findByChannelAndTspId(channel, tspId);
        return idConfigSuggestedRepository.findByChannelAndTspId(channel, tspId);
    }
    @GetMapping("/id_config/{channel}/count")
    public Long idConfigCount(@PathVariable String channel, @PathParam("used") boolean used) {
        //return idConfigRepository.countByChannelAndUsed(channel, used);
        return idConfigSuggestedRepository.countByChannelAndUsedExists(channel, used);
    }




    @GetMapping("/route")
    public String route() {
        return "";
    }
}
