package com.mongodb.javabasic.repositories;

import java.util.List;
import java.util.Map.Entry;

import com.mongodb.javabasic.model.TspConfig;

public interface CustomTspConfigRepository {

    public List<TspConfig> getConfig(List<Entry<String, List<String>>> entries);
}
