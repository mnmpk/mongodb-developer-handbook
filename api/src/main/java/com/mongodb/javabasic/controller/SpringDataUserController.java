package com.mongodb.javabasic.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.jeasy.random.EasyRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.javabasic.model.User;
import com.mongodb.javabasic.service.UserService;

@RestController
@RequestMapping(path = "/spring-data/users")
public class SpringDataUserController extends GenericController<User> {

	@Autowired
	@Qualifier("springDataUserService")
	UserService service;

	@Override
	public Page<User> search(String query, Pageable pageable) {
		return service.search(query, pageable);
	}

	@Override
	public Page<User> list(Pageable pageable) {
		return service.list(pageable);
	}

	@Override
	public User get(String id) {
		return service.get(id);
	}

	@Override
	public User create(User entity) {
		return service.create(entity);
	}

	@Override
	public User update(String id, User entity) {
		return service.update(entity);
	}

	@Override
	public void delete(String id) {
		service.delete(id);
	}

	@Override
	public List<User> bulk(int noOfThreads, int noOfItems) {
		EasyRandom generator = new EasyRandom();
		List<User> users = generator.objects(User.class, noOfItems).map(u->{u.setId(new ObjectId().toHexString());return u;})
				.collect(Collectors.toList());
		return service.bulk(users, noOfThreads);
	}

}
