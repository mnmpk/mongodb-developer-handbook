package com.mongodb.javabasic.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.javabasic.model.CustomEntity;
import com.mongodb.javabasic.model.Stat;
import com.mongodb.javabasic.model.Workload;
import com.mongodb.javabasic.service.CustomEntityService;

@RestController
@RequestMapping(path = "/custom")
public class CustomEntityController extends GenericController<CustomEntity> {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	@Qualifier("customEntityService")
	CustomEntityService driverService;
	@Autowired
	@Qualifier("customEntityRepoService")
	CustomEntityService repoService;
	@Autowired
	@Qualifier("springCustomEntityService")
	CustomEntityService springService;

	@Override
	public Stat<Page<CustomEntity>> search(Workload workload, Pageable pageable) {
		return repoService.search(workload.getQuery(), pageable);
	}

	@Override
	public Stat<Page<CustomEntity>> list(Workload workload, Pageable pageable) {
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
	public Stat<CustomEntity> load(Workload workload) {
		EasyRandom generator = new EasyRandom(new EasyRandomParameters()
				.seed(new Date().getTime()));
		String dummyData = RandomStringUtils.randomAscii(workload.getDocumentSize());
		List<CustomEntity> customEntities = new ArrayList<>();
		switch (workload.getOperationType()) {
			case INSERT:
				customEntities = generator.objects(CustomEntity.class, workload.getQuantity()).map(e -> {
					e.setId(null);
					e.setData(new LinkedHashMap<>(Map.of("text", dummyData)));
					e.setDocument(new Document("text", dummyData));
					e.setVersion(1);
					return e;
				}).collect(Collectors.toList());
				break;
			case DELETE:
				customEntities = IntStream.range(0, workload.getIds().size()).mapToObj(i -> {
					CustomEntity e = CustomEntity.builder().id(workload.getIds().get(i)).build();
					return e;
				}).collect(Collectors.toList());
				break;
			case REPLACE:
			case UPDATE:
				var temp = generator.objects(CustomEntity.class, workload.getIds().size()).collect(Collectors.toList());
				customEntities = IntStream.range(0, workload.getIds().size()).mapToObj(i -> {
					CustomEntity e = temp.get(i);
					e.setId(workload.getIds().get(i));
					e.setData(new LinkedHashMap<>(Map.of("text", dummyData)));
					e.setDocument(new Document("text", dummyData));
					e.setVersion(1);
					return e;
				}).collect(Collectors.toList());
				break;
		}

		switch ((workload.getImplementation())) {
			case DRIVER:
				return driverService.load(customEntities, workload);
			case REPO:
				return repoService.load(customEntities, workload);
			case SPRING:
				return springService.load(customEntities, workload);
		}
		return null;
	}

}
