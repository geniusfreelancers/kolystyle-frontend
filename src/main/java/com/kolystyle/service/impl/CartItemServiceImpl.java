package com.kolystyle.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.CartItem;

import com.kolystyle.domain.Order;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.repository.CartItemRepository;

import com.kolystyle.repository.PromoCodesRepository;
import com.kolystyle.repository.ShoppingCartRepository;
import com.kolystyle.service.CartItemService;
import com.kolystyle.service.PromoCodesService;
import com.kolystyle.service.ShoppingCartService;
import com.kolystyle.service.SiteSettingService;

@Service
public class CartItemServiceImpl implements CartItemService {

	@Autowired
	private CartItemRepository cartItemRepository;
	
	@Autowired
	private ShoppingCartService shoppingCartService;
	
	@Autowired
	private ShoppingCartRepository shoppingCartRepository;
	
	@Autowired
	private SiteSettingService siteSettingService;
	
	@Autowired
	private PromoCodesService promoCodesService;

	@Autowired
	private PromoCodesRepository promoCodesRepository;
	
	public List<CartItem> findByShoppingCart(ShoppingCart shoppingCart){
		return cartItemRepository.findByShoppingCart(shoppingCart);
	}
	
	public CartItem updateCartItem(CartItem cartItem){
		BigDecimal bigDecimal = new BigDecimal(cartItem.getProduct().getOurPrice()).multiply(new BigDecimal(cartItem.getQty()));
		double stitchCost = 0;
		if(cartItem.getProduct().isUnStiched() == true){
			if(!cartItem.getProductSize().equalsIgnoreCase("unstiched")) {
			 stitchCost = cartItem.getProduct().getCategory().getStichingCost();
			}
		}
		cartItem.setStitchingTotal(new BigDecimal(stitchCost*cartItem.getQty()));
		bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
		cartItem.setSubtotal(bigDecimal);
		//Check problem here
		cartItemRepository.save(cartItem);
		
		return cartItem;		
	}
	
	public CartItem addProductToCartItem(Product product,ShoppingCart shoppingCart,int qty,String size,String option){
		List<CartItem> cartItemList = findByShoppingCart(shoppingCart);
		double stitchCost = new Double(0);
		int cartItemQty = shoppingCart.getCartItemQty();
		if(product.isUnStiched() == true){
			if(option.equalsIgnoreCase("readytowear")) {
			 stitchCost = product.getCategory().getStichingCost();
			}
		}
		for(CartItem cartItem : cartItemList){
			
			if(product.getId() == cartItem.getProduct().getId()){
				//Check if product with same size already exist in cart
				if(cartItem.getProductSize().equalsIgnoreCase(size)) {
/*					 if(product.getInStockNumber() < findProductQtyInCart(shoppingCart,product)+qty-cartItem.getQty()) {
							return null;
						}*/
					cartItemQty = cartItemQty+qty-cartItem.getQty();
					cartItem.setStitchingTotal(new BigDecimal(stitchCost*qty));
					cartItem.setQty(qty);
					cartItem.setSubtotal(new BigDecimal(product.getOurPrice()).multiply(new BigDecimal(qty)).setScale(2, BigDecimal.ROUND_HALF_UP));

				cartItemRepository.save(cartItem);	
				shoppingCart.setCartItemQty(cartItemQty);
		       	shoppingCartRepository.save(shoppingCart);
				return cartItem;
				}
			}
			}

		CartItem cartItem = new CartItem();
		cartItem.setShoppingCart(shoppingCart);
		cartItem.setProduct(product);
		cartItem.setProductSize(size);
		cartItem.setStitchingTotal(new BigDecimal(stitchCost*qty));
		cartItem.setQty(qty);
		BigDecimal itemSubTotal = new BigDecimal(product.getOurPrice()).multiply(new BigDecimal(qty)).setScale(2, BigDecimal.ROUND_HALF_UP);
		cartItem.setSubtotal(itemSubTotal);
		
		cartItemRepository.save(cartItem);	
		shoppingCart.setCartItemQty(cartItemQty+qty);
		shoppingCartRepository.save(shoppingCart);
		return cartItem;
	}
	
/*	public CartItem addProductToCartItem(Product product,User user,int qty){
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
	}*/
	
	public CartItem findById(Long id){
		return cartItemRepository.findOne(id);
	}
	
	public void removeOne(Long id){
		cartItemRepository.delete(id);
	}
	
	public void removeCartItem(CartItem cartItem){
		ShoppingCart shoppingCart = cartItem.getShoppingCart();
		shoppingCart.setCartItemQty(shoppingCart.getCartItemQty()-cartItem.getQty());
	//	productToCartItemService.deleteByCartItem(cartItem);
	//	cartItemRepository.deleteById(cartItem.getId());
		cartItemRepository.delete(cartItem);
		shoppingCartRepository.save(shoppingCart);
	}
	
	public CartItem save(CartItem cartItem){
		return cartItemRepository.save(cartItem);
	}
	
	public List<CartItem> findByOrder(Order order){
		return cartItemRepository.findByOrder(order);
	}
	
	
	
	public ShoppingCart findGuestCartBySessionId(String sessionid) {
		return shoppingCartRepository.findBySessionId(sessionid);
	}

	@Override
	public int findProductQtyInCart(ShoppingCart shoppingCart, Product product) {
		List<CartItem> cartItemList = shoppingCart.getCartItemList();
		int productQty = 0;
		for (CartItem cartItem : cartItemList) {
			if(cartItem.getProduct().getId() == product.getId()) {
				productQty += cartItem.getQty(); 
			}
		}
		return productQty;
	}

	@Override
	public boolean ifProductSizeExist(ShoppingCart shoppingCart, Product product, String size) {
		List<CartItem> cartItemList = shoppingCart.getCartItemList();
		boolean productQty = false;
		for (CartItem cartItem : cartItemList) {
			if(cartItem.getProduct().getId() == product.getId() && cartItem.getProductSize() == size) {
				productQty = true;
			}
		}
		return productQty;
	}


}
