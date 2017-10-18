package com.kolystyle.repository;

import org.springframework.data.repository.CrudRepository;

import com.kolystyle.domain.ShoppingCart;

public interface ShoppingCartRepository extends CrudRepository<ShoppingCart, Long>{
	ShoppingCart findBySessionId(String sessionid);

	ShoppingCart findByBagId(String bagId);
}
