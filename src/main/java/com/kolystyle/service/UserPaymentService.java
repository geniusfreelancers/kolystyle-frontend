package com.kolystyle.service;

import com.kolystyle.domain.UserPayment;

public interface UserPaymentService {
	UserPayment findById(Long id);
	
	void removeById(Long id);

}
