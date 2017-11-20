/*package com.kolystyle.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.kolystyle.controller.CheckoutController;
import com.kolystyle.domain.BillingAddress;
import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.ChargeRequest;
import com.kolystyle.domain.Order;
import com.kolystyle.domain.Payment;
import com.kolystyle.domain.ShippingAddress;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.User;
import com.kolystyle.domain.ChargeRequest.Currency;
import com.kolystyle.service.BillingAddressService;
import com.kolystyle.service.CartItemService;
import com.kolystyle.service.OrderService;
import com.kolystyle.service.PaymentService;
import com.kolystyle.service.ShippingAddressService;
import com.kolystyle.service.ShoppingCartService;
import com.kolystyle.service.UserPaymentService;
import com.kolystyle.service.UserService;
import com.kolystyle.service.UserShippingService;
import com.kolystyle.service.impl.StripeService;
import com.kolystyle.utility.MailConstructor;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

@Controller
public class ChargeController {
 
    @Autowired
    private StripeService paymentsService;
    
    
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
 
    @PostMapping("/charge")
    public String charge(ChargeRequest chargeRequest, Model model,
    		@ModelAttribute("shippingAddress") ShippingAddress shippingAddress,
			@ModelAttribute("billingAddress") BillingAddress billingAddress,
			@ModelAttribute("payment") Payment payment,
			@ModelAttribute("billingSameAsShipping") String billingSameAsShipping,
			@ModelAttribute("shippingMethod") String shippingMethod,
			Principal principal)
      throws StripeException {
        chargeRequest.setDescription("Koly Style Guest Product Purchase");
        chargeRequest.setCurrency(Currency.USD);
        
        Charge charge = paymentsService.charge(chargeRequest);
        model.addAttribute("id", charge.getId());
        model.addAttribute("status", charge.getStatus());
        model.addAttribute("chargeId", charge.getId());
       
        model.addAttribute("balance_transaction", charge.getBalanceTransaction());
        

        
//Save in DB
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
		
//		mailSender.send(mailConstructor.constructOrderConfirmationEmail(user,order,Locale.ENGLISH));
		
		shoppingCartService.clearShoppingCart(shoppingCart);
		
		LocalDate today = LocalDate.now();
		LocalDate estimatedDeliveryDate;
		
		if(shippingMethod.equals("groundShipping")){
			estimatedDeliveryDate = today.plusDays(5);
		}else{
			estimatedDeliveryDate = today.plusDays(3);
		}
		
		model.addAttribute("estimatedDeliveryDate",estimatedDeliveryDate);
		
	
	

    //    return "result";
        return "orderSubmittedPage";
    }
 
    @ExceptionHandler(StripeException.class)
    public String handleError(Model model, StripeException ex) {
        model.addAttribute("error", ex.getMessage());
        return "result";
    }
}



package com.kolystyle.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.kolystyle.controller.CheckoutController;
import com.kolystyle.domain.BillingAddress;
import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.ChargeRequest;
import com.kolystyle.domain.Order;
import com.kolystyle.domain.Payment;
import com.kolystyle.domain.ShippingAddress;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.User;
import com.kolystyle.domain.ChargeRequest.Currency;
import com.kolystyle.service.BillingAddressService;
import com.kolystyle.service.CartItemService;
import com.kolystyle.service.OrderService;
import com.kolystyle.service.PaymentService;
import com.kolystyle.service.ShippingAddressService;
import com.kolystyle.service.ShoppingCartService;
import com.kolystyle.service.UserPaymentService;
import com.kolystyle.service.UserService;
import com.kolystyle.service.UserShippingService;
import com.kolystyle.service.impl.StripeService;
import com.kolystyle.utility.MailConstructor;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

@Controller
public class ChargeController {
 
    @Autowired
    private StripeService paymentsService;
    
    
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
 
    @PostMapping("/chargeguest")
    public String charge(ChargeRequest chargeRequest, Model model,
    		@ModelAttribute("shippingAddress") ShippingAddress shippingAddress,
			@ModelAttribute("billingAddress") BillingAddress billingAddress,
			@ModelAttribute("payment") Payment payment,
			@ModelAttribute("billingSameAsShipping") String billingSameAsShipping,
			@ModelAttribute("shippingMethod") String shippingMethod,
			Principal principal)
      throws StripeException {
        chargeRequest.setDescription("Koly Style Guest Product Purchase");
        chargeRequest.setCurrency(Currency.USD);
        
        Charge charge = paymentsService.charge(chargeRequest);
        model.addAttribute("id", charge.getId());
        model.addAttribute("status", charge.getStatus());
        model.addAttribute("chargeId", charge.getId());
       
        model.addAttribute("balance_transaction", charge.getBalanceTransaction());
        

        
//Save in DB
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
		
//		mailSender.send(mailConstructor.constructOrderConfirmationEmail(user,order,Locale.ENGLISH));
		
		shoppingCartService.clearShoppingCart(shoppingCart);
		
		LocalDate today = LocalDate.now();
		LocalDate estimatedDeliveryDate;
		
		if(shippingMethod.equals("groundShipping")){
			estimatedDeliveryDate = today.plusDays(5);
		}else{
			estimatedDeliveryDate = today.plusDays(3);
		}
		
		model.addAttribute("estimatedDeliveryDate",estimatedDeliveryDate);
		
	
	

    //    return "result";
        return "orderSubmittedPage";
    }
 
    @ExceptionHandler(StripeException.class)
    public String handleError(Model model, StripeException ex) {
        model.addAttribute("error", ex.getMessage());
        return "result";
    }
}*/