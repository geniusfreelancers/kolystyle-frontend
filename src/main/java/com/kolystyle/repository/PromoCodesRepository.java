package com.kolystyle.repository;

import org.springframework.data.repository.CrudRepository;

import com.kolystyle.domain.PromoCodes;

public interface PromoCodesRepository extends CrudRepository<PromoCodes, Long> {

	PromoCodes findByCouponCode(String promocode);

}
