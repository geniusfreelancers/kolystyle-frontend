package com.kolystyle.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.ProductToCartItem;
import com.kolystyle.repository.ProductToCartItemRepository;
import com.kolystyle.service.ProductToCartItemService;
@Service
public class ProductToCartItemServiceImpl implements ProductToCartItemService{
	@Autowired
	private ProductToCartItemRepository productToCartItemRepository;

	@Override
	public void deleteByCartItem(CartItem cartItem) {
		ProductToCartItem productToCartItem = productToCartItemRepository.findByCartItem(cartItem);
		productToCartItemRepository.delete(productToCartItem);
		
	}

	
}
