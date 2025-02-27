package com.mongodb.javabasic.repositories;

import java.util.List;
import com.mongodb.javabasic.model.TspRoute;

public interface CustomTspRouteRepository {

    public List<TspRoute> getRoutes(String dep, String arr);
    public List<TspRoute> getRoutes(String dep);
}
