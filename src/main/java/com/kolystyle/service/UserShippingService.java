package com.kolystyle.service;

import com.kolystyle.domain.UserShipping;

public interface UserShippingService {
	UserShipping save(UserShipping userShipping);
	UserShipping findById(Long id);
	
	void removeById(Long id);

}
