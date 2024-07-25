package com.mongodb.javabasic.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.jeasy.random.EasyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.javabasic.model.Stat;
import com.mongodb.javabasic.model.User;
import com.mongodb.javabasic.model.Workload;
import com.mongodb.javabasic.model.Workload.OperationType;
import com.mongodb.javabasic.service.UserService;

@RestController
@RequestMapping(path = "/users")
public class UserController extends GenericController<User> {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	@Qualifier("userService")
	UserService driverService;
	@Autowired
	@Qualifier("userRepoService")
	UserService repoService;
	@Autowired
	@Qualifier("springUserService")
	UserService springService;

	@Override
	public Page<User> search(String query, Pageable pageable) {
		return repoService.search(query, pageable);
	}

	@Override
	public Page<User> list(Pageable pageable) {
		return repoService.list(pageable);
	}

	@Override
	public User get(String id) {
		return repoService.get(id);
	}

	@Override
	public User create(User entity) {
		return repoService.create(entity);
	}

	@Override
	public User update(String id, User entity) {
		return repoService.update(entity);
	}

	@Override
	public void delete(String id) {
		repoService.delete(id);
	}

	@Override
	public Stat<User> load(Workload workload) {
		EasyRandom generator = new EasyRandom();
		List<User> users = generator.objects(User.class, workload.getQuantity()).map(u -> {
			// u.setId(new ObjectId().toHexString());
			if (workload.getOperationType() == OperationType.INSERT)
				u.setId(null);
			return u;
		})
				.collect(Collectors.toList());
		switch ((workload.getImplementation())) {
			case DRIVER:
				return driverService.load(users, workload);
			case REPO:
				return repoService.load(users, workload);
			case SPRING:
				return springService.load(users, workload);
		}
		return null;
	}

}
