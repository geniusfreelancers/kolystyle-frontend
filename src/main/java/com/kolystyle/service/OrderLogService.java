package com.kolystyle.service;

import java.util.List;

import com.kolystyle.domain.Order;
import com.kolystyle.domain.OrderLog;

public interface OrderLogService {
	List<OrderLog> findAllByOrderByIdDesc();
	List<OrderLog> findByOrderByOrderByIdDesc(Order order);

}
