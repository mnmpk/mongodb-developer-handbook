package com.mongodb.javabasic.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.javabasic.model.ApplicationEvent;


@Service
public class ApplicationEventService<T> {
	Logger logger = LoggerFactory.getLogger(getClass());

	private static final Set<Consumer<ApplicationEvent<?>>> listeners = new HashSet<>();

	@Autowired
	private Executor taskExecutor;

	public void publish(ApplicationEvent<T> event) {
		logger.debug("new event:" + event);
		listeners.forEach(l -> {
			CompletableFuture.supplyAsync(() -> {
				try {
					l.accept(event);
				} catch (RuntimeException e) {
					logger.error("Unexpected error publishing event:", e);
				}
				return null;
			}, taskExecutor);
		});
	}

	public void subscribe(Consumer<ApplicationEvent<?>> listener) {
		logger.info("new subscription:" + listeners.add(listener));
	}

	public void unsubscribe(Consumer<ApplicationEvent<T>> listener) {
		logger.info("remove subscription:" + listeners.remove(listener));
	}

	public void clear() {
		logger.info("Clear all subscription");
		listeners.clear();
	}

}
