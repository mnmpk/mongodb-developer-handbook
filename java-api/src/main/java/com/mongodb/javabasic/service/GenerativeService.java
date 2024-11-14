package com.mongodb.javabasic.service;

import java.util.Date;
import java.util.Random;
import java.util.stream.Stream;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.FieldPredicates;
import org.springframework.stereotype.Service;

@Service
public class GenerativeService<T> {
	public T generateRandom(Class<T> clazz) {
		EasyRandom generator = new EasyRandom(
				new EasyRandomParameters().randomize(FieldPredicates.named("acct"), () -> new Random().nextInt(10000))
						.randomize(FieldPredicates.named("bet"), () -> new Random().nextDouble(10000))
						.seed(new Date().getTime()));
		return generator.nextObject(clazz);
	}

	public Stream<T> generateRandom(Class<T> clazz, int noOfObjects) {
		EasyRandom generator = new EasyRandom(new EasyRandomParameters().randomize(FieldPredicates.named("acct"), () -> new Random().nextInt(10000))
		.randomize(FieldPredicates.named("bet"), () -> new Random().nextDouble(10000))
				.seed(new Date().getTime()));
		return generator.objects(clazz, noOfObjects);
	}

}
