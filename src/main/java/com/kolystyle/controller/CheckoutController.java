package com.kolystyle.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.ValidationError;
import com.braintreegateway.Transaction.Status;
import com.kolystyle.KolystyleApplication;
import com.kolystyle.domain.BillingAddress;
import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.Order;
import com.kolystyle.domain.OrderLog;
import com.kolystyle.domain.Payment;
import com.kolystyle.domain.PromoCodes;
import com.kolystyle.domain.ShippingAddress;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.SiteSetting;
import com.kolystyle.domain.User;
import com.kolystyle.domain.UserBilling;
import com.kolystyle.domain.UserPayment;
import com.kolystyle.domain.UserShipping;
import com.kolystyle.repository.OrderLogRepository;
import com.kolystyle.repository.OrderRepository;
import com.kolystyle.repository.PromoCodesRepository;
import com.kolystyle.service.BillingAddressService;
import com.kolystyle.service.CartItemService;
import com.kolystyle.service.OrderLogService;
import com.kolystyle.service.OrderService;
import com.kolystyle.service.PaymentService;
import com.kolystyle.service.PromoCodesService;
import com.kolystyle.service.ShippingAddressService;
import com.kolystyle.service.ShoppingCartService;
import com.kolystyle.service.SiteSettingService;
import com.kolystyle.service.UserPaymentService;
import com.kolystyle.service.UserService;
import com.kolystyle.service.UserShippingService;
import com.kolystyle.utility.MailConstructor;
import com.kolystyle.utility.USConstants;

@Controller
public class CheckoutController {
	private static final Logger LOG = LoggerFactory.getLogger(CheckoutController.class);
	private BraintreeGateway gateway = KolystyleApplication.gateway;

    private Status[] TRANSACTION_SUCCESS_STATUSES = new Status[] {
       Transaction.Status.AUTHORIZED,
       Transaction.Status.AUTHORIZING,
       Transaction.Status.SETTLED,
       Transaction.Status.SETTLEMENT_CONFIRMED,
       Transaction.Status.SETTLEMENT_PENDING,
       Transaction.Status.SETTLING,
       Transaction.Status.SUBMITTED_FOR_SETTLEMENT
    };

	private ShippingAddress shippingAddress = new ShippingAddress();
	private BillingAddress billingAddress = new BillingAddress();
	private Payment payment = new Payment();

	@Autowired
	private UserService userService;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private OrderLogRepository orderLogRepository;
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
	private SiteSettingService siteSettingService;
	
	@Autowired
	private UserPaymentService userPaymentService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private OrderLogService orderLogService;
	
	@Autowired
	private PromoCodesService promoCodesService;
	@Autowired
	private PromoCodesRepository promoCodesRepository;
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private MailConstructor mailConstructor;
	
	@RequestMapping("/cart/guestcheckout")
	public String guestcheckout(HttpServletRequest request,HttpServletResponse response,Model model ) {
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
		ShoppingCart shoppingCart;

		
		shoppingCart = shoppingCartService.findCartByCookie(request);
		System.out.println("SUCCESSFUL WITH COOKIE LOGIC");

		
        if(shoppingCart == null) {
        	return "redirect:/shoppingCart/cart";
        }
        List<CartItem> cartItemList = cartItemService.findByShoppingCart(shoppingCart);
        SiteSetting siteSetting= siteSettingService.findOne((long) 1);
		ShippingAddress shippingAddress = new ShippingAddress();
		BillingAddress billingAddress = new BillingAddress();
		Payment payment = new Payment();
		if(shoppingCart.getShippingMethod().equalsIgnoreCase("premiumShipping")) {
			model.addAttribute("premiumShipping",true);
		}else {
			model.addAttribute("groundShipping",true);
		}
		String clientToken;
		 try {
			 clientToken = gateway.clientToken().generate();
			 LOG.info("Client token {} for paypal generated",clientToken);
	       } catch (Exception e) {
	           System.out.println("Exception: " + e);
	           
	           return "badRequestPage";
	       }
		
		System.out.println("ClientToken: "+clientToken);
		model.addAttribute("siteSetting", siteSetting);
		model.addAttribute("clientToken", clientToken);
		model.addAttribute("shippingAddress",shippingAddress);
		model.addAttribute("payment",payment);
		model.addAttribute("billingAddress",billingAddress);
		model.addAttribute("cartItemList",cartItemList);
		model.addAttribute("shoppingCart",shoppingCart);
		List<String> stateList = USConstants.listOfUSStatesCode;
		Collections.sort(stateList);
		model.addAttribute("stateList", stateList);
		model.addAttribute("noCartExist",true);
		return "guestcheckout";
		
	}
	
	@RequestMapping(value = "/chargeguest", method = RequestMethod.POST)
    public String charge(HttpServletRequest request,HttpServletResponse response,
    		@RequestParam("amount") String amount, @RequestParam("payment_method_nonce") String nonce, 
    		@RequestParam("phoneNumber") String phoneNumber, @RequestParam("email") String email,
    		Model model, final RedirectAttributes redirectAttributes,
    		@ModelAttribute("shippingAddress") ShippingAddress shippingAddress,
			@ModelAttribute("billingAddress") BillingAddress billingAddress,
			@ModelAttribute("payment") Payment payment,
			@ModelAttribute("billingSameAsShipping") String billingSameAsShipping,
			@ModelAttribute("shippingMethod") String shippingMethod,
			Principal principal){    
        
//Save in DB
User user = null;
ShoppingCart shoppingCart;
Cookie[] cookies = request.getCookies();

boolean foundCookie = false;

shoppingCart = shoppingCartService.findCartByCookie(request);
System.out.println("SUCCESSFUL WITH COOKIE LOGIC");
if(shoppingCart!=null) {
	foundCookie = true;
}
if(shoppingCart==null) {
	System.out.println("Bag ID IS MISSING");
	return "redirect:/cart/guestcheckout?missingRequiredField=true";
}
		
		List<CartItem> cartItemList = cartItemService.findByShoppingCart(shoppingCart);
		model.addAttribute("cartItemList",cartItemList);
		if(shippingAddress.getShippingAddressStreet1().isEmpty()|| 
				   shippingAddress.getShippingAddressCity().isEmpty() ||
				   shippingAddress.getShippingAddressState().isEmpty() ||
				   shippingAddress.getShippingAddressZipcode().isEmpty()) {
			return "redirect:/cart/guestcheckout?missingRequiredField=true";
		}
/*		if(billingSameAsShipping.equals("true")){
			billingAddress.setBillingAddressStreet1(shippingAddress.getShippingAddressStreet1());
			billingAddress.setBillingAddressStreet2(shippingAddress.getShippingAddressStreet2());
			billingAddress.setBillingAddressCity(shippingAddress.getShippingAddressCity());
			billingAddress.setBillingAddressState(shippingAddress.getShippingAddressState());
			billingAddress.setBillingAddressCountry(shippingAddress.getShippingAddressCountry());
			billingAddress.setBillingAddressZipcode(shippingAddress.getShippingAddressZipcode());
		}*/
		
		/*if(payment.getCardNumber().isEmpty() ||
		   payment.getCvc() == 0 ||
		   billingAddress.getBillingAddressStreet1().isEmpty() ||
		   billingAddress.getBillingAddressCity().isEmpty() ||
		   billingAddress.getBillingAddressState().isEmpty() ||
		   billingAddress.getBillingAddressName().isEmpty() ||
		   billingAddress.getBillingAddressZipcode().isEmpty()){
			return "redirect:/checkout?id="+shoppingCart.getId()+"&missingRequiredField=true";
		}*/
	
		
		
		//Paypal things humm
		BigDecimal decimalAmount;
	       try {
	           decimalAmount = new BigDecimal(amount);
	       } catch (NumberFormatException e) {
	           redirectAttributes.addFlashAttribute("errorDetails", "Error: 81503: Amount is an invalid format.");
	           return "redirect:/cart/guestcheckout?missingRequiredField=true";
	       }

	       TransactionRequest reequest = new TransactionRequest()
	           .amount(decimalAmount)
	           .paymentMethodNonce(nonce)
	           .options()
	               .submitForSettlement(true)
	               .done();

	       Result<Transaction> result = gateway.transaction().sale(reequest);

	       if (result.isSuccess()) {
	    	   Transaction transaction = result.getTarget();
	           
	   
	    	// Do all order placement stuff after paypal success
	    	Order order = orderService.createOrder(shoppingCart, shippingAddress, billingAddress, payment, shippingMethod, user,email,phoneNumber);
	   		order.setPaymentType(transaction.getPaymentInstrumentType());
	   		order.setPaymentConfirm(transaction.getId());
	    	order.setOrderTotal(decimalAmount);
	   		order.setPromocodeApplied(shoppingCart.getPromoCode());
	   		order.setShippingCost(shoppingCart.getShippingCost());
	   		order.setOrderType(shoppingCart.getCartType());
	   		order.setOrderSubtotal(shoppingCart.getGrandTotal());
	   		order.setDiscount(shoppingCart.getDiscountedAmount());
	   		orderRepository.save(order);
	   		OrderLog orderLog = new OrderLog();
	   		orderLog.setOrder(order);
	   		orderLog.setUpdatedBy("Guest User");
	   		orderLog.setUpdatedDate(Calendar.getInstance().getTime());
	   		orderLog.setProcessingStatus("created");
	   		orderLog.setUserReason("User placed an order using website");
	   		orderLogRepository.save(orderLog);
//	   		mailSender.send(mailConstructor.constructOrderConfirmationEmail(user,order,Locale.ENGLISH));
	   		OrderLog orderLog2 = new OrderLog();
	   		orderLog2.setOrder(order);
	   		orderLog2.setUpdatedBy("System");
	   		orderLog2.setUpdatedDate(Calendar.getInstance().getTime());
	   		orderLog2.setProcessingStatus("created");
	   		orderLog2.setUserReason("Confirmation Email sent to customer");
	   		orderLogRepository.save(orderLog2);
	   		PromoCodes promoCodes = promoCodesService.findByPromoCode(shoppingCart.getPromoCode());
	   		if(promoCodes != null) {
	   			promoCodes.setPromoUsedCount(promoCodes.getPromoUsedCount()+1);
	   			promoCodesRepository.save(promoCodes);
	   		}
	   		shoppingCartService.clearShoppingCart(shoppingCart);
		   	 if (foundCookie) {
		           /* Cookie cookie1 = new Cookie("BagId",shoppingCart.getBagId());
		            cookie1.setPath("/");
		            cookie1.setMaxAge(30*24*60*60);
		            response.addCookie(cookie1);*/ 
		            for (Cookie cookie1 : cookies) {
		            	 if (cookie1.getName().equalsIgnoreCase("BagId")) {
		                cookie1.setValue("");
		                cookie1.setPath("/");
		                cookie1.setMaxAge(0);
		                response.addCookie(cookie1);
		                System.out.println("Cookie for BAGID is deleted");
		            	 }
		            }
		        }
	   		
	    	   //End of Order placement
	    	   
	          
	         return "redirect:thankyou/" + transaction.getId() +"/"+order.getId();          
	           
	       } else if (result.getTransaction() != null) {
	           Transaction transaction = result.getTransaction();
	           return "redirect:checkouts/" + transaction.getId();
	       } else {
	           String errorString = "";
	           for (ValidationError error : result.getErrors().getAllDeepValidationErrors()) {
	              errorString += "Error: " + error.getCode() + ": " + error.getMessage() + "\n";
	           }
	           redirectAttributes.addFlashAttribute("errorDetails", errorString);
	           return "redirect:/cart/guestcheckout";
	       }
     
    }
	
	 @RequestMapping(value = "/thankyou/{transactionId}/{orderId}")
	   public String getThankYou(@PathVariable String transactionId, @PathVariable Long orderId, Model model) {
	       Transaction transaction;
	       Order order;
	       SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
	        model.addAttribute("siteSettings",siteSettings);
	       try {
	           transaction = gateway.transaction().find(transactionId);
	           
	           order = orderService.findOne(orderId);
	       } catch (Exception e) {
	           System.out.println("Exception: " + e);
	           return "badRequestPage";
	       }

	       model.addAttribute("isSuccess", Arrays.asList(TRANSACTION_SUCCESS_STATUSES).contains(transaction.getStatus()));
	       model.addAttribute("transaction", transaction);
	   		LocalDate today = LocalDate.now();
	   		LocalDate estimatedDeliveryDate;
	   		
	   		if(order.getShippingMethod().equals("groundShipping")){
	   			estimatedDeliveryDate = today.plusDays(5);
	   		}else{
	   			estimatedDeliveryDate = today.plusDays(3);
	   		}
	   		if(transaction.getPaymentInstrumentType().equals("paypal_account")){
	        	   model.addAttribute("paypalMethod",true);
		   			 
		   		}else{
		   			model.addAttribute("creditMethod",true);
		   		}
	   		int currentStatus = 1;
	   		if(order.getOrderStatus().equalsIgnoreCase("created")) {
	   			currentStatus = 2;
	   		}else if (order.getOrderStatus().equalsIgnoreCase("processing")) {
	   			currentStatus = 3;
	   		}else if (order.getOrderStatus().equalsIgnoreCase("shipped")) {
	   			currentStatus = 4;
	   		}else if (order.getOrderStatus().equalsIgnoreCase("intransit")) {
	   			currentStatus = 5;
	   		}else if (order.getOrderStatus().equalsIgnoreCase("delivered")) {
	   			currentStatus = 6;
	   		}else {
	   			currentStatus = 2;
	   		}
	   		model.addAttribute("estimatedDeliveryDate",estimatedDeliveryDate);
	   		model.addAttribute("order",order);
	   		model.addAttribute("currentStatus",currentStatus);
	   		model.addAttribute("cartItemList", order.getCartItemList());

	       return "thankyou";
	   }
	
	
	
	
	@RequestMapping("/checkout")
	public String checkout(
			@RequestParam("id") Long cartId,
			@RequestParam(value="missingRequiredField", required=false) boolean missingRequiredField,
			Model model, Principal principal
			){
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
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
		
		//return "checkOutOld";
		return "checkout";
       // return "form";
		
	}
	
	

	
	@RequestMapping("/setShippingAddress")
	public String setShippingAddress(
			@RequestParam("userShippingId") Long userShippingId,
			Principal principal, Model model
			){
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
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
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
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
	
	@RequestMapping(value = "/public/track")
	public String trackOrder(HttpServletRequest request,HttpServletResponse response,Model model ) {
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        Order order = new Order();
        model.addAttribute("order",order);
		model.addAttribute("success",false);
		model.addAttribute("noCartExist",true);
		
		return "track";
		
	}
	
	@RequestMapping(value = "/public/track", method = RequestMethod.POST)
	   public String orderDetailsPage(@ModelAttribute("order") Order order, Model model) {
			SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
	        model.addAttribute("siteSettings",siteSettings);
	        String email = order.getOrderEmail();
	       Transaction transaction;
	       try {
	    	   order = orderService.findOne(order.getId());
	    	   if(order.getOrderEmail().equalsIgnoreCase(email)) {
	    		   model.addAttribute("incorrectEmail",false);
	    	   }else {
	    		   model.addAttribute("success",false);
	    		   model.addAttribute("notfound",false);
	    		   model.addAttribute("incorrectEmail",true);
	    		   return "track";
	    	   }
	    	   String transactionId = order.getPaymentConfirm();
	           transaction = gateway.transaction().find(transactionId);
	          
	       } catch (Exception e) {
	           System.out.println("Exception: " + e);
	           model.addAttribute("success",false);
	           model.addAttribute("notfound",true);
	           return "track";
	       }

	       model.addAttribute("isSuccess", Arrays.asList(TRANSACTION_SUCCESS_STATUSES).contains(transaction.getStatus()));
	       model.addAttribute("transaction", transaction);
	   		LocalDate today = LocalDate.now();
	   		LocalDate estimatedDeliveryDate;
	   		
	   		if(order.getShippingMethod().equals("groundShipping")){
	   			estimatedDeliveryDate = today.plusDays(5);
	   		}else{
	   			estimatedDeliveryDate = today.plusDays(3);
	   		}
	   		if(transaction.getPaymentInstrumentType().equals("paypal_account")){
	        	   model.addAttribute("paypalMethod",true);
		   			 
		   		}else{
		   			model.addAttribute("creditMethod",true);
		   		}
	   		int currentStatus = 1;
	   		if(order.getOrderStatus().equalsIgnoreCase("created")) {
	   			currentStatus = 2;
	   		}else if (order.getOrderStatus().equalsIgnoreCase("processing")) {
	   			currentStatus = 3;
	   		}else if (order.getOrderStatus().equalsIgnoreCase("shipped")) {
	   			currentStatus = 4;
	   		}else if (order.getOrderStatus().equalsIgnoreCase("intransit")) {
	   			currentStatus = 5;
	   		}else if (order.getOrderStatus().equalsIgnoreCase("delivered")) {
	   			currentStatus = 6;
	   		}else {
	   			currentStatus = 2;
	   		}
	   		model.addAttribute("estimatedDeliveryDate",estimatedDeliveryDate);
	   		model.addAttribute("order",order);
	   		model.addAttribute("currentStatus",currentStatus);
	   		model.addAttribute("cartItemList", order.getCartItemList());
	   		List<OrderLog> orderLogList = orderLogService.findByOrderByOrderByIdDesc(order);
	   		if(orderLogList.isEmpty()) {
	   			model.addAttribute("emptyLog",true);
	   		}else {
	   			model.addAttribute("emptyLog",false);
	   			model.addAttribute("orderLogList",orderLogList);
	   		}
	   		model.addAttribute("success",true);
	   		model.addAttribute("incorrectEmail",false);
	   		model.addAttribute("notfound",false);
	   		return "track";
	   }
	
}
