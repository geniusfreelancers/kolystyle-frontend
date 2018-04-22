package com.kolystyle.service;

import java.util.List;

import com.kolystyle.domain.BillingAddress;
import com.kolystyle.domain.Order;
import com.kolystyle.domain.Payment;
import com.kolystyle.domain.ShippingAddress;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.User;
import com.stripe.model.Charge;

public interface OrderService {
	
	Order createOrder(ShoppingCart shoppingCart, ShippingAddress shippingAddress, BillingAddress billingAddress,
			Payment payment, String shippingMethod, User user,String email, String phone);
	/*Order createGuestOrder(ShoppingCart shoppingCart, ShippingAddress shippingAddress, BillingAddress billingAddress,
			Payment payment, String shippingMethod, String phone, String email);*/
	
	Order findOne(Long id);
	List<Order> findByUser(User user);
	List<Order> findAllByOrderDateDesc();
	Order createNewOrder(ShoppingCart shoppingCart,User user, Charge charge);
}
