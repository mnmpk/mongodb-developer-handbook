package com.mongodb.javabasic.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationController {
    @GetMapping("/test")
    public String test() {
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
