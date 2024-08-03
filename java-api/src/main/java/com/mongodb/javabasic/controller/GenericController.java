package com.mongodb.javabasic.controller;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.javabasic.model.Stat;
import com.mongodb.javabasic.model.Workload;

public abstract class GenericController<T> {
	/**
	 * @param pageable
	 * @return
	 */
	@PostMapping("/search")
	public abstract Stat<Page<T>> search(@RequestBody String query, Pageable pageable);

	/**
	 * @param pageable
	 * @return
	 */
	@PostMapping("/list")
	public abstract Stat<Page<T>> list(@RequestBody Workload workload, Pageable pageable);

	/**
	 * @param id
	 * @return
	 * @throws NotFoundException
	 */
	@GetMapping("/{id}")
	public abstract @ResponseBody Stat<T> get(@PathVariable String id);

	/**
	 * @param entity
	 * @return
	 */
	@PostMapping
	public abstract @ResponseBody Stat<T> create(@RequestBody T entity);

	/**
	 * @param id
	 * @param entity
	 * @return
	 * @throws NotFoundException
	 */
	@PutMapping("/{id}")
	public abstract @ResponseBody Stat<T> update(@PathVariable String id, @RequestBody T entity);

	/**
	 * @param id
	 * @throws NotFoundException
	 */
	@DeleteMapping("/{id}")
	public abstract Stat<T> delete(@PathVariable String id);

	@PostMapping("/load")
	public abstract Stat<T> load(@RequestBody Workload workload);
}
