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
import com.mongodb.javabasic.model.Order;
import com.mongodb.javabasic.model.Workload;
import com.mongodb.javabasic.repositories.OrderRepository;
import com.mongodb.javabasic.service.OrderService;

@Service("orderRepoService")
public class RepoOrderService extends OrderService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrderRepository repository;

    @Override
    public Stat<Page<Order>> search(String query, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'search'");
    }

    @Override
    public Stat<Page<Order>> list(Workload workload, Pageable pageable) {
        Stat<Page<Order>> stat = new Stat<>(Order.class);
        time(stat, workload, (v) -> {
            stat.setData(List.of(repository.findAll(pageable)));
            return null;
        });
        return stat;
    }

    @Override
    public Stat<Order> _load(List<Order> entities, Workload workload) {
        Stat<Order> stat = new Stat<>(Order.class);
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
            List<Order> newEntities = new ArrayList<>();
            for (Order e : entities) {

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
