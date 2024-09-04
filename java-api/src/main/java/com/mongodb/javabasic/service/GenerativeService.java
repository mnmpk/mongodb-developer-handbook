package com.mongodb.javabasic.service;

import java.util.Date;
import java.util.stream.Stream;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.springframework.stereotype.Service;

@Service
public class GenerativeService<T> {
	public Stream<T> generateRandom(Class<T> clazz, int noOfObjects) {
		EasyRandom generator = new EasyRandom(new EasyRandomParameters()
				.seed(new Date().getTime()));
		return generator.objects(clazz, noOfObjects);
	}

}
