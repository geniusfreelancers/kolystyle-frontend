package com.kolystyle.service;

import java.util.List;



import com.kolystyle.domain.CartItem;

import com.kolystyle.domain.Order;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.ShoppingCart;


public interface CartItemService {

	List<CartItem> findByShoppingCart(ShoppingCart shoppingCart);
	
	CartItem addProductToCartItem(Product product,ShoppingCart shoppingCart,int qty,String size);
	
	CartItem updateCartItem(CartItem cartItem);
	
	CartItem findById(Long id);
	void removeOne(Long id);
	
	void removeCartItem(CartItem cartItem);
	
	CartItem save(CartItem cartItem);
	
	List<CartItem> findByOrder(Order order);
	
	//Guest Cart Added
	ShoppingCart findGuestCartBySessionId(String sessionid);

	int findProductQtyInCart(ShoppingCart shoppingCart, Product product);

	boolean ifProductSizeExist(ShoppingCart shoppingCart, Product product, String size);
	
	/*List<CartItem> findByGuestShoppingCart(GuestShoppingCart guestShoppingCart);*/
	

	
}
