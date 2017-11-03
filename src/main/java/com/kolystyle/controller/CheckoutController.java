package com.kolystyle.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.kolystyle.domain.BillingAddress;
import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.ChargeRequest;
import com.kolystyle.domain.Order;
import com.kolystyle.domain.Payment;
import com.kolystyle.domain.ShippingAddress;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.User;
import com.kolystyle.domain.UserBilling;
import com.kolystyle.domain.UserPayment;
import com.kolystyle.domain.UserShipping;
import com.kolystyle.service.BillingAddressService;
import com.kolystyle.service.CartItemService;
import com.kolystyle.service.OrderService;
import com.kolystyle.service.PaymentService;
import com.kolystyle.service.ShippingAddressService;
import com.kolystyle.service.ShoppingCartService;
import com.kolystyle.service.UserPaymentService;
import com.kolystyle.service.UserService;
import com.kolystyle.service.UserShippingService;
import com.kolystyle.utility.MailConstructor;
import com.kolystyle.utility.USConstants;

@Controller
public class CheckoutController {

	private ShippingAddress shippingAddress = new ShippingAddress();
	private BillingAddress billingAddress = new BillingAddress();
	private Payment payment = new Payment();
	//Stripe Payment
	@Value("${STRIPE_PUBLIC_KEY}")
    private String stripePublicKey;

	@Autowired
	private UserService userService;
	
	@Autowired
	private ShoppingCartService shoppingCartService;
	
	@Autowired
	private CartItemService cartItemService;
	
	@Autowired
	private ShippingAddressService shippingAddressService;
	
	@Autowired
	private BillingAddressService billingAddressService;
	
	@Autowired
	private PaymentService paymentService;
	
	@Autowired
	private UserShippingService userShippingService;
	
	@Autowired
	private UserPaymentService userPaymentService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private MailConstructor mailConstructor;
	
	@RequestMapping("/guestcheckout")
	public String guestcheckout(HttpServletRequest request,HttpServletResponse response,Model model ) {
		HttpSession session = request.getSession();
		ShoppingCart shoppingCart;
		Cookie[] cookies = request.getCookies();
		String cartId = "";
		boolean foundCookie = false;
   	 	//Check cookie value
        for(int i = 0; i < cookies.length; i++) { 
            Cookie cartID = cookies[i];
            if (cartID.getName().equals("BagId")) {
            	cartId = cartID.getValue();
                System.out.println("BagId = " + cartId);
                foundCookie = true;
            }}
        shoppingCart = (ShoppingCart) session.getAttribute("ShoppingCart");
        if(shoppingCart==null) {
        	System.out.println("Bag ID IS MISSING");
        	shoppingCart = shoppingCartService.findCartByBagId(cartId);
        }
                
        List<CartItem> cartItemList = cartItemService.findByShoppingCart(shoppingCart);
 
		ShippingAddress shippingAddress = new ShippingAddress();
		BillingAddress billingAddress = new BillingAddress();
		Payment payment = new Payment();
		
		model.addAttribute("shippingAddress",shippingAddress);
		model.addAttribute("payment",payment);
		model.addAttribute("billingAddress",billingAddress);
		model.addAttribute("cartItemList",cartItemList);
		model.addAttribute("shoppingCart",shoppingCart);
		List<String> stateList = USConstants.listOfUSStatesCode;
		Collections.sort(stateList);
		model.addAttribute("stateList", stateList);
		return "guestcheckout";
		
	}
	
	@RequestMapping("/checkout")
	public String checkout(
			@RequestParam("id") Long cartId,
			@RequestParam(value="missingRequiredField", required=false) boolean missingRequiredField,
			Model model, Principal principal
			){
		User user = userService.findByUsername(principal.getName());
		
		if(cartId != user.getShoppingCart().getId()){
			return "badRequestPage";
		}
		
		List<CartItem> cartItemList = cartItemService.findByShoppingCart(user.getShoppingCart());
		
		if(cartItemList.size() == 0){
			model.addAttribute("emptyCart",true);
			return "forward:/shoppingCart/cart";
		}
		
		for(CartItem cartItem : cartItemList){
			if(cartItem.getProduct().getInStockNumber() < cartItem.getQty()){
				model.addAttribute("notEnoughStock", true);
				return "forward:/shoppingCart/cart";
			}
		}
		
		List<UserShipping> userShippingList = user.getUserShippingList();
		List<UserPayment> userPaymentList = user.getUserPaymentList();
		
		model.addAttribute("userShippingList", userShippingList);
		model.addAttribute("userPaymentList", userPaymentList);
		
		if(userPaymentList.size() == 0){
			model.addAttribute("emptyPaymentList",true);
		}else{
			model.addAttribute("emptyPaymentList",false);
		}
		
		if(userShippingList.size() == 0){
			model.addAttribute("emptyShippingList",true);
		}else{
			model.addAttribute("emptyShippingList",false);
		}
		
		ShoppingCart shoppingCart = user.getShoppingCart();
		
		for(UserShipping userShipping : userShippingList){
			if(userShipping.isUserShippingDefault()){
				shippingAddressService.setByUserShipping(userShipping, shippingAddress);
			}
		}
		
		for(UserPayment userPayment : userPaymentList){
			if(userPayment.isDefaultPayment()){
				paymentService.setByUserPayment(userPayment, payment);
				billingAddressService.setByUserBilling(userPayment.getUserBilling(), billingAddress);
			}
		}
		
		model.addAttribute("shippingAddress",shippingAddress);
		model.addAttribute("payment",payment);
		model.addAttribute("billingAddress",billingAddress);
		model.addAttribute("cartItemList",cartItemList);
		model.addAttribute("shoppingCart",user.getShoppingCart());
		
		List<String> stateList = USConstants.listOfUSStatesCode;
		Collections.sort(stateList);
		model.addAttribute("stateList", stateList);
		
		model.addAttribute("classActiveShipping", true);
		
		if(missingRequiredField){
			model.addAttribute("missingRequiredField",true);
		}
		
		//Stripe Information
		model.addAttribute("amount", 50 * 100); // in cents
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("currency", ChargeRequest.Currency.USD);
        //Ends
		//return "checkOutOld";
		return "checkout";
       // return "form";
		
	}
	
	@RequestMapping(value="/checkout", method=RequestMethod.POST)
	public String checkoutPost(
			@ModelAttribute("shippingAddress") ShippingAddress shippingAddress,
			@ModelAttribute("billingAddress") BillingAddress billingAddress,
			@ModelAttribute("payment") Payment payment,
			@ModelAttribute("billingSameAsShipping") String billingSameAsShipping,
			@ModelAttribute("shippingMethod") String shippingMethod,
			Principal principal,
			Model model
			){
		ShoppingCart shoppingCart = userService.findByUsername(principal.getName()).getShoppingCart();
		
		List<CartItem> cartItemList = cartItemService.findByShoppingCart(shoppingCart);
		model.addAttribute("cartItemList",cartItemList);
		
		if(billingSameAsShipping.equals("true")){
			billingAddress.setBillingAddressName(shippingAddress.getShippingAddressName());
			billingAddress.setBillingAddressStreet1(shippingAddress.getShippingAddressStreet1());
			billingAddress.setBillingAddressStreet2(shippingAddress.getShippingAddressStreet2());
			billingAddress.setBillingAddressCity(shippingAddress.getShippingAddressCity());
			billingAddress.setBillingAddressState(shippingAddress.getShippingAddressState());
			billingAddress.setBillingAddressCountry(shippingAddress.getShippingAddressCountry());
			billingAddress.setBillingAddressZipcode(shippingAddress.getShippingAddressZipcode());
		}
		
		if(shippingAddress.getShippingAddressStreet1().isEmpty()|| 
		   shippingAddress.getShippingAddressCity().isEmpty() ||
		   shippingAddress.getShippingAddressState().isEmpty() ||
		   shippingAddress.getShippingAddressZipcode().isEmpty() ||
		   shippingAddress.getShippingAddressName().isEmpty() ||
		   payment.getCardNumber().isEmpty() ||
		   payment.getCvc() == 0 ||
		   billingAddress.getBillingAddressStreet1().isEmpty() ||
		   billingAddress.getBillingAddressCity().isEmpty() ||
		   billingAddress.getBillingAddressState().isEmpty() ||
		   billingAddress.getBillingAddressName().isEmpty() ||
		   billingAddress.getBillingAddressZipcode().isEmpty()){
			return "redirect:/checkout?id="+shoppingCart.getId()+"&missingRequiredField=true";
		}
		User user = userService.findByUsername(principal.getName());
		Order order = orderService.createOrder(shoppingCart, shippingAddress, billingAddress, payment, shippingMethod, user);
		
		mailSender.send(mailConstructor.constructOrderConfirmationEmail(user,order,Locale.ENGLISH));
		
		shoppingCartService.clearShoppingCart(shoppingCart);
		
		LocalDate today = LocalDate.now();
		LocalDate estimatedDeliveryDate;
		
		if(shippingMethod.equals("groundShipping")){
			estimatedDeliveryDate = today.plusDays(5);
		}else{
			estimatedDeliveryDate = today.plusDays(3);
		}
		
		model.addAttribute("estimatedDeliveryDate",estimatedDeliveryDate);
		
		return "orderSubmittedPage";
	}
	

	
	@RequestMapping("/setShippingAddress")
	public String setShippingAddress(
			@RequestParam("userShippingId") Long userShippingId,
			Principal principal, Model model
			){
		User user = userService.findByUsername(principal.getName());
		UserShipping userShipping = userShippingService.findById(userShippingId);
		
		if(userShipping.getUser().getId() != user.getId()){
			return "badRequestPage";
		}else{
			shippingAddressService.setByUserShipping(userShipping, shippingAddress);
			
			List<CartItem> cartItemList = cartItemService.findByShoppingCart(user.getShoppingCart());	

			model.addAttribute("shippingAddress",shippingAddress);
			model.addAttribute("payment",payment);
			model.addAttribute("billingAddress",billingAddress);
			model.addAttribute("cartItemList",cartItemList);
			model.addAttribute("shoppingCart",user.getShoppingCart());
			
			List<String> stateList = USConstants.listOfUSStatesCode;
			Collections.sort(stateList);
			model.addAttribute("stateList", stateList);
			
			List<UserShipping> userShippingList = user.getUserShippingList();
			List<UserPayment> userPaymentList = user.getUserPaymentList();
			
			model.addAttribute("userShippingList", userShippingList);
			model.addAttribute("userPaymentList", userPaymentList);
			
			model.addAttribute("shippingAddress",shippingAddress);
			model.addAttribute("classActiveShipping",true);
			
			if(userPaymentList.size() == 0){
				model.addAttribute("emptyPaymentList",true);
			}else{
				model.addAttribute("emptyPaymentList",false);
			}
			
			model.addAttribute("emptyShippingList",false);
			
			
			return "checkout";
			//return "form";
			
		}
	}
	
	@RequestMapping("/setPaymentMethod")
	public String setPaymentMethod(@RequestParam("userPaymentId") Long userPaymentId, Principal principal, Model model){
		User user = userService.findByUsername(principal.getName());
		UserPayment userPayment = userPaymentService.findById(userPaymentId);
		UserBilling userBilling = userPayment.getUserBilling();
		
		if(userPayment.getUser().getId() != user.getId()){
			return "badRequestPage";
		}else{
			paymentService.setByUserPayment(userPayment, payment);
			
			List<CartItem> cartItemList = cartItemService.findByShoppingCart(user.getShoppingCart());
			
			billingAddressService.setByUserBilling(userBilling, billingAddress);
			

			model.addAttribute("shippingAddress",shippingAddress);
			model.addAttribute("payment",payment);
			model.addAttribute("billingAddress",billingAddress);
			model.addAttribute("cartItemList",cartItemList);
			model.addAttribute("shoppingCart",user.getShoppingCart());
			
			List<String> stateList = USConstants.listOfUSStatesCode;
			Collections.sort(stateList);
			model.addAttribute("stateList", stateList);
			
			List<UserShipping> userShippingList = user.getUserShippingList();
			List<UserPayment> userPaymentList = user.getUserPaymentList();
			
			model.addAttribute("userShippingList", userShippingList);
			model.addAttribute("userPaymentList", userPaymentList);
			
			model.addAttribute("shippingAddress",shippingAddress);
			model.addAttribute("classActivePayment",true);
			
			model.addAttribute("emptyPaymentList",false);
			
			
			if(userShippingList.size() == 0){
				model.addAttribute("emptyShippingList",true);
			}else{
				model.addAttribute("emptyShippingList",false);
			}
			
			return "checkout";
			//return "form";
		}
		
		
	}
	
	
	@RequestMapping(value="/checkoutP", method=RequestMethod.POST)
	public String checkoutP(
			ShippingAddress shippingAddress,
			BillingAddress billingAddress,
			Payment payment,
			String billingSameAsShipping,
			String shippingMethod,
			Principal principal,
			Model model
			){
		ShoppingCart shoppingCart = userService.findByUsername(principal.getName()).getShoppingCart();
		
		List<CartItem> cartItemList = cartItemService.findByShoppingCart(shoppingCart);
		model.addAttribute("cartItemList",cartItemList);
		
		if(billingSameAsShipping.equals("true")){
			billingAddress.setBillingAddressName(shippingAddress.getShippingAddressName());
			billingAddress.setBillingAddressStreet1(shippingAddress.getShippingAddressStreet1());
			billingAddress.setBillingAddressStreet2(shippingAddress.getShippingAddressStreet2());
			billingAddress.setBillingAddressCity(shippingAddress.getShippingAddressCity());
			billingAddress.setBillingAddressState(shippingAddress.getShippingAddressState());
			billingAddress.setBillingAddressCountry(shippingAddress.getShippingAddressCountry());
			billingAddress.setBillingAddressZipcode(shippingAddress.getShippingAddressZipcode());
		}
		
		if(shippingAddress.getShippingAddressStreet1().isEmpty()|| 
		   shippingAddress.getShippingAddressCity().isEmpty() ||
		   shippingAddress.getShippingAddressState().isEmpty() ||
		   shippingAddress.getShippingAddressZipcode().isEmpty() ||
		   shippingAddress.getShippingAddressName().isEmpty() ||
		   payment.getCardNumber().isEmpty() ||
		   payment.getCvc() == 0 ||
		   billingAddress.getBillingAddressStreet1().isEmpty() ||
		   billingAddress.getBillingAddressCity().isEmpty() ||
		   billingAddress.getBillingAddressState().isEmpty() ||
		   billingAddress.getBillingAddressName().isEmpty() ||
		   billingAddress.getBillingAddressZipcode().isEmpty()){
			return "redirect:/checkout?id="+shoppingCart.getId()+"&missingRequiredField=true";
		}
		User user = userService.findByUsername(principal.getName());
		Order order = orderService.createOrder(shoppingCart, shippingAddress, billingAddress, payment, shippingMethod, user);
		
		mailSender.send(mailConstructor.constructOrderConfirmationEmail(user,order,Locale.ENGLISH));
		
		shoppingCartService.clearShoppingCart(shoppingCart);
		
		LocalDate today = LocalDate.now();
		LocalDate estimatedDeliveryDate;
		
		if(shippingMethod.equals("groundShipping")){
			estimatedDeliveryDate = today.plusDays(5);
		}else{
			estimatedDeliveryDate = today.plusDays(3);
		}
		
		model.addAttribute("estimatedDeliveryDate",estimatedDeliveryDate);
		
		return "orderSubmittedPage";
	}
	
}
