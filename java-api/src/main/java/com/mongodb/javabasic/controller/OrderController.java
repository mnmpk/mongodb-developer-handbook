package com.mongodb.javabasic.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bson.types.ObjectId;
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

import com.mongodb.javabasic.model.Order;
import com.mongodb.javabasic.model.Product;
import com.mongodb.javabasic.model.Stat;
import com.mongodb.javabasic.model.Workload;
import com.mongodb.javabasic.service.OrderService;
import com.mongodb.javabasic.service.ProductService;

@RestController
@RequestMapping(path = "/orders")
public class OrderController extends GenericController<Order> {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	@Qualifier("orderService")
	OrderService driverService;
	@Autowired
	@Qualifier("orderRepoService")
	OrderService repoService;
	@Autowired
	@Qualifier("springOrderService")
	OrderService springService;

	@Autowired
	ProductService productService;

	@Override
	public Stat<Page<Order>> search(Workload workload, Pageable pageable) {
		return repoService.search(workload.getQuery(), pageable);
	}

	@Override
	public Stat<Page<Order>> list(Workload workload, Pageable pageable) {
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
	public Stat<Order> load(Workload workload) {
		Product p = productService.sample(Product.class);
		EasyRandom generator = new EasyRandom(new EasyRandomParameters()
				.seed(new Date().getTime()));
		List<Order> orders = new ArrayList<>();
		switch (workload.getOperationType()) {
			case INSERT:
			orders = generator.objects(Order.class, workload.getQuantity()).map(o -> {
					o.setId(null);
					o.setVersion(1);
					if(p!=null){
						o.setItems(List.of(p));
						o.setItemIds(List.of(new ObjectId(p.getId())));
					}
					return o;
				}).collect(Collectors.toList());
				break;
			case DELETE:
			orders = IntStream.range(0, workload.getIds().size()).mapToObj(i -> {
					Order o = Order.builder().id(workload.getIds().get(i)).build();
					return o;
				}).collect(Collectors.toList());
				break;
			case REPLACE:
			case UPDATE:
				var temp = generator.objects(Order.class, workload.getIds().size()).collect(Collectors.toList());
				orders = IntStream.range(0, workload.getIds().size()).mapToObj(i -> {
					Order o = temp.get(i);
					o.setId(workload.getIds().get(i));
					o.setVersion(1);
					if(p!=null){
						o.setItems(List.of(p));
						o.setItemIds(List.of(new ObjectId(p.getId())));
					}
					return o;
				}).collect(Collectors.toList());
				break;
		}

		switch ((workload.getImplementation())) {
			case DRIVER:
				return driverService.load(orders, workload);
			case REPO:
				return repoService.load(orders, workload);
			case SPRING:
				return springService.load(orders, workload);
		}
		return null;
	}

}
