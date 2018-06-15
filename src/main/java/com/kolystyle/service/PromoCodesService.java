package com.kolystyle.service;

import com.kolystyle.domain.PromoCodes;
import com.kolystyle.domain.ShoppingCart;

public interface PromoCodesService {
	
	PromoCodes findOne(Long id);
	
	PromoCodes findByPromoCode(String couponCode);

	ShoppingCart validatePromoCode(PromoCodes promoCodes,ShoppingCart shoppingCart,String couponCode);
}
