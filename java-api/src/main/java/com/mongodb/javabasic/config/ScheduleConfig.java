package com.mongodb.javabasic.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Profile("scheduler")
@Configuration
@EnableScheduling
public class ScheduleConfig {

}
