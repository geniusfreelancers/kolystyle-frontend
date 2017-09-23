package com.kolystyle.service.impl;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.GuestShoppingCart;
import com.kolystyle.domain.Order;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.ProductToCartItem;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.User;
import com.kolystyle.repository.CartItemRepository;
import com.kolystyle.repository.GuestShoppingCartRepository;
import com.kolystyle.repository.ProductToCartItemRepository;
import com.kolystyle.service.CartItemService;

@Service
public class CartItemServiceImpl implements CartItemService {

	@Autowired
	private CartItemRepository cartItemRepository;
	
	@Autowired
	private ProductToCartItemRepository productToCartItemRepository;
	
	@Autowired
	private GuestShoppingCartRepository guestShoppingCartRepository;
	
	public List<CartItem> findByShoppingCart(ShoppingCart shoppingCart){
		return cartItemRepository.findByShoppingCart(shoppingCart);
	}
	
	public CartItem updateCartItem(CartItem cartItem){
		BigDecimal bigDecimal = new BigDecimal(cartItem.getProduct().getOurPrice()).multiply(new BigDecimal(cartItem.getQty()));
		
		bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
		cartItem.setSubtotal(bigDecimal);
		
		cartItemRepository.save(cartItem);
		
		return cartItem;		
	}
	
	public CartItem addProductToCartItem(Product product,User user,int qty){
		List<CartItem> cartItemList = findByShoppingCart(user.getShoppingCart());
		
		for(CartItem cartItem : cartItemList){
			if(product.getId() == cartItem.getProduct().getId()){
				cartItem.setQty(qty);
				//cartItem.setQty(cartItem.getQty()+qty);
				cartItem.setSubtotal(new BigDecimal(product.getOurPrice()).multiply(new BigDecimal(qty)));
				cartItemRepository.save(cartItem);
				return cartItem;
				
			}
		}
		
		CartItem cartItem = new CartItem();
		cartItem.setShoppingCart(user.getShoppingCart());
		cartItem.setProduct(product);
		
		cartItem.setQty(qty);
		cartItem.setSubtotal(new BigDecimal(product.getOurPrice()).multiply(new BigDecimal(qty)));
		cartItem = cartItemRepository.save(cartItem);
		
		ProductToCartItem productToCartItem = new ProductToCartItem();
		productToCartItem.setProduct(product);
		productToCartItem.setCartItem(cartItem);
		productToCartItemRepository.save(productToCartItem);
		
		return cartItem;
	}
	
	public CartItem findById(Long id){
		return cartItemRepository.findOne(id);
	}
	
	public void removeCartItem(CartItem cartItem){
		productToCartItemRepository.deleteByCartItem(cartItem);
		cartItemRepository.delete(cartItem);
	}
	
	public CartItem save(CartItem cartItem){
		return cartItemRepository.save(cartItem);
	}
	
	public List<CartItem> findByOrder(Order order){
		return cartItemRepository.findByOrder(order);
	}
	
	
	//Guest Cart Added
	public CartItem addProductToGuestCartItem(Product product,GuestShoppingCart guestShoppingCart,int qty){
		
		
		List<CartItem> cartItemList = findByGuestShoppingCart(guestShoppingCart);
		
		for(CartItem cartItem : cartItemList){
			if(product.getId() == cartItem.getProduct().getId()){
				cartItem.setQty(qty);
				//cartItem.setQty(cartItem.getQty()+qty);
				cartItem.setSubtotal(new BigDecimal(product.getOurPrice()).multiply(new BigDecimal(qty)));
				cartItemRepository.save(cartItem);
				return cartItem;
				
			}
		}
		
		CartItem cartItem = new CartItem();
		cartItem.setGuestShoppingCart(guestShoppingCart);
		cartItem.setProduct(product);
		
		cartItem.setQty(qty);
		cartItem.setSubtotal(new BigDecimal(product.getOurPrice()).multiply(new BigDecimal(qty)));
		cartItem = cartItemRepository.save(cartItem);
		
		ProductToCartItem productToCartItem = new ProductToCartItem();
		productToCartItem.setProduct(product);
		productToCartItem.setCartItem(cartItem);
		productToCartItemRepository.save(productToCartItem);
		
		return cartItem;
	}

	public List<CartItem> findByGuestShoppingCart(GuestShoppingCart guestShoppingCart) {
		return cartItemRepository.findByGuestShoppingCart(guestShoppingCart);
	}
	
	public GuestShoppingCart findGuestCartBySessionId(String sessionid) {
		return guestShoppingCartRepository.findByGuestSession(sessionid);
	}
}
