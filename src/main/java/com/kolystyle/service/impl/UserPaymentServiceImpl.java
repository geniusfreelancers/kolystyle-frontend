package com.kolystyle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.UserPayment;
import com.kolystyle.repository.UserPaymentRepository;

import com.kolystyle.service.UserPaymentService;

@Service
public class UserPaymentServiceImpl implements UserPaymentService{

	@Autowired
	private UserPaymentRepository userPaymentRepository;
	
	public UserPayment findById(Long id){
		return userPaymentRepository.findOne(id);
	}
	
	public void removeById(Long id){
		userPaymentRepository.delete(id);
	}

	
	
}
