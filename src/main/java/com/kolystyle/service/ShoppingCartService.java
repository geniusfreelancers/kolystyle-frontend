package com.kolystyle.service;

import com.kolystyle.domain.GuestShoppingCart;
import com.kolystyle.domain.ShoppingCart;

public interface ShoppingCartService {
	ShoppingCart updateShoppingCart(ShoppingCart shoppingCart);
	
	void clearShoppingCart(ShoppingCart shoppingCart);
	
	GuestShoppingCart updateGuestShoppingCart(GuestShoppingCart guestShoppingCart);
}
