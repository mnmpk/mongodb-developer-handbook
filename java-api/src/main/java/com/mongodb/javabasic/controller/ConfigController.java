package com.mongodb.javabasic.controller;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.javabasic.service.ConfigService;


@RestController
@RequestMapping(path = "/configs")
public class ConfigController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ConfigService<Document> configService;

    @GetMapping("/config")
    public List<Document> config(@RequestParam Map<String, String> allParams) {
        return configService.getConfig(allParams, Document.class);
    }

}
