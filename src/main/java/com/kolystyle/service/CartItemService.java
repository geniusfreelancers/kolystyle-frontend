package com.kolystyle.service;

import java.util.List;



import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.GuestShoppingCart;
import com.kolystyle.domain.Order;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.User;

public interface CartItemService {

	List<CartItem> findByShoppingCart(ShoppingCart shoppingCart);
	
	CartItem addProductToCartItem(Product product,User user,int qty);
	
	CartItem updateCartItem(CartItem cartItem);
	
	CartItem findById(Long id);
	
	void removeCartItem(CartItem cartItem);
	
	CartItem save(CartItem cartItem);
	
	List<CartItem> findByOrder(Order order);
	
	//Guest Cart Added
	GuestShoppingCart findGuestCartBySessionId(String sessionid);
	List<CartItem> findByGuestShoppingCart(GuestShoppingCart guestShoppingCart);
	
	CartItem addProductToGuestCartItem(Product product,GuestShoppingCart guestShoppingCart,int qty);
	
	
	
}
