package com.kolystyle.service.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.controller.ShoppingCartController;
import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.PromoCodes;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.SiteSetting;
import com.kolystyle.repository.ShoppingCartRepository;
import com.kolystyle.service.CartItemService;
import com.kolystyle.service.ShoppingCartService;
import com.kolystyle.service.SiteSettingService;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService{

	private static final Logger LOG = LoggerFactory.getLogger(ShoppingCartServiceImpl.class);
	@Autowired
	private CartItemService cartItemService;
	
	@Autowired
	private ShoppingCartRepository shoppingCartRepository;
	@Autowired
	private SiteSettingService siteSettingService;

	public ShoppingCart findCartByBagId(String bagId) {
		return shoppingCartRepository.findByBagId(bagId);
	}
	
	public void remove(ShoppingCart shoppingCart) {
		shoppingCartRepository.delete(shoppingCart);
	}
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
		shoppingCart.setOrderTotal(cartTotal.add(shoppingCart.getShippingCost()).subtract(shoppingCart.getDiscountedAmount()));
		Date addedDate = Calendar.getInstance().getTime();
		shoppingCart.setUpdatedDate(addedDate);
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
		Date addedDate = Calendar.getInstance().getTime();
		shoppingCart.setUpdatedDate(addedDate);
		shoppingCartRepository.save(shoppingCart);
	}
	
	public BigDecimal calculateCartSubTotal(ShoppingCart shoppingCart) {
		List<CartItem> cartItemList = shoppingCart.getCartItemList();
		BigDecimal cartSubTotal = new BigDecimal(0);
		if(cartItemList != null) {
			for (CartItem cartItem : cartItemList) {
				cartSubTotal = cartSubTotal.add(cartItem.getSubtotal());
			}	
		}
		
		shoppingCart.setGrandTotal(cartSubTotal.setScale(2, BigDecimal.ROUND_HALF_UP));
		return cartSubTotal;	
	}
	
	public BigDecimal calculateDiscountAmount(ShoppingCart shoppingCart, PromoCodes promoCodes) {
		BigDecimal gTotal = shoppingCart.getGrandTotal();
		BigDecimal gNewTotal = new BigDecimal(0);
		if (promoCodes != null) {
		
		BigDecimal promoVal = promoCodes.getPromoValue();
		
		if(promoCodes.getPercentOrDollar().equalsIgnoreCase("dollar")) {
			gNewTotal = promoVal;
					//gTotal- promoCodes.getPromoValue();
			LOG.info("User's applied Coupon Code with dollar value of: {}", promoVal);
			LOG.info("User's New Shopping Cart Grand Total is: {} after {} dollars discount", gNewTotal,promoVal);
		}else {
			gNewTotal = promoVal.divide(new BigDecimal(100),2);
			LOG.info("User's applied Coupon Code with percentage value of: {}%", promoCodes.getPromoValue());
			gNewTotal = gNewTotal.multiply(gTotal);
			LOG.info("User's applied Coupon Code with percentage value of: {}% and gets $ {} discount", promoVal,gNewTotal);
			//gNewTotal = gTotal.subtract(gNewTotal);
			//LOG.info("User's New Shopping Cart Grand Total is: {} after {} percentage discount", gNewTotal,promoVal);
		}	
		}
		shoppingCart.setDiscountedAmount(gNewTotal.setScale(2, BigDecimal.ROUND_HALF_UP));
		return gNewTotal;
	}
	
	public BigDecimal calculateCartOrderTotal(ShoppingCart shoppingCart) {
		BigDecimal cartSubTotal = shoppingCart.getGrandTotal();
		BigDecimal cartDiscountVal = shoppingCart.getDiscountedAmount();
		BigDecimal shippingCost = shoppingCart.getShippingCost();
		BigDecimal cartOrderTotal = cartSubTotal.add(shippingCost).subtract(cartDiscountVal);
		
		shoppingCart.setOrderTotal(cartOrderTotal.setScale(2, BigDecimal.ROUND_HALF_UP));
		return cartOrderTotal;	
	}
	
	public BigDecimal calculateShippingCost(ShoppingCart shoppingCart) {
		BigDecimal cartSubTotal = shoppingCart.getGrandTotal();
		BigDecimal cartDiscountVal = shoppingCart.getDiscountedAmount();
		BigDecimal discountedAmount = cartSubTotal.subtract(cartDiscountVal);
		BigDecimal shippingCost = new BigDecimal(0);
		SiteSetting siteSetting = siteSettingService.findOne((long)1);
		if (discountedAmount.doubleValue() >= siteSetting.getFreeShippingMin().doubleValue()) {
			shippingCost = siteSetting.getShippingCost();
		}
		shoppingCart.setShippingCost(shippingCost.setScale(2, BigDecimal.ROUND_HALF_UP));
		return shippingCost;	
	}
	
	public PromoCodes checkCouponValidity(PromoCodes promoCodes){
		
		return promoCodes;
		
	}
	


}
