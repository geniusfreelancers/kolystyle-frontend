package com.kolystyle.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.GuestShoppingCart;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.repository.GuestShoppingCartRepository;
import com.kolystyle.repository.ShoppingCartRepository;
import com.kolystyle.service.CartItemService;
import com.kolystyle.service.ShoppingCartService;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService{

	@Autowired
	private CartItemService cartItemService;
	
	@Autowired
	private ShoppingCartRepository shoppingCartRepository;
	
	@Autowired
	private GuestShoppingCartRepository guestShoppingCartRepository;
	
	public ShoppingCart updateShoppingCart(ShoppingCart shoppingCart){
		BigDecimal cartTotal = new BigDecimal(0);
		
		List<CartItem> cartItemList = cartItemService.findByShoppingCart(shoppingCart);
		
		for(CartItem cartItem : cartItemList){
			if(cartItem.getProduct().getInStockNumber() > 0){
				cartItemService.updateCartItem(cartItem);
				cartTotal = cartTotal.add(cartItem.getSubtotal());
			}
		}
		
		shoppingCart.setGrandTotal(cartTotal);
		shoppingCartRepository.save(shoppingCart);
		
		return shoppingCart;
	}
	
	
	public void clearShoppingCart(ShoppingCart shoppingCart){
		List<CartItem> cartItemList = cartItemService.findByShoppingCart(shoppingCart);
		
		for(CartItem cartItem : cartItemList){
			cartItem.setShoppingCart(null);
			cartItemService.save(cartItem);
		}
		
		shoppingCart.setGrandTotal(new BigDecimal(0));
		shoppingCartRepository.save(shoppingCart);
	}
	
	//Update Guest Shopping Cart
	public GuestShoppingCart updateGuestShoppingCart(GuestShoppingCart guestShoppingCart){
		BigDecimal cartTotal = new BigDecimal(0);
		
		List<CartItem> cartItemList = cartItemService.findByGuestShoppingCart(guestShoppingCart);
		
		for(CartItem cartItem : cartItemList){
			if(cartItem.getProduct().getInStockNumber() > 0){
				cartItemService.updateCartItem(cartItem);
				cartTotal = cartTotal.add(cartItem.getSubtotal());
			}
		}
		
		guestShoppingCart.setGrandTotal(cartTotal);
		guestShoppingCartRepository.save(guestShoppingCart);
		
		return guestShoppingCart;
	}
	
	//Clear Guest Shopping Cart
	public void clearGuestShoppingCart(GuestShoppingCart guestShoppingCart){
		List<CartItem> cartItemList = cartItemService.findByGuestShoppingCart(guestShoppingCart);
		
		for(CartItem cartItem : cartItemList){
			cartItem.setGuestShoppingCart(null);
			cartItemService.save(cartItem);
		}
		
		guestShoppingCart.setGrandTotal(new BigDecimal(0));
		guestShoppingCartRepository.save(guestShoppingCart);
	}
}
