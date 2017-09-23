package com.kolystyle.service;

import java.util.List;

import com.kolystyle.domain.BillingAddress;
import com.kolystyle.domain.Order;
import com.kolystyle.domain.Payment;
import com.kolystyle.domain.ShippingAddress;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.User;

public interface OrderService {
	
	Order createOrder(ShoppingCart shoppingCart, ShippingAddress shippingAddress, BillingAddress billingAddress,
			Payment payment, String shippingMethod, User user);

	Order findOne(Long id);
	List<Order> findByUser(User user);
}
