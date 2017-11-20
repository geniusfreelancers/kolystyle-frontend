package com.kolystyle.service.impl;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.BillingAddress;
import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.Order;
import com.kolystyle.domain.Payment;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.ShippingAddress;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.User;
import com.kolystyle.repository.OrderRepository;
import com.kolystyle.service.CartItemService;
import com.kolystyle.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService{
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private CartItemService cartItemService;
	
	public synchronized Order createOrder(ShoppingCart shoppingCart, ShippingAddress shippingAddress, 
			BillingAddress billingAddress, Payment payment, String shippingMethod, User user,
			String email, String phone
			){
		Order order = new Order();
		order.setBillingAddress(billingAddress);
		order.setOrderStatus("created");
		order.setPayment(payment);
		order.setShippingAddress(shippingAddress);
		order.setShippingMethod(shippingMethod);
		String orderEmail = email;
		String orderPhone = phone;
		if(user != null) {
			orderEmail = user.getEmail();
			orderPhone = user.getPhone();
		}
		
		List<CartItem> cartItemList = cartItemService.findByShoppingCart(shoppingCart);
		
		for(CartItem cartItem : cartItemList){
			Product product = cartItem.getProduct();
			cartItem.setOrder(order);
			product.setInStockNumber(product.getInStockNumber() - cartItem.getQty());
		}
		order.setOrderEmail(orderEmail);
		order.setOrderPhone(orderPhone);
		order.setCartItemList(cartItemList);
		order.setOrderDate(Calendar.getInstance().getTime());
		order.setOrderTotal(shoppingCart.getGrandTotal());
		shippingAddress.setOrder(order);
		billingAddress.setOrder(order);
		payment.setOrder(order);
		order.setUser(user);
		order = orderRepository.save(order);
		
		return order;
	}

/*	public synchronized Order createGuestOrder(ShoppingCart shoppingCart, ShippingAddress shippingAddress, BillingAddress billingAddress,
			Payment payment, String shippingMethod, String phone, String email) {
		
	}*/
	
	public Order findOne(Long id){
		return orderRepository.findOne(id);
	}
	
	public List<Order> findByUser(User user){
		return orderRepository.findByUserId(user.getId());
	}
}
