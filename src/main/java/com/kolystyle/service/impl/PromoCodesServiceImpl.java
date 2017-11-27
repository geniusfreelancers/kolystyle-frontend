package com.kolystyle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.PromoCodes;
import com.kolystyle.repository.PromoCodesRepository;
import com.kolystyle.service.PromoCodesService;

@Service
public class PromoCodesServiceImpl implements PromoCodesService {
	
	@Autowired
	private PromoCodesRepository promoCodesRepository;
	
	public PromoCodes findOne(Long id){
		return promoCodesRepository.findOne(id);
	}
	
	
	public PromoCodes findByPromoCode(String couponCode){
		return promoCodesRepository.findByCouponCode(couponCode);
	}

}
