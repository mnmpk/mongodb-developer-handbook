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
	public abstract Page<T> search(String query, Pageable pageable);

	/**
	 * @param pageable
	 * @return
	 */
	@GetMapping
	public abstract Page<T> list(Pageable pageable);

	/**
	 * @param id
	 * @return
	 * @throws NotFoundException
	 */
	@GetMapping("/{id}")
	public abstract @ResponseBody T get(@PathVariable String id);

	/**
	 * @param entity
	 * @return
	 */
	@PostMapping
	public abstract @ResponseBody T create(@RequestBody T entity);

	/**
	 * @param id
	 * @param entity
	 * @return
	 * @throws NotFoundException
	 */
	@PutMapping("/{id}")
	public abstract @ResponseBody T update(@PathVariable String id, @RequestBody T entity);

	/**
	 * @param id
	 * @throws NotFoundException
	 */
	@DeleteMapping("/{id}")
	public abstract void delete(@PathVariable String id);

	@PostMapping("/load")
	public abstract Stat<T> load(@RequestBody Workload workload);
}
