package com.mongodb.javabasic.repositories;

import java.util.List;
import java.util.Map.Entry;

public interface CustomConfigRepository<T> {

    public List<T> getConfigs(Class<T> clazz);
    public List<T> getConfigs(List<Entry<String, List<String>>> entries, Class<T> clazz);
    public T getConfig(List<Entry<String, List<String>>> entries, Class<T> clazz);
}
