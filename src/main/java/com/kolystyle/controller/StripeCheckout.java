package com.kolystyle.controller;

import java.util.Collections;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.Cookie;
import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Transaction;
import com.braintreegateway.Transaction.Status;
import com.kolystyle.KolystyleApplication;
import com.kolystyle.domain.BillingAddress;
import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.ChargeRequest;
import com.kolystyle.domain.ChargeRequest.Currency;
import com.kolystyle.domain.Payment;
import com.kolystyle.domain.ShippingAddress;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.SiteSetting;
import com.kolystyle.domain.User;
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
import com.kolystyle.service.impl.StripeService;
import com.kolystyle.utility.MailConstructor;
import com.kolystyle.utility.USConstants;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

@Controller
public class StripeCheckout {

	private static final Logger LOG = LoggerFactory.getLogger(CheckoutController.class);
	/*private BraintreeGateway gateway = KolystyleApplication.gateway;*/

   
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
	
	 @Autowired
	    private StripeService paymentsService;
	 @Value("${STRIPE_PUBLIC_KEY}")
	    private String stripePublicKey;
	    
	    @RequestMapping(value = "/guest/charge", method = RequestMethod.POST)
	    public String charge(ChargeRequest chargeRequest, Model model )
	      throws StripeException {
	        chargeRequest.setDescription("Example charge");
	        chargeRequest.setCurrency(Currency.USD);
	        Charge charge = paymentsService.charge(chargeRequest);
	        model.addAttribute("id", charge.getId());
	        model.addAttribute("status", charge.getStatus());
	        model.addAttribute("chargeId", charge.getId());
	        model.addAttribute("balance_transaction", charge.getBalanceTransaction());
	        return "striperesult";
	    }
	 
	    @ExceptionHandler(StripeException.class)
	    public String handleError(Model model, StripeException ex) {
	        model.addAttribute("error", ex.getMessage());
	        return "striperesult";
	    }
	
	    @RequestMapping(value = "/guest/checkout", method = RequestMethod.POST)
	    public String checkout(HttpServletRequest request,HttpServletResponse response,
	    		Model model, @ModelAttribute("shippingAddress") ShippingAddress shippingAddress,
				@ModelAttribute("shippingMethod") String shippingMethod,
				Principal principal) {
	    	SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
	        model.addAttribute("siteSettings",siteSettings);
	        
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
	        		Double finalamount = amount.doubleValue();
	       ///////////////////////////// 		
	        model.addAttribute("shoppingCart", shoppingCart);
	        model.addAttribute("amount", 100 * 100); // in cents
	        model.addAttribute("stripePublicKey", stripePublicKey);
	        model.addAttribute("currency", ChargeRequest.Currency.USD);
	        return "payment";
	    }
	
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
