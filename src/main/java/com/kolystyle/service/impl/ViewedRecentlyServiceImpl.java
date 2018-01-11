package com.kolystyle.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.Order;
import com.kolystyle.domain.OrderLog;
import com.kolystyle.domain.ViewedRecently;
import com.kolystyle.repository.OrderLogRepository;
import com.kolystyle.repository.ViewedRecentlyRepository;
import com.kolystyle.service.OrderLogService;
import com.kolystyle.service.ViewedRecentlyService;

@Service
public class ViewedRecentlyServiceImpl implements ViewedRecentlyService {
	@Autowired
	private ViewedRecentlyRepository viewedRecentlyRepository;

	
	/*public List<OrderLog> findAll() {
		return (List<OrderLog>) orderLogRepository.findAll();
	}

	public List<OrderLog> findAllByOrderByIdDesc() {
		// TODO Auto-generated method stub
		return orderLogRepository.findAllByOrderByIdDesc();
	}

	public List<OrderLog> findByOrderByOrderByIdDesc(Order order) {
		// TODO Auto-generated method stub
		return orderLogRepository.findByOrderOrderByIdDesc(order);
	}*/
	
	
	public ViewedRecently findByCookieValue(String cookieValue){
		return viewedRecentlyRepository.findByCookieValueOrderByIdDesc(cookieValue);
	}
	
	public ViewedRecently findByBagId(String bagId){
		return viewedRecentlyRepository.findByBagIdOrderByIdDesc(bagId);
	}

}
