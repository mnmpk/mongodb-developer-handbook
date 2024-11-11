package com.mongodb.javabasic.controller;

import java.util.LinkedHashMap;

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

    CustomEntity e = new CustomEntity();
    e.setId("1234");
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put("test", "test"+Math.random());
    e.setData(map);
    logger.info(e.toString());
    repository.save(e);
    logger.info(repository.findById("1234").toString());
        return "OK";
    }

    //spring vs mongodb driver:
    //spring data repo/spring helper/mongodb driver
    //spring converter/mongodb driver codec

    //Operations:
    //insert/update/delete/replace
    //Option:
    //bulk/distributed/write concern
    
    //Transaction:
    //Insert+Update/Validate+Update
    //Seach:
}
