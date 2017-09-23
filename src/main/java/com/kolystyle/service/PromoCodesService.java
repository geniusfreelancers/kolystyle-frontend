package com.kolystyle.service;

import com.kolystyle.domain.PromoCodes;

public interface PromoCodesService {
	
	PromoCodes findOne(Long id);
	
	PromoCodes findByPromoCode(String promocode);
}
