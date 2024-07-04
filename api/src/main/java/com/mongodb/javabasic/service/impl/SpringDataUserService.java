package com.mongodb.javabasic.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mongodb.javabasic.model.User;
import com.mongodb.javabasic.repositories.UserRepository;
import com.mongodb.javabasic.service.UserService;

@Service("springDataUserService")
public class SpringDataUserService extends UserService {

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
    public List<User> _bulk(List<User> entities){
        return repository.saveAll(entities);
    }

}
