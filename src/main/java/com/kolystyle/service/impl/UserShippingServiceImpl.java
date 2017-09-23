package com.kolystyle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.UserShipping;
import com.kolystyle.repository.UserShippingRepository;
import com.kolystyle.service.UserShippingService;

@Service
public class UserShippingServiceImpl implements UserShippingService {
	
	@Autowired
	private UserShippingRepository userShippingRepository;
	
	public UserShipping save(UserShipping userShipping){
		return userShippingRepository.save(userShipping);
	}
	
	public UserShipping findById(Long id){
		return userShippingRepository.findOne(id);
	}

	public void removeById(Long id){
		userShippingRepository.delete(id);
	}
}
