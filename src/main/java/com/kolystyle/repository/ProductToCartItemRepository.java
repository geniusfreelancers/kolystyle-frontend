/*package com.kolystyle.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.ListItem;
import com.kolystyle.domain.ProductToCartItem;

@Transactional
public interface ProductToCartItemRepository extends CrudRepository<ProductToCartItem, Long> {

	ProductToCartItem findByCartItem(CartItem cartItem);
	void deleteByCartItem(CartItem cartItem);
}
*/