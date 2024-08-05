package com.mongodb.javabasic.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
	public Stat<Page<User>> search(Workload workload, Pageable pageable) {
		return repoService.search(workload.getQuery(), pageable);
	}

	@Override
	public Stat<Page<User>> list(Workload workload, Pageable pageable) {
		switch ((workload.getImplementation())) {
			case DRIVER:
				return driverService.list(workload, pageable);
			case REPO:
				return repoService.list(workload, pageable);
			case SPRING:
				return springService.list(workload, pageable);
		}
		return null;
	}

	@Override
	public Stat<User> load(Workload workload) {
		EasyRandom generator = new EasyRandom();
		List<User> users = new ArrayList<>();
		switch (workload.getOperationType()) {
			case INSERT:
				users = generator.objects(User.class, workload.getQuantity()).map(u -> {
					u.setId(null);
					u.setVersion(1);
					return u;
				}).collect(Collectors.toList());
				break;
			case DELETE:
				users = IntStream.range(0, workload.getIds().size()).mapToObj(i -> {
					User u = User.builder().id(workload.getIds().get(i)).build();
					return u;
				}).collect(Collectors.toList());
				break;
			case REPLACE:
			case UPDATE:
				var temp = generator.objects(User.class, workload.getIds().size()).collect(Collectors.toList());
				users = IntStream.range(0, workload.getIds().size()).mapToObj(i -> {
					User u = temp.get(i);
					u.setId(workload.getIds().get(i));
					u.setVersion(1);
					return u;
				}).collect(Collectors.toList());
				break;
		}

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
