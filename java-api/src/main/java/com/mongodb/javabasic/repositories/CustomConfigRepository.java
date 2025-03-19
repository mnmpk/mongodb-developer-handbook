package com.mongodb.javabasic.repositories;

import java.util.List;
import java.util.Map.Entry;

public interface CustomConfigRepository<T> {

    public List<T> getConfig(Class<T> clazz);
    public List<T> getConfig(List<Entry<String, List<String>>> entries, Class<T> clazz);
}
