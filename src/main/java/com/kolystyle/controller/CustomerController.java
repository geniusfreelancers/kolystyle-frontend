package com.kolystyle.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.Order;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.User;
import com.kolystyle.domain.UserBilling;
import com.kolystyle.domain.UserPayment;
import com.kolystyle.domain.UserShipping;
import com.kolystyle.repository.CartItemRepository;
import com.kolystyle.service.CartItemService;
import com.kolystyle.service.OrderService;
import com.kolystyle.service.ShoppingCartService;
import com.kolystyle.service.UserPaymentService;
import com.kolystyle.service.UserService;
import com.kolystyle.service.UserShippingService;
import com.kolystyle.service.impl.UserSecurityService;
import com.kolystyle.utility.SecurityUtility;
import com.kolystyle.utility.USConstants;

@Controller
@RequestMapping("/customer")
public class CustomerController {

	@Autowired
	private UserService userService;
	@Autowired
	private UserSecurityService userSecurityService;
	@Autowired
	private UserShippingService userShippingService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private CartItemService cartItemService;
	@Autowired
	private	UserPaymentService userPaymentService;
	@Autowired
	private CartItemRepository cartItemRepository;
	@Autowired
	private ShoppingCartService shoppingCartService;
	
    @RequestMapping("/myprofile")
    public String myprofile(Model model,@AuthenticationPrincipal User activeUser){
        User user = userService.findByUsername(activeUser.getUsername());

        model.addAttribute("user", user);

        return "profile";
    }
    
	@RequestMapping("/account")
	public String account(Model model,@AuthenticationPrincipal User activeUser,HttpServletRequest request,HttpServletResponse response) {
		User user = userService.findByUsername(activeUser.getUsername());
		//Getting User info
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
                cartID.setPath("/");
                cartID.setMaxAge(0);
         		response.addCookie(cartID);
                ShoppingCart guestShoppingCart = shoppingCartService.findCartByBagId(cartId);
                List<CartItem> cartItemList = cartItemService.findByShoppingCart(guestShoppingCart);
                
                for(CartItem cartItem : cartItemList) {
                	//Need to check if the item is already in user cart
                 	cartItem.setShoppingCart(user.getShoppingCart());
                	cartItemRepository.save(cartItem);
                }
                
                user.getShoppingCart().setGrandTotal(guestShoppingCart.getGrandTotal());
                user.getShoppingCart().setPromoCode(guestShoppingCart.getPromoCode());
                user.getShoppingCart().setDiscountedAmount(guestShoppingCart.getDiscountedAmount());
                shoppingCartService.remove(guestShoppingCart);
            }
        }
        model.addAttribute("user", user);
		return "account";
	}
    
    @RequestMapping("/preferences")
    public String myPreferences(Model model,@AuthenticationPrincipal User activeUser){
        User user = userService.findByUsername(activeUser.getUsername());

        model.addAttribute("user", user);

        return "myPreferences";
    }
    @RequestMapping(value="/preferences", method=RequestMethod.POST)
	public String myPreferencesPost(		
			@ModelAttribute("user") User user,BindingResult result,
			Principal principal,
			Model model
			) {
    
    	User currentUser = userService.findByUsername(principal.getName());
        currentUser.setUserPreferences(user.isUserPreferences());
        if(user.isUserPreferences()) {
        	model.addAttribute("updateSuccesstrue", true);
        }else {
        	model.addAttribute("updateSuccessfalse", true);
        }
        userService.save(currentUser);
        model.addAttribute("prefUpdated", true);
        
        model.addAttribute("user", currentUser);

        return "myPreferences";
    }
    @RequestMapping("/addressbook")
    public String myAddressBook(Model model,@AuthenticationPrincipal User activeUser){
    	User user = userService.findByUsername(activeUser.getUsername());
    	List<UserShipping> userShippingList = user.getUserShippingList();
    	if(userShippingList.size() >= 10){
    		model.addAttribute("cannotAddNewAddress", true);
    	}else {
    		model.addAttribute("canAddNewAddress", true);
    	}
        model.addAttribute("user", user);
        model.addAttribute("userShippingList",userShippingList );
        return "shippingAddresses";
    }
	@RequestMapping(value="/addNewShippingAddress", method=RequestMethod.GET)
    public String addNewShippingAddress(Model model,@AuthenticationPrincipal User activeUser) {
		User user = userService.findByUsername(activeUser.getUsername());
		List<UserShipping> userShippingList = user.getUserShippingList();
    	if(userShippingList.size() >= 10){
    		model.addAttribute("cannotAddNewAddress", true);
    		model.addAttribute("cannotAddShippingAddress", true);
    		model.addAttribute("user", user);
    	    model.addAttribute("userShippingList",userShippingList );
    		return "shippingAddresses";
    	}
		
		UserShipping userShipping = new UserShipping();
		List<String> stateList = USConstants.listOfUSStatesCode;
		Collections.sort(stateList);
		model.addAttribute("stateList",stateList);
        model.addAttribute("user", user);
    	model.addAttribute("userShipping", userShipping);
    	return "addNewShippingAddress";
    	
    }
    
	@RequestMapping(value="/addNewShippingAddress", method=RequestMethod.POST)
	public String addNewShippingAddressPost(
			@ModelAttribute("userShipping") UserShipping userShipping,
			Principal principal, Model model
			){	
		User user = userService.findByUsername(principal.getName());
		
		UserShipping newuserShipping = new UserShipping();
		newuserShipping.setUserShippingName(userShipping.getUserShippingName());
		newuserShipping.setUserShippingFirstName(userShipping.getUserShippingFirstName());
		newuserShipping.setUserShippingLastName(userShipping.getUserShippingLastName());
		newuserShipping.setUserShippingStreet1(userShipping.getUserShippingStreet1());
		newuserShipping.setUserShippingStreet2(userShipping.getUserShippingStreet2());
		newuserShipping.setUserShippingCity(userShipping.getUserShippingCity());
		newuserShipping.setUserShippingState(userShipping.getUserShippingState());
		newuserShipping.setUserShippingZipcode(userShipping.getUserShippingZipcode());
		newuserShipping.setUserShippingCountry(userShipping.getUserShippingCountry());
		newuserShipping.setUserShippingPhone(userShipping.getUserShippingPhone());
		newuserShipping.setUser(user);
		userShippingService.save(newuserShipping);
		userService.setUserDefaultShipping(newuserShipping.getId(), user);
		List<String> stateList = USConstants.listOfUSStatesCode;
		Collections.sort(stateList);
		model.addAttribute("stateList",stateList);
		model.addAttribute("user", user);
		model.addAttribute("userShippingList", user.getUserShippingList());
		model.addAttribute("listOfShippingAddresses", true);
		model.addAttribute("newAddressAdded", true);
		
		return "redirect:addressbook";
		
	}
	
	@RequestMapping("/editUserShipping")
	public String editUserShipping(@ModelAttribute("id") Long shippingAddressId, Principal principal, Model model){
		User user = userService.findByUsername(principal.getName());
		UserShipping userShipping = userShippingService.findById(shippingAddressId);
		if(user.getId() != userShipping.getUser().getId()){
			return "badRequestPage";
		}
		List<String> stateList = USConstants.listOfUSStatesCode;
		Collections.sort(stateList);
		model.addAttribute("stateList",stateList);
		model.addAttribute("user", user);
		model.addAttribute("userShipping", userShipping);
		return "editUserShipping";
	}
    
	@RequestMapping(value="/editUserShipping", method=RequestMethod.POST)
	public String editUserShippingPost(
			@ModelAttribute("userShipping") UserShipping userShipping,
			@ModelAttribute("id") Long shippingAddressId,BindingResult result,
			Principal principal, Model model
			){
		User user = userService.findByUsername(principal.getName());
		UserShipping currentuserShipping = userShippingService.findById(shippingAddressId);
		
		currentuserShipping.setUserShippingName(userShipping.getUserShippingName());
		currentuserShipping.setUserShippingFirstName(userShipping.getUserShippingFirstName());
		currentuserShipping.setUserShippingLastName(userShipping.getUserShippingLastName());
		currentuserShipping.setUserShippingStreet1(userShipping.getUserShippingStreet1());
		currentuserShipping.setUserShippingStreet2(userShipping.getUserShippingStreet2());
		currentuserShipping.setUserShippingCity(userShipping.getUserShippingCity());
		currentuserShipping.setUserShippingState(userShipping.getUserShippingState());
		currentuserShipping.setUserShippingZipcode(userShipping.getUserShippingZipcode());
		currentuserShipping.setUserShippingCountry(userShipping.getUserShippingCountry());
		currentuserShipping.setUserShippingPhone(userShipping.getUserShippingPhone());
		currentuserShipping.setUser(user);
		userShippingService.save(currentuserShipping);
		
		
		List<String> stateList = USConstants.listOfUSStatesCode;
		Collections.sort(stateList);
		model.addAttribute("stateList",stateList);
		model.addAttribute("user", user);
	//	userService.updateUserShipping(userShipping, user);
		
		//return "shippingAddresses";
		return "redirect:addressbook";
	}
	
	@RequestMapping(value="/setDefaultShippingAddress", method=RequestMethod.POST)
	public String setDefaultShippingAddress(@ModelAttribute("defaultShippingAddressId") Long defaultShippingId, Principal principal, Model model){
		User user = userService.findByUsername(principal.getName());
		userService.setUserDefaultShipping(defaultShippingId, user);
		
		model.addAttribute("user", user);
		
		model.addAttribute("userShippinglist", user.getUserShippingList());
		model.addAttribute("shippingDefaultUpdated",true);
		return "forward:addressbook";
		
	}
	@RequestMapping("/removeUserShipping")
	public String removeUserShipping(@ModelAttribute("id") Long userShippingId, Principal principal, Model model){
		User user = userService.findByUsername(principal.getName());
		UserShipping userShipping = userShippingService.findById(userShippingId);
		if(userShipping==null) {
			return "forward:addressbook";
		}
		
		if(user.getId() != userShipping.getUser().getId()){
			return "badRequestPage";
		}else{
			model.addAttribute("user", user);
			
			userShippingService.removeById(userShippingId);
			model.addAttribute("addressRemoved",true);
			model.addAttribute("userShippinglist", user.getUserShippingList());
			
			return "forward:addressbook";
		}
	}
	
    @RequestMapping("/wallet")
    public String mywallet(Model model, Principal principal, HttpServletRequest request){
    	User user = userService.findByUsername(principal.getName());
		model.addAttribute("user",user);
		model.addAttribute("userPaymentList", user.getUserPaymentList());
        return "wallet";
    }
    
    
    @RequestMapping("/addcard")
    public String addCards(Model model, Principal principal){
    	User user = userService.findByUsername(principal.getName());
		model.addAttribute("user",user);
    	UserBilling userBilling = new UserBilling();
		UserPayment userPayment = new UserPayment();
		
		model.addAttribute("userBilling", userBilling);
		model.addAttribute("userPayment", userPayment);
		
		List<String> stateList = USConstants.listOfUSStatesCode;
		Collections.sort(stateList);
		model.addAttribute("stateList",stateList);
        return "addcard";
    }
	
	@RequestMapping(value="/addcard", method=RequestMethod.POST)
	public String addcardPost(
			@ModelAttribute("userPayment") UserPayment userPayment,
			@ModelAttribute("userBilling") UserBilling userBilling,
			Principal principal, Model model
			){	
		User user = userService.findByUsername(principal.getName());
		
		UserPayment newuserPayment = new UserPayment();
		newuserPayment.setType(userPayment.getType());
		newuserPayment.setCardNumber(userPayment.getCardNumber());
		newuserPayment.setExpiryMonth(userPayment.getExpiryMonth());
		newuserPayment.setExpiryYear(userPayment.getExpiryYear());
		newuserPayment.setCvc(userPayment.getCvc());
		UserBilling newuserBilling = new UserBilling();
		newuserBilling.setUserBillingStreet1(userBilling.getUserBillingStreet1());
		newuserBilling.setUserBillingStreet2(userBilling.getUserBillingStreet2());
		newuserBilling.setUserBillingCity(userBilling.getUserBillingCity());
		newuserBilling.setUserBillingState(userBilling.getUserBillingState());
		newuserBilling.setUserBillingZipcode(userBilling.getUserBillingZipcode());
	/*	newuserBilling.setUser(user);
		userPaymentService.save(newuserPayment);*/
		userService.setUserDefaultShipping(newuserPayment.getId(), user);
		List<String> stateList = USConstants.listOfUSStatesCode;
		Collections.sort(stateList);
		model.addAttribute("stateList",stateList);
		model.addAttribute("user", user);
		model.addAttribute("userPaymentList", user.getUserPaymentList());
		model.addAttribute("listOfShippingAddresses", true);
		model.addAttribute("newAddressAdded", true);
		
		return "redirect:addressbook";
		
	}

    @RequestMapping("/orderhistory")
    public String myOrderHistory(Model model, @AuthenticationPrincipal User activeUser){
    	User user = userService.findByUsername(activeUser.getUsername());
    	List<Order> orderList = orderService.findByUser(user);
    	model.addAttribute("orderList", orderList);
        model.addAttribute("user", user);
        model.addAttribute("listOfOrders", true);
        

        return "orderhistory";
    }
    
    
    @RequestMapping(value="/findmyorder", method=RequestMethod.POST)
    public String findMyOrder(@ModelAttribute("orderid") Long orderid,
    		@ModelAttribute("orderemail") String orderemail,
    		Principal principal, Model model){
    	User user = userService.findByUsername(principal.getName());
    	Order order = orderService.findOne(orderid);
    	model.addAttribute("user", user);
    	String email = order.getOrderEmail();
    	if(email.equalsIgnoreCase(orderemail)) {
    		model.addAttribute("incorrectEmail", false);
   		 	model.addAttribute("findOrder", true);
    	}else{
    		model.addAttribute("incorrectEmail", true);
    		model.addAttribute("findOrder", false);
    		List<Order> orderList = orderService.findByUser(user);
        	model.addAttribute("orderList", orderList);
        	model.addAttribute("listOfOrders", true);
    		return "orderhistory";
    		
    	}
    	model.addAttribute("order", order);
        
       
        return "orderhistory";
    }
    
    @RequestMapping("/orderdetails")
	public String orderDetail(
			@RequestParam("id") Long orderId,
			Principal principal, Model model
			){
		User user = userService.findByUsername(principal.getName());
		Order order = orderService.findOne(orderId);
		
		if(order.getUser().getId() != user.getId()){
			return "badRequestPage";
		}else{
			List<CartItem> cartItemList = cartItemService.findByOrder(order);
			model.addAttribute("cartItemList",cartItemList);
			model.addAttribute("user", user);
			model.addAttribute("order",order);
			
			model.addAttribute("orderShipping", order.getShippingAddress());
			model.addAttribute("orderBilling", order.getBillingAddress());
			String cardNum = order.getPayment().getCardNumber();
			model.addAttribute("orderPayment",order.getPayment().getType());
			model.addAttribute("billingName",order.getPayment().getHolderName());
			model.addAttribute("orderPaymentNum",cardNum.substring(12));

			
			List<String> stateList = USConstants.listOfUSStatesCode;
			Collections.sort(stateList);
			model.addAttribute("stateList",stateList);
					
			return "orderdetails";
		}
		
	}

    
    @RequestMapping(value = "/updateUserInfo", method= RequestMethod.POST)
	public String updateUserInfo(
			@ModelAttribute("user") User user,BindingResult result,
			@ModelAttribute("newPassword") String newPassword,
			Principal principal,
			Model model
			) throws Exception{
    	User currentUser = userService.findByUsername(principal.getName());
		
		if(currentUser == null){
			throw new Exception("User not found");
		}
		
		/*check email already exist*/
		if(userService.findByEmail(user.getEmail()) != null){
			if(userService.findByEmail(user.getEmail()).getId() != currentUser.getId()){
				model.addAttribute("emailExists", true);
				return "profile";
			}
		}
		
		/*check username already exist*/
		if(userService.findByUsername(user.getUsername()) != null){
			if(userService.findByUsername(user.getUsername()).getId() != currentUser.getId()){
				model.addAttribute("usernameExists", true);
				return "profile";
			}
		}
		
		//Update Password
		if(newPassword != null && !newPassword.isEmpty() && !newPassword.equals("")){
			BCryptPasswordEncoder passwordEncoder = SecurityUtility.passwordEncoder();
			String dbPassword = currentUser.getPassword();
			if(passwordEncoder.matches(user.getPassword(), dbPassword)){
				currentUser.setPassword(passwordEncoder.encode(newPassword));
			}else{
				model.addAttribute("incorrectPassword", true);
				return "profile";
			}
		}
		
		currentUser.setFirstName(user.getFirstName());
		currentUser.setLastName(user.getLastName());
		currentUser.setPhone(user.getPhone());
		currentUser.setMailingStreet1(user.getMailingStreet1());
		currentUser.setMailingStreet2(user.getMailingStreet2());
		currentUser.setMailingCity(user.getMailingCity());
		currentUser.setMailingState(user.getMailingState());
		currentUser.setMailingZipcode(user.getMailingZipcode());
		currentUser.setMailingCountry(user.getMailingCountry());
		currentUser.setDob(user.getDob());
		currentUser.setGender(user.getGender());
		currentUser.setEmail(user.getEmail());
		userService.save(currentUser);
		
		model.addAttribute("updateSuccess", true);
		model.addAttribute("user", currentUser);
		
		return "profile";
	}
    
    
    
}
