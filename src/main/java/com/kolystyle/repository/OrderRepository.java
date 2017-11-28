package com.kolystyle.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.kolystyle.domain.Order;

public interface OrderRepository extends CrudRepository<Order, Long>{

	List<Order> findByUserId(Long id);
	List<Order> findAllByOrderByOrderDateDesc();
	
}
