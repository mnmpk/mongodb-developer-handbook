package com.mongodb.javabasic.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.mongodb.javabasic.model.Stat;
import com.mongodb.javabasic.model.User;
import com.mongodb.javabasic.model.Workload;
import com.mongodb.javabasic.repositories.UserRepository;
import com.mongodb.javabasic.service.UserService;

@Service("springUserService")
public class SpringUserService extends UserService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserRepository repository;

    @Override
    public Page<User> search(String query, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'search'");
    }

    @Override
    public Page<User> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public User get(String id) {
        return repository.findById(id).get();
    }

    @Override
    public User create(User entity) {
        return repository.save(entity);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public User update(User entity) {
        return repository.save(entity);
    }

    @Override
    public Stat<User> _load(List<User> entities, Workload workload) {
        Stat<User> stat = new Stat<>();
        stat.setWorkload(Workload.builder().implementation(workload.getImplementation())
                .converter(workload.getConverter()).bulk(workload.isBulk()).writeConcern(workload.getWriteConcern())
                .operationType(workload.getOperationType())
                .collection(workload.getCollection()).noOfWorkers(1)
                .quantity(entities.size()).build());
        stat.setStartAt(new Date());
        long min = 0;
        long max = 0;
        long total = 0;
        if (workload.isBulk()) {
            StopWatch sw = new StopWatch();
            sw.start();
            switch (workload.getOperationType()) {
                case DELETE:
                    repository.deleteAll(entities);
                    break;
                default:
                    stat.setData(repository.saveAll(entities));
                    break;
            }
            sw.stop();
            min = sw.getTotalTimeMillis();
            max = sw.getTotalTimeMillis();
            total = sw.getTotalTimeMillis();
        } else {
            List<User> newEntities = new ArrayList<>();
            for (User e : entities) {
                StopWatch sw = new StopWatch();
                sw.start();
                switch (workload.getOperationType()) {
                    case DELETE:
                        repository.delete(e);
                        break;
                    default:
                        newEntities.add(repository.save(e));
                        break;
                }
                sw.stop();
                long time = sw.getTotalTimeMillis();
                total += time;
                if (min == 0) {
                    min = time;
                }
                min = Math.min(min, time);
                max = Math.max(max, time);
            }
            stat.setData(newEntities);
        }
        stat.setMinLatency(min);
        stat.setMaxLatency(max);
        stat.setDuration(total);
        stat.setEndAt(new Date());
        stat.setDuration(total);
        return stat;
    }

}
