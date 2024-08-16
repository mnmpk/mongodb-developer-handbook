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
import com.mongodb.javabasic.model.CustomEntity;
import com.mongodb.javabasic.model.Workload;
import com.mongodb.javabasic.repositories.CustomEntityRepository;
import com.mongodb.javabasic.service.CustomEntityService;

@Service("customEntityRepoService")
public class RepoCustomEntityService extends CustomEntityService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CustomEntityRepository repository;

    @Override
    public Stat<Page<CustomEntity>> search(String query, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'search'");
    }

    @Override
    public Stat<Page<CustomEntity>> list(Workload workload, Pageable pageable) {
        Stat<Page<CustomEntity>> stat = new Stat<>(CustomEntity.class);
        time(stat, workload, (v) -> {
            stat.setData(List.of(repository.findAll(pageable)));
            return null;
        });
        return stat;
    }

    @Override
    public Stat<CustomEntity> _load(List<CustomEntity> entities, Workload workload) {
        Stat<CustomEntity> stat = new Stat<>(CustomEntity.class);
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
            List<CustomEntity> newEntities = new ArrayList<>();
            for (CustomEntity e : entities) {

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
