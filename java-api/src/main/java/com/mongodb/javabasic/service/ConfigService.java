package com.mongodb.javabasic.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.mongodb.javabasic.repositories.ConfigRepository;

@Service
public class ConfigService<T> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ConfigRepository<T> configRepository;

    public List<T> getConfig(Map<String, String> allParams, Class<T> clazz) {
        if (allParams.size() > 0) {
            return configRepository.getConfigs(allParams.entrySet().stream()
                    .map(e -> Map.entry(e.getKey(), Arrays.asList(e.getValue().split(",")))).toList(), clazz);
        }
        return configRepository.getConfigs(clazz);
    }

    @Cacheable(value = "config")
    public List<T> getConfigs(String key, Class<T> clazz) {
        return configRepository.getConfigs(List.of(Map.entry("key", List.of(key))), clazz);
    }
    
    @Cacheable(value = "config")
    public T getConfig(String key, Class<T> clazz) {
        return configRepository.getConfig(List.of(Map.entry("key", List.of(key))), clazz);
    }
}
