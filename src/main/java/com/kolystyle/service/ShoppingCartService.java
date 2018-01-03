package com.kolystyle.service;


import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;

import com.kolystyle.domain.PromoCodes;
import com.kolystyle.domain.ShoppingCart;

public interface ShoppingCartService {
	ShoppingCart updateShoppingCart(ShoppingCart shoppingCart);
	
	void clearShoppingCart(ShoppingCart shoppingCart);
	ShoppingCart findCartByBagId(String bagId);
	void remove(ShoppingCart shoppingCart);
	BigDecimal calculateCartSubTotal(ShoppingCart shoppingCart);
	BigDecimal calculateDiscountAmount(ShoppingCart shoppingCart, PromoCodes promoCodes);
	BigDecimal calculateCartOrderTotal(ShoppingCart shoppingCart); 
	BigDecimal calculateShippingCost(ShoppingCart shoppingCart);
	PromoCodes checkCouponValidity(PromoCodes promoCodes);
	ShoppingCart findCartByCookie( HttpServletRequest request);

	int cartItemCount(ShoppingCart shoppingCart);
	
/*	GuestShoppingCart updateGuestShoppingCart(GuestShoppingCart guestShoppingCart);
	
	GuestShoppingCart findByGuestShoppingCartId(String id);*/
}
