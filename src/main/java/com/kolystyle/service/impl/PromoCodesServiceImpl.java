package com.kolystyle.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.controller.CartController;
import com.kolystyle.domain.PromoCodes;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.repository.PromoCodesRepository;
import com.kolystyle.repository.ShoppingCartRepository;
import com.kolystyle.service.PromoCodesService;
import com.kolystyle.service.ShoppingCartService;

@Service
public class PromoCodesServiceImpl implements PromoCodesService {
	private static final Logger LOG = LoggerFactory.getLogger(PromoCodesServiceImpl.class);
	@Autowired
	private PromoCodesRepository promoCodesRepository;
	@Autowired
	private ShoppingCartService shoppingCartService;
	@Autowired
	private ShoppingCartRepository shoppingCartRepository;
	
	public PromoCodes findOne(Long id){
		return promoCodesRepository.findOne(id);
	}
	
	
	public PromoCodes findByPromoCode(String couponCode){
		return promoCodesRepository.findByCouponCode(couponCode);
	}


	@Override
	public ShoppingCart validatePromoCode(PromoCodes promoCodes,ShoppingCart shoppingCart,String couponCode) {
		String errors = null;
		
		if(promoCodes==null){
			if(couponCode != null && !couponCode.equalsIgnoreCase("null")) {
				LOG.info("User entered invalid promo code: {}", couponCode.toUpperCase());
				errors = "The promo code "+couponCode.toUpperCase()+" you entered is invalid.";
			}
		}else{
			LOG.info("User entered valid promo code: {} value of {} {}", couponCode.toUpperCase(),promoCodes.getPromoValue(),promoCodes.getPercentOrDollar());
			BigDecimal gTotal = shoppingCart.getGrandTotal();
			BigDecimal promoVal = promoCodes.getPromoValue();
			BigDecimal discountedAmount = new BigDecimal(0);
			LOG.info("User's Shopping Cart Grand Total is: {}", gTotal);
			
			Date start = promoCodes.getStartDate();
	        Date expiry = promoCodes.getExpiryDate();
	        Date today = Calendar.getInstance().getTime();
			//check for cart minimum and expiry and start date
	        if(!promoCodes.isPromoStatus()){
				errors = "Promo code "+couponCode.toUpperCase()+" not active";
			}else if(gTotal.compareTo(promoCodes.getCartTotal()) < 0) {
				LOG.info("User entered valid promo code: {} . But Shopping Cart Total is {} which is less than required Pormo Cart minimum of {} ", couponCode.toUpperCase(),gTotal,promoCodes.getCartTotal());
				errors = "Subtotal must be $"+promoCodes.getCartTotal()+" or above to apply promo code "+couponCode.toUpperCase();
			}else if(promoCodes.getPromoUsedCount() >= promoCodes.getPromoUseCount()){
				errors = "Promo code "+couponCode.toUpperCase()+" is not available anymore";
			}else if(start.after(today)) {
				errors = "Promo code "+couponCode.toUpperCase()+" can be used on or after "+new SimpleDateFormat("MM-dd-yyyy").format(promoCodes.getStartDate());
			}else if(expiry.before(today)) {
				errors = "Promo code "+couponCode.toUpperCase()+" expired on "+new SimpleDateFormat("MM-dd-yyyy").format(promoCodes.getExpiryDate());
			}else if(promoCodes.getCartItemQty() > shoppingCartService.cartItemCount(shoppingCart)) {
				errors = "Minimum "+promoCodes.getCartItemQty()+" items required to use "+couponCode.toUpperCase();
			}else {
				LOG.info("We can proceed with applying promo code {}. It passes all validation",couponCode.toUpperCase());			
			if(promoCodes.getPercentOrDollar().equalsIgnoreCase("dollar")) {
				discountedAmount = promoVal;
						//gTotal- promoCodes.getPromoValue();
				LOG.info("User's applied Coupon Code with dollar value of: {}", promoVal);
				LOG.info("User's New Shopping Cart Grand Total is: {} after {} dollars discount", discountedAmount,promoVal);
			}else {
				discountedAmount = promoVal.divide(new BigDecimal(100),2);
				LOG.info("User's applied Coupon Code with percentage value of: {}%", promoCodes.getPromoValue());
				discountedAmount = discountedAmount.multiply(gTotal);
				LOG.info("User's applied Coupon Code with percentage value of: {}% and gets $ {} discount", promoVal,discountedAmount);
				//discountedAmount = gTotal.subtract(discountedAmount);
				LOG.info("User's New Shopping Cart Grand Total is: {} after {} percentage discount", discountedAmount,promoVal);
			}
			
			
			shoppingCart.setPromoCode(couponCode);
			LOG.info("Promo Code {} is stored in Shopping Cart with Bag ID {}",couponCode, shoppingCart.getBagId());
			shoppingCart.setDiscountedAmount(discountedAmount);
			LOG.info("Stored Discounted Amount {} Shopping Cart with Bag ID {} where Grand Total was {}",discountedAmount,couponCode, shoppingCart.getBagId(),shoppingCart.getGrandTotal());
			//check for shipping cost
			
			shoppingCart.setOrderTotal(gTotal.add(shoppingCart.getShippingCost()).subtract(discountedAmount));

			LOG.info("Shopping Cart is saved and returning ShoppingCart as JSON");
			}
		}
			shoppingCart.setErrors(errors);

			LOG.info("Promo code not applied because it didn't pass requirenment, Shopping Cart is saving NOW and returned as JSON");
			shoppingCartRepository.save(shoppingCart);
		return shoppingCart;
	}

}
