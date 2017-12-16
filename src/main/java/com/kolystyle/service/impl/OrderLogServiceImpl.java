package com.kolystyle.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.Order;
import com.kolystyle.domain.OrderLog;
import com.kolystyle.repository.OrderLogRepository;
import com.kolystyle.service.OrderLogService;

@Service
public class OrderLogServiceImpl implements OrderLogService {
	@Autowired
	private OrderLogRepository orderLogRepository;

	
	public List<OrderLog> findAll() {
		return (List<OrderLog>) orderLogRepository.findAll();
	}

	public List<OrderLog> findAllByOrderByIdDesc() {
		// TODO Auto-generated method stub
		return orderLogRepository.findAllByOrderByIdDesc();
	}

	public List<OrderLog> findByOrderByOrderByIdDesc(Order order) {
		// TODO Auto-generated method stub
		return orderLogRepository.findByOrderOrderByIdDesc(order);
	}

}
