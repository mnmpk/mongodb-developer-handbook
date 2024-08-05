package com.mongodb.javabasic.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mongodb.javabasic.model.Stat;
import com.mongodb.javabasic.model.Product;
import com.mongodb.javabasic.model.Workload;
import com.mongodb.javabasic.repositories.ProductRepository;
import com.mongodb.javabasic.service.ProductService;

@Service("productRepoService")
public class ProductRepoService extends ProductService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProductRepository repository;

    @Override
    public Stat<Page<Product>> search(String query, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'search'");
    }

    @Override
    public Stat<Page<Product>> list(Workload workload, Pageable pageable) {
        Stat<Page<Product>> stat = new Stat<>(Product.class);
        time(stat, workload, (v) -> {
            stat.setData(List.of(repository.findAll(pageable)));
            return null;
        });
        return stat;
    }

    @Override
    public Stat<Product> _load(List<Product> entities, Workload workload) {
        Stat<Product> stat = new Stat<>(Product.class);
        if (workload.isBulk()) {
            time(stat, workload, (v) -> {
                switch (workload.getOperationType()) {
                    case DELETE:
                        repository.deleteAll(entities);
                        break;
                    case INSERT:
                    case REPLACE:
                    case UPDATE:
                        stat.setData(repository.saveAll(entities));
                        break;
                }
                return null;
            });
        } else {
            List<Product> newEntities = new ArrayList<>();
            for (Product e : entities) {

                time(stat, workload, (v) -> {
                    switch (workload.getOperationType()) {
                        case DELETE:
                            repository.delete(e);
                            break;
                        case INSERT:
                        case REPLACE:
                        case UPDATE:
                            newEntities.add(repository.save(e));
                            break;
                    }
                    return null;
                });
            }
            stat.setData(newEntities);
        }
        return stat;
    }

}
