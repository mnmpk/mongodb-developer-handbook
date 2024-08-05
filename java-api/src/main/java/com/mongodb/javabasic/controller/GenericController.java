package com.mongodb.javabasic.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.mongodb.javabasic.model.Stat;
import com.mongodb.javabasic.model.Workload;

public abstract class GenericController<T> {

	@PostMapping("/search")
	public abstract Stat<Page<T>> search(@RequestBody Workload workload, Pageable pageable);

	@PostMapping("/list")
	public abstract Stat<Page<T>> list(@RequestBody Workload workload, Pageable pageable);

	@PostMapping("/load")
	public abstract Stat<T> load(@RequestBody Workload workload);
}
