package com.kolystyle.controller;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.http.Cookie;
import com.kolystyle.domain.BillingAddress;
import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.ChargeRequest;
import com.kolystyle.domain.Newsletter;
import com.kolystyle.domain.ChargeRequest.Currency;
import com.kolystyle.domain.Order;
import com.kolystyle.domain.OrderLog;
import com.kolystyle.domain.Payment;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.PromoCodes;
import com.kolystyle.domain.ShippingAddress;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.SiteSetting;
import com.kolystyle.domain.User;
import com.kolystyle.repository.NewsletterRepository;
import com.kolystyle.repository.OrderLogRepository;
import com.kolystyle.repository.OrderRepository;
import com.kolystyle.repository.ProductRepository;
import com.kolystyle.repository.PromoCodesRepository;
import com.kolystyle.service.BillingAddressService;
import com.kolystyle.service.CartItemService;
import com.kolystyle.service.NewsletterService;
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
import com.kolystyle.service.impl.AmazonClient;
import com.kolystyle.service.impl.StripeService;
import com.kolystyle.utility.MailConstructor;
import com.kolystyle.utility.USConstants;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

@Controller
public class StripeCheckout {

	private static final Logger LOG = LoggerFactory.getLogger(CheckoutController.class);
	/*private BraintreeGateway gateway = KolystyleApplication.gateway;*/
	 private AmazonClient amazonClient;
		

	    @Value("${amazonProperties.endpointUrl}")
	    private String endpointUrl;
	    @Value("${amazonProperties.bucketName}")
	    private String bucketName;
	    @Value("${amazonProperties.accessKey}")
	    private String accessKey;
	    @Value("${amazonProperties.secretKey}")
	    private String secretKey;
	    @Autowired
	    StripeCheckout(AmazonClient amazonClient) {
	        this.amazonClient = amazonClient;
	    }
   
/*	private ShippingAddress shippingAddress = new ShippingAddress();
	private BillingAddress billingAddress = new BillingAddress();
	private Payment payment = new Payment();*/

	@Autowired
	private UserService userService;
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private ProductRepository productRepository;
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
	private NewsletterService newsletterService;
	@Autowired
	private NewsletterRepository newsletterRepository;
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private MailConstructor mailConstructor;
	
	 @Autowired
	    private StripeService paymentsService;
	 @Value("${STRIPE_PUBLIC_KEY}")
	    private String stripePublicKey;
	    
	    @RequestMapping(value = "/guest/charge", method = RequestMethod.POST)
	    public String charge(ChargeRequest chargeRequest,@ModelAttribute("finalamount") int finalamount,BindingResult result, Model model ,HttpServletRequest request,HttpServletResponse response)
	      throws StripeException {
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
        	int orderSize = 0;
        	for(CartItem cartItem : cartItemList) {
        		orderSize+=cartItem.getQty();
        	}
        	model.addAttribute("cartItemList",cartItemList);
	        chargeRequest.setDescription(shoppingCart.getBagId());
	        chargeRequest.setCurrency(Currency.USD);
	        chargeRequest.setAmount(finalamount);
	        Charge charge = paymentsService.charge(chargeRequest);
	        Order order = null;
	        System.out.println(charge.getStatus());
	        if(charge.getStatus().equalsIgnoreCase("succeeded")) {
	        	
	        	
	        	
	        	// Do all order placement stuff after paypal success

	        	order = orderService.createNewOrder(shoppingCart,user, charge);
		    	//Order order = orderService.createOrder(shoppingCart, shoppingCart.getShippingAddress(), billingAddress, payment, shoppingCart, user,email,phoneNumber);
		   		order.setCustomerFname(shoppingCart.getShippingAddress().getFirstName());
		   		order.setCustomerLname(shoppingCart.getShippingAddress().getLastName());
	        	order.setPaymentType("credit_card");
		   		order.setPaymentConfirm(charge.getId());
		    	order.setOrderTotal(shoppingCart.getOrderTotal());
		   		order.setPromocodeApplied(shoppingCart.getPromoCode());
		   		order.setShippingCost(shoppingCart.getShippingCost());
		   		order.setOrderType(shoppingCart.getCartType());
		   		order.setOrderSubtotal(shoppingCart.getGrandTotal());
		   		order.setDiscount(shoppingCart.getDiscountedAmount());
		   		order.setStitchingTotal(shoppingCart.getStitchingTotal());
		   		order.setOrderSize(orderSize);
		   		LocalDate today = LocalDate.now();
		   		LocalDate estimatedDeliveryDate;
		   		if(order.getShippingMethod().equals("groundShipping")){
		   			estimatedDeliveryDate = today.plusDays(5);
		   		}else{
		   			estimatedDeliveryDate = today.plusDays(3);
		   		}
		   		
		   		Date date = Date.from(estimatedDeliveryDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		   		order.setEstimatedDeliveryDate(date);
		   		orderRepository.save(order);
		   		Newsletter newsletter = newsletterService.findByEmail(order.getOrderEmail());
		   		if(newsletter == null) {
		   			newsletter = new Newsletter();
		   			newsletter.setEmail(order.getOrderEmail());
					Date enrolledDate = Calendar.getInstance().getTime();
					newsletter.setEnrolledDate(enrolledDate);
					String verifyToken = USConstants.randomAlphaNumeric(10);
					newsletter.setVerifyToken(verifyToken);
		   		}		   		
		   		newsletterRepository.save(newsletter);
		   		OrderLog orderLog = new OrderLog();
		   		orderLog.setOrder(order);
		   		orderLog.setUpdatedBy("Guest User");
		   		orderLog.setUpdatedDate(Calendar.getInstance().getTime());
		   		orderLog.setProcessingStatus("created");
		   		orderLog.setUserReason("User placed an order using website");
		   		orderLogRepository.save(orderLog);
		   		//Sending Confirmation Email to customer
		   	//	mailSender.send(mailConstructor.constructGuestOrderConfirmationEmail(order,Locale.ENGLISH));
		   		
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
			            Cookie cookie1 = new Cookie("BagId",shoppingCart.getBagId());
			            cookie1.setPath("/");
			            cookie1.setMaxAge(30*24*60*60);
			            response.addCookie(cookie1); 
			            for (Cookie cookie11 : cookies) {
			            	 if (cookie11.getName().equalsIgnoreCase("BagId")) {
			                cookie11.setValue("");
			                cookie11.setPath("/");
			                cookie11.setMaxAge(0);
			                response.addCookie(cookie11);
			                System.out.println("Cookie for BAGID is deleted");
			            	 }
			            }
			        }
		   		
		    	   //End of Order placement*/
	        }
	      /*  model.addAttribute("id", charge.getId());
	        model.addAttribute("status", charge.getStatus());
	        model.addAttribute("chargeId", charge.getId());
	        model.addAttribute("balance_transaction", charge.getBalanceTransaction());*/
	 
	        return "redirect:/thankyou/" + charge.getId() +"/"+order.getId();   
	      //  return "striperesult";
	    }
	 
	    @ExceptionHandler(StripeException.class)
	    public String handleError(Model model, StripeException ex) {
	        model.addAttribute("error", ex.getMessage());
	        return "striperesult";
	    }
	    
	    @RequestMapping(value = "/thankyou/{transactionId}/{orderId}")
		   public String getThankYou(@PathVariable String transactionId, @PathVariable Long orderId, Model model) {
		       Charge charge;
		       Order order;
		       SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
		        model.addAttribute("siteSettings",siteSettings);
		        String fileUrl = endpointUrl + "/" + bucketName + "/";
				model.addAttribute("fileUrl", fileUrl);
		       try {
		    	   charge = Charge.retrieve(transactionId);
		           order = orderService.findOne(orderId);
		           
		       } catch (Exception e) {
		           System.out.println("Exception: " + e);
		           return "badRequestPage";
		       }

		       model.addAttribute("isSuccess", charge.getStatus());
		       
		       model.addAttribute("transaction", charge);
		   		
		   		
			   			model.addAttribute("creditMethod",true);
			   		
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
		   		model.addAttribute("estimatedDeliveryDate",order.getEstimatedDeliveryDate());
		   		model.addAttribute("order",order);
		   		model.addAttribute("currentStatus",currentStatus);
		   		model.addAttribute("cartItemList", order.getCartItemList());
		   		model.addAttribute("noCartExist",true);
		       return "thankyou";
		   }
	
	    @RequestMapping(value = "/guest/checkout", method = RequestMethod.GET)
	    public String checkoutGet(HttpServletRequest request,HttpServletResponse response,Model model) {
	    	SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
	        model.addAttribute("siteSettings",siteSettings);
	        String fileUrl = endpointUrl + "/" + bucketName + "/";
			model.addAttribute("fileUrl", fileUrl);
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
	        ShippingAddress shippingAddress = shoppingCart.getShippingAddress();
	        		if(shippingAddress.getShippingAddressStreet1().isEmpty()|| 
	        				   shippingAddress.getShippingAddressCity().isEmpty() ||
	        				   shippingAddress.getShippingAddressState().isEmpty() ||
	        				   shippingAddress.getShippingAddressZipcode().isEmpty()) {
	        			return "redirect:/cart/guestcheckout?missingRequiredField=true";
	        		}
	        ///////////////////////

	        		
	        		
	        BigDecimal amount = (shoppingCart.getOrderTotal()).multiply(new BigDecimal(100));
	        int finalamount = amount.intValue();
	       ///////////////////////////// 		
	        model.addAttribute("shoppingCart", shoppingCart);
	        model.addAttribute("amount", finalamount); // in cents
	        model.addAttribute("stripePublicKey", stripePublicKey);
	        model.addAttribute("currency", ChargeRequest.Currency.USD);
	        model.addAttribute("noCartExist",true);
	        return "payment";
	    }
	    
	    
	    @RequestMapping(value = "/guest/checkout", method = RequestMethod.POST)
	    public String checkout(HttpServletRequest request,HttpServletResponse response,
	    		Model model, @ModelAttribute("shippingAddress") ShippingAddress shippingAddress,
				@ModelAttribute("shippingMethod") String shippingMethod,
				Principal principal) {
	    	SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
	        model.addAttribute("siteSettings",siteSettings);
	        String fileUrl = endpointUrl + "/" + bucketName + "/";
			model.addAttribute("fileUrl", fileUrl);
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
	        ///////////////////////
	       // Get updated shipping and contact info and save in DB to retrive for review page
	        		
	        		String status = shoppingCartService.updateOrderInfo(shoppingCart,shippingAddress,shippingMethod);   	
	        		if(status == "added") {
	        			
	        		}
	        		BigDecimal amount = (shoppingCart.getOrderTotal()).multiply(new BigDecimal(100));
	        		int finalamount = amount.intValue();
	       ///////////////////////////// 		
	        model.addAttribute("shoppingCart", shoppingCart);
	        model.addAttribute("amount", finalamount); // in cents
	        model.addAttribute("stripePublicKey", stripePublicKey);
	        model.addAttribute("currency", ChargeRequest.Currency.USD);
	        model.addAttribute("noCartExist",true);
	        return "payment";
	    }
	
	@RequestMapping("/cart/guestcheckout")
	public String guestcheckout(HttpServletRequest request,HttpServletResponse response,Model model ) {
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
		ShoppingCart shoppingCart;

		
		shoppingCart = shoppingCartService.findCartByCookie(request);
		System.out.println("SUCCESSFUL WITH COOKIE LOGIC");

		
        if(shoppingCart == null || shoppingCart.getCartItemList().size() < 1) {
        	return "redirect:/shoppingCart/cart";
        }
        List<CartItem> cartItemList = cartItemService.findByShoppingCart(shoppingCart);
        SiteSetting siteSetting= siteSettingService.findOne((long) 1);
		ShippingAddress shippingAddress = shoppingCart.getShippingAddress();
		if (shippingAddress == null) {
			shippingAddress = new ShippingAddress();
		}
				
				
		/*BillingAddress billingAddress = new BillingAddress();
		Payment payment = new Payment();
		model.addAttribute("payment",payment);*/
		if(shoppingCart.getShippingMethod().equalsIgnoreCase("premiumShipping")) {
			model.addAttribute("premiumShipping",true);
		}else {
			model.addAttribute("groundShipping",true);
		}
	
		
		
		model.addAttribute("siteSetting", siteSetting);
		
		model.addAttribute("shippingAddress",shippingAddress);
	
	/*	model.addAttribute("billingAddress",billingAddress);*/
		model.addAttribute("cartItemList",cartItemList);
		model.addAttribute("shoppingCart",shoppingCart);
		List<String> stateList = USConstants.listOfUSStatesCode;
		Collections.sort(stateList);
		model.addAttribute("stateList", stateList);
		model.addAttribute("noCartExist",true);
		return "guestcheckoutstripe";
		
	}
}
