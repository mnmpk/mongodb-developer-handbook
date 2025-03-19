package com.mongodb.javabasic.controller;

import java.util.LinkedHashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.javabasic.model.CustomEntity;
import com.mongodb.javabasic.repositories.CustomEntityRepository;
@RestController
@RequestMapping(path = "/")
public class ApplicationController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private CustomEntityRepository repository;

    @GetMapping
    public String health() {
        return "OK";
    }

    @GetMapping("/test")
    public String test() {

        logger.info(mongoTemplate.getDb().getReadConcern().toString());
        logger.info(mongoTemplate.getDb().getReadPreference().toString());
        logger.info(mongoTemplate.getDb().getWriteConcern().toString());
        for(int i = 0; i < 1000; i++){
            t();
        }
        return "OK";
    }
    private void t(){
        List<CustomEntity> l = repository.findAll();

        CustomEntity ce = new CustomEntity();
        if (l.size() > 0)
            ce = l.get(0);

        logger.info("***Original entity***:" + ce.toString());
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("test", "test" + Math.random());
        ce.setData(map);
        CustomEntity newCE = repository.save(ce);
        logger.info("***entity returned by save***:" + newCE.toString());

        CustomEntity queryCE = repository.findById(newCE.getId()).get();
        logger.info("***find same entity by id***:" + queryCE.toString());
        logger.info(
                "***Compare value***: old - " + ce.getData().get("test") + " new - " + queryCE.getData().get("test"));
        if (ce.getData().containsKey("test") && queryCE.getData().containsKey("test"))
            logger.info("equal? " + ce.getData().get("test").equals(queryCE.getData().get("test")));
        else
            logger.info("************************** Missing value");
    }

    // spring vs mongodb driver:
    // spring data repo/spring helper/mongodb driver
    // spring converter/mongodb driver codec

    // Operations:
    // insert/update/delete/replace
    // Option:
    // bulk/distributed/write concern

    // Transaction:
    // Insert+Update/Validate+Update
    // Seach:

    //Cache using MongoDB

    //Search
    //Create index
}
